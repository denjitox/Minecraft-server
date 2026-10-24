package ar.com.maydana.mushokucore.integration;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Implementación de {@link LuckPermsHook} — agente A (infra-core). Se llama
 * {@code LuckPermsService} (y no {@code LuckPermsHook}) porque ese nombre lo ocupa el
 * interface CONGELADO de F0 (discrepancia plan §6.3 vs regla 2, resuelta en el javadoc
 * del propio interface).
 *
 * <p>API de LuckPerms <b>por reflexión</b>: no hay jar de LuckPerms en el classpath
 * offline de build.ps1 y el plugin es de terceros; la ausencia de LP degrada a
 * {@code disponible()=false} sin tumbar el arranque (spec §2.5; DECISION D16 2026-10-18:
 * LuckPerms es softdepend).</p>
 *
 * <p><b>Hilos:</b> la API de LuckPerms es síncrona ({@code loadUser} devuelve futuro y se
 * espera con timeout). Por eso, si el llamador está en el hilo principal de Bukkit,
 * {@link #agregarGrupo}/{@link #quitarGrupo} devuelven <b>false</b> en vez de bloquear:
 * la operación entra al outbox de reintento (spec §11 L592). Las llamadas correctas
 * vienen del outbox/tareas async (agente B).</p>
 *
 * <p>[DECISIÓN D1 pendiente] los grupos por defecto son {@code potencia1..7}
 * (compatibilidad con potencias.sk L23/L27); este hook no fija nombres: recibe el
 * grupo ya nombrado.</p>
 */
public final class LuckPermsService implements LuckPermsHook {

    private static final long TIMEOUT_SEGUNDOS = 5;

    private final Logger log;
    private final Object api;
    private final Method getUserManager;
    private final Method getUser;
    private final Method loadUser;
    private final Method saveUser;
    private final Method data;
    private final Method toBuilder;
    private final Method builderAdd;
    private final Method builderRemove;
    private final Method builderBuild;
    private final Method nodeBuilder;
    private final Method cachedData;
    private final Method permissionData;
    private final Method checkPermission;
    private final Method asBoolean;

    public LuckPermsService(JavaPlugin plugin) {
        this.log = plugin.getLogger();
        Object apiObj = null;
        Method mGetUserManager = null;
        Method mGetUser = null;
        Method mLoadUser = null;
        Method mSaveUser = null;
        Method mData = null;
        Method mToBuilder = null;
        Method mBuilderAdd = null;
        Method mBuilderRemove = null;
        Method mBuilderBuild = null;
        Method mNodeBuilder = null;
        Method mCachedData = null;
        Method mPermissionData = null;
        Method mCheckPermission = null;
        Method mAsBoolean = null;
        try {
            if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
                throw new IllegalStateException("plugin LuckPerms no cargado");
            }
            Class<?> claseApi = Class.forName("net.luckperms.api.LuckPermsProvider");
            apiObj = claseApi.getMethod("get").invoke(null);
            Class<?> claseUserManager = Class.forName("net.luckperms.api.UserManager");
            Class<?> claseUser = Class.forName("net.luckperms.api.model.user.User");
            Class<?> claseData = Class.forName("net.luckperms.api.model.user.UserData");
            Class<?> claseNode = Class.forName("net.luckperms.api.node.Node");
            Class<?> claseCD = Class.forName("net.luckperms.api.CachedData");
            Class<?> clasePD = Class.forName("net.luckperms.api.permission.PermissionData");
            Class<?> claseRes = Class.forName("net.luckperms.api.permission.PermissionCheckResult");
            mGetUserManager = apiObj.getClass().getMethod("getUserManager");
            Object umClase = claseUserManager;
            mGetUser = ((Class<?>) umClase).getMethod("getUser", UUID.class);
            mLoadUser = ((Class<?>) umClase).getMethod("loadUser", UUID.class);
            mSaveUser = ((Class<?>) umClase).getMethod("saveUser", claseUser);
            mData = claseUser.getMethod("data");
            mToBuilder = claseData.getMethod("toBuilder");
            Class<?> claseBuilder = Class.forName("net.luckperms.api.model.user.UserData$Builder");
            mBuilderAdd = claseBuilder.getMethod("add", claseNode);
            mBuilderRemove = claseBuilder.getMethod("remove", claseNode);
            mBuilderBuild = claseBuilder.getMethod("build");
            mNodeBuilder = claseNode.getMethod("builder", String.class);
            mCachedData = claseUser.getMethod("getCachedData");
            mPermissionData = claseCD.getMethod("getPermissionData");
            mCheckPermission = clasePD.getMethod("checkPermission", String.class);
            mAsBoolean = claseRes.getMethod("asBoolean");
            log.info("LuckPerms detectado: API de grupos/permisos activa (por reflexión).");
        } catch (Throwable cualquierFallo) {
            log.log(Level.FINE, "LuckPerms no disponible; el modulo ranks degrada "
                    + "(no concede grupos, spec §2.5).", cualquierFallo);
        }
        this.api = apiObj;
        this.getUserManager = mGetUserManager;
        this.getUser = mGetUser;
        this.loadUser = mLoadUser;
        this.saveUser = mSaveUser;
        this.data = mData;
        this.toBuilder = mToBuilder;
        this.builderAdd = mBuilderAdd;
        this.builderRemove = mBuilderRemove;
        this.builderBuild = mBuilderBuild;
        this.nodeBuilder = mNodeBuilder;
        this.cachedData = mCachedData;
        this.permissionData = mPermissionData;
        this.checkPermission = mCheckPermission;
        this.asBoolean = mAsBoolean;
    }

    @Override
    public boolean disponible() {
        return api != null;
    }

    @Override
    public boolean agregarGrupo(UUID jugador, String grupo) {
        return modificarGrupo(jugador, grupo, true);
    }

    @Override
    public boolean quitarGrupo(UUID jugador, String grupo) {
        return modificarGrupo(jugador, grupo, false);
    }

    private boolean modificarGrupo(UUID jugador, String grupo, boolean agregar) {
        if (!disponible() || jugador == null || grupo == null || grupo.isBlank()) {
            return false;
        }
        if (Bukkit.isPrimaryThread()) {
            // Bloquear el hilo del servidor con .get() está prohibido (plan §6 regla 3):
            // el outbox (§11 L592, agente B) reintenta desde un hilo async.
            log.fine("LuckPerms.modificarGrupo llamado desde el hilo principal; se rechaza "
                    + "(usar el outbox async). grupo=" + grupo);
            return false;
        }
        try {
            Object userManager = getUserManager.invoke(api);
            Object user = getUser.invoke(userManager, jugador);
            if (user == null) {
                @SuppressWarnings("unchecked")
                var futuro = (java.util.concurrent.CompletableFuture<Object>) loadUser.invoke(userManager, jugador);
                user = futuro.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
            }
            Object nodo = nodeBuilder.invoke(null, "group." + grupo);
            Object builder = toBuilder.invoke(data.invoke(user));
            builder = agregar ? builderAdd.invoke(builder, nodo) : builderRemove.invoke(builder, nodo);
            Object datosNuevos = builderBuild.invoke(builder);
            data.invoke(user, datosNuevos);
            @SuppressWarnings("unchecked")
            var guardado = (java.util.concurrent.CompletableFuture<Object>) saveUser.invoke(userManager, user);
            guardado.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
            return true;
        } catch (Throwable fallo) {
            log.log(Level.FINE, "LuckPerms rechazo " + (agregar ? "agregar" : "quitar")
                    + " grupo " + grupo + " (entra al outbox).", fallo);
            return false;
        }
    }

    @Override
    public boolean tienePermiso(UUID jugador, String permiso) {
        if (!disponible() || jugador == null || permiso == null || permiso.isBlank()) {
            return false;
        }
        try {
            Object userManager = getUserManager.invoke(api);
            Object user = getUser.invoke(userManager, jugador);
            if (user == null) {
                if (Bukkit.isPrimaryThread()) {
                    return false; // sin bloquear el hilo principal; el llamador decide degradar
                }
                @SuppressWarnings("unchecked")
                var futuro = (java.util.concurrent.CompletableFuture<Object>) loadUser.invoke(userManager, jugador);
                user = futuro.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
            }
            Object cd = cachedData.invoke(user);
            Object pd = permissionData.invoke(cd);
            Object resultado = checkPermission.invoke(pd, permiso);
            return Boolean.TRUE.equals(asBoolean.invoke(resultado));
        } catch (Throwable fallo) {
            log.log(Level.FINE, "LuckPerms no pudo consultar el permiso " + permiso, fallo);
            return false;
        }
    }
}

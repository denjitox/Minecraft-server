package ar.com.maydana.mushokucore.integration;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.bootstrap.PluginContextImpl;
import ar.com.maydana.mushokucore.module.PlaceholderFuente;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;

/**
 * Puente con PlaceholderAPI — agente D (spec §7 L306, §18; tabla plan §6.3).
 *
 * <p>Publica los placeholders del §18 vía {@code PluginContext.registrarPlaceholderFuente(...)}
 * (ver {@code module/commands/PlaceholderFuenteMushoku}) y detecta si PlaceholderAPI está cargado.</p>
 *
 * <p><b>Ambigüedad V3 cerrada (2026-10-18).</b> La expansión real es {@link MushokuExpansion}
 * (subclase de {@code me.clip.placeholderapi.expansion.PlaceholderExpansion}, abstracta) y
 * {@link #registrar()} la registra cuando PlaceholderAPI está presente. Antes de esta fecha el
 * método era un stub con aviso, porque {@code build.ps1} no incluía el jar de PAPI en el classpath
 * de compilación; ahora sí lo incluye ({@code servidor/plugins/PlaceholderAPI-2.12.3.jar},
 * verificado con {@code javap} el 2026-10-18).</p>
 *
 * <p>Si PAPI no está, el registro no se intenta: {@link MushokuExpansion} no se carga y no hay
 * {@code NoClassDefFoundError}. Los valores siguen consultables internamente por
 * {@link #fuentes()}.</p>
 *
 * <p>Los valores en sí (las 9 claves) ya están resueltos sin SQL por las fuentes de D, que quedan
 * registradas en el contexto y son consultables por el integrador vía {@link #fuentes()}.</p>
 */
public final class PlaceholderHook {

    private final PluginContext ctx;

    public PlaceholderHook(PluginContext ctx) {
        this.ctx = Objects.requireNonNull(ctx, "ctx");
    }

    /** true si el plugin PlaceholderAPI está presente y su API es cargable. */
    public boolean disponible() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return false;
        }
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            return true;
        } catch (ClassNotFoundException | LinkageError ausente) {
            return false;
        }
    }

    /**
     * Registra la expansión {@code mushoku} en PlaceholderAPI.
     *
     * @return true si quedó registrada; false si PAPI no está presente o el registro falló
     *         (en ese caso se informa el motivo por log, nunca se finge éxito).
     */
    public boolean registrar() {
        if (!disponible()) {
            ctx.plugin().getLogger().info("PlaceholderAPI no esta presente: los placeholders de "
                    + "MushokuCore quedan disponibles para TAB/hologramas internos, sin expansion PAPI.");
            return false;
        }
        try {
            List<PlaceholderFuente> fuentes = fuentes();
            MushokuExpansion expansion = new MushokuExpansion(
                    ctx.plugin().getPluginMeta().getVersion(), fuentes);
            if (!expansion.canRegister()) {
                ctx.plugin().getLogger().warning("PlaceholderAPI presente, pero la expansion '"
                        + MushokuExpansion.IDENTIFICADOR + "' no puede registrarse (canRegister=false).");
                return false;
            }
            if (!expansion.register()) {
                ctx.plugin().getLogger().warning("PlaceholderAPI presente, pero el registro de la "
                        + "expansion '" + MushokuExpansion.IDENTIFICADOR + "' devolvio false.");
                return false;
            }
            ctx.plugin().getLogger().info("Expansion PlaceholderAPI '" + MushokuExpansion.IDENTIFICADOR
                    + "' registrada con " + expansion.getPlaceholders().size() + " claves del §18.");
            return true;
        } catch (RuntimeException | LinkageError fallo) {
            // LinkageError cubre un PAPI con API incompatible: el plugin NO se cae por placeholders.
            ctx.plugin().getLogger().warning("No se pudo registrar la expansion de PlaceholderAPI: "
                    + fallo.getClass().getSimpleName() + ": " + fallo.getMessage());
            return false;
        }
    }

    /** Fuentes registradas en el contexto (para que el integrador arme la expansion). */
    public List<PlaceholderFuente> fuentes() {
        if (ctx instanceof PluginContextImpl impl) {
            return impl.fuentesPlaceholder();
        }
        return List.of();
    }
}

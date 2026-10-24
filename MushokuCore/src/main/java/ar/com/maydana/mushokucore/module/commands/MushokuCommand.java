package ar.com.maydana.mushokucore.module.commands;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.bootstrap.PluginContextImpl;
import ar.com.maydana.mushokucore.config.MessagesService;
import ar.com.maydana.mushokucore.config.PluginConfig;
import ar.com.maydana.mushokucore.database.IDatabase;
import ar.com.maydana.mushokucore.module.commands.importer.ImportarCommand;
import ar.com.maydana.mushokucore.module.diseases.IEnfermedadesService;
import ar.com.maydana.mushokucore.module.ranks.IPotenciasService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * {@code /mushoku} — agente D (ui-comandos-import; spec §17 L799-838, plan §5.1 tabla L555-560).
 *
 * <p>Subcomandos: {@code ayuda} ({@code mushoku.use}), {@code estado} ({@code mushoku.admin}),
 * {@code reload} ({@code mushoku.admin.reload}), {@code diagnostico} ({@code mushoku.admin}),
 * {@code backupdb} ({@code mushoku.admin}) e {@code import} (delegado a {@link ImportarCommand},
 * {@code mushoku.admin.import}; DECISION D12 2026-10-18: invocable en juego y por consola).</p>
 *
 * <p><b>Hilos (§19 L860):</b> {@code backupdb} (usa {@link IDatabase#backup}) y la comprobación de
 * salud de {@code diagnostico} corren en tarea async; el mensaje vuelve al hilo principal con
 * {@code runTask}. {@code estado}/{@code ayuda} solo leen el holder de config y el
 * {@code PluginManager}: cero SQL.</p>
 *
 * <p><b>Fallo seguro §2.5:</b> {@code ctx.config()}/{@code ctx.db()} pueden lanzar
 * {@link IllegalStateException} si el wiring de Fase 2 todavía no los proveyó; se degrada con
 * {@link Textos#opcional} en vez de tumbar el comando.</p>
 *
 * <p><b>Textos faltantes:</b> messages.yml (F0) no trae las claves {@code mushoku.*}; se usa un
 * literal con la marca {@code [PROPUESTA D - clave messages.yml … pendiente Fase 2]} para que el
 * integrador las agregue (no se edita messages.yml: es de F0).</p>
 */
public final class MushokuCommand implements CommandExecutor, TabCompleter {

    private final PluginContext ctx;
    private final ImportarCommand importar;

    public MushokuCommand(PluginContext ctx) {
        this.ctx = Objects.requireNonNull(ctx, "ctx");
        this.importar = new ImportarCommand(ctx);
    }

    @Override
    public boolean onCommand(CommandSender emisor, Command comando, String etiqueta, String[] args) {
        if (args.length == 0) {
            if (!emisor.hasPermission("mushoku.use")) {
                Textos.enviar(emisor, Textos.msg(ctx, "generic.no-permission"));
                return true;
            }
            ayuda(emisor);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "ayuda":
            case "help":
                if (exigir(emisor, "mushoku.use")) {
                    ayuda(emisor);
                }
                return true;
            case "estado":
                if (exigir(emisor, "mushoku.admin")) {
                    estado(emisor);
                }
                return true;
            case "reload":
                if (exigir(emisor, "mushoku.admin.reload")) {
                    reload(emisor);
                }
                return true;
            case "diagnostico":
                if (exigir(emisor, "mushoku.admin")) {
                    diagnostico(emisor);
                }
                return true;
            case "backupdb":
                if (exigir(emisor, "mushoku.admin")) {
                    backupdb(emisor);
                }
                return true;
            case "import":
                // ImportarCommand valida mushoku.admin.import y el resto de la sintaxis.
                importar.ejecutar(emisor, args);
                return true;
            default:
                Textos.enviar(emisor, Textos.prefijo(ctx) + "&cSubcomando desconocido. Probá &e/mushoku ayuda&c.");
                return true;
        }
    }

    /** @return true si el emisor tiene el permiso; si no, ya le envió el mensaje. */
    private boolean exigir(CommandSender emisor, String permiso) {
        if (emisor.hasPermission(permiso)) {
            return true;
        }
        Textos.enviar(emisor, Textos.msg(ctx, "generic.no-permission"));
        return false;
    }

    // ---------------------------------------------------------------- ayuda
    private void ayuda(CommandSender emisor) {
        // [PROPUESTA D - clave messages.yml mushoku.help-header pendiente para Fase 2]
        Textos.enviar(emisor, "&8&m-------------------------------");
        Textos.enviar(emisor, "&6&lMUSHOKUCORE");
        Textos.enviar(emisor, "&e/potencias &7— Las Siete Potencias Mundiales");
        Textos.enviar(emisor, "&e/mtestado &7— tu estado de personaje");
        Textos.enviar(emisor, "&e/faccion &7/ &e/alianza &7/ &e/reliquia &7— próximamente");
        if (emisor.hasPermission("mushoku.admin")) {
            // [PROPUESTA D - clave messages.yml mushoku.help-admin pendiente para Fase 2]
            Textos.enviar(emisor, "&7Admin: &e/mushoku estado&7, &e/mushoku diagnostico&7, &e/mushoku backupdb");
        }
        if (emisor.hasPermission("mushoku.admin.reload")) {
            Textos.enviar(emisor, "&7Admin: &e/mushoku reload");
        }
        if (emisor.hasPermission("mushoku.admin.import")) {
            Textos.enviar(emisor, "&7Admin: &e/mushoku import skript <ruta> [--simular]");
        }
        Textos.enviar(emisor, "&8&m-------------------------------");
    }

    // --------------------------------------------------------------- estado
    private void estado(CommandSender emisor) {
        // [PROPUESTA D - clave messages.yml mushoku.estado-header pendiente para Fase 2]
        Textos.enviar(emisor, "&8&m-------------------------------");
        Textos.enviar(emisor, "&6&lESTADO DE MUSHOKUCORE");
        Textos.enviar(emisor, "&7Versión: &f" + version());
        Textos.enviar(emisor, "&7Servidor: &f" + Bukkit.getVersion());

        PluginConfig config = Textos.opcional(ctx::config);
        if (config == null) {
            // [PROPUESTA D - clave messages.yml mushoku.estado-sin-config pendiente para Fase 2]
            Textos.enviar(emisor, "&cConfiguración no disponible (config sin proveer).");
            return;
        }
        Textos.enviar(emisor, "&7Habilitado: &f" + (config.enabled() ? "sí" : "no")
                + " &7schema: &f" + config.schema());
        Textos.enviar(emisor, "&7Almacenamiento: &f" + config.storageType() + " &7(" + config.storageFile() + ")");
        Textos.enviar(emisor, "&7Mundo: &f" + config.mundoNombre()
                + " &7borde &f" + config.bordeDiametro());

        // [PROPUESTA D - clave messages.yml mushoku.estado-modulos pendiente para Fase 2]
        Textos.enviar(emisor, "&7Módulos:");
        modulo(emisor, "ranks", config.moduloRanks(), ctx.obtener(IPotenciasService.class) != null);
        modulo(emisor, "diseases", config.moduloDiseases(), ctx.obtener(IEnfermedadesService.class) != null);
        modulo(emisor, "factions", config.moduloFactions(), false);
        modulo(emisor, "relics", config.moduloRelics(), false);
        modulo(emisor, "dimensions", config.moduloDimensions(), false);
        modulo(emisor, "bedrock-bridge", config.moduloBedrockBridge(), null);

        IDatabase db = Textos.opcional(ctx::db);
        Textos.enviar(emisor, "&7Base de datos: &f" + (db == null ? "no provista" : "provista"));
        Textos.enviar(emisor, "&8&m-------------------------------");
    }

    private void modulo(CommandSender emisor, String nombre, boolean configurado, Boolean servicio) {
        String estadoModulo;
        if (!configurado) {
            estadoModulo = "&cdesactivado";
        } else if (servicio == null) {
            estadoModulo = "&aactivado";
        } else if (servicio) {
            estadoModulo = "&aactivado";
        } else {
            estadoModulo = "&eactivado (servicio aún no publicado)";
        }
        Textos.enviar(emisor, "  &7- &f" + nombre + "&7: " + estadoModulo);
    }

    // --------------------------------------------------------------- reload
    private void reload(CommandSender emisor) {
        // §17 L838: solo archivos propios seguros. Hoy recargable sin riesgo: messages.yml.
        // config.yml NO se recarga en caliente: no hay método de reload en PluginConfig/Impl y
        // §17 L838 prohíbe reconstruir dependencias críticas en caliente → [PENDIENTE integrador].
        try {
            MessagesService recargado = MessagesService.cargar(ctx.plugin());
            if (ctx instanceof PluginContextImpl impl) {
                impl.proveerMessages(recargado);
            } else {
                Textos.enviar(emisor, "&cNo se pudo recargar: el contexto no es recargable.");
                return;
            }
        } catch (RuntimeException fallo) {
            Textos.enviar(emisor, Textos.msg(ctx, "generic.db-error"));
            ctx.plugin().getLogger().log(Level.SEVERE, "/mushoku reload fallo al recargar messages.yml", fallo);
            return;
        }
        Textos.enviar(emisor, Textos.prefijo(ctx)
                + Textos.msg(ctx, "generic.reload-ok", Map.of("modulos", modulosActivos())));
        try {
            ctx.audit().registrar(null, "config.reload", "messages.yml", "recarga manual");
        } catch (RuntimeException sinAuditoria) {
            // La auditoría nunca debe tumbar el comando (IAuditoria no lanza; cinturón y tiradores).
            ctx.plugin().getLogger().fine("Auditoría no disponible en reload: " + sinAuditoria.getMessage());
        }
    }

    private String modulosActivos() {
        PluginConfig config = Textos.opcional(ctx::config);
        if (config == null) {
            return "config sin proveer";
        }
        List<String> activos = new ArrayList<>();
        if (config.moduloRanks()) {
            activos.add("ranks");
        }
        if (config.moduloDiseases()) {
            activos.add("diseases");
        }
        if (config.moduloBedrockBridge()) {
            activos.add("bedrock-bridge");
        }
        return activos.isEmpty() ? "ninguno" : String.join(", ", activos);
    }

    // ---------------------------------------------------------- diagnostico
    private void diagnostico(CommandSender emisor) {
        // §24 L1007. Todo lo barato en el hilo principal; solo el chequeo de salud de la BD (SQL)
        // va a una tarea async (§19 L860: cero SQL en el hilo del servidor).
        Textos.enviar(emisor, "&8&m-------------------------------");
        Textos.enviar(emisor, "&6&lDIAGNÓSTICO DE MUSHOKUCORE");
        Textos.enviar(emisor, "&7Plugin: &f" + version());
        Textos.enviar(emisor, "&7Servidor: &f" + Bukkit.getVersion());
        Textos.enviar(emisor, "&7Java: &f" + System.getProperty("java.version"));
        Textos.enviar(emisor, "&7So: &f" + System.getProperty("os.name"));

        PluginConfig config = Textos.opcional(ctx::config);
        Textos.enviar(emisor, "&7Config: &f" + (config == null ? "no provista"
                : "schema " + config.schema() + (config.enabled() ? " (habilitado)" : " (deshabilitado)")));

        // Servicios publicados por B/C (lectura de mapa, sin SQL).
        Textos.enviar(emisor, "&7IPotenciasService: &f" + presencia(ctx.obtener(IPotenciasService.class)));
        Textos.enviar(emisor, "&7IEnfermedadesService: &f" + presencia(ctx.obtener(IEnfermedadesService.class)));

        // Integraciones: lecturas del PluginManager (hilo principal).
        Textos.enviar(emisor, "&7Integraciones:");
        integracion(emisor, "LuckPerms");
        integracion(emisor, "PlaceholderAPI");
        integracion(emisor, "floodgate");
        integracion(emisor, "Towny");

        IDatabase db = Textos.opcional(ctx::db);
        if (db == null) {
            Textos.enviar(emisor, "&7BD: &cno provista");
            Textos.enviar(emisor, "&8&m-------------------------------");
            return;
        }
        Textos.enviar(emisor, "&7BD: &7consultando…");
        ctx.plugin().getServer().getScheduler().runTaskAsynchronously(ctx.plugin(), () -> {
            boolean sano;
            try {
                sano = db.isHealthy();
            } catch (RuntimeException fallo) {
                sano = false;
            }
            final boolean sanoFinal = sano;
            enPrincipal(() -> {
                Textos.enviar(emisor, "&7BD: &f" + (sanoFinal ? "&aOK" : "&csin respuesta"));
                Textos.enviar(emisor, "&8&m-------------------------------");
            });
        });
    }

    private static String presencia(Object servicio) {
        return servicio == null ? "&cno publicado" : "&aOK";
    }

    private void integracion(CommandSender emisor, String nombre) {
        boolean presente = Bukkit.getPluginManager().getPlugin(nombre) != null;
        Textos.enviar(emisor, "  &7- &f" + nombre + "&7: " + (presente ? "&aOK" : "&causente"));
    }

    // ------------------------------------------------------------- backupdb
    private void backupdb(CommandSender emisor) {
        IDatabase db = Textos.opcional(ctx::db);
        if (db == null) {
            // [PROPUESTA D - clave messages.yml mushoku.backupdb-fail pendiente para Fase 2]
            Textos.enviar(emisor, Textos.prefijo(ctx) + "&cNo hay base de datos para respaldar.");
            return;
        }
        Textos.enviar(emisor, Textos.prefijo(ctx) + "&7Creando copia de seguridad…");
        ctx.plugin().getServer().getScheduler().runTaskAsynchronously(ctx.plugin(), () -> {
            java.nio.file.Path destino = null;
            String error = null;
            try {
                destino = db.backup("manual /mushoku backupdb");
            } catch (Exception fallo) {
                error = fallo.getMessage() == null ? fallo.getClass().getSimpleName() : fallo.getMessage();
                ctx.plugin().getLogger().log(Level.SEVERE, "/mushoku backupdb fallo", fallo);
            }
            final java.nio.file.Path destinoFinal = destino;
            final String errorFinal = error;
            enPrincipal(() -> {
                if (destinoFinal != null) {
                    // [PROPUESTA D - clave messages.yml mushoku.backupdb-ok pendiente para Fase 2]
                    Textos.enviar(emisor, Textos.prefijo(ctx) + "&aCopia creada: &f" + destinoFinal);
                    try {
                        ctx.audit().registrar(null, "db.backup", String.valueOf(destinoFinal), "manual");
                    } catch (RuntimeException sinAuditoria) {
                        ctx.plugin().getLogger().fine("Auditoría no disponible en backupdb: "
                                + sinAuditoria.getMessage());
                    }
                } else {
                    Textos.enviar(emisor, Textos.prefijo(ctx) + "&cFallo la copia de seguridad: &f" + errorFinal);
                }
            });
        });
    }

    private String version() {
        return ctx.plugin().getPluginMeta().getVersion();
    }

    private void enPrincipal(Runnable tarea) {
        ctx.plugin().getServer().getScheduler().runTask(ctx.plugin(), tarea);
    }

    // ---------------------------------------------------------- tab-complete
    @Override
    public List<String> onTabComplete(CommandSender emisor, Command comando, String etiqueta, String[] args) {
        if (args.length == 1) {
            String prefijo = args[0].toLowerCase(Locale.ROOT);
            List<String> opciones = new ArrayList<>();
            if (emisor.hasPermission("mushoku.use")) {
                opciones.add("ayuda");
            }
            if (emisor.hasPermission("mushoku.admin")) {
                opciones.add("estado");
                opciones.add("diagnostico");
                opciones.add("backupdb");
            }
            if (emisor.hasPermission("mushoku.admin.reload")) {
                opciones.add("reload");
            }
            if (emisor.hasPermission("mushoku.admin.import")) {
                opciones.add("import");
            }
            return filtrar(opciones, prefijo);
        }
        if (args.length == 2 && "import".equalsIgnoreCase(args[0])
                && emisor.hasPermission("mushoku.admin.import")) {
            return filtrar(List.of("skript"), args[1].toLowerCase(Locale.ROOT));
        }
        return List.of();
    }

    private static List<String> filtrar(List<String> opciones, String prefijo) {
        List<String> resultado = new ArrayList<>();
        for (String opcion : opciones) {
            if (opcion.startsWith(prefijo)) {
                resultado.add(opcion);
            }
        }
        return resultado;
    }
}

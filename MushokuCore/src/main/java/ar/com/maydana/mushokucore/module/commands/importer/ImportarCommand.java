package ar.com.maydana.mushokucore.module.commands.importer;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.importer.SkriptImporter;
import ar.com.maydana.mushokucore.module.commands.Textos;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;

/**
 * Subcomando {@code /mushoku import skript <ruta> [--simular]} — agente D (plan §5.1).
 * No está en §17 del spec: el plan lo propone como subcomando admin (permiso
 * {@code mushoku.admin.import}). DECISION D12 (2026-10-18): es invocable <b>en juego</b> por un
 * jugador con ese permiso (y también por consola); no exige consola.
 *
 * <p><b>Hilo:</b> la lectura del archivo y las escrituras corren en una tarea async; el mensaje
 * vuelve al hilo principal con {@code runTask} (§19 L860). La simulación (spec §21 L900) no escribe.</p>
 */
public final class ImportarCommand {

    private final PluginContext ctx;

    public ImportarCommand(PluginContext ctx) {
        this.ctx = Objects.requireNonNull(ctx, "ctx");
    }

    /**
     * @param args argumentos completos del comando, incluido {@code "import"} en {@code args[0]}.
     */
    public void ejecutar(CommandSender emisor, String[] args) {
        if (!emisor.hasPermission("mushoku.admin.import")) {
            Textos.enviar(emisor, Textos.msg(ctx, "generic.no-permission"));
            return;
        }

        int indice = 1;
        if (args.length > 1 && "skript".equalsIgnoreCase(args[1])) {
            indice = 2;   // /mushoku import skript <ruta> ...
        }
        if (args.length <= indice) {
            Textos.enviar(emisor, Textos.prefijo(ctx)
                    + "&cUso: /mushoku import skript <ruta-a-variables.csv> [--simular]");
            return;
        }

        String rutaTexto = args[indice];
        boolean simular = false;
        for (int i = indice + 1; i < args.length; i++) {
            if ("--simular".equalsIgnoreCase(args[i])) {
                simular = true;
            }
        }

        final Path archivo;
        try {
            archivo = resolverRuta(rutaTexto);
        } catch (InvalidPathException rutaInvalida) {
            Textos.enviar(emisor, Textos.prefijo(ctx) + "&cRuta invalida: " + rutaTexto);
            return;
        }

        final boolean enSimulacion = simular;
        Textos.enviar(emisor, Textos.prefijo(ctx) + "&7Importando " + archivo
                + (enSimulacion ? " &7(modo &esimulacion&7)" : " &7(modo &creal&7)") + "…");

        ctx.plugin().getServer().getScheduler().runTaskAsynchronously(ctx.plugin(), () -> {
            if (!Files.isRegularFile(archivo)) {
                enPrincipal(() -> Textos.enviar(emisor,
                        Textos.prefijo(ctx) + "&cNo existe el archivo: " + archivo));
                return;
            }
            new SkriptImporter(ctx).procesar(archivo, !enSimulacion)
                    .whenComplete((resultado, error) -> enPrincipal(() -> {
                        if (error != null) {
                            ctx.plugin().getLogger().log(Level.SEVERE,
                                    "Importacion de Skript fallo (" + archivo + ")", error);
                            Textos.enviar(emisor, Textos.msg(ctx, "generic.db-error"));
                            return;
                        }
                        informar(emisor, resultado, enSimulacion);
                    }));
        });
    }

    private void informar(CommandSender emisor, SkriptImporter.Resultado resultado, boolean simular) {
        if (resultado.variables() == 0) {
            // [PROPUESTA D - clave messages.yml import.sin-variables pendiente para Fase 2]
            Textos.enviar(emisor, Textos.prefijo(ctx)
                    + "&eEl archivo no tiene variables persistidas (solo cabecera). Nada que importar.");
        }
        if (simular) {
            if (resultado.conflictos().isEmpty()) {
                Textos.enviar(emisor, Textos.msg(ctx, "import.dry-run-ok",
                        java.util.Map.of("n", String.valueOf(resultado.variables()))));
            } else {
                Textos.enviar(emisor, Textos.msg(ctx, "import.dry-run-conflicts",
                        java.util.Map.of("n", String.valueOf(resultado.conflictos().size()))));
            }
        } else {
            int aplicados = resultado.puestos() + resultado.puntos() + resultado.cooldowns();
            Textos.enviar(emisor, Textos.msg(ctx, "import.real-done",
                    java.util.Map.of("n", String.valueOf(aplicados))));
        }
        // [PROPUESTA D - clave messages.yml import.resumen pendiente para Fase 2]
        Textos.enviar(emisor, "&7puestos=" + resultado.puestos() + " puntos=" + resultado.puntos()
                + " cooldowns=" + resultado.cooldowns() + " conflictos=" + resultado.conflictos().size());
        for (String conflicto : resultado.conflictos()) {
            Textos.enviar(emisor, "&e- " + conflicto);
        }
    }

    private void enPrincipal(Runnable tarea) {
        ctx.plugin().getServer().getScheduler().runTask(ctx.plugin(), tarea);
    }

    /** Rutas relativas se resuelven contra la raiz del servidor (…/plugins/MushokuCore → raiz). */
    private Path resolverRuta(String rutaTexto) {
        Path ruta = Path.of(rutaTexto.trim());
        if (ruta.isAbsolute()) {
            return ruta.normalize();
        }
        Path datos = ctx.plugin().getDataFolder().toPath().toAbsolutePath();
        Path raiz = datos.getParent() == null ? datos : datos.getParent().getParent();
        return (raiz == null ? datos : raiz).resolve(ruta).normalize();
    }
}

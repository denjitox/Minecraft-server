package ar.com.maydana.mushokucore.module.commands;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.ui.JavaChatUi;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /mtestado} — agente D (plan §5.1, spec §17 L801). Delega en {@link JavaChatUi#showStatus},
 * que consume {@code IPotenciasService} (B) e {@code IEnfermedadesService} (C) <b>por el contexto</b>
 * y degrada si alguno es {@code null} (fallo seguro §2.5). Sin SQL: solo cachés/servicios.
 *
 * <p>Solo jugadores (necesita un {@code Player} para el estado del personaje).</p>
 */
public final class MtestadoCommand implements CommandExecutor {

    private final PluginContext ctx;
    private final JavaChatUi ui;

    public MtestadoCommand(PluginContext ctx, JavaChatUi ui) {
        this.ctx = Objects.requireNonNull(ctx, "ctx");
        this.ui = Objects.requireNonNull(ui, "ui");
    }

    @Override
    public boolean onCommand(CommandSender emisor, Command comando, String etiqueta, String[] args) {
        if (!emisor.hasPermission("mushoku.use")) {
            Textos.enviar(emisor, Textos.msg(ctx, "generic.no-permission"));
            return true;
        }
        if (!(emisor instanceof Player jugador)) {
            // [Fase 2] la clave messages.yml mtestado.solo-jugadores ya existe.
            Textos.enviar(emisor, Textos.msg(ctx, "mtestado.solo-jugadores"));
            return true;
        }
        try {
            ui.showStatus(jugador);
        } catch (RuntimeException excepcion) {
            // Nunca stacktrace al jugador (spec §17 L836); fallo seguro §2.5.
            Textos.enviar(jugador, Textos.msg(ctx, "generic.db-error"));
            ctx.plugin().getLogger().log(Level.SEVERE,
                    "/mtestado fallo para " + jugador.getUniqueId(), excepcion);
        }
        return true;
    }
}

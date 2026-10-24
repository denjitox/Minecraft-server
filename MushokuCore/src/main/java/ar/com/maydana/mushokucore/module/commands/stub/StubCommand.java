package ar.com.maydana.mushokucore.module.commands.stub;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.module.commands.Textos;
import java.util.Objects;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Base de los comandos-stub del MVP — agente D (ui-comandos-import).
 *
 * <p>{@code /faccion}, {@code /alianza} y {@code /reliquia} pertenecen a módulos que el MVP
 * <b>no</b> incluye (1.1/1.2, spec §3 L107-124). El plan §4.1 los deja declarados en
 * {@code plugin.yml} como stub informativo: responden siempre con
 * {@code messages.yml → stub.module-future} y nunca ejecutan lógica. DECISION D3 (2026-10-18):
 * se mantienen en 1.0 — la opción de sacarlos del plugin.yml quedó descartada.</p>
 *
 * <p>Fallo seguro §2.5: el texto sale de {@link Textos#msg}, que nunca lanza aunque
 * {@code messages.yml} no esté provisto.</p>
 */
abstract class StubCommand implements CommandExecutor {

    private final PluginContext ctx;
    private final String modulo;

    StubCommand(PluginContext ctx, String modulo) {
        this.ctx = Objects.requireNonNull(ctx, "ctx");
        this.modulo = Objects.requireNonNull(modulo, "modulo");
    }

    /** Id del módulo que llegará en una versión futura (para el log; el texto es de messages.yml). */
    String modulo() {
        return modulo;
    }

    @Override
    public final boolean onCommand(CommandSender emisor, Command comando, String etiqueta, String[] args) {
        if (!emisor.hasPermission("mushoku.use")) {
            Textos.enviar(emisor, Textos.msg(ctx, "generic.no-permission"));
            return true;
        }
        Textos.enviar(emisor, Textos.prefijo(ctx) + Textos.msg(ctx, "stub.module-future"));
        return true;
    }
}

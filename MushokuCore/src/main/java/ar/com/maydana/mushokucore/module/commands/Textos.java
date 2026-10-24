package ar.com.maydana.mushokucore.module.commands;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Utilidades de texto para los comandos del módulo D (ui-comandos-import).
 * [PROPUESTA D] helpers compartidos por MushokuCommand/MtestadoCommand/stubs/JavaChatUi.
 *
 * <p>Fallo seguro §2.5: si {@code ctx.messages()} todavía no está provisto (wiring Fase 2
 * pendiente), {@link #msg} devuelve la propia clave en lugar de lanzar — un texto faltante
 * no puede tumbar un comando.</p>
 */
public final class Textos {

    private Textos() {
    }

    /** Texto de messages.yml; si el servicio no está provisto devuelve la clave. */
    public static String msg(PluginContext ctx, String clave) {
        try {
            return ctx.messages().texto(clave);
        } catch (IllegalStateException servicioAusente) {
            return clave;
        }
    }

    /** Texto de messages.yml con reemplazos {@code {clave} -> valor}. */
    public static String msg(PluginContext ctx, String clave, Map<String, String> reemplazos) {
        try {
            return ctx.messages().texto(clave, reemplazos);
        } catch (IllegalStateException servicioAusente) {
            return clave;
        }
    }

    /** Traduce códigos {@code &} legacy (messages.yml los usa; plan §4.3). */
    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }

    /** Envía un mensaje al destinatario con colores traducidos. */
    public static void enviar(CommandSender destino, String mensaje) {
        destino.sendMessage(color(mensaje));
    }

    /** Prefijo de messages.yml ({@code prefix}); clave si no está provisto. */
    public static String prefijo(PluginContext ctx) {
        return msg(ctx, "prefix") + " ";
    }

    /**
     * Ejecuta y devuelve el valor, o {@code null} si el servicio no está provisto todavía.
     * [PROPUESTA D] el scaffold de Fase 0 lanza {@link IllegalStateException} en
     * {@code config()}/{@code db()}/{@code luckperms()} hasta que A + integrador hacen el
     * wiring: los comandos degradan en vez de explotar (fallo seguro §2.5).
     */
    public static <T> T opcional(java.util.function.Supplier<T> proveedor) {
        try {
            return proveedor.get();
        } catch (IllegalStateException sinProveer) {
            return null;
        }
    }
}

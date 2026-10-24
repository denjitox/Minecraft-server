package ar.com.maydana.mushokucore.command;

import ar.com.maydana.mushokucore.config.Messages;
import ar.com.maydana.mushokucore.module.diseases.DiseasesService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * {@code /enfermedad} — agente C (enfermedades), plan §5.1 y spec §17 L818-819.
 *
 * <pre>
 *   /enfermedad aplicar &lt;jugador&gt; &lt;id&gt;
 *   /enfermedad curar   &lt;jugador&gt; [id|todas]
 * </pre>
 *
 * <p>Permiso {@code mushoku.admin.enfermedades} (declarado en {@code plugin.yml}). Funciona
 * desde consola y desde jugador; autocompleta subcomando, jugadores e ids. Toda escritura se
 * audita dentro de {@link DiseasesService}. Acá solo se resuelve el jugador objetivo en línea
 * (como el Skript actual) — un objetivo offline responde {@code generic.player-not-found}
 * <b>[PROPUESTA]</b>.</p>
 */
public final class EnfermedadCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISO = "mushoku.admin.enfermedades";
    private static final String USO = "&cUso: /enfermedad aplicar <jugador> <id> | "
            + "/enfermedad curar <jugador> [id|todas]";

    private final DiseasesService servicio;
    private final Messages mensajes;

    public EnfermedadCommand(DiseasesService servicio, Messages mensajes) {
        this.servicio = servicio;
        this.mensajes = mensajes;
    }

    @Override
    public boolean onCommand(CommandSender emisor, Command comando, String etiqueta, String[] args) {
        if (!emisor.hasPermission(PERMISO)) {
            emisor.sendMessage(mensajes.texto("generic.no-permission"));
            return true;
        }
        if (args.length < 3) {
            emisor.sendMessage(color(USO));
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        Player objetivo = Bukkit.getPlayerExact(args[1]);
        if (objetivo == null) {
            emisor.sendMessage(mensajes.texto("generic.player-not-found",
                    Map.of("jugador", args[1])));
            return true;
        }
        switch (sub) {
            case "aplicar" -> aplicar(emisor, objetivo, args[2]);
            case "curar" -> curar(emisor, objetivo, args[2]);
            default -> emisor.sendMessage(color(USO));
        }
        return true;
    }

    private void aplicar(CommandSender emisor, Player objetivo, String id) {
        if (!servicio.idsCatalogo().contains(id)) {
            emisor.sendMessage(color("&cEnfermedad desconocida: &f" + id
                    + "&c. Validas: " + String.join(", ", servicio.idsCatalogo()) + "."));
            return;
        }
        if (servicio.aplicar(objetivo.getUniqueId(), id, "admin")) {
            emisor.sendMessage(mensajes.texto("enfermedad.admin-applied",
                    Map.of("id", id, "jugador", objetivo.getName())));
        } else {
            emisor.sendMessage(color("&eNo se aplicó &f" + id + "&e a " + objetivo.getName()
                    + " (¿ya activa o inmune?)."));
        }
    }

    private void curar(CommandSender emisor, Player objetivo, String id) {
        String objetivoId = id.equalsIgnoreCase("todas") ? null : id;
        int curadas = servicio.curar(objetivo.getUniqueId(), objetivoId);
        emisor.sendMessage(mensajes.texto("enfermedad.admin-cured",
                Map.of("jugador", objetivo.getName()))
                + color(" &7(" + curadas + " enfermedad(es))"));
    }

    @Override
    public List<String> onTabComplete(CommandSender emisor, Command comando, String etiqueta, String[] args) {
        if (!emisor.hasPermission(PERMISO)) {
            return List.of();
        }
        List<String> opciones = new ArrayList<>();
        if (args.length == 1) {
            opciones.add("aplicar");
            opciones.add("curar");
        } else if (args.length == 2) {
            for (Player jugador : Bukkit.getOnlinePlayers()) {
                opciones.add(jugador.getName());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("aplicar")) {
                opciones.addAll(servicio.idsCatalogo());
            } else if (args[0].equalsIgnoreCase("curar")) {
                opciones.addAll(servicio.idsCatalogo());
                opciones.add("todas");
            }
        }
        return filtrar(opciones, args[args.length - 1]);
    }

    private static List<String> filtrar(List<String> opciones, String prefijo) {
        String buscado = prefijo.toLowerCase(Locale.ROOT);
        List<String> resultado = new ArrayList<>();
        for (String opcion : opciones) {
            if (opcion.toLowerCase(Locale.ROOT).startsWith(buscado)) {
                resultado.add(opcion);
            }
        }
        return resultado;
    }

    private static String color(String texto) {
        return ChatColor.translateAlternateColorCodes('&', texto);
    }
}

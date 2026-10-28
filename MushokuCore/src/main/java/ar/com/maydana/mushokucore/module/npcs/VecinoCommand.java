package ar.com.maydana.mushokucore.module.npcs;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.module.commands.Textos;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * {@code /vecino}: administracion y respaldo de la interaccion con los NPC.
 *
 * <pre>
 *   /vecino lista                 ids definidos en npcs.yml y si estan colocados
 *   /vecino info &lt;id&gt;             skin, regalos, trueques y ubicacion
 *   /vecino hablar &lt;id&gt;          mismo dialogo que el click derecho
 *   /vecino trueques &lt;id&gt;        abre la pantalla de trueques
 *   /vecino reputacion &lt;id&gt;      reputacion propia con ese vecino
 *   /vecino colocar &lt;id&gt;         coloca el NPC en tu posicion (admin)
 *   /vecino quitar &lt;id&gt;          lo elimina del mundo (admin)
 *   /vecino recargar              relee npcs.yml y npcs-locations.yml (admin)
 * </pre>
 *
 * <p>Los subcomandos de juego existen para Bedrock: si el cliente no puede clickear el
 * mannequin, el jugador llega al mismo dialogo y a los mismos trueques por comando.</p>
 */
public final class VecinoCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISO_USO = "mushoku.vecino.use";
    private static final String PERMISO_ADMIN = "mushoku.admin.vecinos";

    private final PluginContext ctx;
    private final NpcsService servicio;

    public VecinoCommand(PluginContext ctx, NpcsService servicio) {
        this.ctx = ctx;
        this.servicio = servicio;
    }

    @Override
    public boolean onCommand(CommandSender emisor, Command comando, String etiqueta, String[] args) {
        if (!emisor.hasPermission(PERMISO_USO)) {
            avisar(emisor, "generic.no-permission", Map.of());
            return true;
        }
        if (args.length == 0) {
            ayuda(emisor);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "lista" -> listar(emisor);
            case "info" -> info(emisor, args);
            case "hablar" -> hablar(emisor, args);
            case "trueques" -> trueques(emisor, args);
            case "reputacion" -> reputacion(emisor, args);
            case "colocar" -> colocar(emisor, args);
            case "quitar" -> quitar(emisor, args);
            case "recargar" -> recargar(emisor);
            default -> ayuda(emisor);
        }
        return true;
    }

    private void ayuda(CommandSender emisor) {
        avisar(emisor, "vecinos.ayuda", Map.of());
    }

    private void listar(CommandSender emisor) {
        if (servicio.definiciones().isEmpty()) {
            avisar(emisor, "vecinos.sin-definiciones", Map.of());
            return;
        }
        avisar(emisor, "vecinos.lista-header", Map.of(
                "total", String.valueOf(servicio.definiciones().size()),
                "ubicados", String.valueOf(servicio.ubicados())));
        for (NpcDefinition definicion : servicio.definiciones()) {
            avisar(emisor, "vecinos.lista-item", Map.of(
                    "id", definicion.id(),
                    "nombre", definicion.nombre(),
                    "estado", servicio.ubicacion(definicion.id()) == null
                            ? Textos.msg(ctx, "vecinos.estado-sin-ubicar")
                            : Textos.msg(ctx, "vecinos.estado-ubicado")));
        }
    }

    private void info(CommandSender emisor, String[] args) {
        NpcDefinition definicion = definicion(emisor, args);
        if (definicion == null) {
            return;
        }
        avisar(emisor, "vecinos.info-cabecera", Map.of(
                "id", definicion.id(),
                "nombre", definicion.nombre()));
        avisar(emisor, "vecinos.info-linea", Map.of(
                "clave", "titulo",
                "valor", definicion.titulo() == null || definicion.titulo().isBlank()
                        ? "-" : definicion.titulo()));
        avisar(emisor, "vecinos.info-linea", Map.of(
                "clave", "skin",
                "valor", definicion.tieneSkin()
                        ? Textos.msg(ctx, "vecinos.skin-si") : Textos.msg(ctx, "vecinos.skin-no")));
        avisar(emisor, "vecinos.info-linea", Map.of(
                "clave", "regalos",
                "valor", String.valueOf(definicion.regalos().size())));
        avisar(emisor, "vecinos.info-linea", Map.of(
                "clave", "trueques",
                "valor", String.valueOf(definicion.trueques().size())));
        NpcLocation ubicacion = servicio.ubicacion(definicion.id());
        avisar(emisor, "vecinos.info-linea", Map.of(
                "clave", "ubicacion",
                "valor", ubicacion == null
                        ? Textos.msg(ctx, "vecinos.estado-sin-ubicar")
                        : ubicacion.mundo() + " " + Math.round(ubicacion.x()) + " "
                                + Math.round(ubicacion.y()) + " " + Math.round(ubicacion.z())));
    }

    private void hablar(CommandSender emisor, String[] args) {
        if (!(emisor instanceof Player jugador)) {
            avisar(emisor, "vecinos.solo-jugadores", Map.of());
            return;
        }
        NpcDefinition definicion = definicion(emisor, args);
        if (definicion == null) {
            return;
        }
        servicio.interactuar(jugador, definicion.id(), null);
    }

    private void trueques(CommandSender emisor, String[] args) {
        if (!(emisor instanceof Player jugador)) {
            avisar(emisor, "vecinos.solo-jugadores", Map.of());
            return;
        }
        NpcDefinition definicion = definicion(emisor, args);
        if (definicion == null) {
            return;
        }
        if (definicion.trueques().isEmpty()) {
            avisar(emisor, "vecinos.trueque-vacio", Map.of("npc", definicion.nombre()));
            return;
        }
        servicio.abrirTrueques(jugador, definicion.id());
    }

    private void reputacion(CommandSender emisor, String[] args) {
        if (!(emisor instanceof Player jugador)) {
            avisar(emisor, "vecinos.solo-jugadores", Map.of());
            return;
        }
        NpcDefinition definicion = definicion(emisor, args);
        if (definicion == null) {
            return;
        }
        servicio.reputacionDe(jugador.getUniqueId(), definicion.id(), valor ->
                avisar(jugador, "vecinos.reputacion", Map.of(
                        "npc", definicion.nombre(),
                        "valor", String.valueOf(valor),
                        "nivel", Textos.msg(ctx, "vecinos.nivel-" + servicio.nivelReputacion(valor)))));
    }

    private void colocar(CommandSender emisor, String[] args) {
        if (!exigirAdmin(emisor)) {
            return;
        }
        if (!(emisor instanceof Player jugador)) {
            avisar(emisor, "vecinos.solo-jugadores", Map.of());
            return;
        }
        NpcDefinition definicion = definicion(emisor, args);
        if (definicion == null) {
            return;
        }
        Location lugar = jugador.getLocation();
        if (!servicio.colocar(definicion.id(), lugar)) {
            avisar(emisor, "vecinos.colocar-error", Map.of("id", definicion.id()));
            return;
        }
        avisar(emisor, "vecinos.colocado", Map.of(
                "id", definicion.id(),
                "mundo", lugar.getWorld() == null ? "?" : lugar.getWorld().getName(),
                "x", String.valueOf(lugar.getBlockX()),
                "y", String.valueOf(lugar.getBlockY()),
                "z", String.valueOf(lugar.getBlockZ())));
    }

    private void quitar(CommandSender emisor, String[] args) {
        if (!exigirAdmin(emisor)) {
            return;
        }
        NpcDefinition definicion = definicion(emisor, args);
        if (definicion == null) {
            return;
        }
        if (!servicio.quitar(definicion.id())) {
            avisar(emisor, "vecinos.quitar-error", Map.of("id", definicion.id()));
            return;
        }
        avisar(emisor, "vecinos.quitado", Map.of("id", definicion.id()));
    }

    private void recargar(CommandSender emisor) {
        if (!exigirAdmin(emisor)) {
            return;
        }
        int definidos = servicio.recargar();
        avisar(emisor, "vecinos.recargado", Map.of(
                "definiciones", String.valueOf(definidos),
                "ubicados", String.valueOf(servicio.ubicados())));
    }

    // ------------------------------------------------------------------ helpers

    private NpcDefinition definicion(CommandSender emisor, String[] args) {
        if (args.length < 2) {
            avisar(emisor, "vecinos.falta-id", Map.of());
            return null;
        }
        NpcDefinition definicion = servicio.definicion(args[1]);
        if (definicion == null) {
            avisar(emisor, "vecinos.desconocido", Map.of("id", args[1]));
        }
        return definicion;
    }

    private boolean exigirAdmin(CommandSender emisor) {
        if (emisor.hasPermission(PERMISO_ADMIN)) {
            return true;
        }
        avisar(emisor, "generic.no-permission", Map.of());
        return false;
    }

    private void avisar(CommandSender emisor, String clave, Map<String, String> reemplazos) {
        String texto = reemplazos.isEmpty()
                ? Textos.msg(ctx, clave)
                : Textos.msg(ctx, clave, reemplazos);
        Textos.enviar(emisor, Textos.prefijo(ctx) + texto);
    }

    @Override
    public List<String> onTabComplete(CommandSender emisor, Command comando, String etiqueta,
                                      String[] args) {
        if (args.length == 1) {
            List<String> opciones = new ArrayList<>(
                    List.of("lista", "info", "hablar", "trueques", "reputacion"));
            if (emisor.hasPermission(PERMISO_ADMIN)) {
                opciones.add("colocar");
                opciones.add("quitar");
                opciones.add("recargar");
            }
            return filtrar(opciones, args[0]);
        }
        if (args.length == 2) {
            List<String> ids = new ArrayList<>();
            for (NpcDefinition definicion : servicio.definiciones()) {
                ids.add(definicion.id());
            }
            return filtrar(ids, args[1]);
        }
        return List.of();
    }

    private static List<String> filtrar(List<String> opciones, String prefijo) {
        String inicio = prefijo.toLowerCase(Locale.ROOT);
        List<String> resultado = new ArrayList<>();
        for (String opcion : opciones) {
            if (opcion.toLowerCase(Locale.ROOT).startsWith(inicio)) {
                resultado.add(opcion);
            }
        }
        return resultado;
    }
}

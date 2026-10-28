package ar.com.maydana.mushokucore.command;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.model.DueñoPuesto;
import ar.com.maydana.mushokucore.module.ranks.IPotenciasService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * {@code /potencias} — spec §17 L800/L816-817. Dueño: agente B (plan §2, §6.3).
 *
 * <ul>
 *   <li>{@code /potencias} — lista las Siete Potencias (permiso {@code mushoku.potencias.view}).</li>
 *   <li>{@code /potencias admin asignar <1-7> <jugador> <motivo>} — fuerza una asignación
 *       (permiso {@code mushoku.admin.potencias}).</li>
 *   <li>{@code /potencias admin liberar <1-7> <motivo>} — libera un puesto.</li>
 * </ul>
 *
 * <p>Funciona desde consola (actor {@code null}) y con autocompletado (§17 L829-836). Toda
 * escritura la audita {@code PotenciasService}; este comando no revela stacktraces. Consume
 * {@link IPotenciasService} <b>por el contexto</b>: si el módulo ranks está apagado el
 * servicio es {@code null} y el comando degrada (fallo seguro §2.5). No lee SQL.</p>
 *
 * <p>[PROPUESTA B — claves messages.yml {@code potencias.admin-usage} y
 * {@code potencias.puesto-invalido} pendientes para Fase 2; hoy se usan literales de respaldo.]</p>
 */
public final class PotenciasCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISO_VER = "mushoku.potencias.view";
    private static final String PERMISO_ADMIN = "mushoku.admin.potencias";

    private final PluginContext ctx;

    public PotenciasCommand(PluginContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public boolean onCommand(CommandSender emisor, Command comando, String etiqueta, String[] args) {
        if (args.length > 0 && "admin".equalsIgnoreCase(args[0])) {
            if (!emisor.hasPermission(PERMISO_ADMIN)) {
                enviar(emisor, mensaje("generic.no-permission"));
                return true;
            }
            return admin(emisor, args);
        }
        if (!emisor.hasPermission(PERMISO_VER)) {
            enviar(emisor, mensaje("generic.no-permission"));
            return true;
        }
        listar(emisor);
        return true;
    }

    // ------------------------------------------------------------------
    // /potencias  (listado)
    // ------------------------------------------------------------------

    private void listar(CommandSender emisor) {
        enviar(emisor, mensaje("potencias.list-header"));
        IPotenciasService servicio = servicio();
        if (servicio == null) {
            // Módulo ranks apagado/ausente: no se inventan títulos ni dueños (fallo seguro §2.5).
            enviar(emisor, "&cLas Siete Potencias no estan disponibles ahora mismo.");
            return;
        }
        for (int puesto = 1; puesto <= 7; puesto++) {
            String titulo = servicio.tituloDe(puesto);
            Optional<DueñoPuesto> dueno = servicio.propietario(puesto);
            String propietario = "No conquistado"; // messages.yml comenta este literal
            if (dueno.isPresent() && dueno.get().dueno() != null) {
                String nombre = dueno.get().nombreDueno();
                if (nombre == null || nombre.isBlank()) {
                    // Filas viejas guardadas con owner_name NULL: se resuelve acá, en el hilo
                    // principal, antes de caer al UUID crudo.
                    nombre = Bukkit.getOfflinePlayer(dueno.get().dueno()).getName();
                }
                propietario = (nombre == null || nombre.isBlank())
                        ? dueno.get().dueno().toString()
                        : nombre;
            }
            enviar(emisor, mensaje("potencias.list-entry", Map.of(
                    "puesto", String.valueOf(puesto),
                    "titulo", titulo == null ? "" : titulo,
                    "propietario", propietario)));
        }
        enviar(emisor, mensaje("potencias.list-footer"));
    }

    // ------------------------------------------------------------------
    // /potencias admin …
    // ------------------------------------------------------------------

    private boolean admin(CommandSender emisor, String[] args) {
        if (args.length < 3) {
            enviar(emisor, usoAdmin());
            return true;
        }
        String accion = args[1].toLowerCase(Locale.ROOT);
        Integer puesto = puestoValido(args[2]);
        if (puesto == null) {
            enviar(emisor, "&cPuesto invalido: '" + args[2] + "' (se espera 1-7). " + usoAdmin());
            return true;
        }
        IPotenciasService servicio = servicio();
        if (servicio == null) {
            enviar(emisor, "&cEl modulo de Potencias no esta disponible; no se aplico el cambio.");
            return true;
        }
        UUID actor = emisor instanceof Player jugador ? jugador.getUniqueId() : null;

        switch (accion) {
            case "asignar" -> {
                if (args.length < 4) {
                    enviar(emisor, usoAdmin());
                    return true;
                }
                OfflinePlayer objetivo = buscarJugador(args[3]);
                if (objetivo == null || objetivo.getUniqueId() == null) {
                    enviar(emisor, mensaje("generic.player-not-found", Map.of("jugador", args[3])));
                    return true;
                }
                String motivo = motivoDe(args, 4);
                boolean ok = servicio.asignar(puesto, objetivo.getUniqueId(), motivo, actor);
                String nombre = objetivo.getName() == null
                        ? objetivo.getUniqueId().toString() : objetivo.getName();
                if (ok) {
                    enviar(emisor, mensaje("potencias.admin-assigned", Map.of(
                            "puesto", String.valueOf(puesto), "jugador", nombre)));
                } else {
                    enviar(emisor, "&eEl puesto " + puesto + " quedo igual (ya lo tenia ese jugador "
                            + "o la cache no estaba lista): no se aplico el cambio.");
                }
                return true;
            }
            case "liberar" -> {
                String motivo = motivoDe(args, 3);
                boolean ok = servicio.liberar(puesto, motivo, actor);
                if (ok) {
                    enviar(emisor, mensaje("potencias.admin-freed", Map.of("puesto", String.valueOf(puesto))));
                } else {
                    enviar(emisor, "&eEl puesto " + puesto + " ya estaba libre (o la cache no estaba "
                            + "lista): no se aplico el cambio.");
                }
                return true;
            }
            default -> {
                enviar(emisor, usoAdmin());
                return true;
            }
        }
    }

    private String usoAdmin() {
        return "&7Uso: &f/potencias admin asignar <1-7> <jugador> <motivo> "
                + "&7| &f/potencias admin liberar <1-7> <motivo>";
    }

    // ------------------------------------------------------------------
    // Autocompletado (§17 L833)
    // ------------------------------------------------------------------

    @Override
    public List<String> onTabComplete(CommandSender emisor, Command comando, String etiqueta, String[] args) {
        if (args.length == 1) {
            return filtrar(List.of("admin"), args[0]);
        }
        if (args.length == 2 && "admin".equalsIgnoreCase(args[0])) {
            return filtrar(List.of("asignar", "liberar"), args[1]);
        }
        if (args.length == 3 && "admin".equalsIgnoreCase(args[0])) {
            return filtrar(List.of("1", "2", "3", "4", "5", "6", "7"), args[2]);
        }
        if (args.length == 4 && "admin".equalsIgnoreCase(args[0]) && "asignar".equalsIgnoreCase(args[1])) {
            return filtrar(nombresEnLinea(), args[3]);
        }
        return List.of();
    }

    private static List<String> nombresEnLinea() {
        List<String> nombres = new ArrayList<>();
        for (Player jugador : Bukkit.getOnlinePlayers()) { // el tab-complete corre en el hilo principal
            nombres.add(jugador.getName());
        }
        return nombres;
    }

    private static List<String> filtrar(List<String> opciones, String prefijo) {
        String buscado = prefijo == null ? "" : prefijo.toLowerCase(Locale.ROOT);
        List<String> resultado = new ArrayList<>();
        for (String opcion : opciones) {
            if (opcion.toLowerCase(Locale.ROOT).startsWith(buscado)) {
                resultado.add(opcion);
            }
        }
        return resultado;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private IPotenciasService servicio() {
        return ctx.obtener(IPotenciasService.class); // null si el modulo ranks esta apagado (§2.5)
    }

    /** @return el número 1-7 o {@code null} si no es válido (para no fabricar una asignación). */
    private static Integer puestoValido(String texto) {
        try {
            int puesto = Integer.parseInt(texto.trim());
            return (puesto >= 1 && puesto <= 7) ? puesto : null;
        } catch (NumberFormatException noEsNumero) {
            return null;
        }
    }

    private static OfflinePlayer buscarJugador(String nombre) {
        Player enLinea = Bukkit.getPlayerExact(nombre);
        if (enLinea != null) {
            return enLinea;
        }
        OfflinePlayer fuera = Bukkit.getOfflinePlayer(nombre);
        return fuera.getUniqueId() == null ? null : fuera;
    }

    /** Concatena el motivo; si no hay, no se inventa texto: "(sin motivo)". */
    private static String motivoDe(String[] args, int desde) {
        if (args.length <= desde) {
            return "(sin motivo)";
        }
        StringBuilder motivo = new StringBuilder();
        for (int i = desde; i < args.length; i++) {
            if (i > desde) {
                motivo.append(' ');
            }
            motivo.append(args[i]);
        }
        return motivo.length() == 0 ? "(sin motivo)" : motivo.toString();
    }

    private String mensaje(String clave) {
        try {
            return ctx.messages().texto(clave);
        } catch (IllegalStateException sinProveer) {
            return clave; // fallo seguro §2.5: un texto faltante no tumba el comando
        }
    }

    private String mensaje(String clave, Map<String, String> reemplazos) {
        try {
            return ctx.messages().texto(clave, reemplazos);
        } catch (IllegalStateException sinProveer) {
            return clave;
        }
    }

    private static void enviar(CommandSender destino, String texto) {
        destino.sendMessage(ChatColor.translateAlternateColorCodes('&', texto == null ? "" : texto));
    }
}

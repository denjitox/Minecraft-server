package ar.com.maydana.mushokucore.module.commands;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.model.Disease;
import ar.com.maydana.mushokucore.model.DueñoPuesto;
import ar.com.maydana.mushokucore.model.PlaceholderContext;
import ar.com.maydana.mushokucore.module.PlaceholderFuente;
import ar.com.maydana.mushokucore.module.diseases.IEnfermedadesService;
import ar.com.maydana.mushokucore.module.ranks.IPotenciasService;
import ar.com.maydana.mushokucore.ui.JavaChatUi;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Placeholders de MushokuCore — agente D (spec §18 L844-854, plan §6.1/§7).
 *
 * <p><b>Una instancia por clave.</b> El interface congelado {@link PlaceholderFuente} solo recibe
 * el <i>contexto</i> (jugador), no la clave: {@code valor(PlaceholderContext)}. Para que eso sea
 * resoluble, cada fuente declara en {@link #claves()} una única clave y {@link #valor} devuelve el
 * valor de esa clave. {@link #todas(PluginContext)} devuelve las 9 del §18.</p>
 *
 * <p><b>Cero SQL por render</b> (spec §18 L856, §19 L874): todo sale de servicios de caché
 * ({@code IPotenciasService}, {@code IEnfermedadesService}) y de Permisos de LuckPerms; si el
 * servicio no está disponible, devuelve {@link Optional#empty()} — nunca fabrica un valor (§2.5).</p>
 *
 * <p>[A VERIFICAR §18/D15] {@code dimensions_nether_state}/{@code dimensions_end_state} son
 * constantes de mensaje en MVP (el estado real llega en 1.3); {@code player_allegiance} sale de los
 * permisos de LuckPerms mientras {@code modules.factions=false} (§21 L893).</p>
 */
public final class PlaceholderFuenteMushoku implements PlaceholderFuente {

    /** Las 9 claves del §18 L844-854 (sin el prefijo {@code mushoku_}). */
    public static final Set<String> CLAVES = Set.of(
            "power_1_name",
            "power_1_title",
            "player_power_position",
            "player_power_title",
            "player_allegiance",
            "player_disease",
            "player_valid_kills",
            "dimensions_nether_state",
            "dimensions_end_state");

    private final PluginContext ctx;
    private final String clave;

    private PlaceholderFuenteMushoku(PluginContext ctx, String clave) {
        this.ctx = Objects.requireNonNull(ctx, "ctx");
        this.clave = Objects.requireNonNull(clave, "clave");
    }

    /** Una fuente por cada clave del §18 (requisito del contrato congelado, ver javadoc de clase). */
    public static List<PlaceholderFuente> todas(PluginContext ctx) {
        List<PlaceholderFuente> fuentes = new ArrayList<>(CLAVES.size());
        for (String clave : CLAVES) {
            fuentes.add(new PlaceholderFuenteMushoku(ctx, clave));
        }
        return fuentes;
    }

    @Override
    public Set<String> claves() {
        return Set.of(clave);
    }

    @Override
    public Optional<String> valor(PlaceholderContext pctx) {
        try {
            return resolver(pctx);
        } catch (RuntimeException excepcion) {
            // Un placeholder no puede tumbar a TAB ni al holograma (fallo seguro §2.5).
            return Optional.empty();
        }
    }

    private Optional<String> resolver(PlaceholderContext pctx) {
        switch (clave) {
            case "power_1_name": {
                IPotenciasService potencias = ctx.obtener(IPotenciasService.class);
                if (potencias == null) {
                    return Optional.empty();
                }
                Optional<DueñoPuesto> dueno = potencias.propietario(1);
                if (dueno.isEmpty() || dueno.get().dueno() == null) {
                    // "No conquistado": messages.yml solo lo usa en /potencias; acá va literal.
                    return Optional.of("No conquistado");
                }
                String nombre = dueno.get().nombreDueno();
                return Optional.of(nombre == null || nombre.isBlank()
                        ? dueno.get().dueno().toString() : nombre);
            }
            case "power_1_title": {
                IPotenciasService potencias = ctx.obtener(IPotenciasService.class);
                if (potencias == null) {
                    return Optional.empty();
                }
                String titulo = potencias.tituloDe(1);
                return titulo == null || titulo.isBlank() ? Optional.empty() : Optional.of(titulo);
            }
            case "player_power_position": {
                UUID jugador = pctx.jugador();
                IPotenciasService potencias = ctx.obtener(IPotenciasService.class);
                if (jugador == null || potencias == null) {
                    return Optional.empty();
                }
                return Optional.of(String.valueOf(potencias.posicionDelJugador(jugador)));
            }
            case "player_power_title": {
                UUID jugador = pctx.jugador();
                IPotenciasService potencias = ctx.obtener(IPotenciasService.class);
                if (jugador == null || potencias == null) {
                    return Optional.empty();
                }
                int puesto = potencias.posicionDelJugador(jugador);
                if (puesto <= 0) {
                    return Optional.empty();
                }
                String titulo = potencias.tituloDe(puesto);
                return titulo == null || titulo.isBlank() ? Optional.empty() : Optional.of(titulo);
            }
            case "player_allegiance": {
                UUID jugador = pctx.jugador();
                if (jugador == null) {
                    return Optional.empty();
                }
                String alianza = JavaChatUi.alianzaId(ctx, jugador);
                // "n/d" = LuckPerms no disponible: no se fabrica una alianza (§2.5).
                return "n/d".equals(alianza) ? Optional.empty() : Optional.of(alianza);
            }
            case "player_disease": {
                UUID jugador = pctx.jugador();
                IEnfermedadesService enfermedades = ctx.obtener(IEnfermedadesService.class);
                if (jugador == null || enfermedades == null) {
                    return Optional.empty();
                }
                Optional<Disease> activa = enfermedades.activa(jugador);
                if (activa.isEmpty()) {
                    return Optional.empty();
                }
                Disease enfermedad = activa.get();
                String nombre = enfermedad.displayName();
                return Optional.of(nombre == null || nombre.isBlank() ? enfermedad.id() : nombre);
            }
            case "player_valid_kills": {
                UUID jugador = pctx.jugador();
                IPotenciasService potencias = ctx.obtener(IPotenciasService.class);
                if (jugador == null || potencias == null) {
                    return Optional.empty();
                }
                return Optional.of(String.valueOf(potencias.bajasValidas(jugador)));
            }
            case "dimensions_nether_state":
                return Optional.of(Textos.msg(ctx, "dimensiones.locked-nether"));
            case "dimensions_end_state":
                return Optional.of(Textos.msg(ctx, "dimensiones.locked-end"));
            default:
                return Optional.empty();
        }
    }
}

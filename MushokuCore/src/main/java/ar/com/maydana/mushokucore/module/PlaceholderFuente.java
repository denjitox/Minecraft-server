package ar.com.maydana.mushokucore.module;

import ar.com.maydana.mushokucore.model.PlaceholderContext;
import java.util.Optional;
import java.util.Set;

/**
 * CONTRATO CONGELADO — Fase 0 (agente F0), plan §6.1.
 * <p>Proveedor de placeholders para TAB/hologramas (spec §18). <b>Regla dura:
 * solo caché, cero SQL por render</b> (spec §18 L856, §19 L874) — un placeholder que
 * consulte SQLite en cada actualización rompe el criterio de aceptación §26.</p>
 *
 * <p><b>Desviación del plan anotada (Fase 0):</b> el plan §6.1 escribía
 * {@code valor(PlaceholderAPIAPIContextual ctx)} — tipo inexistente (typo del plan, y meter
 * la API de PlaceholderAPI en el contrato obligaría a esa dependencia en el classpath).
 * Se sustituye por {@link PlaceholderContext} (model/, propio). Si el integrador de Fase 2
 * prefiere el tipo real de PAPI, hay que actualizar el plan §6.1 y avisar (regla 2).</p>
 *
 * <p>Claves sugeridas por el spec §18 L844-854 (los 9 del MVP): {@code power_1_name},
 * {@code power_1_title}, {@code player_power_position}, {@code player_power_title},
 * {@code player_allegiance}, {@code player_disease}, {@code player_valid_kills},
 * {@code dimensions_nether_state}, {@code dimensions_end_state} (prefijo publicado por
 * {@code PlaceholderHook}, de D).</p>
 */
public interface PlaceholderFuente {

    /** Claves absolutas que esta fuente puede resolver (sin el {@code %}). */
    Set<String> claves();

    /**
     * @param ctx contexto del render; {@code ctx.jugador()} puede ser null (TAB global,
     *            holograma sin jugador).
     * @return valor presente si la clave responde; vacío si no aplica ahora.
     */
    Optional<String> valor(PlaceholderContext ctx);
}

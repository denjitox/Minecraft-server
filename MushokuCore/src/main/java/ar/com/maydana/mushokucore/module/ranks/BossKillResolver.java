package ar.com.maydana.mushokucore.module.ranks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolución de la primera muerte válida de un jefe — spec §11 L563-575.
 * Dueño: agente B (plan §6.3). <b>Clase pura</b>, sin Bukkit ni BD (testeable unitariamente).
 *
 * <p>Pasos del spec aplicados aquí (los que son reglas puras): descartar participantes por
 * debajo del mínimo configurable (L568), elegir mayor contribución (L569) y resolver empates
 * por primer instante en alcanzar el daño final empatado (L570). La comprobación de puesto
 * libre (L565), del mundo permitido (L566) y la transacción idempotente (L571) viven en el
 * listener/servicio.</p>
 *
 * <p>[A VERIFICAR — V7] El plan marca como ambiguo el desempate. Este implementa: si dos o más
 * quedan con el mismo daño, gana quien alcanzó ese total <b>primero</b> (menor
 * {@code ultimoGolpeMs}, que es el instante del golpe que completó su contribución final).
 * [A VERIFICAR — umbral] "mínimo configurable" = {@code ranks.minimum-contribution-percent}
 * (§9 L437, default 1.0) interpretado como <b>porcentaje de la mayor contribución</b> de la
 * pelea; el spec no dice de qué base es el porcentaje.</p>
 */
public final class BossKillResolver {

    private BossKillResolver() {
    }

    /**
     * Contribución acumulada de un jugador contra un jefe.
     *
     * @param jugador      UUID del atacante real (ya resuelto proyectil/mascota por el listener).
     * @param danio        daño efectivo acumulado (con tope de overkill, §11 L558).
     * @param ultimoGolpeMs epoch ms del golpe que completó su contribución final.
     */
    public record Contribucion(UUID jugador, double danio, long ultimoGolpeMs) {
    }

    /**
     * Elige el ganador de la pelea.
     *
     * @param aportes                todas las contribuciones registradas contra el jefe.
     * @param minimoPorcentajeDeLaMayor {@code ranks.minimum-contribution-percent} (percentual,
     *                                 p. ej. 1.0 = 1 %). Valores {@code <= 0} desactivan el filtro.
     * @return la contribución ganadora; vacío si no hay ningún aporte válido.
     */
    public static Optional<Contribucion> ganador(List<Contribucion> aportes, double minimoPorcentajeDeLaMayor) {
        if (aportes == null || aportes.isEmpty()) {
            return Optional.empty();
        }
        double maximo = 0.0;
        for (Contribucion c : aportes) {
            if (c.danio() > maximo) {
                maximo = c.danio();
            }
        }
        if (maximo <= 0.0) {
            return Optional.empty();
        }
        double umbral = maximo * (minimoPorcentajeDeLaMayor / 100.0);
        List<Contribucion> validos = new ArrayList<>(aportes.size());
        for (Contribucion c : aportes) {
            if (c.danio() > 0.0 && c.danio() >= umbral) {
                validos.add(c);
            }
        }
        if (validos.isEmpty()) {
            return Optional.empty();
        }
        validos.sort(Comparator
                .comparingDouble(Contribucion::danio).reversed()
                // Empate de daño: gana quien alcanzó ese total primero (§11 L570; [A VERIFICAR V7]).
                .thenComparingLong(Contribucion::ultimoGolpeMs));
        return Optional.of(validos.get(0));
    }
}

package ar.com.maydana.mushokucore.module.diseases;

import java.util.Map;
import java.util.Set;
import org.bukkit.potion.PotionEffectType;

/**
 * Definición inmutable de una enfermedad del catálogo — agente C (enfermedades).
 * Es la lectura de un bloque de {@code diseases.yml} (spec §13 L648-682; plan §4.4).
 * <p>Es <b>interna del módulo</b> (package-private): el contrato congelado hacia afuera es
 * {@code IEnfermedadesService} y el record {@code model/Disease}. Ningún otro paquete la ve.</p>
 *
 * @param id                   clave del YAML (ej. {@code "dryne"})
 * @param displayName          {@code display-name}; si falta, el id
 * @param fuentes              {@code sources} en mayúsculas (ej. {@code HUSK_DAMAGE})
 * @param chanceBasePorcentaje {@code base-chance-percent} (4.0 = 4 %)
 * @param duracionSegundos     {@code duration-seconds}; 0 o menos = sin expiración
 * @param efectos              {@code effects} mapeado a {@link PotionEffectType} (amplificador 0 = nivel I)
 * @param curas                {@code cures} en mayúsculas (hoy solo {@code MILK})
 * @param inmunidadSegundos    {@code immunity-after-cure-seconds} (0 = sin inmunidad)
 */
record DiseaseDefinition(String id, String displayName, Set<String> fuentes,
                         double chanceBasePorcentaje, int duracionSegundos,
                         Map<PotionEffectType, Integer> efectos, Set<String> curas,
                         int inmunidadSegundos) {
}

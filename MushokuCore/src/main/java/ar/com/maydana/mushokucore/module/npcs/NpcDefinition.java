package ar.com.maydana.mushokucore.module.npcs;

import java.util.List;
import org.bukkit.Material;

/**
 * Vecino definido en {@code npcs.yml}. El subconjunto MCA que se implementa es
 * dialogo, regalos, reputacion y trueques; matrimonio, hijos y genetica no son
 * viables sin el mod y quedan fuera.
 */
public record NpcDefinition(
        String id,
        String nombre,
        String titulo,
        String skinValue,
        String skinSignature,
        List<String> dialogoBaja,
        List<String> dialogoNeutral,
        List<String> dialogoAlta,
        List<Regalo> regalos,
        List<Trueque> trueques) {

    public NpcDefinition {
        dialogoBaja = dialogoBaja == null ? List.of() : List.copyOf(dialogoBaja);
        dialogoNeutral = dialogoNeutral == null ? List.of() : List.copyOf(dialogoNeutral);
        dialogoAlta = dialogoAlta == null ? List.of() : List.copyOf(dialogoAlta);
        regalos = regalos == null ? List.of() : List.copyOf(regalos);
        trueques = trueques == null ? List.of() : List.copyOf(trueques);
    }

    /** Regalo compatible con el item de la mano, o null si no hay ninguno. */
    public Regalo regaloPara(Material material, int cantidad) {
        for (Regalo regalo : regalos) {
            if (regalo.material() == material && cantidad >= regalo.cantidad()) {
                return regalo;
            }
        }
        return null;
    }

    public boolean tieneSkin() {
        return skinValue != null && !skinValue.isBlank();
    }

    /** Un regalo consume {@code cantidad} del material y suma {@code reputacion}. */
    public record Regalo(String id, Material material, int cantidad, int reputacion,
                         int cooldownHoras) {
    }

    /** Un trueque entrega {@code entrega} y cobra {@code costo}. */
    public record Trueque(String id, Material entrega, int entregaCantidad,
                          Material costo, int costoCantidad) {
    }
}

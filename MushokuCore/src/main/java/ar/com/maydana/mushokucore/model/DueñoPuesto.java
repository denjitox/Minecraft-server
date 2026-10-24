package ar.com.maydana.mushokucore.model;

import java.util.UUID;

/**
 * Dueño actual de un puesto — modelo inmutable (plan §6.2, F0). Sin lógica.
 *
 * <p>[A VERIFICAR — §6.1/§6.2] El plan no fija si un puesto libre se representa como
 * {@code dueno == null} en este record o como {@code Optional} vacío en
 * {@code IPotenciasService.propietario()}. Este record admite lo primero; la semántica
 * exacta la fija <b>B</b> al implementar y debe quedar documentada en el plan si difiere.</p>
 *
 * @param posicion     número del puesto (1–7)
 * @param titulo       título del puesto (§11 L541-548)
 * @param dueno        UUID del propietario o {@code null} si está libre
 * @param nombreDueno  solo presentación; la identidad es el UUID (spec §2.4 L75-77). Puede ser null.
 */
public record DueñoPuesto(int posicion, String titulo, UUID dueno, String nombreDueno) {
}

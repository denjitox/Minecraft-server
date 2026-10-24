package ar.com.maydana.mushokucore.model;

import java.time.Instant;

/**
 * Enfermedad activa de un jugador — modelo inmutable (plan §6.2, F0). Sin lógica.
 * Los datos provienen de {@code diseases.yml} (catálogo, spec §13 L648-682) y de la
 * tabla {@code diseases} (spec §10 L507-514).
 *
 * <p>[A VERIFICAR — §6.2] Campos mínimos propuestos por F0 para que
 * {@code IEnfermedadesService.activa()} y {@code /mtestado} compilen. Si C necesita más
 * campos (efectos, duración total…), se amplía el record <b>actualizando el plan</b>
 * antes (regla 2, plan §6): un record es contrato congelado.</p>
 *
 * @param id          identificador de diseases.yml (ej. {@code "dryne"})
 * @param displayName {@code display-name} de diseases.yml (ej. "Fiebre de Dryne")
 * @param fuente      {@code diseases.source} (quién la contagió) o null si no consta
 * @param expira      instante de expiración o {@code null} si no expira (expires_at vacío)
 */
public record Disease(String id, String displayName, String fuente, Instant expira) {
}

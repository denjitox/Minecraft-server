package ar.com.maydana.mushokucore.ui;

import ar.com.maydana.mushokucore.model.Allegiance;
import org.bukkit.entity.Player;

/**
 * Interface TEXTUAL del spec §16 L777-783 — CONGELADA (plan §6.1, texto literal).
 *
 * <p>Implementaciones previstas (§16 L785-790):</p>
 * <ul>
 *   <li>{@code JavaChatUi} — la crea F0 según la tabla §6.3; en Fase 0 <b>no existe todavía</b>
 *       (el encargo de Fase 0 fue interfaces + resources): quedó <b>pendiente</b>.
 *       [A MEDIAS] quién la completa: dueño F0 según §6.3, pero el candidato natural es
 *       D (ui-comandos-import) o I (Fase 2) — <b>resolución a confirmar por el integrador</b>
 *       para no pisar la tabla de dueños.</li>
 *   <li>{@code BedrockFormUi} — <b>NO se crea en MVP</b>: formularios son 1.3 (§3 L132, §16 L789).</li>
 * </ul>
 *
 * <p>Si Floodgate no está cargado o el formulario falla, se usa automáticamente texto y
 * comandos (§16 L792); la lógica de negocio nunca vive dentro del formulario.</p>
 */
public interface PlayerUi {

    /** Lista de las Siete Potencias (usa {@code messages.yml}: claves potencias.*). */
    void showPowers(Player player);

    /** Estado del personaje: alianza, enfermedades, puesto (claves mtestado.*). */
    void showStatus(Player player);

    /**
     * Confirmación de elección de alianza. <b>Sin invocar en MVP</b>: las alianzas llegan
     * en 1.1 (plan §6.1, nota final sobre esta firma).
     */
    void confirmAllegiance(Player player, Allegiance allegiance);
}

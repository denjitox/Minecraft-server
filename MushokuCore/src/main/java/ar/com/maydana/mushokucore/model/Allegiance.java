package ar.com.maydana.mushokucore.model;

/**
 * Alianzas del mundo — modelo inmutable (plan §6.2, F0).
 * <p>Definición [PROPUESTA F0]: el spec no define la clase, pero la referencia en
 * {@code PlayerUi.confirmAllegiance} (§16 L781). Los nombres de nación fija en config son
 * {@code Orsted_Corp} / {@code Apostoles_Humanos} (§9 L430-432).</p>
 * <p>En MVP el enum existe solo para satisfacer la firma de PlayerUi:
 * {@code confirmAllegiance} queda <b>sin invocar hasta 1.1</b> (plan §6.1, nota final).
 * El dueño de la lógica de alianzas en 1.1 es el módulo factions (nación Orsted/Apóstoles
 * vive en Skript hasta ese día, §21 L893).</p>
 */
public enum Allegiance {
    ORSTED,
    APOSTOLES,
    NEUTRAL
}

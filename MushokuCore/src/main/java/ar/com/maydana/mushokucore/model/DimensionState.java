package ar.com.maydana.mushokucore.model;

/**
 * Estado de una dimensión — modelo inmutable (plan §6.2, F0; estados del spec §15 L753).
 * <p><b>Inerte en MVP:</b> el módulo dimensions es 1.3 (spec §3 L126-132; plan §1.2).
 * Se declara desde Fase 0 para que los placeholders {@code dimensions_nether_state} /
 * {@code dimensions_end_state} (§18 L852-853) tengan tipo al que apuntar.</p>
 * <p>Nota de decisión del usuario (2026-10-17): el End está <b>permitido</b> en bukkit.yml
 * ({@code settings.allow-end: true}); el bloqueo real es el del módulo (LOCKED) y depende de
 * los sellos — no confundir con la config del servidor.</p>
 */
public enum DimensionState {
    LOCKED,
    PREPARED,
    OPEN
}

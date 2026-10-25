package ar.com.maydana.mushokucore.config;

import java.util.Set;

/**
 * CONTRATO CONGELADO — Fase 0 (agente F0), plan §6.1:
 * "holder inmutable con getters de TODO §9" (ranks.*, diseases.*, factions.* aunque el
 * módulo esté apagado, world.*, dimensions.*, bedrock.*, modules.*).
 *
 * <p><b>La implementación es de A</b>: {@code ConfigService.java} carga/valida config.yml y
 * migra schema 1→2; A construye la clase concreta que implementa estos getters (p. ej.
 * {@code PluginConfigImpl}). Este interface no se edita sin actualizar el plan (plan §6, regla 2);
 * si A/B/C/D necesitan un getter nuevo, se agrega aquí primero y se avisa al integrador.</p>
 *
 * <p>Cada bloque indica la clave YAML y su sección del spec §9. Los valores marcados como
 * <b>[DECISIÓN Dn pendiente]</b> no están decididos: el default del jar es la propuesta del plan
 * §4.2 y el usuario puede cambiarlo en config.yml (plan §9).</p>
 */
public interface PluginConfig {

    /** {@code schema} — spec §9 L392. 1 o ausente ⇒ fallo seguro §2.5: sin módulos de escritura. */
    int schema();

    /** {@code enabled} — spec §9 L393. */
    boolean enabled();

    // ---- storage (spec §9 L395-398) ----

    /** {@code storage.type} — hoy solo "sqlite". */
    String storageType();

    /** {@code storage.file} — nombre del archivo de la BD. */
    String storageFile();

    /** {@code storage.busy-timeout-ms} — busy_timeout de SQLite. */
    int busyTimeoutMs();

    // ---- modules (spec §9 L400-406) ----
    // DECISION D4 (2026-10-18): factions/relics/dimensions quedan false en el MVP (1.1/1.2/1.3);
    // el spec §9 los muestra todos true (V9), pero el plan §4.2 fija false y manda esa decision.
    // relics=false esta ademas respaldado por spec §21 L910 (mientras el datapack entrega loot).

    /** {@code modules.ranks} — Siete Potencias (módulo de B). */
    boolean moduloRanks();

    /** {@code modules.factions} — 1.1; inerte en MVP. DECISION D4: false. */
    boolean moduloFactions();

    /** {@code modules.diseases} — enfermedades (módulo de C). */
    boolean moduloDiseases();

    /** {@code modules.relics} — 1.2; mientras sea false el datapack sigue entregando loot (§21 L910). DECISION D4: false. */
    boolean moduloRelics();

    /** {@code modules.dimensions} — 1.3; inerte en MVP. DECISION D4: false. */
    boolean moduloDimensions();

    /** {@code modules.bedrock-bridge} — detección/desarrollo Java+Bedrock. */
    boolean moduloBedrockBridge();

    // ---- world (spec §9 L408-414) ----

    /** {@code world.name} — DECISIÓN DEL USUARIO: {@code mushoku_world}. */
    String mundoNombre();

    /** {@code world.border-diameter} — DECISIÓN DEL USUARIO 2026-10-16: 21800. */
    int bordeDiametro();

    /** {@code world.center-x} */
    int centroX();

    /** {@code world.center-z} */
    int centroZ();

    /** {@code world.min-y} — -64 (verificado datapack sonda). */
    int minY();

    /**
     * {@code world.max-y} — 511, DECISIÓN DEL USUARIO 2026-10-17 (verificado en disco).
     * El spec §9 L414 dice 320 y §9 L445 advierte contra 511 (ambigüedad V1): <b>manda el usuario</b>.
     */
    int maxY();

    // ---- dimensions (spec §9 L416-420) — inerte en MVP (1.3) ----

    /** {@code dimensions.nether-locked-at-launch} */
    boolean netherSelladoAlArranque();

    /** {@code dimensions.end-locked-at-launch}. Ojo: el End está permitido en bukkit.yml ({@code settings.allow-end: true}, decisión 2026-10-17); esto es el estado del módulo. */
    boolean endSelladoAlArranque();

    /** {@code dimensions.minimum-unlock-days} */
    int diasDesbloqueoMinimo();

    /** {@code dimensions.allow-admin-bypass} */
    boolean bypassAdminPermitido();

    // ---- factions (spec §9 L422-432) — inerte en MVP (1.1); valores ya decididos por el usuario ----

    /** {@code factions.min-members} — 3 */
    int miembrosMinimos();

    /** {@code factions.max-members} — 8 */
    int miembrosMaximos();

    /** {@code factions.claims-base} — 8 */
    int claimsBase();

    /** {@code factions.claims-per-member} — 5 */
    int claimsPorMiembro();

    /** {@code factions.claims-max} — 48 */
    int claimsMaximos();

    /** {@code factions.allegiance-lock-days} — 14 */
    int diasBloqueoAlianza();

    /** {@code factions.deserter-cooldown-days} — 7 */
    int diasCooldownDesertor();

    /** {@code factions.fixed-nations.orsted} */
    String nacionOrsted();

    /** {@code factions.fixed-nations.apostles} */
    String nacionApostoles();

    // ---- ranks (spec §9 L434-438 + claves nuevas del plan §4.2) ----

    /** {@code ranks.pvp-same-victim-cooldown-hours} — 12 (§9 L435; = Skript actual). */
    int pvpCooldownMismaVictimaHoras();

    /** {@code ranks.boss-contribution-expiry-minutes} — 60 (§9 L436). */
    int contribucionJefeExpiraMinutos();

    /** {@code ranks.minimum-contribution-percent} — 1.0 (§9 L437). */
    double contribucionMinimaPorcentaje();

    /** {@code ranks.announce-global} — anuncio global de nueva potencia (§9 L438). */
    boolean anuncioGlobal();

    /**
     * {@code ranks.min-victim-online-seconds} — DECISION D6 (2026-10-18):
     * umbral de "víctima conectada demasiado poco" del anti-farmeo (§11 L585 no da número).
     * Valor fijado: 60.
     */
    int victimaMinimaConectadaSegundos();

    /** {@code ranks.excluded-worlds} — DECISION D7 (2026-10-18) = {@code []}: sin mundos excluidos (§11 L584). */
    Set<String> mundosExcluidosPvp();

    /** {@code ranks.valid-boss-worlds} — DECISION D7 (2026-10-18) = {@code [world]}: mundos válidos para jefes (§11 L565). */
    Set<String> mundosValidosJefes();

    /** {@code ranks.boss-buffer-flush-seconds} — [PROPUESTA F0] flush del buffer de daño (§19 L863). */
    int bufferFlushSegundos();

    /** {@code ranks.reconcile-minutes} — [PROPUESTA F0] recálculo periódico del puesto 5 (§11 L588). */
    int reconciliacionMinutos();

    // ---- diseases (claves [PROPUESTA F0] — §13 no trae sección de config) ----

    /** {@code diseases.tick-check-ticks} — tarea liviana de expiración (§13 L693). */
    int frecuenciaChequeoTicks();

    /** {@code diseases.orsted-chance-multiplier} — 0.5, reducción de contagio de Orsted (§12 L620). */
    double multiplicadorOrsted();

    /** {@code diseases.orsted-permission} — [DECISIÓN D11 PENDIENTA] nombre del permiso; default el del Skript: {@code mt.alianza.orsted}. */
    String permisoOrsted();

    // ---- bedrock (spec §9 L440-442) ----

    /** {@code bedrock.enable-forms} — false en MVP: los formularios son 1.3 (§3 L132); §9 L441 dice true (ambigüedad V10, prevalece el alcance). */
    boolean formulariosHabilitados();

    /** {@code bedrock.always-provide-command-fallback} — siempre respaldo por comando/texto (§9 L442, §16 L792). */
    boolean fallbackComandoSiempre();

    // ---- audit (clave nueva, DECISION D17-D20 2026-10-18) ----

    /**
     * {@code audit.retention-days} — DECISION D17-D20 (2026-10-18) = 7: días que se conservan las
     * filas de {@code audit_log} antes de archivarlas a CSV y borrarlas. 0 o negativo = desactivado.
     */
    int auditRetencionDias();

    // ---- regional-difficulty (escalado de vida/daño por distancia) ----

    /** {@code modules.regional-difficulty} — módulo de escalado regional (nuevo módulo). */
    boolean moduloRegionalDifficulty();

    /** {@code regional-difficulty.enabled} — activación global del escalado. */
    boolean regionalDifficultyHabilitado();

    /** {@code regional-difficulty.center-x} — centro X de referencia (spawn). */
    int regionalDifficultyCentroX();

    /** {@code regional-difficulty.center-z} — centro Z de referencia (spawn). */
    int regionalDifficultyCentroZ();

    /**
     * {@code regional-difficulty.zones} — lista de zonas Overworld con radio mínimo/máximo y multiplicadores.
     * Cada zona: name, min-radius (opcional), max-radius, health-multiplier, damage-multiplier.
     */
    java.util.List<RegionalDifficultyZone> regionalDifficultyZonas();

    /**
     * {@code regional-difficulty.dimensions} — multiplicadores fijos por dimensión (Nether/End).
     * Clave = nombre de dimensión (the_nether, the_end), valor = mapa con health-multiplier, damage-multiplier.
     */
    java.util.Map<String, RegionalDifficultyDimension> regionalDifficultyDimensiones();

    /** {@code regional-difficulty.excluded-entities} — entidades excluidas del escalado (nombres vanilla). */
    java.util.Set<String> regionalDifficultyExcluidas();

    /** {@code regional-difficulty.trial-spawner-limit} — si limitar el escalado en Trial Spawners. */
    boolean regionalDifficultyTrialSpawnerLimite();

    /** {@code regional-difficulty.trial-spawner-max-health-multiplier} — tope de vida para Trial Spawners. */
    double regionalDifficultyTrialSpawnerMaxHealthMultiplier();

    /** {@code regional-difficulty.interpolate} — transición suave entre zonas (default true). */
    default boolean regionalDifficultyInterpolar() {
        return true;
    }

    /** {@code regional-difficulty.blend-distance} — bloques de transición entre zonas (default 200). */
    default int regionalDifficultyBlendDistancia() {
        return 200;
    }

    /** Zona de dificultad regional (Overworld). */
    interface RegionalDifficultyZone {
        String name();
        default int minRadius() {
            return 0;
        }
        int maxRadius();
        double healthMultiplier();
        double damageMultiplier();
    }

    /** Multiplicadores por dimensión (Nether/End). */
    interface RegionalDifficultyDimension {
        double healthMultiplier();
        double damageMultiplier();
    }
}

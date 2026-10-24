-- V001__initial.sql — migracion inicial. Fase 0 (agente F0), plan §3.
-- Las 10 tablas del spec §10 L453-533 VERBATIM (incluye schema_version), mas la propuesta
-- luckperms_outbox (exigida por §11 L592, ver ambiguedad V8) y los indices [PROPUESTA plan §3].
-- NUNCA editar una migracion ya publicada (spec §10 L535): se agrega V002, V003...
-- Aplicacion (MigrationRunner): en orden, registro en schema_version, copia de BD a backups/
-- ANTES de aplicar (spec §20 L889). Timestamps: texto ISO-8601 UTC [PROPUESTA; V17].
-- Ejecutor: agente A (aceptacion de Fase 0: aplicar sobre un .db temporal sin error —
-- todavia NO ejecutado; sqlite-jdbc no esta en el classpath offline de build.ps1).

-- ===== spec §10 L453-533, textual =====
CREATE TABLE schema_version (
    version INTEGER PRIMARY KEY,
    applied_at TEXT NOT NULL
);

CREATE TABLE players (
    uuid TEXT PRIMARY KEY,
    last_name TEXT NOT NULL,
    first_join_at TEXT NOT NULL,
    last_seen_at TEXT NOT NULL,
    bedrock INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE world_powers (
    position INTEGER PRIMARY KEY CHECK (position BETWEEN 1 AND 7),
    title TEXT NOT NULL,
    owner_uuid TEXT,
    owner_name TEXT,
    acquired_at TEXT,
    reason TEXT,
    revision INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE boss_damage (
    boss_uuid TEXT NOT NULL,
    boss_type TEXT NOT NULL,
    player_uuid TEXT NOT NULL,
    damage REAL NOT NULL,
    last_hit_at TEXT NOT NULL,
    PRIMARY KEY (boss_uuid, player_uuid)
);

CREATE TABLE pvp_score (
    player_uuid TEXT PRIMARY KEY,
    valid_kills INTEGER NOT NULL DEFAULT 0,
    updated_at TEXT NOT NULL
);

CREATE TABLE pvp_pair_cooldown (
    attacker_uuid TEXT NOT NULL,
    victim_uuid TEXT NOT NULL,
    last_valid_kill_at TEXT NOT NULL,
    PRIMARY KEY (attacker_uuid, victim_uuid)
);

CREATE TABLE allegiances (
    player_uuid TEXT PRIMARY KEY,
    allegiance TEXT NOT NULL,
    joined_at TEXT NOT NULL,
    locked_until TEXT,
    deserter_until TEXT
);

CREATE TABLE diseases (
    player_uuid TEXT NOT NULL,
    disease_id TEXT NOT NULL,
    contracted_at TEXT NOT NULL,
    expires_at TEXT,
    source TEXT,
    PRIMARY KEY (player_uuid, disease_id)
);

CREATE TABLE relic_instances (
    relic_uuid TEXT PRIMARY KEY,
    relic_id TEXT NOT NULL,
    created_at TEXT NOT NULL,
    original_owner_uuid TEXT,
    source TEXT NOT NULL,
    status TEXT NOT NULL
);

CREATE TABLE audit_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    occurred_at TEXT NOT NULL,
    actor_uuid TEXT,
    action TEXT NOT NULL,
    target TEXT,
    details TEXT
);

-- ===== [PROPUESTA plan §3] fuera del §10, pero exigido por §11 L592 (ambiguedad V8) =====
CREATE TABLE luckperms_outbox (            -- operaciones LP pendientes de reintento
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    created_at TEXT NOT NULL,
    player_uuid TEXT NOT NULL,
    group_add TEXT,                        -- potenciaN a conceder
    group_remove TEXT,                     -- potenciaN a retirar
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    done INTEGER NOT NULL DEFAULT 0
);

-- ===== [PROPUESTA plan §3] indices (el spec §10 no los lista; V18) =====
CREATE INDEX idx_boss_damage_player  ON boss_damage(player_uuid);
CREATE INDEX idx_boss_damage_boss    ON boss_damage(boss_uuid);
CREATE INDEX idx_diseases_expires    ON diseases(expires_at);
CREATE INDEX idx_audit_occurred      ON audit_log(occurred_at);
CREATE INDEX idx_pvp_pair_lastkill   ON pvp_pair_cooldown(last_valid_kill_at);

-- Notas plan §3:
--  * allegiances y relic_instances se crean aunque sus modulos sean 1.1/1.2: el V001 no se
--    rompe al activarlos (§10 las exige como tablas minimas).
--  * PRAGMAs (journal_mode=WAL, busy_timeout desde storage.busy-timeout-ms, foreign_keys=ON)
--    NO van aqui: los aplica DatabaseManager al abrir [PROPUESTA plan §3, V18] — agente A.

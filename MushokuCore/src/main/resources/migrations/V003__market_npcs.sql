-- V003__market_npcs.sql - economia por diamantes y vecinos (NPC).
-- El mercado NO usa Vault ni saldo virtual: cobra diamantes fisicos del inventario.
-- npc_reputation guarda el valor por jugador y vecino; npc_gifts, el ultimo regalo
-- entregado para aplicar el cooldown sin depender de la memoria del servidor.

CREATE TABLE IF NOT EXISTS market_purchases (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_uuid TEXT NOT NULL,
    item_id TEXT NOT NULL,
    amount INTEGER NOT NULL,
    price_diamonds INTEGER NOT NULL,
    purchased_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_market_purchases_player
    ON market_purchases (player_uuid, purchased_at);

CREATE TABLE IF NOT EXISTS npc_reputation (
    player_uuid TEXT NOT NULL,
    npc_id TEXT NOT NULL,
    value INTEGER NOT NULL DEFAULT 0,
    updated_at TEXT NOT NULL,
    PRIMARY KEY (player_uuid, npc_id)
);

CREATE TABLE IF NOT EXISTS npc_gifts (
    player_uuid TEXT NOT NULL,
    npc_id TEXT NOT NULL,
    gift_id TEXT NOT NULL,
    last_given_at TEXT NOT NULL,
    PRIMARY KEY (player_uuid, npc_id, gift_id)
);

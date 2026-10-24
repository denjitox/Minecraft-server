CREATE TABLE town_allegiances (
    town_uuid TEXT PRIMARY KEY,
    town_name TEXT NOT NULL,
    allegiance TEXT NOT NULL CHECK (allegiance IN ('ORSTED', 'APOSTOLES', 'NEUTRAL')),
    joined_at TEXT NOT NULL,
    locked_until TEXT,
    deserter_until TEXT,
    updated_at TEXT NOT NULL
);

CREATE INDEX idx_town_allegiances_allegiance ON town_allegiances(allegiance);

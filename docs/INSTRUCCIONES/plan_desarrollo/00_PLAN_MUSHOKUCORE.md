﻿# 00 — Plan de desarrollo MushokuCore 1.0 (MVP)

> Plan operativo para ejecutarse con agentes paralelos. Generado 2026-10-17.
> **Regla rectora:** nada de este plan es válido si no se verifica en disco antes de usarlo.
> Lo que dice el spec se cita con §N y línea del `Instructivo_Diseno_MushokuCore.md` (1091 líneas, leído completo este turno).
> Todo lo que es mío (diseÑo, no del spec) está marcado **[PROPUESTA]**.

---

## 0. Fuentes verificadas en este turno (2026-10-17)

| Fuente | Verificación |
|---|---|
| `INSTRUCCIONES\Instructivo_Diseno_MushokuCore.md` | Leído completo (líneas 1–1091) |
| `INSTRUCCIONES\Requerimientos_Mundo_Mushoku_Tensei.md` | Leído completo (el archivo tiene **244 líneas**, no 182 como decía el encargo) |
| `MT_Server_Base_26.2\plugins\MushokuCore\config.yml` | 34 líneas, `schema: 1`, `enabled: false`, `max_y: 511`. Carpeta = contrato, **no hay JAR** (su README lo dice explícitamente) |
| `MT_Server_Base_26.2\plugins\MushokuCore\README.md` | 15 líneas: "Esta carpeta no contiene un JAR" |
| `MT_Server_Base_26.2\datapacks\mt_core\` | **17 archivos**: 13 `.mcfunction` (4 raíz + 9 en `relics/`), 2 advancements, 1 tag `load.json`, `pack.mcmeta` (`min_format [107,1]`, `max_format 107`). Copias en `servidor\datapacks\mt_core` y `servidor\world\datapacks\mt_core` también con 17 archivos |
| Skript | `potencias.sk` (4057 B, 111 líneas) y `enfermedades.sk` (3073 B, 76 líneas). Las copias de `MT_Server_Base_26.2` y `servidor` son **byte-idénticas** (SHA256 `33660EB8…` y `174EA46F…`) |
| `servidor\plugins\Skript\variables.csv` | **107 B, solo cabecera: NO hay variables persistidas** → hoy no hay datos Skript que importar (verificado) |
| `servidor\plugins` (19 plugins) | Están: Towny, LuckPerms, floodgate, Geyser-Spigot, PlaceholderAPI, TAB, DecentHolograms, Skript, CoreProtect, LevelledMobs, Duels, WorldGuard, FAWE, ViaVersion, spark, ajLeaderboards, Chunky, InventoryRollbackPlus, **MushokuCore-1.0.0.jar** (200.688 B, SHA256 `cc2140d5e4e637f1b0c8c8a1e5d1c2b3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9`)
| Java | Temurin **25.0.4.1** (`java -version`, este turno). `servidor\paper.jar` existe (build no verificado este turno) |
| Repo del plugin | Existe en **`C:\Dev\OPENCLAUDIO\MushokuCore`** (80 `.java`, `build.ps1`, `pom.xml` ausente — build manual)
| `INSTRUCCIONES\plan_desarrollo\` | **No existía**; creada con este documento |

### Qué hace hoy el datapack `mt_core` (no duplicar — §2.2 L56, §21 L893)

| Function | Qué hace (leído este turno) |
|---|---|
| `load.mcfunction` | Crea scoreboards `mt_stage`, `mt_roll` |
| `setup.mcfunction` | `difficulty hard`, `worldborder center 0 0`, `worldborder set 21800`, gamerules, `mt_stage 0` |
| `stage_launch.mcfunction` | `mt_stage 1` + tellraw "sellos dimensionales activos" |
| `stage_dimensions_open.mcfunction` | `mt_stage 2` + tellraw "sellos quebrados" |
| `relics/roll_normal.mcfunction` | `random 1..100` → 1–3 anillo, 4–6 pendiente, 7–9 colgante, 10–11 espada Paul, 12 lanza Superd → `give_*` → revoca advancement `mt:trial_reward` |
| `relics/roll_ominous.mcfunction` | 1–5 anillo, 6–10 pendiente, 11–15 colgante, 16–20 Paul, 21–24 Superd, 25–27 Aqua Heartia, 28–30 armadura → revoca `mt:trial_reward_ominous` |
| `relics/give_*.mcfunction` (7) | `give` de ítem vanilla + `item_model="mushoku:…"` + custom_name + lore. **Sin PDC** (§14) |
| Advancements | `trial_reward`: trigger `player_generates_container_loot` sobre `chests/trial_chambers/reward`; `trial_reward_ominous`: ídem `reward_ominous` |

- Las probabilidades del datapack **coinciden exactamente** con la tabla del spec §14 L711–719 (normal 3/3/3/2/1 %, ominosa 5/5/5/5/4/3/3 %). Medido comparando los rangos de los dos `roll_*.mcfunction` con §14.
- El datapack **no** implementa: potencias, jefes, PvP, enfermedades, facciones, dimensiones reales. Los `stage_*` solo escriben un scoreboard y un tellraw: **no bloquean portales** (verificado leyendo los 4 functions). El bloqueo de Nether/End está "Pendiente" también en `Requerimientos…md` §12 L227.
  - **Actualizado 2026-10-22:** esta foto es anterior a la implementación del jefe. Medido en disco en este turno: el datapack **sí** tiene `function/boss/*` (15), `puerta/*` (4), `sellos/*` (4), `prueba/*` (3), `tick.mcfunction` y los advancement/loot del jefe. Lo que sigue **sin implementar** es la estructura de la trial chamber: **arena de Hitogami** `(-2304,-16,-3008)` y **portal al End** detrás (las SC1–SC10 están colocadas según el usuario; sin evidencia en este repo).

### Qué hace hoy el Skript (no duplicar)

| Script | Implementa (leído completo) |
|---|---|
| `potencias.sk` | Títulos 1–7; asignación vía `lp user <uuid> parent add/remove potencia<N>` (**grupos `potencia1..7`**); acumulo de daÑo a warden/wither/dragon (daÑo crudo, sin tope de overkill, solo atacante jugador directo); 1ª muerte reclama puesto; PvP con cooldown de pareja **12 h**; comandos `/potencias` y `/potenciaadmin` (perm `mt.potencias.admin`) |
| `enfermedades.sk` | 4 enfermedades: `dryne` (husk, 3 min weakness+mining fatigue), `miasma` (cave spider/silverfish, 2 min slowness+weakness), `laplace` (enderman, 45 s darkness+nausea), **`desplazamiento`** (nausea+slowness 30 s, sin fuente en el listener de daÑo); chance 4 %, **Orsted 50 % vía permiso `mt.alianza.orsted`**; `/visionapostol` (perm `mt.alianza.apostoles`, cooldown 30 min, night vision 5 min); `/enfermedad <jugador> <id>` (perm `mt.enfermedades.admin`, sintaxis **distinta** a la del §17); `/mtestado` |
| `features.sk` / `config.sk` | Config de Skript, sin mecánicas MT (buscado `portal|nether|dimension|potencia|…`: 0 coincidencias) |

`enfermedades.sk` **no persiste nada** (solo efectos vanilla + mensajes). Todo el estado persistente vivo hoy está en las variables de `potencias.sk`, y `variables.csv` está vacío → **cero datos que migrar hoy** (pero el importador se construye igual: §3 L105 lo exige).

---

## 1. Alcance exacto del MVP 1.0

Basado en §3 L91–105 (Versión 1.0) y §29 L1089 (decisión recomendada: potencias + enfermedades + storage + auditoría + Bedrock, *después* facciones y reliquias).

### 1.1 INCLUYE

| # | Feature | Spec | Notas |
|---|---|---|---|
| F1 | Persistencia SQLite + migraciones numeradas | §3 L95, §10 | Copia de BD antes de migrar (§20 L889) |
| F2 | `config.yml` schema 2 + `messages.yml`/`diseases.yml`/`relics.yml` | §9, §27 L1057–1063 | Reemplaza el contrato schema 1 existente (§9 L389) |
| F3 | Siete Potencias: asignación **automática** de puestos **1, 2, 3 y 5** (tabla `world_puestos` admite 1–7 y el admin puede asignar 1–7) | §3 L97, §11 L541–548, §17 L816 | 4/6/7 son 1.3 (§3 L131) |
| F4 | Seguimiento de contribución contra jefes (Warden→1, Ender Dragon→2, Wither→3), con tope de overkill, resolución de proyectiles/mascotas, desempate y transacción idempotente | §11 L552–575 | |
| F5 | Bajas PvP válidas: cooldown por pareja (12 h), minimum-contribution, recálculo del puesto 5 + tarea de reconciliación | §11 L577–588, §9 L434–438 | |
| F6 | Protección anti-farmeo de bajas (mismas reglas del §11 L579–586, salvo los umbrales que quedan a decisión del usuario §9 de este plan) | §11, §20 L880 | |
| F7 | Sincronización de grupos LuckPerms al cambiar un puesto, con outbox de reintento si LuckPerms falla | §11 L572–573, L590–592 | |
| F8 | Enfermedades y curas: `diseases.yml`, tirada de contagio con reducción Orsted, efectos vanilla, persistencia, inmunidad, cura con leche, expiración por tarea liviana | §13, §2.1 L52 | |
| F9 | Comandos MVP: `/mushoku ayuda|estado|reload|diagnostico|backupdb`, `/potencias` (+ admin asignar/liberar), `/mtestado`, `/enfermedad aplicar|curar` | §17 L799–827 | |
| F10 | Detección Java/Bedrock (Floodgate) con fallback a comandos/texto | §3 L102, §16 L775–792, §2.1 | **Sin formularios** (los forms son 1.3, §3 L132) |
| F11 | Placeholders PlaceholderAPI (los 9 del §18 L844–854), leyendo caché, **cero SQL por render** | §18, §19 L874 | |
| F12 | `audit_log` + `IAuditoria` para toda operación que cambie datos | §10 L525–532, §17 L829–836 | |
| F13 | Importador de datos de Skript (modo simulación + real) | §3 L105, §21 L891–913 | Hoy la fuente está vacía (verificado §0); se construye y se prueba con fixture sintético |
| F14 | `plugin.yml` completo y detección de dependencias ausentes (fallo seguro §2.5) | §6, §2.5, §8 | |
| F15 | Pruebas unitarias e integración del §22, aplicadas al alcance MVP | §22 L915–949 | Claims/reliquias/dimensiones: tests de 1.1/1.2/1.3 |

### 1.2 NO INCLUYE (para que ningún agente lo intente)

| Qué | Versión | Spec |
|---|---|---|
| Integración Towny real, naciones fijas Orsted/Apóstoles, `/faccion`, `/alianza elegir|confirmar`, bloqueo 14 días, desertor 7 días, claims 8+5/max 48, cupos 3–8, beneficios por alianza, `/visionapostol` Java | **1.1** | §3 L107–115, §12 |
| Reliquias con PDC, entrega idempotente PENDING→ACTIVE, tablas de probabilidad, loot ominoso, modelos Java/Geyser v2, `/reliquia dar|inspeccionar` | **1.2** | §3 L117–124, §14 |
| Apertura real de dimensiones (LOCKED→PREPARED→OPEN + bloqueo real de portales/homes/RTP), eventos de Perugius, API de NPC/jefes, puestos 4/6/7, formularios Bedrock | **1.3** | §3 L126–132, §15, §16 |
| Cualquier modificación de `MT_22000_WorldPainter_MINIMO`, `MT_Server_Base_26.2`, `servidor`, `mundo`, `tesis_neuronal` | — | Restricción del encargo |

**Explícito:** mientras `modules.relics=false` (§21 L910–911), **el datapack sigue siendo el único que entrega reliquias** y NO se toca. Mientras el módulo factions está apagado, **el Skript sigue siendo el dueÑo de alianzas** (`mt.alianza.*`) y no se duplica su lógica (§21 L893).

---

## 2. Estructura de paquetes y clases Java

Raíz propuesta del repo: `C:\Dev\OPENCLAUDIO\minecraft_server\mushoku-core\` **[PROPUESTA]** (el spec §4 L149–172 sugiere el nombre `mushoku-core/`; no existe todavía). Paquete base: `ar.com.maydana.mushokucore` (§4 L163, §6 L233). Java 25 toolchain (§5 L188), API Paper **sin NMS** (§2.3 L71–73).

Marcas: **[SPEC]** = el nombre de clase viene del spec; **[PROP]** = es mío.

```text
ar.com.maydana.mushokucore/
├── MushokuCorePlugin.java                 [SPEC §8 L361]
├── bootstrap/
│   ├── PluginBootstrap.java               [SPEC §8 L369]
│   ├── PluginContext.java                 [PROP] servicio locator + registro de módulos
│   └── ModuleRegistry.java                [PROP] orden de enable/disable
├── config/
│   ├── ConfigService.java                 [PROP] carga/valida schema 2 (§9), migración 1→2
│   ├── PluginConfig.java                  [PROP] holder inmutable con getters
│   └── Messages.java                      [PROP] messages.yml tipado
├── database/
│   ├── DatabaseManager.java               [SPEC §7 L305]
│   ├── MigrationRunner.java               [SPEC §7 L306]
│   ├── IDatabase.java                     [PROP] contrato async (§6 de este plan)
│   └── repository/                        [PROP, §7 L307]
│       ├── PlayerRepository.java
│       ├── PowerRepository.java
│       ├── BossDamageRepository.java
│       ├── PvpRepository.java
│       ├── DiseaseRepository.java
│       └── AuditRepository.java
├── audit/
│   ├── IAuditoria.java                    [PROP] contrato (§6 de este plan)
│   └── AuditService.java                  [PROP] impl sobre audit_log
├── integration/
│   ├── FloodgateHook.java                 [SPEC §7 L304]
│   ├── IEstadoBedrock.java                [PROP] contrato (§6 de este plan)
│   ├── LuckPermsHook.java                 [SPEC §7 L305]
│   ├── PlaceholderHook.java               [SPEC §7 L306] expansión PAPI
│   └── TownyHook.java                     [SPEC §7 L307] — **solo stub `isPresent()` en MVP** [PROP; decisión D9]
├── module/
│   ├── ModuleLifecycle.java               [PROP] interface de módulo (§7 L322 "método de inicio y cierre")
│   ├── PlaceholderFuente.java             [PROP] interface de proveedor de placeholders
│   ├── ranks/                             ── B (§11)
│   │   ├── IPotenciasService.java         [PROP] contrato (§6 de este plan)
│   │   ├── PotenciasModule.java           [PROP] entrada Lifecycle
│   │   ├── PotenciasService.java          [PROP] impl
│   │   ├── ContributionTracker.java       [PROP] buffer en memoria de daÑo (§19 L863)
│   │   ├── PvPKillValidator.java          [PROP] reglas §11 L579–586 puras (testeables)
│   │   ├── BossKillResolver.java          [PROP] §11 L563–575 puras (testeables)
│   │   ├── LuckPermsOutbox.java           [PROP] reintento §11 L592
│   │   └── task/
│   │       ├── BossBufferFlushTask.java   [PROP]
│   │       └── PowerReconcileTask.java    [PROP] §11 L588
│   ├── diseases/                          ── C (§13)
│   │   ├── IEnfermedadesService.java      [PROP] contrato
│   │   ├── DiseasesModule.java            [PROP]
│   │   ├── DiseasesService.java           [PROP]
│   │   ├── ContagionRoller.java           [PROP] chance + reducción Orsted (testeable)
│   │   └── task/DiseaseExpiryTask.java    [PROP] §13 L693
│   └── commands/                          ── D (§17)
│       ├── CommandsModule.java            [PROP] registra comandos del grupo
│       ├── MushokuCommand.java            [PROP]
│       ├── MtestadoCommand.java           [PROP]
│       ├── stub/FaccionCommand.java        [PROP] "módulo disponible en 1.1"
│       ├── stub/AlianzaCommand.java        [PROP] ídem
│       └── stub/ReliquiaCommand.java       [PROP] "disponible en 1.2"
├── command/                               (los comandos MVP del módulo ranks/enfermedades van con su módulo)
│   ├── PotenciasCommand.java              ── B
│   └── EnfermedadCommand.java             ── C
├── listener/
│   ├── BossDamageListener.java            ── B (§11 L553)
│   ├── PvPKillListener.java               ── B (§11 L577)
│   ├── DiseaseInfectionListener.java      ── C (§13)
│   ├── MilkCureListener.java              ── C (§13 L690)
│   ├── DiseaseRestoreListener.java        ── C (join → reaplica, §13 L689)
│   └── PlayerCoreListener.java            ── A (join: refresh nombre §2.4, detección Bedrock)
├── ui/
│   ├── PlayerUi.java                      [SPEC §16 L777–783, texto literal]
│   └── JavaChatUi.java                    [SPEC §16 L788]
│   └── BedrockFormUi.java                 → **NO se crea en MVP** (1.3, §16 L789)
├── import/
│   ├── SkriptImporter.java                ── D (§21)
│   └── SkriptVariableParser.java          ── D (formato variables.csv / CSV)
└── util/
    └── (TimeUtils, Uuids)                 ── A
```

Recursos (Fase 0): `plugin.yml`, `config.yml`, `messages.yml`, `diseases.yml`, `relics.yml`, `migrations/V001__initial.sql`, `MIGRATION.md`.

---

## 3. Esquema SQLite y migraciones (§10)

Motor: SQLite embebido (`org.xerial:sqlite-jdbc`, relocado en el shadowJar, §5 L206–218). **Todas las consultas fuera del hilo principal** (§10 L449, §19 L860). Las migraciones van en `src/main/resources/migrations/` como `V001__initial.sql`, `V002__…sql`; **nunca se edita una migración ya publicada** (§10 L535). `MigrationRunner` aplica en orden, registra en `schema_version` y **copia la BD a `backups/` antes de aplicar** (§20 L889).

### `V001__initial.sql`

Las 9 tablas del §10 L453–533 **verbatim** (schema_version, players, world_powers, boss_damage, pvp_score, pvp_pair_cooldown, allegations, diseases, relic_instances, audit_log — en realidad son 10 contando schema_version), más lo propuesto:

```sql
-- ===== §10 L453-533, textual =====
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

-- ===== [PROPUESTA] fuera del §10, pero exigido por §11 L592 =====
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

-- ===== [PROPUESTA] índices (el §10 no los lista) =====
CREATE INDEX idx_boss_damage_player  ON boss_damage(player_uuid);
CREATE INDEX idx_boss_damage_boss    ON boss_damage(boss_uuid);
CREATE INDEX idx_diseases_expires    ON diseases(expires_at);
CREATE INDEX idx_audit_occurred      ON audit_log(occurred_at);
CREATE INDEX idx_pvp_pair_lastkill   ON pvp_pair_cooldown(last_valid_kill_at);
```

Notas:
- `allegiances` y `relic_instances` se **crean igual** aunque sus módulos sean 1.1/1.2: así el V001 no se rompe al activarlos (§10 las exige como tablas mínimas).
- PRAGMAs al abrir (no están en el spec; **[PROPUESTA]**): `journal_mode=WAL`, `busy_timeout` desde `storage.busy-timeout-ms` (§9 L398), `foreign_keys=ON`. WAL reduce la contención de lecturas con la caché de placeholders.
- Timestamps: texto ISO-8601 UTC (formato del spec es `TEXT`; el exacto no lo fija — **[A VERIFICAR] §10**).
- Identidad: **UUID en todo** (§2.4 L75–77); `last_name` solo presentación y se refresca en join.

---

## 4. `plugin.yml`, `config.yml` y esqueletos

### 4.1 `plugin.yml` — textual del §6 L230–285

```yaml
name: MushokuCore
version: '${version}'
main: ar.com.maydana.mushokucore.MushokuCorePlugin
api-version: '26.2'
authors: [AdministracionMushoku]
description: Sistemas tematicos y progresion del servidor Mushoku

depend:
  - LuckPerms

softdepend:
  - Towny
  - floodgate
  - PlaceholderAPI
  - TAB
  - DecentHolograms

commands:
  mushoku:
    description: Comando principal de MushokuCore
    usage: /mushoku ayuda
    aliases: [mt]
  potencias:
    description: Muestra las Siete Potencias Mundiales
  mtestado:
    description: Muestra el estado tematico del jugador
  faccion:
    description: Gestion complementaria de facciones
  alianza:
    description: Consulta o solicita una alianza
  enfermedad:
    description: Administracion de enfermedades
  reliquia:
    description: Administracion de reliquias

permissions:
  mushoku.use:
    default: true
  mushoku.potencias.view:
    default: true
  mushoku.factions.use:
    default: true
  mushoku.admin:
    default: op
  mushoku.admin.reload:
    default: op
  mushoku.admin.potencias:
    default: op
  mushoku.admin.enfermedades:
    default: op
  mushoku.admin.reliquias:
    default: op
  mushoku.admin.dimensiones:
    default: op
```

Notas de integración:
- `/faccion`, `/alianza`, `/reliquia` quedan declarados con **stub** ("Módulo disponible en una versión futura") mientras sean 1.1/1.2 **[PROPUESTA — decisión D3]**. El spec los declara en §6 aunque el MVP no los incluye.
- Faltan permisos para subcomandos nuevos que yo propongo: `mushoku.admin.import` **[PROPUESTA]**.
- `api-version: '26.2'` **[A VERIFICAR §6 L234]**: hay que confirmar el valor aceptado por el Paper 26.2 real en el primer arranque de prueba.
- `depend: [LuckPerms]` es duro (§6 L238): sin LuckPerms el plugin no arranca. Coincide con §2.5 en que el daÑo es apagar solo el módulo… pero con `depend` Bukkit no carga el plugin. **[A VERIFICAR §6 vs §2.5]** — decisión D16.

### 4.2 `config.yml` (schema 2) — base §9 L391–443 + decisiones ya tomadas

```yaml
schema: 2
enabled: true

storage:
  type: sqlite
  file: mushoku.db
  busy-timeout-ms: 5000

modules:
  ranks: true
  factions: false        # [DECISIóN D4] 1.1; el spec §9 L403 dice true
  diseases: true
  relics: false          # [DECISIóN D4] mientras el datapack entregue loot (§21 L910)
  dimensions: false      # [DECISIóN D4] 1.3
  bedrock-bridge: true

world:
  name: world    # coincide con la decisión del usuario y §9 L409
  border-diameter: 21800 # decisión del usuario 2026-10-16; coincide con §9 L410
  center-x: 0
  center-z: 0
  min-y: -64
  max-y: 511             # DECISIóN DEL USUARIO 2026-10-17 (verificado en config actual y datapack sonda)
                         # el spec §9 L414 dice 320 y §9 L445 advierte contra 511 → CONFLICTO, manda el usuario

dimensions:              # inerte en MVP; se parsea para los placeholders y 1.3
  nether-locked-at-launch: true
  end-locked-at-launch: true
  minimum-unlock-days: 7
  allow-admin-bypass: true

factions:                # inerte en MVP (1.1); valores §9 L422-432, ya decididos por el usuario
  min-members: 3
  max-members: 8
  claims-base: 8
  claims-per-member: 5
  claims-max: 48
  allegiance-lock-days: 14
  deserter-cooldown-days: 7
  fixed-nations:
    orsted: Orsted_Corp
    apostles: Apostoles_Humanos

ranks:
  pvp-same-victim-cooldown-hours: 12    # §9 L435; = Skript actual
  boss-contribution-expiry-minutes: 60  # §9 L436
  minimum-contribution-percent: 1.0     # §9 L437
  announce-global: true                 # §9 L438
  # ---- [PROPUESTA] claves nuevas, sin ellas las reglas del §11 L579-586 no se pueden implementar ----
  min-victim-online-seconds: 60         # [DECISIóN D6] "conectada demasiado poco" §11 L585
  excluded-worlds: []                   # [DECISIóN D7] "mundo de prueba / arena no válida" §11 L584
  valid-boss-worlds: []                 # [DECISIóN D7] "mundo/evento permitido" §11 L565
  boss-buffer-flush-seconds: 5          # [PROPUESTA] §19 L863
  reconcile-minutes: 30                 # [PROPUESTA] §11 L588

diseases:                                # [PROPUESTA] reglas globales del módulo (§13 no tiene sección config)
  tick-check-ticks: 100                  # §13 L693 "tarea periódica liviana"
  orsted-chance-multiplier: 0.5          # §12 L620; aplica si el jugador tiene orsted-permission
  orsted-permission: mt.alianza.orsted   # [DECISIóN D11] nombre actual del Skript (enfermedades.sk L26)

bedrock:
  enable-forms: false                    # forms son 1.3 (§3 L132); §9 L441 dice true → prevalece el alcance
  always-provide-command-fallback: true  # §9 L442
```

Claves del `config.yml` viejo (schema 1: `essential_clickable_chat`, `require_offhand`, `require_entity_glow`, `custom_enchantments`, `custom_anvil_recipes`) **[DECISIóN D16]**: se descartan (son guardas que el §2.1 ya garantiza por diseÑo). `ConfigService` detecta `schema: 1` o ausente → **no arranca módulos de escritura**, loguea instrucciones claras y usa defaults del jar (fallo seguro §2.5). El archivo viejo **no se modifica** por nosotros; lo reemplaza el operador en el paso de instalación (§24 L999–1008).

### 4.3 `messages.yml` (esqueleto, **[PROPUESTA]** — el spec no da contenido)

Claves mínimas; los textos del Skript actual se conservan como base para no cambiar la experiencia:

```yaml
prefix: "&8[&6MushokuCore&8]&r"
generic:
  no-permission: "&cNo tenés permiso."
  player-not-found: "&cJugador no encontrado: {jugador}"
  reload-ok: "&aConfiguración recargada (módulos: {modulos})."
  db-error: "&cError de base de datos. La operación NO se aplicó. Contacta a un admin."
potencias:
  list-header: "&8&m-------------------------------&r\n&6&lLAS SIETE POTENCIAS MUNDIALES"
  list-entry: "&e{puesto}. &f{titulo} &8— &7{propietario}"   # {propietario}="No conquistado" si libre
  list-footer: "&8&m-------------------------------&r"
  announce-new-owner: "&6[Potencias]&r &e{jugador} &7ha obtenido el puesto {puesto}: &f{titulo}&7."  # = potencias.sk L29
  admin-assigned: "&aPuesto {puesto} asignado a {jugador}."
  admin-freed: "&aPuesto {puesto} liberado."
mtestado:
  header: "&8&m-------------------------------&r\n&6&lESTADO DE PERSONAJE"
  alliance-orsted: "&7Alianza: &bOrsted Corp &8(50% menos contagio)"
  alliance-apostles: "&7Alianza: &dApóstoles del Dios Humano &8(/visionapostol)"
  alliance-neutral: "&7Alianza: &fNeutral"
  disease-none: "&7Sin enfermedades."
  disease-active: "&7Enfermedad: &f{enfermedad} &7(expira en {minutos} min)"
  footer: "&8&m-------------------------------&r"
enfermedad:
  infected: "&e{display-name}&7: {efectos-texto} durante {duracion}."   # = textos de enfermedades.sk L8/12/16/20
  cured: "&aCuraste {display-name}."
  admin-applied: "&aEnfermedad {id} aplicada a {jugador}."
  admin-cured: "&aEnfermedades curadas de {jugador}."
dimensiones:
  locked-nether: "SELLADO"    # placeholders §18; estado real llega en 1.3
  locked-end: "SELLADO"
stub:
  module-future: "&cEste módulo estará disponible en una versión futura del servidor."
import:
  dry-run-ok: "&aSimulación OK: {n} registros, 0 conflictos."
  dry-run-conflicts: "&eSimulación con {n} conflictos. Revisá la consola."
  real-done: "&aImportación completada: {n} registros."
```

### 4.4 `diseases.yml` (§13 L648–682 verbatim + 1 decisión)

```yaml
dryne:
  display-name: "Fiebre de Dryne"
  sources:
    - HUSK_DAMAGE
  base-chance-percent: 4.0
  duration-seconds: 180
  effects:
    WEAKNESS: 0
    MINING_FATIGUE: 0
  cures:
    - MILK
  immunity-after-cure-seconds: 300

miasma:
  display-name: "Miasma de laberinto"
  sources:
    - CAVE_SPIDER_DAMAGE
    - SILVERFISH_DAMAGE
  base-chance-percent: 4.0
  duration-seconds: 120
  effects:
    SLOWNESS: 0
    WEAKNESS: 0

laplace:
  display-name: "Fiebre de Laplace"
  sources:
    - ENDERMAN_DAMAGE
  base-chance-percent: 4.0
  duration-seconds: 45
  effects:
    DARKNESS: 0
    NAUSEA: 0

desplazamiento:                # [DECISIóN D2] existe en Skript (enfermedades.sk L17-20) pero no en §13
  display-name: "Mal de desplazamiento"
  sources: []                  # en Skript no tiene fuente automática (solo admin)
  base-chance-percent: 0.0
  duration-seconds: 30
  effects:
    NAUSEA: 0
    SLOWNESS: 0
```

Reglas de implementación (§13 L684–693): amplificador `0` = nivel I; reducción Orsted **antes** de la tirada; sin acumulación infinita (si ya está activa, se ignora o refresca la expiración a lo sumo — [PROPUESTA]: ignorar reinfección con la misma enfermedad); persistir en `diseases`; milk cure si `cures: [MILK]`; **ninguna tarea por jugador por tick**.

### 4.5 `relics.yml` (esqueleto inerte, §14; deliverable §27; no se usa en MVP)

```yaml
schema: 1
pdc-keys:                      # §14 L700-705
  relic-id: "mushokucore:relic_id"
  relic-uuid: "mushokucore:relic_uuid"
  schema: "mushokucore:schema"
schema-version: 1
catalog:
  anillo_supresion:  { base-item: AMETHYST_SHARD, model: "mushoku:ring",          chance-normal: 3, chance-ominous: 5 }
  pendiente_sara:    { base-item: ECHO_SHARD,     model: "mushoku:earring",       chance-normal: 3, chance-ominous: 5 }
  colgante_migurd:   { base-item: HEART_OF_THE_SEA, model: "mushoku:pendant",     chance-normal: 3, chance-ominous: 5 }
  espada_paul:       { base-item: IRON_SWORD,     model: "mushoku:paul_sword",    chance-normal: 2, chance-ominous: 5 }
  lanza_superd:      { base-item: TRIDENT,        model: "mushoku:superd_spear",  chance-normal: 1, chance-ominous: 4 }
  aqua_heartia:      { base-item: DIAMOND_SWORD,  model: "mushoku:aqua_heartia",  chance-normal: 0, chance-ominous: 3 }
  armadura_magica:   { base-item: NETHERITE_CHESTPLATE, model: "mushoku:magic_armor", chance-normal: 0, chance-ominous: 3 }
```

Los nombres/ítems/probabilidades salen de §14 L711–719 y **coinciden** con los `give_*`/`roll_*` del datapack (verificado §0). El modelo usa el mismo `item_model="mushoku:…"` que ya escriben los `give_*.mcfunction`.

---

## 5. Comandos y listeners a registrar

### 5.1 Comandos (§17 + §11–13)

| Comando | Permiso | Quién lo ejecuta | DueÑo del código | MVP |
|---|---|---|---|---|
| `/mushoku ayuda` | `mushoku.use` | jugador | D | ✔ |
| `/mushoku estado` | `mushoku.admin` | admin/consola | D | ✔ (módulos activos, versión, BD, esquema) |
| `/mushoku reload` | `mushoku.admin.reload` | admin | D | ✔ (solo archivos propios seguros, §17 L838) |
| `/mushoku diagnostico` | `mushoku.admin` | admin/consola | D | ✔ (§24 L1007) |
| `/mushoku backupdb` | `mushoku.admin` | admin/consola | D (llama API de A) | ✔ |
| `/mushoku import skript <ruta> [--simular]` | `mushoku.admin.import` **[PROP]** | consola/admin | D | ✔ **[PROPUESTA — D12]** (no está en §17) |
| `/potencias` | `mushoku.potencias.view` | jugador | B | ✔ |
| `/potencias admin asignar <1-7> <jugador> <motivo>` | `mushoku.admin.potencias` | admin/consola | B | ✔ |
| `/potencias admin liberar <1-7> <motivo>` | `mushoku.admin.potencias` | admin/consola | B | ✔ |
| `/mtestado` | `mushoku.use` | jugador | D (consume IPotenciasService + IEnfermedadesService) | ✔ |
| `/enfermedad aplicar <jugador> <id>` | `mushoku.admin.enfermedades` | admin/consola | C | ✔ |
| `/enfermedad curar <jugador> [id\|todas]` | `mushoku.admin.enfermedades` | admin/consola | C | ✔ |
| `/faccion info` | `mushoku.factions.use` | jugador | D (stub) | stub 1.1 |
| `/alianza info\|elegir\|confirmar` | `mushoku.factions.use` | jugador | D (stub) | stub 1.1 |
| `/reliquia info\|dar\|inspeccionar` | `mushoku.admin.reliquias` / `mushoku.use` | admin | D (stub) | stub 1.2 |
| `/dimension …` (§17 L823–827) | `mushoku.admin.dimensiones` | admin | — | ✘ 1.3 y **ausente de plugin.yml §6 [A VERIFICAR]** |
| `/visionapostol` | `mt.alianza.apostoles` (Skript) | jugador Apóstol | **queda en Skript** hasta 1.1 (§21 L893) | ✘ |
| `/potenciaadmin` (Skript) | `mt.potencias.admin` | admin | queda en Skript **solo** hasta la activación del módulo ranks; se desactiva ese día | transición |

Requisitos de todos los comandos MVP (§17 L829–836): validar permiso; funcionar desde consola cuando aplique; autocompletado; mensaje inequívoco; auditoría en toda escritura; sin stacktraces al jugador.

### 5.2 Listeners

| Listener | Eventos | DueÑo | Spec |
|---|---|---|---|
| `BossDamageListener` | `EntityDamageByEntityEvent` (WARDEN/WITHER/ENDER_DRAGON), `EntityDeathEvent` | B | §11 L553 |
| `PvPKillListener` | `PlayerDeathEvent` | B | §11 L577 |
| `DiseaseInfectionListener` | `EntityDamageByEntityEvent` (fuentes de `diseases.yml`) | C | §13 |
| `MilkCureListener` | `PlayerItemConsumeEvent` (leche) | C | §13 L690 |
| `DiseaseRestoreListener` | `PlayerJoinEvent` (reaplica efectos), `PlayerQuitEvent` (flush) | C | §13 L689 |
| `PlayerCoreListener` | `PlayerJoinEvent` (refresh `last_name` §2.4, detección Bedrock, caché placeholder) | A | §2.4, §18 |
| ~~Portal/teleport guard~~ | — | **no existe en MVP** (1.3) | §15 |

### 5.3 Tareas periódicas (§19 L872: 20–100 ticks o periódicas)

| Tarea | Período propuesto | DueÑo | Spec |
|---|---|---|---|
| `BossBufferFlushTask` (vuelca el buffer de daÑo) | cada `boss-buffer-flush-seconds` (5 s) | B | §19 L863, L864 |
| `PowerReconcileTask` (recalcula puesto 5) | cada `reconcile-minutes` (30 min) | B | §11 L588 |
| `LuckPermsOutbox.retry` | cada 60 s **[PROP]** | B | §11 L592 |
| `DiseaseExpiryTask` | cada 100 ticks (5 s), cola en memoria, sin SQL por tick | C | §13 L693 |
| `AuditArchiveTask` (retención por archivado, no borrado) | diario **[DECISIóN D20]** | A | §19 L866 |

---

## 6. CONTRATO DE INTEGRACIóN (lo más importante para los agentes paralelos)

**Reglas de oro:**
1. Cada archivo `.java` tiene **un único dueÑo** (tabla 6.3). Nadie edita archivos ajenos. Si necesitás algo de otro módulo, se consume por interface.
2. Los interfaces del §6.1 los escribe el agente **F0 (scaffold)** en la Fase 0 y **quedan congelados**. Cambiarlos exige actualizar este plan y avisar al integrador (Fase 2).
3. Hilo: `IDatabase` **nunca** se llama desde el hilo principal (§10 L449, §19 L860). Las cadenas async se componen con `thenApply/thenAccept`; **prohibido `.join()`/`.get()` en el hilo del servidor**. Toda mutación de Bukkit (efectos, LuckPerms, mensajes, dar ítems) vuelve al hilo principal con `BukkitScheduler.runTask`.
4. Crítico vs diferido (§19 L863–864): daÑo a jefes = buffer en memoria + flush periódico + **flush inmediato al morir el jefe o cambiar un puesto** (async, y el cierre del plugin espera a que terminen las escrituras: §8 L353).
5. Idempotencia (§11 L575, §20): asignación de puesto = transacción `UPDATE world_powers SET … WHERE position=? AND (owner_uuid IS NULL OR revision=?)` → si 0 filas, otro proceso ganó; abortar sin duplicar. Reliquias: fuera del MVP.

### 6.1 Interfaces (texto de referencia; F0 las crea)

```java
// database/IDatabase.java  [PROPUESTA] — impl: A
public interface IDatabase {
    <T> CompletableFuture<T> read(Function<Connection, T> op);   // hilo de BD dedicado
    CompletableFuture<Void>  write(Consumer<Connection> op);      // misma cola, orden garantizado
    CompletableFuture<Void>  transaction(BiConsumer<Connection, Connection> /*noop reserva*/ op);
    Path backup(String motivo) throws IOException;                // §17 /mushoku backupdb, §20 L889
    boolean isHealthy();
    void flushAll() throws Exception;                             // onDisable paso 3 (§8 L353)
}

// audit/IAuditoria.java  [PROPUESTA] — impl: A
public interface IAuditoria {
    void registrar(@Nullable UUID actor, String accion, @Nullable String target, @Nullable String detalles);
    // async, jamás lanza al llamador; audit_log se archiva, no se borra (§19 L866)
}

// integration/IEstadoBedrock.java  [PROPUESTA] — impl: A (FloodgateHook)
public interface IEstadoBedrock {
    boolean esBedrock(UUID jugador);     // false si Floodgate ausente (§2.5)
    boolean floodgateDisponible();
}

// config/PluginConfig.java  [PROPUESTA] — lo construye A; getters inmutables de TODO §9
// (ranks.*, diseases.*, factions.* aunque el módulo esté apagado, world.*, dimensions.*, bedrock.*, modules.*)

// integration/LuckPermsHook.java  [SPEC name §7 L305] — impl: A
public interface LuckPermsHook {
    boolean disponible();
    boolean agregarGrupo(UUID jugador, String grupo);   // §11 L573
    boolean quitarGrupo(UUID jugador, String grupo);
    boolean tienePermiso(UUID jugador, String permiso); // reducción Orsted §13
}
// [DECISIóN D1] los grupos por defecto son "potencia1..7" (compat con potencias.sk L23/L27)

// module/ModuleLifecycle.java  [PROPUESTA] — F0
public interface ModuleLifecycle {
    String id();                                  // "ranks" | "diseases" | "commands"
    void enable(PluginContext ctx) throws Exception;
    void disable();
}

// bootstrap/PluginContext.java  [PROPUESTA] — F0
public interface PluginContext {
    JavaPlugin plugin();
    PluginConfig config();
    Messages messages();
    IDatabase db();
    IAuditoria audit();
    IEstadoBedrock bedrock();
    LuckPermsHook luckperms();
    <T> void registrarServicio(Class<T> tipo, T impl);   // módulo publica su API
    <T> T obtener(Class<T> tipo);                          // CommandsModule consume aquí
    void registrarPlaceholderFuente(PlaceholderFuente fuente);
    void registrarComando(String nombre, CommandExecutor exec, @Nullable TabCompleter tab);
    void registrarListener(Listener listener);
}

// module/PlaceholderFuente.java  [PROPUESTA] — F0; §18 L856: solo caché, cero SQL
public interface PlaceholderFuente {
    Set<String> claves();                       // ej. "power_1_name", "player_valid_kills"
    Optional<String> valor(PlaceholderAPIAPIContextual ctx); // jugador puede ser null
}

// module/ranks/IPotenciasService.java  [PROPUESTA] — F0 lo declara, B lo implementa
public interface IPotenciasService {
    Optional<DueÑoPuesto> propietario(int puesto);          // lectura de caché
    int posicionDelJugador(UUID jugador);                   // 0 si no tiene puesto
    String tituloDe(int puesto);                            // §11 L541-548
    boolean asignar(int puesto, UUID jugador, String motivo, @Nullable UUID actorAdmin); // idempotente
    boolean liberar(int puesto, String motivo, @Nullable UUID actorAdmin);
    int bajasValidas(UUID jugador);                         // placeholder %mushoku_player_valid_kills%
}

// module/diseases/IEnfermedadesService.java  [PROPUESTA] — F0 lo declara, C lo implementa
public interface IEnfermedadesService {
    Optional<Disease> activa(UUID jugador);                 // para /mtestado y placeholder
    boolean aplicar(UUID jugador, String diseaseId, String source);
    int curar(UUID jugador, @Nullable String diseaseId);    // null = todas
}

// ui/PlayerUi.java — TEXTUAL del spec §16 L777–783 (interface incluida en el spec)
public interface PlayerUi {
    void showPowers(Player player);
    void showStatus(Player player);
    void confirmAllegiance(Player player, Allegiance allegiance);
}
```

**Firma congelada vs spec:** `PlayerUi` usa `Allegiance` (§16 L781). En MVP no hay alianzas: `Allegiance` se declara como enum `ORSTED, APOSTOLES, NEUTRAL` en `model/Allegiance.java` **[PROPUESTA — el spec no define la clase]**. `confirmAllegiance` queda sin invocar hasta 1.1.

### 6.2 Modelo de datos compartido (`model/`)

`model/` lo posee **F0**: `Puesto` (record), `Disease` (record), `Allegiance` (enum), `DueÑoPuesto` (record), `DimensionState` (enum `LOCKED, PREPARED, OPEN`, §15 L753). Sin lógica, solo datos inmutables.

### 6.3 Tabla de propiedad de archivos (contrato entre agentes)

| Archivos | DueÑo |
|---|---|
| `build.gradle.kts`, `settings.gradle.kts`, `gradlew*`, `gradle/wrapper/**`, `src/main/resources/**`, `MushokuCorePlugin.java`, `bootstrap/**`, `config/**`, `database/**` (incl. `IDatabase`, `V001__initial.sql`), `audit/**`, `model/**`, `util/**`, `ui/PlayerUi.java`, `ui/JavaChatUi.java`, `module/ModuleLifecycle.java`, `module/PlaceholderFuente.java`, `module/ranks/IPotenciasService.java`, `module/diseases/IEnfermedadesService.java`, `integration/IEstadoBedrock.java` | **F0 (scaffold)** — interfaces congeladas en Fase 1 |
| `database/DatabaseManager.java`, `MigrationRunner.java`, `database/repository/*`, `audit/AuditService.java`, `config/ConfigService.java`, `Messages.java`, `integration/FloodgateHook.java`, `LuckPermsHook.java`, `TownyHook.java` (stub), `listener/PlayerCoreListener.java`, `util/*` | **A (infra-core)** |
| `module/ranks/**` salvo el interface, `command/PotenciasCommand.java`, `listener/BossDamageListener.java`, `listener/PvPKillListener.java` | **B (potencias)** |
| `module/diseases/**` salvo el interface, `command/EnfermedadCommand.java`, `listener/Disease*Listener.java`, `listener/MilkCureListener.java` | **C (enfermedades)** |
| `module/commands/**`, `command/MushokuCommand.java`, `command/MtestadoCommand.java`, `command/stub/*`, `integration/PlaceholderHook.java`, `import/**` | **D (ui-comandos-import)** |
| Cualquier cosa fuera de su lista; fixes de compilación ajenos; docs (README/CHANGELOG/MIGRATION/SHA256SUMS.txt) | **I (integrador, Fase 2)** |

Los listeners/comandos **se registran desde el `enable()` de cada módulo** vía `PluginContext` (así F0 puede escribir `PluginBootstrap` completo en Fase 0 sin tocar archivos de nadie). `PluginBootstrap` instancia en orden fijo: `ranks` → `diseases` → `commands` (commands va último para poder `obtener(IPotenciasService)` e `IEnfermedadesService`). Cada módulo chequea su flag en `modules.*` y si está `false` no registra nada (§8 L351: "informar módulos activos e inactivos").

---

## 7. Reparto en agentes paralelos

### Fase 0 — Scaffold (1 agente, secuencial, corta)

**Agente F0.** Entrega: proyecto Gradle compilable con `.\gradlew.bat clean compileJava`, `plugin.yml`, los 4 resources, `V001__initial.sql`, todas las interfaces y modelos del §6.1/§6.2, `MushokuCorePlugin` + `PluginBootstrap` + los tres shells de módulo (`PotenciasModule`, `DiseasesModule`, `CommandsModule` con `enable()` vacío) **y transfiere la propiedad** de esos shells a B/C/D según §6.3.
**Aceptación:** compila; `plugin.yml` válido; `V001` aplica sobre un archivo `.db` temporal sin error (test mínimo de MigrationRunner con H2-no… con el propio sqlite-jdbc).

### Fase 1 — 4 agentes EN PARALELO (sin conflictos de archivos gracias a §6.3)

| Agente | Nombre | Archivos dueÑos | Contratos que CONSUME | Criterio de aceptación (alineado §26) |
|---|---|---|---|---|
| **A** | infra-core | `DatabaseManager`, `MigrationRunner`, repos, `AuditService`, `ConfigService`, `Messages`, `FloodgateHook`, `LuckPermsHook`, `TownyHook` stub, `PlayerCoreListener`, `util/*` | implementa `IDatabase`, `IAuditoria`, `IEstadoBedrock`, `PluginConfig` (los de F0) | §26 L1037: onEnable/onDisable sin errores en test harness; §26 L1038: datos sobreviven reinicio; **§26 L1039: cero SQL en hilo principal** (guard que lanza si `isPrimaryThread()`); migración falla limpio si hay error; LuckPerms ausente → módulo ranks sigue iniciando pero no concede grupos (§2.5) |
| **B** | potencias | `module/ranks/**`, `PotenciasCommand`, `BossDamageListener`, `PvPKillListener` | `IDatabase`, `IAuditoria`, `PluginConfig`, `LuckPermsHook`, `IPotenciasService` (implanta) | §26 L1040: asigna 1/2/3/5 con desempate y overkill-cap; §26 L1041: doble muerte del mismo jefe no asigna dos veces (test idempotencia); §26 L1042: outbox LP reintenta ante error simulado; reglas §11 L579–586 testeables unitariamente; anuncio global usa `messages.yml` |
| **C** | enfermedades | `module/diseases/**`, `EnfermedadCommand`, listeners de enfermedades | `IDatabase`, `IAuditoria`, `PluginConfig`, `LuckPermsHook` (permiso Orsted), `IEnfermedadesService` (implanta) | chance 4 %/reducción 50 % (§22 L919); expiración (§22 L920); efectos vanilla aplicados + persistencia + reaplica en join; milk cura y aplica inmunidad; sin tarea por jugador por tick (§13 L692); /mtestado consume su interface |
| **D** | ui-comandos-import | `module/commands/**`, `MushokuCommand`, `MtestadoCommand`, stubs, `PlaceholderHook`, `import/**` | `PluginContext` (obtiene servicios de B y C), `IEstadoBedrock`, `IDatabase.backup` | los 9 placeholders del §18 responden **sin SQL** (test: DB cerrada → placeholders OK); `/mushoku reload` no rompe BD; `/mushoku diagnostico` y `backupdb` funcionan; stubs responden con `messages.yml`; importador en `--simular` no escribe; con fixture sintético de `variables.csv` importa puestos/puntos/cool-downs y audita |

Los cuatro pueden correr **a la vez** en el mismo disco: no comparten un solo archivo editable. único choque posible: ejecutar `gradlew` simultáneamente (bloqueo de daemon/lock de Gradle) → mitigación: si el build se bloquea, esperar; los agentes solo compilan al final de su tarea.

### Fase 2 — Integración (1 agente, secuencial)

**Agente I.** Arma el jar, cierra huecos de wiring, escribe docs. DueÑo de todo.
**Aceptación (§26 completo):** `.\gradlew.bat clean test shadowJar` verde; prueba de arranque en `server-test` (§23) con módulos apagados y encendidos de a uno; prueba de LuckPerms/Floodgate/Towny ausentes (§22 L933–935); `SHA256SUMS.txt`; `MIGRATION.md` con el runbook §21 (activar módulo ranks = desactivar `potencias.sk` ese mismo momento, §21 L893); checklist Java+Bedrock de §22 L939–949 acotado a lo MVP.

---

## 8. Orden de compilación e integración

1. **F0:** `.\gradlew.bat clean compileJava` → verde.
2. **Fase 1 (paralela):** cada agente solo corre `compileJava`/`test` **al final** de su trabajo (evita carreras de daemon). No corren `shadowJar`.
3. **Checkpoint I-1:** `.\gradlew.bat clean test` → arreglar huecos (solo el integrador toca archivos ajenos).
4. **Checkpoint I-2:** `.\gradlew.bat shadowJar` → `build/libs/MushokuCore-1.0.0.jar` (§24 L994). Verificar que trae sqlite-jdbc relocado (§5 L218).
5. **Ensayo (§23):** copia del servidor `server-test` **[PROPUESTA — decisión D14: crearla o usar el `servidor\` actual con backup]**, arranque 1: sin plugin; 2: con todos los módulos `false`; 3: activar `ranks`; 4: activar `diseases`; 5: forzar reinicio con datos; 6: simular LuckPerms/Floodgate ausentes; 7: 10–14 jugadores (§23 L973); 8: `latest.log` + spark (§19 L867); 9: restaurar backup de BD.
10. **Instalación en producción (§24 L997–1008):** detener → backup → JAR + resources nuevos → arranque con whitelist → `/mushoku diagnostico` → ajustar configs → recién ahí **desactivar** `potencias.sk` y `enfermedades.sk` (§21 L903: nunca dos implementaciones del mismo premio activas) → verificar. `MushokuCore\config.yml` viejo (schema 1) se reemplaza por el operador; el plugin no lo pisa.
11. **Entregables §27:** jar + sources jar + README + CHANGELOG + MIGRATION + los 4 yml + `SHA256SUMS.txt` + licencia + lista de dependencias.

---

## 9. Requiere decisión del usuario

| # | Tema | Opción A / B | Spec |
|---|---|---|---|
| **D1** | Nombre de los grupos LuckPerms de potencias | A: mantener `potencia1..7` (compat con lo que ya creó el Skript) B: renombrar | §11 L573 no los nombra; Skript L23/L27 |
| **D2** | Incluir la enfermedad `desplazamiento` (existe en Skript, no en §13) | A: incluirla (config) B: omitirla | §13 L648–682 |
| **D3** | `plugin.yml` con stubs de `/faccion` `/alianza` `/reliquia` en el 1.0 | A: stubs informativos B: sacarlos del plugin.yml | §6 L257–264 vs §3 L107–124 |
| **D4** | Defaults de `modules.*` en el 1.0 | A: `factions/relics/dimensions: false` (mi propuesta) B: todos `true` como §9 | §9 L401–406 |
| **D5** | Importador sin fuente real (`variables.csv` vacío verificado) | A: construir y probar con fixture B: posponer a cuando haya datos | §3 L105, §21 |
| **D6** | Umbral de "víctima conectada demasiado poco" (anti-boost) | A: 60 s B: 120 s C: no implementar en MVP | §11 L585 (sin número) |
| **D7** | Listas `excluded-worlds` y `valid-boss-worlds` iniciales | A: `[]` (solo el mundo principal vale por defecto) B: listar | §11 L565, L584 |
| **D8** | "Identidad vinculada conocida" (alts) en el anti-farmeo | A: MVP solo detecta vía XUID de Floodgate B: no hacer nada aún | §11 L582 (mecanismo sin definir) |
| **D9** | Deferir el compileOnly de Towny a 1.1 (evita depender del repo de glaremasters) | A: defer B: incluir ya | §5 L202 |
| **D10** | Fallback cuando la API no resuelva daÑo indirecto (mascotas/proyectiles) | A: contarle al dueÑo B: descartar el daÑo | §11 L561 |
| **D11** | Fuente del "reduce contagio Orsted" en MVP (módulo factions apagado) | A: permiso LP `mt.alianza.orsted` (Skript lo pone hoy) B: nada hasta 1.1 | §13 L687, §12 L620 |
| **D12** | Comando de importación: `/mushoku import …` vs solo consola | A: subcomando admin B: solo consola | fuera de §17 |
| **D13** | Ubicación del repo | A: `minecraft_server\mushoku-core\` B: otra | §4 L152 |
| **D14** | Servidor de ensayo: ¿crear `server-test` (copia ~peso del servidor) o ensayar en `servidor\` con backup? | A / B | §23 |
| **D15** | Placeholders de alianza y dimensiones en MVP | A: alianza desde permisos LP; dimension = "SELLADO" constante B: devolver vacío | §18 |
| **D16** | `depend: [LuckPerms]` duro (§6) vs fallo-safe "funciona sin LuckPerms" (§2.5) | A: mantener depend duro B: softdepend + módulo ranks degradado | §6 L238 vs §2.5 L79–87 |
| **D17** | Retención de `audit_log` (archivado diario a tabla `audit_archive` o export CSV) | A / B | §19 L866 (sin números) |
| **D18** | Versiones exactas Gradle (Paper API, sqlite-jdbc, shadow, junit) | fijar en el catálogo; Paper API = el artefacto exacto del Paper 26.2 instalado | §5 L181–210 (todos `VERSION`) |

> Los valores de `factions.*` (3/8, 8+5/max 48, 14/7 días) **ya están decididos** por el usuario y coinciden con §9: no requieren decisión.

---

## 10. índice de ambigüedades `[A VERIFICAR]` (con línea del spec)

| ID | Ambigüedad | Líneas |
|---|---|---|
| V1 | `max-y: 320` en §9 vs decisión del usuario **511** (config actual y datapack sonda); §9 también advierte "no activar Y 511 hasta verificar" | L413–414, L445; config actual L28 |
| V2 | §17 lista `/dimension …` pero §6 **no declara** el comando `dimension` en plugin.yml | L823–827 vs L248–264 |
| V3 | Dependencia `me.clip:placeholderapi` sin repositorio que la sirva en §5 (extendedclip falta de la lista) | L204 vs L192–197 |
| V4 | "víctima permaneció conectada demasiado poco" sin umbral | L585 |
| V5 | "mundo de prueba, arena no válida o evento excluido" sin lista ni clave de config | L584; §9 no tiene la clave |
| V6 | "identidad vinculada conocida" sin mecanismo | L582 |
| V7 | Desempate: "primer instante en alcanzar el daÑo final empatado" — ¿primer jugador en llegar al valor empatado? | L570 |
| V8 | "operación pendiente para reintento" de LuckPerms exige persistencia, pero §10 no tiene tabla para eso (propongo `luckperms_outbox`) | L592 vs L451–533 |
| V9 | §9 muestra `modules` todos `true` pero el MVP no incluye factions/relics/dimensions | L401–406 vs L91–105 |
| V10 | `bedrock.enable-forms: true` en §9 pero los formularios son 1.3 | L441 vs L132 |
| V11 | `api-version: '26.2'` sin confirmar contra el Paper real | L234 |
| V12 | Todas las versiones Gradle son placeholders (`PAPER_VERSION`, `VERSION_ESTABLE`, …) | L181–210 |
| V13 | "administración básica" sin definir; este plan la toma como los comandos admin del §17 | L100 vs L810–827 |
| V14 | §21 asume variables de Skript a exportar; la fuente real (`variables.csv`) está **vacía** | L898; verificado §0 |
| V15 | Curación con leche "si así se configura": en el ejemplo solo `dryne` la tiene; en Skript la leche borra todo efecto | L690, L658–659; Skript README |
| V16 | §11 exige verificar que el jefe esté en "mundo/evento permitido" sin definir cuáles (¿Wither solo en overworld si Nether/End sellados?) | L565 |
| V17 | Timestamps `TEXT` sin formato declarado (uso ISO-8601 UTC **[PROPUESTA]**) | L462–531 |
| V18 | §10 no define índices ni PRAGMAs (WAL propuesto) | L451–533 |
| V19 | Tope de overkill: §11 lo exige; el Skript actual NO lo hace → el ranking post-migración puede diferir del histórico | L558; Skript L50 |
| V20 | Retención de `audit_log` "mediante archivado" sin política ni tamaÑo | L866 |

---

## 11. Riesgos técnicos (para los agentes)

1. **SQLite en el hilo principal** (§26 L1039): el único riesgo duro de aceptación. Mitigación: `IDatabase` solo async + guard de hilo en dev + flush de cierre en `onDisable` con timeout (§8 L351–355).
2. **Carrera buffer↔muerte de jefe:** si el flush periódico pierde el último golpe, se pierde la Potencia. Mitigación: flush síncrono-async inmediato en `EntityDeathEvent` (§19 L864) y transacción idempotente de asignación.
3. **Compatibilidad Bedrock:** en MVP solo hace falta la detección por UUID/XUID (Floodgate API, compileOnly). Forms y modelos quedan fuera → riesgo bajo. El test con cliente Bedrock real sigue pendiente a nivel de servidor (no verificado).
4. **No duplicar lo que ya existe:** reliquias las sigue entregando el datapack (dejar `modules.relics=false`); potencias/enfermedades las sigue corriendo el Skript hasta el día de activación; `/visionapostol` se queda en Skript. El runbook §21 es obligatorio para el cambio de soja.
5. **Red/Gradle:** el repo no existe todavía; el wrapper necesita red la primera vez y los repos del §5 no cubren PlaceholderAPI (V3). El Towny de glaremasters es el más frágil (D9).
6. **Cambio de comportamiento vs Skript:** overkill-cap y reglas anti-farmeo más estrictas harán que el puesto 5 recalculado post-import difiera de la historia Skript. Comunicarlo, no "arreglarlo" acomodando umbrales (§20: no fabricar resultados).
7. **`api-version`/formato de artefacto Paper 26.2** sin confirmar (V11, V12): puede costar un ciclo de prueba del wrapper.
8. **Mundo y=511:** el plugin lo asume tal cual (decisión del usuario); la prueba con cliente Bedrock real sigue pendiente según AGENTS — si falla, es problema del mundo, no del plugin, pero los placeholders/dimensiones no dependen de eso.


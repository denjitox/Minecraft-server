# Decisiones — 2026-10-18

Registro de las decisiones tomadas sobre el documento `INSTRUCCIONES\Plan_Eventos_y_Caracteristicas_Servidor_Mushoku.md`
y los pendientes D1–D18 del plan de MushokuCore. Cada decisión se aplica en los archivos que se indican.

**Nota:** las decisiones sobre `online-mode` se dejan como están (`online-mode=false`, protegido por whitelist).

---

## Punto 1 — Contradicciones entre documentos: se toma SIEMPRE el valor B

| Tema | Valor B elegido |
|---|---|
| Altura máxima del mundo | **y=511** (techo medido con datapack sonda) |
| Borde en el datapack | **21.800** (el `mt:setup` ya lo tenía; el 22.000 solo sobrevive en un backup) |
| Apertura de dimensiones | **7 días = mínimo**, la confirma la administración (no es automático) |
| Alcance 1.0 (MVP) | Puestos **1, 2, 3 y 5**; 4, 6 y 7 quedan para 1.3 |
| Formas Bedrock | **`enable-forms: false`** (las forms son 1.3) |
| Modificar `MT_Server_Base_26.2` | **Sí**: es la única base de escritura (lo resuelve el contrato del datapack) |

**Config vieja:** la config schema 1 que quedaba en `MT_Server_Base_26.2\plugins\MushokuCore\config.yml` se reemplaza por la schema 2 (mismos valores B).

---

## Punto 2 — Pendientes D1–D18

| Id | Decisión | Acción |
|---|---|---|
| D2 | **Sí**, incluir la enfermedad `desplazamiento` | Ya está en `diseases.yml`. Se confirma. |
| D5 | **Construir** el importador de Skript | `importer\SkriptImporter.java` + `module\commands\importer\ImportarCommand.java` existen; se verifican y cablean. |
| D6 | **Implementar** el umbral de víctima conectada poco | `ranks.min-victim-online-seconds: 60` (ya en `PvPKillValidator`). |
| D7 | Listas de mundos: **arena del jefe (Hitogami)** | `valid-boss-worlds` / `excluded-worlds` según arena. **Ver nota abajo.** |
| D8 | **Nada**: la whitelist hace ese trabajo | No se implementa detección de multicuentas (XUID). `identidadVinculada=false`. |
| D13 | **Crear carpeta `server-test`** | `C:\Dev\OPENCLAUDIO\minecraft_server\server-test\` |
| D16 | **`softdepend`** LuckPerms | `plugin.yml`: `depend` → `softdepend`. |
| D17–D20 | **Sí, 7 días** | Retención de `audit_log`: 7 días, con archivado previo. |
| D18 | Versiones del **servidor alquilado** | `build.gradle.kts` alineado a las versiones del server. **Ver nota.** |
| D3 | **Sí**, los stubs van en 1.0 (que los descubran) | `faccion` / `alianza` / `reliquia` se mantienen. |
| D4 / V9 | **`false`** en los módulos no-MVP | `factions`, `relics`, `dimensions` en `false`; `ranks`/`diseases`/`bedrock-bridge` en `true`. |
| D12 | **Jugador**: `/mushoku import` es comando invocable en juego | No solo consola. |

### Nota D7 (ambigüedad a confirmar)

El plan pedía dos listas de **mundos**: `excluded-worlds` (donde las bajas PvP no cuentan) y
`valid-boss-worlds` (donde cuenta el daño a jefes). La respuesta fue "arena del jefe (Hitogami)".
Como la config trabaja **por mundo** y la arena de Hitogami está en el overworld `world`, se interpretó:

- `valid-boss-worlds: [world]`
- `excluded-worlds: []`

**Si lo que se quería era excluir la región de la arena de las bajas PvP**, eso es por *región*, no por mundo,
y requiere otro mecanismo (pendiente de confirmar).

### Nota D18

"Versiones del servidor alquilado": no hay acceso directo al panel desde acá, así que se usa
`servidor\` (la copia de trabajo que refleja el panel) como fuente de versiones.

---

## Punto 3 — Diseño jugable

| Tema | Decisión |
|---|---|
| Altura | y=511 (ver Punto 1) |
| Plugin de duelos | **Sí habrá** (habilita el puesto 6) |
| Dificultad regional (L113–129) | **Sí**, implementar por región. LevelledMobs 4.5.3.2 lo soporta nativo (`min/max-distance-from-spawn`, fórmulas de atributo). |
| Laberintos (L179–197) | Schematics extraídos a `minecraft_server\laberintos\` (menor = Dungeons & Taverns; mayor = SH Buried Halls). |
| NPC | Perugius + sirvientes; skins descargadas a `minecraft_server\perugius\skins\`. Plugin recomendado: Citizens 2.0.44. |
| `/visionapostol` | **Desarrollarlo** en MushokuCore (hoy en Skript). Visión nocturna 5 min, cooldown 30 min, permiso `mt.alianza.apostoles`. |

---

## Punto 4 — Jefe Hitogami

| Tema | Decisión |
|---|---|
| `max_health` | **400** (reemplaza la escala 300/400/420 por cantidad de jugadores) |
| Subtítulo | **"DIOS HUMANO"** |
| Nombre | **Aprobado** (Hitogami) |
| Verificación | Pendiente de correr en `server-test` |

---

## Punto 5 — Pruebas

Todo se testea en **`server-test`**.

---

## Pendientes que NO son decisiones (recordatorio)

- Regenerar `mt_core_26.2.zip` + `SHA256SUMS.txt` tras cambiar el datapack (regla 6 de AGENTS.md).
- Definir cuál de las dos copias de `mt_core` se queda (`datapacks\` vs `world\datapacks\`).
- Limpiar el jar duplicado de Duels (`8.2` + `8.3` + archivo `Duels` de 1 B) si aplica.
- Test con cliente Bedrock real.

---

## D7 — valor medido (2026-10-20)

- **D7 —** `MT_Server_Base_26.2\plugins\MushokuCore\config.yml` **línea 61**: `valid-boss-worlds: [world]` (NO incluye `world_the_end`). Verificado 2026-10-20.

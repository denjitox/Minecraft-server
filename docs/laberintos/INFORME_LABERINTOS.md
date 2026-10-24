# Laberintos para el servidor — informe de extracción y conversión

Fecha: 2026-10-18. Trabajo hecho en `minecraft_server\laberintos\`.

## (a) Qué trae cada ZIP — formato real (verificado en disco)

Ambos ZIP son **datapacks de worldgen** (no colecciones de `.schem`). Contienen
piezas de estructura `.nbt` en formato **structure block**: NBT comprimido con gzip,
con los tags `DataVersion`, `size`, `palette`, `blocks` (con `pos`/`state`/`nbt`).
Se ensamblan en el mundo por *jigsaw* (bloques `minecraft:jigsaw` + *template pools*).

### Dungeons and Taverns v5.3.2.zip (20.724.798 B)
- 7.928 entradas: `data/` 7.833 · `assets/` 92 · `pack.png` · `pack.mcmeta` · `credits.txt`
- Extensiones: **.nbt 5.072** · .json 2.194 · .mcfunction 59 · .png 26 · .mcmeta 1 · .txt 1
- Namespace propio: `data/nova_structures/structure/...` (además reutiliza `minecraft`)
- Colecciones de estructura (por carpeta): `tavern` 300, `illager_mansion` 242, `cave_chamber` 141,
  `lone_citadel` 125, `ruin_town` 107, `bunker` 102, `pale_residence` 86, `sealing_halls` 82,
  `sea_*` 73/58/49, `toxic_lair` 79, `trial_dungeon` 40, `undead_crypt` 22, `catacomb` 30…
- **Trial Dungeons** = `data/nova_structures/structure/trial_dungeon/`: **40 .nbt**
  - `room/` 14 piezas (centro, cruce, curvas, T, puertas, extremos) — cuartos de **11×7×11** (uno de 15×7×11)
  - `deco/` 26 piezas de 5×3×5 … 5×5×5 (mobiliario)
  - Pools: `worldgen/template_pool/trial_dungeon/{start,parts,parts_vault_bias,rooms_1,rooms_2,foundation}.json`
- Definiciones de *trial spawner* (JSON, no bloques): `data/nova_structures/trial_spawner/...`
  (shrine, sealing_halls, toxic_lair, undead_crypt, trident_trials_monument…).

### skyhunters-buried-halls-1.5-mc26.2.zip (586.098 B)
- 150 entradas: **.nbt 100** · .json 48 · .mcmeta 1 · .png 1
- `data/skyhunters_buried_halls/structure/`: **100 .nbt** (namespace `skyhunters_buried_halls`)
- Piezas clave: `arena.nbt` (32×9×32), `arena2.nbt` (32×12×32), `arenacontent2/3.nbt` (20×9×20),
  `start*.nbt` (7×26×7), `tunnelway*.nbt` (9×6×11), `door*.nbt`, `roomdeco*.nbt`, `wall*.nbt`…
- Worldgen: `worldgen/structure/buried_halls*.json`, `worldgen/template_pool/pool*.json`,
  `worldgen/structure_set/spacing.json`; loot tables en `loot_table/...`; **trial_spawner definidos
  en JSON** (`trial_spawner/arena_spawner.json`, `arena_ominous_spawner.json`).

## (b) Camino de conversión a .schem pegable con FAWE

FAWE 2.16.1 (Paper) está instalado. FAWE lee **Sponge Schematic v2/v3** (`.schem`) y también
soporta el formato structure block. Dos caminos:

**Camino A — offline (el que ejecuté).** Convertidor propio `converter.py`:
structure `.nbt` → Sponge `.schem` v2 (gzip + NBT; `Version=2`, `DataVersion`, `Width/Height/Length`,
`Offset`, `PaletteMax`, `Palette` {state→idx}, `BlockData` (varints), `BlockEntities`, `Entities`).
Reglas aplicadas: `minecraft:jigsaw` → aire (no queremos el bloque técnico al pegar),
`minecraft:structure_void` → aire, tipos NBT de block entities preservados exactos.
Se puede pegar con `//schem load <nombre>` + `//paste`.

**Camino B — in-game (recomendado para una versión ENSAMBLADA real).** Instalar el datapack en
un mundo de prueba, `/place structure skyhunters_buried_halls:arena2` (o structure block),
`//copy` y `//schem save`. Captura el resultado tal cual lo genera el jigsaw. Este camino no lo
ejecuté: no levanté el servidor.

**Bonus — ensamblador de laberinto (`assemble_maze` en `converter.py`).** Toma una pieza base,
la tilea en una grilla y **tapa con muro las aberturas que no usa un laberinto perfecto** (DFS
aleatorio). Deja una entrada (borde Oeste de la celda 0,0) y una salida (borde Este de la última).

## (c) Archivos dejados en `minecraft_server\laberintos\`

```
schem\laberinto_menor_DnT_trial_room_cross.schem   452 B   SHA256 82768FBA…95AC4FB
schem\laberinto_mayor_SH_buried_halls_arena.schem 5.169 B  SHA256 552C95D1…5018C35F
schem\laberinto_menor_DnT_5x5.schem                769 B   SHA256 A8062692…438208794
schem\laberinto_mayor_SH_5x5.schem                1.329 B  SHA256 5DBF11D4…6A94A05C
fuente_nbt\laberinto_menor_DnT_trial_dungeon_cross_center.nbt  3.278 B
fuente_nbt\laberinto_mayor_SH_arena2.nbt                      29.220 B
```
- **MENOR (DnT):** base `trial_dungeon_cross_center.nbt` (11×7×11). Crudo + versión laberinto 5×5
  (55×7×55) con muros tapiados (bloque de relleno = `minecraft:mossy_cobblestone`).
- **MAYOR (SH):** base `arena2.nbt` (32×12×32). Crudo (trae puntaje) + versión laberinto 5×5
  (45×6×55) tileada desde `tunnelway3.nbt` (relleno `minecraft:stone_bricks`).

## (d) Utilidad para puntaje ("Dios Demonio")

| Origen | vaults | trial spawners | Trae config de puntaje |
|---|---|---|---|
| `arena2.nbt` (SH, mayor) | **3** | **2** | **Sí**, con loot tables y modos normal/ominous |
| `trial_dungeon_*` (DnT, menor) | 0 | **0** | No — solo bloques `jigsaw` (los spawners los coloca el datapack aparte) |
| `tunnelway3.nbt` (base del laberinto mayor ensamblado) | 0 | 0 | No |

Detalle real del `arena2.nbt` convertido (leído de la salida):
- Vaults: `loot_table = skyhunters_buried_halls:vaults/vaultloot` (2) y `.../ominous_loot` (1, con
  `key_item = minecraft:ominous_trial_key`).
- Trial spawners: `bogged` y `illusioner`/`vindicator`, con `normal_config` y `ominous_config`
  y `items_to_drop_when_ominous`.

**Consecuencia:** el laberinto MAYOR crudo ya puntúa; el MENOR **no**. Para que puntúe hay que:
1. **Agregar** trial spawners y/o vaults (block entities) en las celdas del laberinto menor, o
2. usar como menor el `.schem` ya existente
   `...\plugins\Litematica\schematics\compact_trial_chamber.schem` (110×85×91) que **sí trae
   23 vaults + 25 trial spawners + 26 dispensers + 14 cofres** (verificado con nbtlib).
   Nota: `large_vanilla_trial_chamber.schem` (128×72×128) tiene **0 block entities** → decorativo.

**Aviso de dependencia (importante):** los vault/spawner de SH referencian loot tables del
namespace `skyhunters_buried_halls:`, que existen **solo si el datapack está instalado**. Para
"que no genere libremente" hay dos opciones: instalar el datapack **recortado** (dejar solo
`loot_table/`, `trial_spawner/` y tags; borrar `worldgen/structure*`, `template_pool`, `tags/worldgen`)
o **reescribir** las referencias a loot tables vanilla.

## Estado y límites
- Verificado en disco: contenido de los ZIP, dimensiones y block entities de las piezas,
  validez de los `.schem` (parseo con **nbtlib 2.0.4**, librería independiente, y longitud de
  `BlockData` = Width×Height×Length exacta).
- **NO verificado:** el pegado real en FAWE (no se levantó el servidor) y la licencia de
  redistribución de ambos datapacks (no traen archivo LICENSE; DnT tiene `credits.txt`,
  SH solo `pack.mcmeta`). **[A VERIFICAR]**.

---

# Actualización 2026-10-19 — recuento corregido, dependencias de loot y licencias

Todo lo de esta sección está **medido el 2026-10-19** con las herramientas que se indican.

## (e) El recuento viejo de vaults/spawners estaba mal: `verify_schem.py` subreportaba

`verify_schem.py` cuenta `minecraft:vault` y `minecraft:trial_spawner` con **clave exacta**, pero en el
`.schem` los estados llevan propiedades (`minecraft:vault[ominous=false]`, `minecraft:trial_spawner[ominous=…]`).
Resultado: devolvía **0** y parecía que no había dispositivos de puntaje. Los números de la sección (d) eran
correctos porque salieron de la **fuente** (`.nbt`), no del `.schem`.

Herramienta nueva, de solo lectura: **`inspeccionar_schem.py`** — cuenta por **nombre base** (antes del `[`) y
lista las block entities con sus campos clave (`config`, `key_item`, `loot_table`, `spawn_data`):

```
python inspeccionar_schem.py <archivo.schem> [más archivos…]
python inspeccionar_schem.py --json <archivo.schem>
```

Medido con la herramienta nueva:

| Archivo | dims | bloques no aire | vaults | trial spawners | otros | block entities |
|---|---|---:|---:|---:|---|---:|
| `schem\laberinto_menor_DnT_trial_room_cross.schem` | 11×7×11 | 528 | 0 | 0 | — | 0 |
| `schem\laberinto_menor_DnT_5x5.schem` | 55×7×55 | 13.632 | 0 | 0 | — | 0 |
| `schem\laberinto_mayor_SH_buried_halls_arena.schem` | 32×12×32 | 6.613 | **3** | **2** | 3 cofres, 1 horno, 1 ahumador | 19 |
| `schem\laberinto_mayor_SH_5x5.schem` | 45×6×55 | 9.123 | 0 | 0 | — | 0 |
| `…\Litematica\schematics\compact_trial_chamber.schem` | 110×85×91 | — | **23** | **25** | 26 dispensers, 14 cofres, 8 barriles, 36 macetas decoradas, 1 spawner | 144 |
| `…\Litematica\schematics\large_vanilla_trial_chamber.schem` | 128×72×128 | 194.220 | 9 | 4 | 58 cofres, 58 barriles, 4 hornos, 4 altos hornos, 3 ahumadores | **0** |

**Lo importante:** de los laberintos armados hasta ahora, **el único con dispositivos de puntaje es el arena
crudo**. Los dos "laberinto … 5x5" son corredores tapiados sin nada que dé botín.

## (f) Las dependencias de loot del laberinto mayor quedaron resueltas (datapack mínimo)

El `.schem` del arena referencia **8 ids** del namespace `skyhunters_buried_halls`. Detalle con el campo exacto:

| Dispositivo | Config medida |
|---|---|
| vault ×2 | `config.loot_table = skyhunters_buried_halls:vaults/vaultloot`, `key_item = minecraft:trial_key` |
| vault ×1 | `config.loot_table = skyhunters_buried_halls:vaults/ominous_loot`, `key_item = minecraft:ominous_trial_key` |
| cofre ×3 | `LootTable = …/chests/arena_loot1`, `…/chests/musicloot`, `…/chests/plantloot` |
| trial spawner ×2 | `normal_config`/`ominous_config` con `items_to_drop_when_ominous`, `loot_tables_to_eject`, `spawn_potentials`; `spawn_data.entity.id`: `minecraft:bogged` y `minecraft:illusioner` |
| tablas de spawner | `…/spawners/corridor_loot`, `…/spawners/ominous_arena_loot`, `…/spawners/arena_ominous_hazards` |

Ninguna de esas 8 tablas referencia otra: **el cierre transitivo son las 8 y nada más** (0 sin resolver).

Herramienta nueva: **`armar_loot_minimo.py`** — calcula el cierre desde los schematics a pegar, copia solo esos
`.json`, arrastra el `pack.mcmeta` del propio SH y **rechaza** cualquier cosa de `worldgen/` o `structure/`.

Salida medida: `sh_loot_minimo\` (**10 archivos**: 8 loot tables + `pack.mcmeta` + `pack.png`), sin
`worldgen/` ni `structure/`, `pack_format` **107** con `min_format [107,0]` y `max_format [120,0]` → compatible
con el 26.2 del servidor. Empaquetado: `sh_loot_minimo.zip` — **342.907 B**, SHA256 `D5FA69D2…203CA1F4`,
12 entradas, `pack.mcmeta` en la raíz (como exige Minecraft para un datapack zip).

**Consecuencia:** se puede pegar el arena **sin instalar el worldgen de SH** en el mundo de producción. Los
cofres, los vaults y los spawners van a funcionar. Si se suman más piezas de SH, se regenera con
`python armar_loot_minimo.py <piezas…>`.

## (g) Las piezas son tileables (rugosidad del borde, medida)

`converter.border_openings()` da cuántas posiciones de aire hay en cada borde de la pieza:

| Pieza | dims | N | S | W | E |
|---|---|---:|---:|---:|---:|
| `arena2` (SH, **el que tiene puntaje**) | 32×12×32 | 179 | 179 | 186 | 186 |
| `trial_dungeon_cross_center` (DnT) | 11×7×11 | 9 | 9 | 9 | 9 |
| `tunnelway3` (SH, base del 5×5 actual) | 9×6×11 | 34 | 34 | 43 | 43 |
| `tunnelway5d` (SH, **solo S**) | 9×6×11 | 0 | 3 | 0 | 0 |

El arena está abierto en los **cuatro** lados: se puede tilear (a diferencia del 5×5 viejo, que se armó con
`tunnelway3` y por eso quedó sin puntaje). Las `tunnelway*d` (abiertas solo al Sur) sirven como **callejones
sin salida**.

## (h) Licencias — qué se resolvió y qué no

| Origen | Estado |
|---|---|
| `large_vanilla_trial_chamber.schem` | El `README.txt` de esa carpeta documenta `ultimate-trial-chamber-survival-base.schem` (schemcraft.com, autor **BuildSchem**) con licencia **CC BY 4.0** (uso libre, **incluso comercial**, **con atribución**). El README declara **128×72×128 / 194.220 bloques** y lo medido en el archivo presente es **exactamente 128×72×128 / 194.220 bloques** → es casi seguro ese build **renombrado**. El ZIP original **no está en disco**. **[A VERIFICAR]** que sea el mismo archivo (el nombre no coincide) |
| `compact_trial_chamber.schem` | **Sin README, sin ZIP de origen** → licencia desconocida. **[A VERIFICAR]** |
| `skyhunters-buried-halls-1.5-mc26.2.zip` | Solo trae `pack.mcmeta`, `pack.png` y `data/` — **sin LICENSE ni README**. Autor declarado en el pack: *"by Skyhunter"*. **[A VERIFICAR]** |
| `Dungeons and Taverns v5.3.2.zip` | Tiene `credits.txt` (nombres), **sin archivo LICENSE**. **[A VERIFICAR]** |

**Aviso medido:** `large_vanilla_trial_chamber.schem` tiene **0 block entities** con 58 cofres, 58 barriles,
9 vaults y 4 trial spawners en la paleta → al pegarse, esos dispositivos **no van a dar botín ni a invocar
nada** (les falta el dato de la block entity). No hay en disco ningún script que explique qué las quitó ni
copia original para comparar. **[A VERIFICAR]** la causa.

## (i) Pendiente de laberintos (2026-10-19)

1. **El pegado real en FAWE sigue sin probar** y no se puede probar sin un jugador conectado: los comandos de
   WorldEdit/FAWE necesitan un *actor* con posición (`//paste` no se puede invocar desde la consola), y la
   consola de Paper 26.2 por stdin no-terminal ya está documentada como defectuosa
   (`NullPointerException: …CommandSourceStack.getLevel() is null`, incluso `/stop`) en `AGENTS.md`.
   → El test requiere **conectar un cliente** (Java o Bedrock) o **habilitar RCON**. Es el único paso que
   convierte estos archivos en "laberinto probado".
2. **Definir cuál es el "menor":** el candidato autocontenido (sin datapack) es `compact_trial_chamber.schem`
   — 110×85×91, 23 vaults, 25 spawners, **todo con loot tables vanilla**
   (`minecraft:chests/trial_chambers/reward_ominous` medido) —, pero por tamaño y cantidad de dispositivos se
   parece más a un "mayor" que a las 1–8 salas del menor.
3. El **menor de DnT no puntúa**: sus piezas no traen spawners (el datapack los coloca aparte). Para que puntúe
   hay que **inyectar** block entities de vault/trial spawner en las celdas.
4. Los dos `… 5x5` actuales no sirven como laberinto jugable: son corredores vacíos.

## (j) VEREDICTO medido: las piezas de SH no son celdas de laberinto

Esto se midió después de intentar armar el mayor desde el arena, y **invalida** la idea de
"tilear el arena". El método: flood fill 3D sobre el aire de la pieza, restringido a su caja
(herramientas `converter.count_components_aire` / `componente_aire`).

| Pieza | dims | componentes de aire | mayor | veredicto |
|---|---|---:|---:|---|
| `trial_dungeon_cross_center` (DnT) | 11×7×11 | **1** | 319/319 (100 %) | **celda válida**, abierta en los 4 lados |
| `trial_dungeon_center` (DnT) | 11×7×11 | 1 | 289/289 | válida, abierta solo al Oeste |
| `trial_dungeon_end` (DnT) | 11×7×11 | 1 | 290/290 | válida, abierta al Oeste |
| `trial_dungeon_curve_center` (DnT) | 11×7×11 | 1 | 299/299 | válida, abierta Oeste+Norte |
| `arena2` (SH, **la que tiene puntaje**) | 32×12×32 | **12** | 3.147 de 5.675 (55 %) | **NO es celda** |
| `tunnelway3` (SH) | 9×6×11 | **5** | 153 de 225 (68 %) | **NO es celda** |
| `tunnelway5` (SH) | 9×6×11 | **5** | 189 de 369 | **NO es celda** |
| `tunnelway7` (SH) | 9×6×11 | **5** | 149/254 | **NO es celda** |

**Por qué:** las piezas de SH están modeladas como estructuras de *jigsaw* (se conectan por
bloques `minecraft:jigsaw` y por el volumen de arriba), no como corredores de boca a boca. Su
"aire" mayor es el **volumen que envuelve la pieza** (techo abierto: en `arena2`, **y=0 es aire
en todo el borde**) y el "borde" que detecta `border_openings` es aire de arriba, no una boca.
Verificado leyendo el mapa ASCII por capa: en `arena2` el piso es **y=1**, el primer nivel
caminable es y=2 y no hay ninguna pared de borde que abra a un pasillo.

**Consecuencia sobre los schematics que ya existían** (medido igual):

| Archivo | componentes de aire | mayor | veredicto |
|---|---:|---:|---|
| `laberinto_menor_DnT_5x5.schem` | 33 | 7.495/7.543 = **99,4 %** | **transitable** |
| `laberinto_mayor_SH_5x5.schem` | **53** | 3.399/5.727 = 59,4 % | **NO transitable**: quedó partido en 53 pedazos |

O sea: **el "laberinto mayor" nunca fue un laberinto**, y el arena —la única pieza con vaults y
trial spawners— **no se puede tilear con esta técnica**. No es un bug del ensamblador: es que la
pieza no es una celda.

## (k) Lo que quedó armado y verificado hoy

`armar_laberintos.py` ensambla el menor tileando la celda que **sí** es válida
(`trial_dungeon_cross_center`), y verifica el resultado con flood fill:

| Salida | dims | aire | componentes | mayor | alcanzable desde la boca Oeste |
|---|---|---:|---:|---:|---:|
| `schem\laberinto_menor_DnT_arena_3x3.schem` | **33×7×33** | 2.727 | 13 | 2.711 (**99,41 %**) | **2.711 (99,41 %)** |

Los 13 componentes son los 16 voxeles de aire suelto del piso de la celda (pozos de 1 bloque):
el laberinto es **un solo espacio transitable**. El ensamblador viejo (`assemble_maze`) sirve
—lo que no servía era la pieza de SH—.

**Lo que este laberinto NO tiene:** dispositivos de puntaje (0 vaults, 0 trial spawners). La
pieza de DnT no los trae: el datapack los coloca aparte con jigsaw. Para que puntúe hay que
**inyectar** block entities de vault/trial spawner, y eso está pendiente (ver (i)).

### Artefacto descartado (se conserva como evidencia)

`armar_laberinto_mayor.py` + `schem\laberinto_mayor_SH_arena_3x3.schem` (96×12×96, 7.695 B)
fueron el intento de tilear el arena con **sello por celda** (tapar todas las aberturas de cada
copia y reabrir solo los pasos del laberinto). Se evaluó y **no se usa**: al sellar las caras
incluyendo el "borde" del techo abierto, el resultado quedó en **349 componentes** y solo **7
voxeles** alcanzables desde la boca (0,01 %) — es decir, peor que el original. El script y el
archivo se conservan porque documentan el intento y porque las funciones nuevas que aporta
(`count_components_aire`, `componente_aire`, `tapar_borde`, `leer_schem`) quedaron en
`converter.py` / `armar_laberinto_mayor.py` y las usa `armar_laberintos.py`. **No pegar este
`.schem`.**

## (l) Pendiente actualizado

1. **El pegado real en FAWE sigue sin probar** y necesita un jugador conectado o RCON
   (`//paste` exige un actor con posición; la consola de Paper 26.2 por stdin no-terminal ya
   está documentada como defectuosa en `AGENTS.md`).
   **Actualizado 2026-10-22:** esto vale para el **servidor local**. En el **server oficial**
   sí se pegó: las 10 trial chambers secundarias **SC1–SC10 están colocadas** según el usuario
   (*no verificable desde este repo*: los `.log` de texto no registran `//paste`). Eso no
   valida el pegado de las piezas de laberinto de abajo.
2. **Elegir la fuente del menor.** El candidato autocontenido y *sin dependencias* sigue siendo
   `compact_trial_chamber.schem` (110×85×91, **23 vaults + 25 spawners, todo con loot tables
   vanilla**), aunque por tamaño se parece más a un mayor que a las "1–8 salas" del menor.
   Alternativa: el `laberinto_menor_DnT_arena_3x3.schem` de arriba (33×7×33, transitable, sin
   puntaje) + spawners inyectados.
3. **El mayor no tiene fuente tileable hoy.** Opciones medidas posibles:
   a) usar `compact_trial_chamber.schem` y `large_vanilla_trial_chamber.schem` como salas
      sueltas (con `//paste` en posiciones elegidas), no como grilla tileada;
   b) **arreglar las piezas de SH** (tapar el techo abierto y abrir bocas reales en los bordes)
      antes de tilear — es trabajo de edición de NBT, factible pero no hecho;
   c) cambiar de fuente a un datapack cuyas piezas sí sean corredores.
4. `large_vanilla_trial_chamber.schem` tiene **0 block entities** con 58 cofres y 9 vaults en
   la paleta → sus dispositivos no van a funcionar. **[A VERIFICAR]** la causa.
5. Licencias: solo `large_vanilla_trial_chamber.schem` tiene licencia identificada (CC BY 4.0,
   con atribución, ver (h)). El resto sigue **[A VERIFICAR]**.

## Roxy village → .schem (2026-10-20)

`Roxy village fixed.zip` era un mundo guardado, no un paquete de schematics; se convirtió a
**103 `.schem`, 25,47 MB**, en `MT_Server_Base_26.2\plugins\Litematica\schematics\roxy_village\`
(con copia en `server-test\...\roxy_village\`), un archivo por región de 512×512
(`roxy_r.<rx>.<rz>.schem`, Y de origen −64). Se recorrieron las 106 regiones del bbox
X −1234..1233, Z −6065..2835: 103 con contenido y 3 vacías (r.2.-9, r.2.-8, r.2.3; 0 vóxeles,
no son errores). Se extrajo **sin filtro**: `--solo-construccion` no sirve acá porque los
materiales de terreno de WorldPainter no están en la tabla NATURAL (medido 28,5 M → 6,7 M
bloques en r.-1.-3). Los `.schem` **sí incluyen block entities** (ej. r.-1.-3 con 7.057 BE).
Herramientas: `laberintos\extraer_mundo_regiones.py` (orquesta región por región en proceso
hijo; soporta `--bbox`, `--y-min/--y-max`, `--solo`, `--dry-run`) y `mundo_a_schem.py`
reescrito con bytearray plano (~500 MB pico vs 5,3 GB del dict anterior) e inclusión de BE.

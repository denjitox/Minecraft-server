# 10 — Contrato de implementación: Trial Chamber + jefe Hitogami + portal al End

Documento de trabajo del bloque de la trial chamber. **Complementa** `DISENO_TRIAL_CHAMBER_JEFE.md`
(el diseño) y `00_PLAN_MUSHOKUCORE.md` (el plan). Si hay contradicción, manda este contrato para
**nombres, rutas y sintaxis**, y el diseño para **gameplay** (valores de fases, vida, tiempos).

Creado 2026-10-18 por el integrador, tras el digest del diseño y dos sondeos reales sobre
Paper 26.2 (logs `prueba_mushokucore_20261018_0127*_nbtprobe*.log`).

Regla dura de todo este bloque (AGENTS.md regla 1): **no inventar sintaxis**. Si un comando no está
medido, se escribe en el `.mcfunction` con un comentario `# [A VERIFICAR: ...]` y se reporta. Nunca
se asume que una sintaxis "debería funcionar".

---

## 1. Base de archivos y propiedad exclusiva

**Única base de escritura:** `C:\Dev\OPENCLAUDIO\minecraft_server\MT_Server_Base_26.2\datapacks\mt_core\`

Esa es la fuente autoritativa (paquete del servidor). El integrador copia a `servidor\datapacks\mt_core`
y regenera `mt_core_26.2.zip` + `SHA256SUMS.txt` (AGENTS.md regla 6). **Los agentes NO escriben en
`servidor\`** (evita dos verdades): escriben solo en la base de arriba.

| Agente | Rutas que le pertenecen (y solo esas) |
|---|---|
| **TC-1** (núcleo del jefe) | `data\mt\function\tick.mcfunction`, `data\mt\function\boss\*.mcfunction`, `data\mt\function\prueba\*.mcfunction`, `data\minecraft\tags\function\tick.json`, `data\minecraft\tags\function\load.json`, y las 3 líneas de objetivos nuevos en `data\mt\function\load.mcfunction` |
| **TC-2** (puerta, sellos, botín) | `data\mt\function\puerta\*.mcfunction`, `data\mt\function\sellos\*.mcfunction`, `data\mt\advancement\sello_*.json`, `data\mt\loot_table\boss\hitogami.json`, `data\mt\loot_table\sellos\*.json` |
| **TC-3** (Skript) | `MT_Server_Base_26.2\plugins\Skript\scripts\hitogami.sk` (archivo nuevo) |

Nadie toca lo ajeno. Si algo de otra ruta hace falta, se reporta al integrador.

**No tocar** (existen y se conservan tal cual): `data\mt\function\load.mcfunction` salvo las 3 líneas de
objetivos, `data\mt\function\setup.mcfunction`, `stage_launch.mcfunction`, `stage_dimensions_open.mcfunction`
(está **huérfana**: nadie la llama — medido 2026-10-18 — y no se borra), `data\mt\function\relics\*`,
`data\mt\advancement\trial_reward*.json`, `potencias.sk`, `enfermedades.sk`, `features.sk`, `config.sk`.

---

## 2. Hechos MEDIDOS sobre Paper 26.2 (2026-10-18, servidor local)

Fuente: `%TEMP%\opencode\prueba_mushokucore_20261018_012741_nbtprobe.log`,
`..._012902_nbtprobe2.log`.

| Hecho | Evidencia literal |
|---|---|
| La consola por stdin **sí** tiene contexto de mundo (el NPE viejo de AGENTS.md no se reprodujo) | `Changed the block at 8, 200, 8` |
| **Sin jugador conectado los chunks NO están cargados**; hay que forzarlos | `That position is not loaded` → con `forceload add 0 0 32 32` el mismo `setblock` funcionó |
| La clave de atributo es **`minecraft:max_health`**; `minecraft:generic.max_health` **no existe** en 26.2 | `Unknown registry key in ResourceKey[minecraft:root / minecraft:attribute]: minecraft:generic.max_health` |
| Atributos del ravager vanilla: `max_health 100`, `attack_damage 12`, `movement_speed 0.3`, `follow_range 32`, `knockback_resistance 0.75`, `attack_knockback 1.5`, `armor 0`, `armor_toughness 0`, `jump_strength 0.42`, `scale 1.0` | `data get entity` completo |
| El campo `Health` existe y `data get` lo lee | `Health: 10.0f` (el ravager se cayó de y=200 y se lastimó) |
| **LevelledMobs interfiere con el jefe**: al invocar por comando le puso un modificador | `attributes: [{base: 100.0d, id:"minecraft:max_health", modifiers:[{operation:"add_value", amount:45.0d, id:"minecraft:max_health"}]}]` y `BukkitValues: {levelledmobs:level: 9, levelledmobs:spawnreason:"COMMAND"}` |
| Bossbar con id con namespace funciona, con duración persistente | `bossbar add mt:jefe "Hitogami"` → `Created custom bossbar [Hitogami]`; `color purple` / `style notched_10` / `max 400` OK |
| Crear objetivos funciona | `scoreboard objectives add mt_fight dummy` → `Created new objective [mt_fight]` |
| NBT por defecto de un `trial_spawner` en 26.2 (bloque) | `{z:10, spawn_data:{entity:{}}, id:"minecraft:trial_spawner", y:200, x:10, components:{}}` — **no** hay `normal_config`/`ominous_config` en el bloque |

### 2.1 Pendiente de medición (NO inventar hasta que esté acá)

Estos puntos los mide el integrador y actualiza este documento antes de que se escriba el `summon`
del jefe. Mientras tanto valen las marcas `[PENDIENTE]`:

- `[PENDIENTE]` Si `attributes:[{id:"minecraft:max_health",base:400}]` en el `summon` **queda aplicado**
  o no (en el sondeo 2 el ravager terminó con `base: 100.0d`).
- `[PENDIENTE]` Si existe el comando `/attribute` y su sintaxis exacta para las fases.
- `[PENDIENTE]` Si `data modify entity <target> Health set value <f>` funciona.

---

## 3. Interfaz congelada

### 3.1 Objetivos (`dummy`, score del jugador/global ficticio `global`)

| Objetivo | Valores | Quién lo crea | Para qué |
|---|---|---|---|
| `mt_stage` | 0/1/2 | **ya existe** (`load.mcfunction`) | etapa informativa heredada |
| `mt_roll` | — | **ya existe** (`load.mcfunction`) | contraseña de tirada de reliquias |
| `mt_fight` | 0 = libre, 1 = pelea activa, 2 = derrota pendiente de procesar | TC-1 en `load` | máquina de estados del jefe |
| `mt_prep` | 0/1 | TC-1 en `load` | cuenta atrás en curso |
| `mt_end` | 0 = portal sellado, 1 = portal abierto | TC-1 en `load` | **nunca se vuelve a 0** (ni con reset) |
| `mt_seal` | 0..3 | TC-1 en `load` | sellos completados (lo incrementa TC-2) |
| `mt_timer` | ticks (arranca 200) | TC-1 en `load` | cuenta atrás |
| `mt_calc` | auxiliar | TC-1 en `load` | cálculos (% de vida) |
| `mt_boss_first_kill` | 0/1 | TC-1 en `load` | primera caída mundial (lo lee/escribe TC-3) |

Patrón obligatorio para crearlos sin ensuciar el log en cada `/reload`:

```
execute unless score global mt_fight matches -2147483648..2147483647 run scoreboard objectives add mt_fight dummy
```

(Las dos líneas viejas de `load.mcfunction` NO usan ese patrón; se dejan como están y se reporta.)

### 3.2 Bossbar

- id: `mt:jefe`, nombre `Hitogami`, `color purple`, `style notched_10`, `max 400`.
- Se crea en `mt:load` (TC-1). Si ya existe, el `bossbar add` vuelve a error por log: `[A VERIFICAR]`
  si conviene guardarlo con `execute store result ... bossbar ...` o tolerar el error.

### 3.3 Funciones — contrato de nombres

Nadie inventa nombres nuevos. Los que se llaman entre agentes están acá y son **obligatorios**:

| Función | La llama | Garantiza |
|---|---|---|
| `mt:tick` | `tick.json` (TC-1) | delega en `mt:boss/tick` |
| `mt:boss/reset` | `load.json` (TC-1) | mata al jefe y a los adds; deja `mt_fight=0`, `mt_prep=0`; **no** toca `mt_end` |
| `mt:boss/tick` | `mt:tick` | rutea por `mt_fight`/`mt_prep` |
| `mt:boss/preparar`, `cuenta_atras`, `iniciar`, `combate`, `barra`, `fase_1`, `fase_2`, `fase_3`, `_fangs`, `detectar_fin`, `derrotar`, `limpiar`, `estado` | TC-1 (internas) | ver §8 del diseño |
| `mt:puerta/vano_cerrar` | TC-1 (`preparar`) | rellena el vano con `minecraft:barrier` |
| `mt:puerta/vano_abrir` | TC-1 (`derrotar` vía desbloquear / `limpiar`) | saca el `barrier` |
| `mt:puerta/desbloquear` | **TC-1** (desde `mt:boss/derrotar`) | `execute if score global mt_end matches 0 run function mt:puerta/_desbloquear_real` |
| `mt:puerta/_desbloquear_real` | TC-2 (interna) | abre el vano, pone el `end_portal`, `mt_end=1` |
| `mt:sellos/sello_1`, `sello_2`, `sello_3` | TC-2 (desde los advancements) | `add global mt_seal 1` + aviso; al llegar a 3 llama `mt:sellos/abrir_arena` |
| `mt:sellos/abrir_arena` | TC-2 | aviso de arena abierta (el portón lo abre la estructura) |
| `mt:prueba/combate`, `mt:prueba/terminar`, `mt:prueba/estado` | jugador (op) | modo ensayo: fuerza `mt:boss/preparar`; `terminar` fuerza la derrota; `estado` informa |

### 3.4 Tags de entidad

`mt_boss` (el jefe), `mt_add` (invocados de las fases), `mt_add_protegido` (`[A VERIFICAR]`: el diseño
lo lista pero no define uso — TC-1 decide y reporta).

### 3.5 Coordenadas (del diseño §3 — no reinterpretar)

| Elemento | Coordenadas |
|---|---|
| Centro/piso de arena (ancla de paste) | `X=-2304, Y=-16, Z=-3008` |
| Volumen de la arena (interior 48×48×40) | `X [-2328,-2281]`, `Y [-16,+23]`, `Z [-3031,-2984]` |
| Vano del portón (plano `z=-2984`) | `X [-2312,-2297]`, `Y [-15,-11]` |
| Marco del portal (centro) | `(-2304, -43, -2970)`; hueco 3×3 `Y [-42,-40]`, `X [-2305,-2303]`, `Z [-2971,-2969]` |
| Detección de entrada (diseño §8) | `@a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64]` |

### 3.6 Nombre del jefe

**Aprobado por el usuario el 2026-10-18: el jefe se llama `Hitogami`.** El diseño lo escribe
"Hitogami, el Dios Humano": el `CustomName` va con exactamente `Hitogami` y el subtítulo, si se usa,
queda `[A VERIFICAR]` (no inventar el título).

---

## 4. Reglas de código

1. **Un comando por línea**; comentarios con `#`. Sin `\t` ni líneas de más de 200 caracteres.
2. `summon` del jefe: usar la forma **medida** (§2.1). Los nombres CamelCase del diseño (`Health`,
   `CustomName`, `Tags`) se conservan **solo** si están medidos; si no, `# [A VERIFICAR]`.
3. Los adds de las fases van **sin** `PersistenceRequired` salvo que el diseño lo pida, y **con** tag
   `mt_add`.
4. Los JSON (advancements, loot tables) se escriben **válidos y compactos**, UTF-8 sin BOM. Un JSON
   mal formado rompe la carga del datapack entero (`/reload` lo reporta).
5. No se toca nada fuera de §1. No se borra nada.
6. Todo dato que se afirme en el reporte tiene que estar **verificado en disco en ese turno**
   (contar líneas, leer el archivo). Si no se verificó: "no verificado".

---

## 5. Entregable y reporte de cada agente

Además de los archivos, un reporte corto con:

- Lista de archivos escritos, con ruta y tamaño.
- Qué se verificó en disco en ese turno.
- Qué quedó bajo `# [A VERIFICAR: ...]` y por qué (comando no medido, decisión de diseño faltante).
- Cualquier contradicción detectada contra `DISENO_TRIAL_CHAMBER_JEFE.md`.

El integrador (Claudio) corre después: copia a `servidor\datapacks\`, `/reload`, `/function mt:prueba/*`
y `mt:boss/estado`, y recién ahí el bloque se considera probado.

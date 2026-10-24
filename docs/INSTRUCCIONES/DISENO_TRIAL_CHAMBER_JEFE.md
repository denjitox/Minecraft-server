# Diseño de la Trial Chamber + Jefe final (Hitogami) — Portal al End

> **Estado de este documento.** Reconstruido el 2026-10-17 desde el historial de
> sesiones de opencode. El archivo **nunca existió en disco**: el diseño quedó sólo
> en el chat de la sesión hija `ses_eb85516f1ffejxdWSrIJnS07DQ` ("Diseño prueba del jefe",
> 2026-10-16 23:22–23:47) y las decisiones se aprobaron en la sesión principal
> `ses_ec34de8a2ffeGSmmea47uEeNc8` el 2026-10-17 03:15–03:17.
>
> **Verificado en disco el 2026-10-17:** en ese momento **nada de esto estaba implementado.**
> No existían `data/mt/function/boss|puerta|sellos|prueba/`, ni `hitogami.sk`, ni `tick.json`.
> Este documento es el **diseño + spec de implementación**, no un informe de algo hecho.
>
> **⚠️ Actualizado 2026-10-22 — la parte de datapack/Skript YA ESTÁ EN DISCO.** Medido con
> `Get-ChildItem` en este turno, en `MT_Server_Base_26.2\datapacks\mt_core\data\mt\function\`:
> `boss\*` (15), `puerta\*` (4), `sellos\*` (4), `prueba\*` (3) y `tick.mcfunction`.
> `MT_Server_Base_26.2\plugins\Skript\scripts\hitogami.sk` existe (6.912 B). Copia espejo en
> `servidor\world\datapacks\mt_core\data\mt\function\` (mismo listado).
> **✅ ACTUALIZACIÓN 2026-10-22 (medido sobre los chunks del mundo vivo).** Se decodificaron los voxeles
> de las regiones del backup `BACKUP MUNDO 22.10 16.23hs` (TAR de 14,7 GB, 1.946 regiones):
>
> - **La trial chamber principal YA ESTÁ PEGADA**, y las **10 secundarias también** (antes esto era
>   "reportado por el usuario, no verificable"). Cada una se detecta por sus bloques firma
>   (`trial_spawner`, `vault`, `tuff_bricks`, `chiseled_tuff`, `copper_grate`, `copper_bulb`).
> - **Caja real de la principal:** `X[−2293,−2188] Y[−12,52] Z[−2994,−2883]`, centro **`−2241 20 −2939`**
>   (región `r.-5.-6`). Es la única con `copper_grate`/`copper_bulb` **sin encerar** y sin `chiseled_tuff`
>   → corresponde a `large_vanilla_trial_chamber.schem`, no a `compact_trial_chamber`.
> - **El punto del diseño `(−2304, −16, −3008)` NO es el centro de la cámara:** cae 11 bloques al oeste y
>   14 al norte de la caja. Es el origen de pegado.
> - **Lo que NO está:** en `r.-5.-6` no hay **ni un** bloque `barrier`, `end_portal`, `reinforced_deepslate`
>   ni `mossy_stone_bricks`. O sea que **la arena de Hitogami y el portal al End del diseño no están
>   construidos**: lo pegado es la trial chamber vanilla, no el complejo con la puerta.
> - Tabla completa de las 11 cajas y el detalle del método:
>   `mundo\COMANDOS_NATURALIZACION_RIKARISU_v2_VERIFICADO_2026-10-22.md`, sección **A.1**.
>
> ⚠️ **ACTUALIZACIÓN 2026-10-17 (posterior a esta reconstrucción).** El usuario confirmó el
> **End permitido**, y `allow-end: true` **YA ESTÁ APLICADO EN DISCO** — verificado:
> `MT_Server_Base_26.2\configuracion\bukkit.yml.fragmento-inicial`, su copia en
> `ENTREGA_Loding_20261017\03_configuracion\` y `servidor\bukkit.yml` (los tres en `true`).
> *(`[A VERIFICAR]` 2026-10-22: la copia de la entrega ya no existe en disco; hoy son **dos** copias comparables, `MT_Server_Base_26.2\configuracion\` y `servidor\`.)*
> También `level-name=world` en `configuracion\server.properties.inicial`, y quedó
> confirmado que debe haber **UN SOLO portal**, bloqueado hasta completar los 3 sellos.
> **Consecuencia:** toda afirmación de este documento que diga que `allow-end` está en
> `false`, "no aplicado" o "no está en disco" es un **snapshot anterior** a ese cambio y
> **NO debe tomarse como el estado actual**. El diagnóstico de datapack/Skript (`boss`,
> `puerta`, `sellos`, `prueba`, `tick` y `hitogami.sk`) **también quedó viejo**: el 2026-10-22
> se verificó que esos archivos **existen en disco** (ver bloque del 2026-10-22 más arriba). Lo único de ese
> diagnóstico que sigue vigente es que la **estructura** (arena + portal) no está colocada.
>
> Los datos marcados **[A VERIFICAR]** se reconstruyeron por inferencia o quedaron
> ambiguos/inconsistentes en el historial: hay que confirmarlos antes de construir.

---

## 1. Concepto

Prueba tipo *trial chamber* **subterránea y oculta** dentro de una cordillera. El jugador
tiene que **derrotar a un jefe difícil** para que se habilite el **portal al End**. Hasta
ese momento el portal **no existe como bloque**: el hueco está sellado y el End queda
inaccesible.

En este mundo **no hay estructuras vanilla** (`generate-structures=false` no se aplica a un
mundo importado → cero strongholds, minas, aldeas o trial chambers vanilla). Sin stronghold
no hay portal al End vanilla: **hay que construirlo a mano**.

**Nombre del jefe:** **"Hitogami, el Dios Humano"** *(nombre elegido en la sesión de diseño;
no hay constancia de que el usuario lo haya aprobado explícitamente — ver §9)*.

---

## 2. Resumen ejecutivo del diseño original

Texto textual de la sesión de diseño (2026-10-16 23:42:44):

> Se diseña una dungeon tipo trial chamber subterránea oculta en una cordillera, donde el
> jugador debe derrotar a un jefe difícil («Hitogami, el Dios Humano»). Solo al morir el jefe
> se activa el portal al End.
>
> **Decisión clave:** se fusiona con la Fortaleza End (`FORTALEZA_END_DISENO.md`). Se consideran
> los tres sellos (Trial Spawners) como el descenso/antecomplejo (la «prueba»), pero el
> **portal** queda exclusivamente bloqueado por la muerte del jefe. Esto resuelve la
> contradicción entre el documento (sellos → desbloquean portal) y el pedido del usuario
> (jefe → desbloquea portal).

---

## 3. Ubicación / coordenadas

**Confirmado en el historial** (informe de diseño §3 y resúmenes de la sesión principal).
La coordenada que circuló, **`(-2304, -16, -3008)`**, es el **ancla de paste** (piso, centro
de la arena) y **está confirmada** — no es [A VERIFICAR].

| Elemento | Coordenadas documentadas |
|---|---|
| **Ancla de paste (piso/centro arena)** | `X=-2304, Y=-16, Z=-3008` |
| Centro de arena | `(-2304, -16, -3007.5)` |
| Boca de caverna (entrada) | `X=-2304, Y=240, Z=-3175` **[A VERIFICAR]** (el Y no cierra con la superficie medida, ver abajo) |
| Arena (interior 48×48×40) | `X ∈ [-2328,-2281]`, `Y ∈ [-16,+23]`, `Z ∈ [-3031,-2984]` |
| Portón cámara-puerta (vano) | plano `z=-2984`, vano `X ∈ [-2312,-2297]`, `Y ∈ [-15,-11]` |
| Cámara del portal (23×23×13) | `X ∈ [-2315,-2293]`, `Y ∈ [-44,-31]`, `Z ∈ [-2980,-2958]` |
| Marco del portal (5×5) | centro `(-2304, -43, -2970)`; hueco 3×3 en `Y=-42..-40`, `X ∈ [-2305,-2303]`, `Z ∈ [-2971,-2969]` |
| Tres salas de sello (Trial Spawners) | `X ∈ [-2328,-2256]`, `Y ∈ [56,76]`, `Z ∈ [-3040,-3100]` |

**Terreno (verificado con las herramientas del proyecto sobre el kit congelado):**
bioma **`windswept_hills` (valor 3)**, **100% de los píxeles** de la huella del complejo.
Superficie en la huella entre **y≈231,7 y y≈368,2** (o sea, montaña sólida). Altura en el
punto exacto `(-2304,-3072)`: **y≈347,4**. Distancia al spawn **≈3789–3840 bloques**, sin
cruzar océano (el camino va `plains → forest → windswept_hills`). Todo dentro del borde
(`21800` → límite ±10900).

**Inconsistencias de altura marcadas [A VERIFICAR]:**
- La "boca de caverna" figura a `Y=240` pero la superficie ahí es ≈347: hay **107 bloques** de
  diferencia. O la boca va sobre la superficie (≈347) o es un túnel ya enterrado.
- El informe habla de un "corredor descendente (~240→64)" pero la arena está en `Y=-16..+23`
  (no en 64). El tramo exacto de galerías **no quedó definido**.

---

## 4. Secuencia de la prueba

**3 sellos → abrir arena → invocar → combatir → derrotar → cruzar el portal.**

1. **Acceso.** Boca de caverna oculta en la ladera de la montaña → corredor en descenso hasta
   las salas de los sellos.
2. **Tres sellos (Trial Spawners) — OBLIGATORIOS.** Completar cada sello (abrir el cofre de su
   Trial Spawner) incrementa `mt_seal` y otorga el loot `mt:sellos/sello_N`. **Al llegar a
   `mt_seal == 3`** se libera el **portón de la arena** (`mt:sellos/abrir_arena`). **Nunca** se
   abre la arena sin pasar por los tres.
3. **Arena «Trono del Dios Humano».** Al entrar un jugador a la zona de pelea, arranca una
   **cuenta atrás de 10 s** (`mt_prep=1`, `mt_timer=200` ticks) con título y sonido; el vano se
   vuelve a cerrar con `barrier`.
4. **Invocación.** Al terminar la cuenta, `mt:boss/iniciar` invoca a **Hitogami** (ravager) y
   pasa a `mt_fight=1`. Se cuenta a los jugadores presentes para escalar la vida.
5. **Combate por fases** (ver §5).
6. **Derrota.** Al morir el jefe (o al quedar `mt_fight=1` sin `mt_boss`), `mt:boss/detectar_fin`
   pone `mt_fight=2` y `mt:boss/derrotar` llama a `mt:puerta/desbloquear`:
   - quita el `barrier` del vano,
   - reemplaza el sello 3×3 (`reinforced_deepslate`) por `minecraft:end_portal`,
   - setea `mt_end = 1`,
   - anuncia con título + sonido y **oculta la bossbar**.
7. **Acceso al End.** Recién entonces el jugador puede cruzar el portal.

**Repetibilidad:** el jefe es **re-invocable con cooldown** (ver §7). El portal es **global y
permanente** (`mt_end=1` no se revierte): gana quien lo abra primero; después queda abierto
para todos.

---

## 5. El jefe — Hitogami

- **Nombre:** `"Hitogami, el Dios Humano"`
- **Entidad base:** `minecraft:ravager`
- **Tags:** `mt_boss`, `mt_add_protegido`
- **NBT:** `CustomNameVisible: 1b`, `PersistenceRequired: 1b`, `Silent: 0b`,
  `DeathLootTable: "mt:boss/hitogami"`
- **Atributos base (diseño):** `max_health 400`, `attack_damage 14`, `armor 8`,
  `knockback_resistance 1.0`, `movement_speed 0.30`, `follow_range 48`,
  `attack_knockback 1.0`, `step_height 1.0`.
  IDs modernos sin prefijo `generic.` (`minecraft:max_health`, etc.) — verificado en el jar
  de Paper 26.2.
- **Escalado por cantidad de jugadores en el área al iniciar:** `2 → 300`, `3 → 400`,
  `≥4 → 420` (cap).
- **Sinergia con los adds:** invoca hasta `mt_add` (zombies/skeletons/wither_skeletons)
  **acotados** para no tumbar el server.

### Fases (por `% HP = Health / max_health`)

| Fase | Rango | Nombre | Mecánicas concretas |
|---|---|---|---|
| **1** | 100%–66% | «El Trueno Silencioso» | *Charge* del ravager + **onda de choque** (`wind_charge` + knockback + partículas + sonido). Invoca hasta **3 zombies** (`mt_add`, nombre "Seguidor de Hitogami") cada ~45 s. |
| **2** | 66%–33% | «El Juicio» | Ataques **telegráficos** (partículas 1–2 s antes). **`evoker_fangs`** en la posición de los jugadores cada 6–10 s. **`darkness` 5 s** cada 20 s. Invoca **2 skeletons** ("Verdugo de Hitogami") cada ~40 s (máx 4 adds). |
| **3** | <33% | «El Vacío» | *Enrage*: `movement_speed 0.42`, `strength`/`resistance` I. Invoca **2 wither_skeletons** ("Ejecutor de Hitogami") cada ~30 s (máx 2). **Teletransporte** entre puntos de la arena cada 12 s. Ráfagas de `small_fireball` / `wind_charge` cada 15–20 s. |

**Reglas de la pelea (comunes a las 3 fases):**
- **`max-distance` de la pelea: 40 bloques.** Techo sólido, arena acotada.
- **Grupo:** `mt_fight=1` mientras quede ≥1 participante en el área. Si no queda nadie →
  `mt:boss/limpiar` mata jefe y adds y resetea (`mt_fight=0`, `mt_prep=0`, bossbar oculta).
- **Muerte del grupo:** la pelea se reinicia en la siguiente entrada (no queda a mitad de HP).
- **Reinicio de server mid-fight:** `mt:load` llama `mt:boss/reset`, que **mata `mt_boss` y
  `mt_add`** y resetea los flags → al reentrar la pelea arranca de cero. **No** resetea `mt_end`.
- **Telegrafía:** antes de cada ataque fuerte (20–40 ticks) → sonido + partículas.
- **Bossbar:** `mt:jefe` (Java/Bedrock vía Geyser). **Fallback obligatorio: `actionbar`** con
  `% HP` (ver §6 y §8).

---

## 6. La puerta (portal al End) y el mecanismo anti-trampa

### Estados
- `mt_end = 0` → **sellado**.
- `mt_end = 1` → **portal activo** (inmutable).

### Mientras el jefe vive
- En la **cámara del portal**, el hueco 3×3 tiene **sólo `reinforced_deepslate`** (sello
  decorativo). **No existe ningún bloque `end_portal`.**
- El **vano** que conecta con la arena queda cerrado por una "puerta" de **`barrier`**
  (invisible + indestructible) + frontal de `reinforced_deepslate` (visual).

### Al morir el jefe — `mt:puerta/desbloquear`
1. Remueve el `barrier` del vano (`van_abrir`).
2. Reemplaza cada bloque del hueco 3×3 por `minecraft:end_portal`.
3. `scoreboard players set global mt_end 1`.
4. Oculta la bossbar, anuncia y toca sonido.

### Por qué es a prueba de trampas
- **Sin portal previo:** no hay `end_portal` que exista. `ender_pearl`, `chorus_fruit` o
  creative **no** pueden usar un portal que no existe.
- **Shell sellado:** sin huecos de 1 bloque; techo y paredes sólidos → bloquea aterrizajes de
  ender pearl / chorus fruit dentro de la cámara.
- **Indestructible:** `barrier` es indestructible; `reinforced_deepslate` es muy resistente;
  `end_portal` no se rompe en supervivencia.
- **WorldGuard:** región `prueba_hitogami` con `build: deny` para no-miembros (`entry`/`exit`
  según criterio). Sólo admins con bypass.
- **`allow-end=true` desde el día 1.** El diseño descarta abrir el End por tiempo
  (`mt:stage_dimensions_open` queda obsoleto): la apertura se ata **únicamente** a
  `mt_end=1`. Con `allow-end=true`, al colocar el `end_portal`, Paper genera/usa el End.
  **[A VERIFICAR]** que la generación del End al primer uso funcione en este build.

---

## 7. Atomización / repetibilidad (decisiones APROBADAS por el usuario)

Estos 7 puntos fueron **propuestos por el asistente el 2026-10-17 03:15:46** y **aprobados
por el usuario ese mismo día 03:17:39** ("si claudio, realizalo como lo describiste, mantene
un ojo en el export"). Son vinculantes.

| # | Tema | Decisión aprobada |
|---|---|---|
| 1 | **Recompensa única** | **Al mayor daño (top damage) de la PRIMERA muerte mundial.** Ítem único con lore personalizado (`"Conquistado por <nombre>"`, `"Primera caída de Hitogami"`, fecha). Los demás reciben sólo el loot normal (`mt:boss/hitogami.json`). |
| 2 | **Cooldown / re-pelea** | **Re-invocable con cooldown.** Sólo la PRIMERA muerte entrega el ítem único; cualquier re-invocación posterior **nunca** lo vuelve a dar. El loot table **sí** es repetible. |
| 3 | **Seguridad del End** | **`allow-end=true` desde el día 1**, atado **únicamente** a `mt_end=1`. Descartar `mt:stage_dimensions_open`. Acción: `server.properties` + `bukkit.yml` en `true`. |
| 4 | **Sellos obligatorios** | **Sí.** Los 3 sellos son **obligatorios** para abrir el portón de la arena. |
| 5 | **Colocación** | **`.schem` con FastAsyncWorldEdit** (`//schem load` + `//paste`). Ruta sugerida: `plugins/FastAsyncWorldEdit/schematics/mt_trial_chamber_hitogami_v1.schem`. **NO** sobreescribir `Celestara` ni `perugius` (complejo distinto). |
| 6 | **Skript** | **Sí, pero en `hitogami.sk` SEPARADO. NO tocar `potencias.sk`.** |
| 7 | **Bedrock** | **Mantener el fallback `actionbar`** + **test con cliente Bedrock real** antes de subir al hosting (pendiente). |

**Cooldown — valor exacto [A VERIFICAR]:** la recomendación dice **20–30 minutos**; el informe
de diseño usaba "**ej. 20 min**" como ejemplo y la implementación narrada no fijó un número.
Hay que fijar el valor final.

**Flag de primera muerte:** scoreboard `mt_boss_first_kill` (0/1).

### Nota de honestidad (ya corregida en el diseño)
El *brief* original afirmaba que "la bossbar de Java no existe en Bedrock". El agente de
diseño lo **corrigió leyendo el jar**: Geyser 2.12.0 tiene `JavaBossEventTranslator` y
traduce `BossEvent` de Java a Bedrock. Aun así, por robustez se **conserva el fallback de
`actionbar`** (cuesta 0). **[A VERIFICAR]** el mapeo de color y el apilado de bossbars con
cliente Bedrock real.

---

## 8. Arquitectura del datapack (namespace `mt`, dentro de `mt_core`)

### Árbol de archivos a agregar
```text
data/minecraft/tags/function/
  load.json            <- agregar "mt:boss/reset"  (además de "mt:load")
  tick.json            <- NUEVO: ["mt:tick"]

data/mt/function/
  tick.mcfunction                  # llama mt:boss/tick
  stage_end_open.mcfunction        # (del informe; función de apertura del End)
  boss/
    reset.mcfunction               # llamado desde load
    tick.mcfunction
    preparar.mcfunction
    cuenta_atras.mcfunction
    iniciar.mcfunction
    combate.mcfunction
    barra.mcfunction
    fase_1.mcfunction
    fase_2.mcfunction
    fase_3.mcfunction
    _fangs.mcfunction              # helper de fase 2 (evoker_fangs)
    detectar_fin.mcfunction
    derrotar.mcfunction
    limpiar.mcfunction
    estado.mcfunction               # debug/tellraw
  puerta/
    vano_cerrar.mcfunction
    vano_abrir.mcfunction
    desbloquear.mcfunction
    _desbloquear_real.mcfunction
  sellos/
    sello_1.mcfunction
    sello_2.mcfunction
    sello_3.mcfunction
    abrir_arena.mcfunction
  prueba/
    combate.mcfunction
    terminar.mcfunction
    estado.mcfunction

data/mt/advancement/
  sello_1.json    sello_2.json    sello_3.json

data/mt/loot_table/
  boss/hitogami.json
  sellos/sello_1.json   sello_2.json   sello_3.json
```

### Scoreboard objectives
`mt_stage` (ya existe), y nuevos: `mt_roll`, **`mt_fight`** (0/1/2), **`mt_prep`** (0/1),
**`mt_end`** (0/1), **`mt_seal`** (0–3), `mt_timer`, `mt_calc`, **`mt_boss_first_kill`** (0/1).

### Tags
- `load.json`: `["mt:load", "mt:boss/reset"]`
- `tick.json` (NUEVO): `["mt:tick"]` → sólo `mt:boss/tick`; todo lo demás va gateado por score.

### Funciones críticas (contenido del historial)

**`mt:boss/tick`**
```mcfunction
# Detección de entrada (sin pelea, sin preparación, sin portal ya abierto)
execute if score global mt_fight matches 0 unless score global mt_prep matches 1 unless score global mt_end matches 1 if entity @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64] run function mt:boss/preparar
execute if score global mt_prep matches 1 run function mt:boss/cuenta_atras
execute if score global mt_fight matches 1 run function mt:boss/combate
execute if score global mt_fight matches 2 run function mt:boss/derrotar
```
> El volumen de arena es `x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64`.

**`mt:boss/preparar`**
```mcfunction
scoreboard players set global mt_prep 1
scoreboard players set global mt_timer 200
function mt:puerta/vano_cerrar
bossbar set mt:jefe players @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64]
bossbar set mt:jefe visible true
title @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64] title {"text":"HITOGAMI","color":"dark_purple","bold":true}
title @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64] subtitle {"text":"El Dios Humano despierta.","color":"light_purple"}
playsound minecraft:entity.wither.spawn hostile @a[...] ~ ~ ~ 1 0.6
```

**`mt:boss/cuenta_atras`** → `remove mt_timer 1`; títulos "3"/"2"/"1" en `100/60/20`;
en `mt_timer==0` llama `mt:boss/iniciar`.

**`mt:boss/iniciar`** → `mt_prep=0`, `mt_fight=1`; cuenta jugadores en el área → `#n`;
vida `300` (2), `400` (3), `420` (≥4); setea `bossbar mt:jefe max/value`;
`kill @e[tag=mt_boss]` + `kill @e[tag=mt_add]`; **`summon minecraft:ravager`** con los NBT de §5;
`data merge entity ... {Health:...}`.

**`mt:boss/combate`** → actualiza `barra`; calcula `% = Health / bossbar max`; despacha a
`fase_1` (67..), `fase_2` (34..66), `fase_3` (..33); si no queda ningún jugador en el área →
`limpiar`; si no queda `mt_boss` con `mt_fight==1` → `mt_fight=2` → `derrotar`.

**`mt:boss/barra`** → `execute store result bossbar mt:jefe value run data get entity @e[tag=mt_boss,limit=1] Health`.

**`mt:boss/limpiar`** → `kill @e[tag=mt_boss]`, `kill @e[tag=mt_add]`, `mt_fight=0`,
`mt_prep=0`, bossbar oculta.

**`mt:boss/derrotar`** → `function mt:puerta/desbloquear`, `kill @e[tag=mt_add]`, bossbar oculta,
`mt_fight=0`, `mt_prep=0`, títulos "VICTORIA" + "Hitogami ha caído. El portal al End se ha abierto."

**`mt:puerta/vano_cerrar`** / **`vano_abrir`**
```mcfunction
# cerrar
fill -2312 -15 -2984 -2297 -11 -2984 minecraft:barrier replace minecraft:air
# abrir
fill -2312 -15 -2984 -2297 -11 -2984 minecraft:air replace minecraft:barrier
```

**`mt:puerta/desbloquear`** → `execute if score global mt_end matches 0 run function mt:puerta/_desbloquear_real`

**`mt:puerta/_desbloquear_real`**
```mcfunction
function mt:puerta/vano_abrir
fill -2305 -43 -2970 -2303 -40 -2968 minecraft:air replace minecraft:reinforced_deepslate
fill -2305 -43 -2970 -2303 -40 -2968 minecraft:end_portal
scoreboard players set global mt_end 1
title @a actionbar {"text":"La puerta al End ha sido desbloqueada.","color":"light_purple"}
playsound minecraft:block.portal.trigger master @a ~ ~ ~ 0.8 1.2
```
> **[A VERIFICAR]** El rango del `fill` (`-2305 -43 -2970` a `-2303 -40 -2968`) cubre
> **3×4×3** bloques, no el 3×3 `Y=-42..-40` que dice el informe §3. Unificar.

**`mt:sellos/sello_N`** → `add global mt_seal 1`, sonido, tellraw "Sello N/3 activado.",
y `execute if score global mt_seal matches 3 run function mt:sellos/abrir_arena`.

**`mt:sellos/abrir_arena`** → sonido + títulos "ARENA ABIERTA" + tellraw (el portón lo abre la
propia estructura; la función marca el momento).

**`mt:prueba/{combate,terminar,estado}`** → modo de ensayo: fuerza `preparar` sin pasar por
los sellos; `terminar` mata al jefe y fuerza la derrota para verificar el portal; `estado`
muestra `mt_fight/mt_prep/mt_end/mt_seal/mt_boss_first_kill` y adds.

### Advancements (un disparador por sello)
Cada `data/mt/advancement/sello_N.json` usa el trigger
`minecraft:player_generates_container_loot` con `conditions.loot_table: "mt:sellos/sello_N"`
y `rewards.function: "mt:sellos/sello_N"`. **[A VERIFICAR]** que un `trial_spawner`/cofre con
loot table propia dispare ese trigger en 26.2.

### Loot tables
- **`mt:boss/hitogami.json`** (`type: minecraft:entity`): 2 rollos con `diamond 2–5`,
  `netherite_scrap 1–3`, `emerald_block 1–2`; 1 rollo de `enchanted_book` con
  `enchant_randomly`. **Repetible en cada muerte.**
- **`mt:sellos/sello_N.json`** (`type: minecraft:chest`): escalan de hierro/oro/lapislázuli +
  libro con `sharpness` (sello 1) hasta `diamond`/`netherite_scrap`/`enchanted_golden_apple`
  (sello 3).
- **El ítem único NO sale del loot table**: lo entrega `hitogami.sk` (top damage, 1ª muerte).

---

## 9. Contrato del script Skript de tracking de daño (`hitogami.sk`)

**Ruta:** `servidor/plugins/Skript/scripts/hitogami.sk` — **separado**. **NO tocar `potencias.sk`.**

**Options / constantes:**
```skript
options:
  boss_tag: "mt_boss"
  first_flag_obj: "mt_boss_first_kill"
```

**Contrato (lo que el script DEBE hacer):**

| Aspecto | Contrato |
|---|---|
| **Evento de daño** | `on damage of ravager` filtrando `if victim has scoreboard tag {@boss_tag}` → acumular en variables por UUID del atacante: `{hitogami.dmg::%uuid of attacker%}` (daño de esta pelea) y `{hitogami.total::%uuid of attacker%}` (histórico). |
| **Evento de muerte** | `on death of ravager` con `if victim has scoreboard tag {@boss_tag}`. |
| **Premio único** | **Sólo si `mt_boss_first_kill == 0`**: recorrer jugadores, elegir el de **mayor `hitogami.dmg`** (top damage), darle el **ítem único** y setear `mt_boss_first_kill = 1`. |
| **Ítem único** | `netherite sword` con nombre `&5Hoja de Hitogami` y lore `"&7Conquistado por <jugador>"`, `"&7Primera caída de Hitogami"`, `"&8Fecha: …"`. |
| **Anuncio** | Título a todos: `"&6¡PRIMERA CAÍDA!"` / `"&ePremio único para <jugador>"` + sonido `ui.toast.challenge_complete`. |
| **Kills posteriores** | **Nunca** vuelven a dar el ítem único. El loot normal sale por `mt:boss/hitogami.json`. |
| **Limpieza** | Al morir el jefe: limpiar `{hitogami.dmg::*}` de todos los jugadores. Comando opcional `/hitogami_reset_dmg` (permiso op) para forzar la limpieza. |
| **Coherencia con el datapack** | El flag `mt_boss_first_kill` vive en el scoreboard del datapack (se inicializa en `mt:load`); el Skript lo lee/escribe. **No** reimplementar la puerta en Skript: la lógica del portal es del datapack. |

**Puntos [A VERIFICAR] del Skript:**
- Que el evento `on damage of ravager` capture **todo** el daño relevante (meleé, flechas,
  proyectiles, veneno, caída) y que la expresión `damage` sea la correcta para la versión de
  Skript 2.16.2.
- Alternativa más robusta sugerida en el historial: filtrar por entidad con tag
  `mt_boss` (no sólo por tipo `ravager`), para no depender del tipo.
- Manejo de **empates** en daño (el borrador "mantiene el primero").
- El borrador usa un block `on load` que sólo hace `pass`: revisar si hace falta inicializar
  algo.
- Este archivo **nunca se escribió a disco** — es un contrato, no código existente.

---

## 10. Tabla de parámetros (valores concretos)

| Parámetro | Valor | Origen / estado |
|---|---|---|
| Entidad del jefe | `minecraft:ravager` | Diseño |
| Nombre | `Hitogami, el Dios Humano` | Diseño (no consta aprobación del nombre) |
| Tags | `mt_boss`, `mt_add_protegido` | Diseño |
| `max_health` base | **400** (diseño) / **340** (summon implementado) | **[A VERIFICAR]** — inconsistencia |
| Vida por jugadores | **2→300, 3→400, ≥4→420** | Aprobado (escalado) |
| `attack_damage` | 14 | Diseño |
| `armor` | 8 | Diseño |
| `knockback_resistance` | 1.0 | Diseño |
| `movement_speed` | 0.30 (fase 3: **0.42**) | Diseño |
| `follow_range` | 48 | Diseño |
| `attack_knockback` | 1.0 | Diseño |
| `step_height` | 1.0 | Diseño |
| Umbrales de fase | **100–66% / 66–33% / <33%** | Diseño |
| Cuenta atrás | **200 ticks (10 s)** | Implementación narrada |
| Distancia máx. de pelea | **40 bloques** | Diseño |
| Adds — zombies (F1) | HP 28, máx 3, cada ~45 s | Implementación narrada |
| Adds — skeletons (F2) | HP 24, máx 4, cada ~40 s | Implementación narrada |
| Adds — wither_skeletons (F3) | HP 32, máx 2, cada ~30 s | Implementación narrada |
| Entidades simultáneas | boss 1 + adds ≤6 + proyectiles/fangs ≤~40 → **≤ ~50** | Estimado **[A VERIFICAR]** |
| Bossbar | id `mt:jefe`, `color purple`, `style notched_10`, `max 400` | Implementación narrada |
| Fallback de vida | `actionbar` con `% HP` cada ~5 ticks | **Aprobado (conservar)** |
| Interior arena | 48×48×40 | Diseño |
| **Cooldown de re-pelea** | **20–30 min** | **Aprobado**; valor exacto **[A VERIFICAR]** |
| Flag primera muerte | `mt_boss_first_kill` (0/1) | Aprobado |
| `allow-end` | **`true` desde el día 1** | **Aprobado** — en disco hoy: **`false`** |
| Objetivo de portal | `end_portal` reemplaza el sello 3×3 al morir | Aprobado |

---

## 11. Compatibilidad Bedrock (Geyser)

- **Bossbar:** `JavaBossEventTranslator` presente en Geyser-Spigot 2.12.0 → la bossbar de
  Java **sí** se traduce a Bedrock. **[A VERIFICAR]** color/estilo y apilado con cliente real.
- **Fallback:** mantener `actionbar` con `% HP` (decisión aprobada).
- **Títulos/subtítulos/actionbar/partículas/sonidos:** traducidos en su mayor parte. Evitar
  partículas raras (`sonic_boom`, `sculk_*`); preferir `flame`, `end_rod`, `soul_fire_flame`,
  `crit`.
- **Bloques:** `reinforced_deepslate`, `barrier`, `end_portal`, `end_portal_frame` renderizan
  en Bedrock. `barrier` invisible y sólido.
- **Altura:** el complejo va de y≈240 a y≈-48, lejos del techo 511. Geyser extiende el overworld
  a `-64..512` cuando el `dimension_type` de Java es >320 (verificado por bytecode;
  **falta** la prueba con cliente real).
- **Mobs:** `ravager`, `zombie`, `skeleton`, `wither_skeleton`, `evoker_fangs`, `wind_charge`
  soportados.
- **Pendiente:** test con **cliente Bedrock real** (bossbar, títulos, partículas, ítems no
  invisibles).

---

## 12. Plan de prueba (del informe de diseño)

1. Copia de ensayo del server; `/function mt:setup` (o `mt:load`).
2. **Colocar estructura:** `//schem load mt_trial_chamber_hitogami_v1` → `//paste` en el ancla
   `(-2304, -16, -3008)`. Verificar bbox y conteo de bloques no-aire.
3. **WorldGuard:** `/rg define prueba_hitogami` cubriendo el complejo → `build deny`.
4. **Modo prueba:** `/function mt:prueba/combate` → verifica bossbar visible y cambio de fases
   con la salud.
5. **Test puerta:** `/function mt:prueba/terminar` → verifica `mt_end==1`, que existan los
   bloques `end_portal` y que el vano esté abierto. Probar el teleporte al End.
6. **Reset:** salir del área → `mt_fight` vuelve a 0, bossbar oculta, jefe eliminado.
7. **Reinicio:** entrar en pelea → `stop` → `start` → `mt_fight==0`, `@e[tag=mt_boss]`
   inexistente, **portal NO abierto**.
8. **Rendimiento:** 4 jugadores en pelea → `spark profiler start`, medir TPS/MSPT 3–5 min.
9. **Bedrock:** bossbar + title/actionbar + partículas.
10. **Integridad:** verificar que la cámara del portal no tenga huecos de 1 bloque.
11. **LevelledMobs:** confirmar que `mt_boss` y `mt_add` estén **excluidos** de todas las
    reglas de escalado/renombrado (regla crítica).

**Mediciones objetivas:** `execute if entity @e[tag=mt_boss]` (0/1);
`scoreboard players get global mt_end` (0/1); `bossbar get mt:jefe value` vs
`data get entity @e[tag=mt_boss,limit=1] Health`; `tps`/`mspt` con spark;
`execute if block <centro portal> minecraft:end_portal`.

---

## 13. Conflictos con documentos existentes y decisiones abiertas

| Documento | Conflicto | Resolución adoptada |
|---|---|---|
| `FORTALEZA_END_DISENO.md` | Decía que los sellos (Trial Spawners) desbloquean el portal; el usuario quiere que lo haga el jefe. | **Fusionar:** los 3 sellos abren el portón de la arena; **sólo la muerte del jefe** abre el portal. No eliminar los sellos. |
| `Manual_Completo_Hosting_Mushoku_Tensei.md §11` | Abría el End el "día 8" con `mt:stage_dimensions_open` y `allow-end` false→true. | **Cortar la apertura por tiempo:** `allow-end=true` desde el día 1 y el End sólo se abre por `mt_end`. |
| `ESTRUCTURAS_SUBTERRANEAS.md` / `FORTALEZA_END_DISENO.md` vs `Celestara` | Ambigüedad entre la fortaleza subterránea con portal y la Fortaleza de Perugius (castillo flotante). | **No son la misma.** El portal va en la fortaleza subterránea detrás de la trial chamber. Celestara queda para otra fase. |
| `RELIQUIAS_TRIAL_CHAMBERS.md` | Asume Trial Chambers vanilla. | **Reutilizar:** cofres con loot tables propias en las salas; las advancements existentes (`mt:trial_reward*.json`) siguen funcionando. |
| `AGENTS.md` | `worldborder` 22000 vs 21800. | Sin efecto: la ubicación está a ≈3840 del spawn, dentro de ambos. No cambiar. |
| `LevelledMobs` | Podría escalar/renombrar al jefe y a los adds. | **Excluir** `mt_boss` y `mt_add`. |
| `potencias.sk` | Sólo rastrea warden/wither/dragon; el jefe no da "puesto 4" automático. | **No tocar**; el tracking va en `hitogami.sk`. |

### Decisiones que quedan abiertas / a confirmar
1. **Nombre del jefe:** "Hitogami, el Dios Humano" es elección del diseño — **confirmar** con el usuario. **[A VERIFICAR]**
2. **Valor exacto del cooldown** de re-pelea: 20 min vs 20–30 min. **[A VERIFICAR]**
3. **Quién recibe** el ítem único en empates de daño.
4. **`allow-end`:** ya está decidido (`true`), pero **no está aplicado** en disco.
5. **Ubicación de la boca de caverna** y el tramo de galerías (Y 240 vs superficie ≈347). **[A VERIFICAR]**
6. **NBT exacto de `trial_spawner`** para 26.2: **copiar de un spawner vanilla generado**
   (`/data get block ...`); no inventar. Bloqueante para que cargue. **[A VERIFICAR]**
7. **NBT CamelCase vs snake_case** en `summon` para este build: el histórico recomienda
   CamelCase (`Tags`, `Attributes`, `Health`, …) pero advierte que hay campos en ambos formatos;
   validar en la copia de ensayo. **[A VERIFICAR]**
8. **`max_health` base 340 vs 400** y el clamp de `Health` escalado (si la base es 340 y la
   vida escalada es 420, el valor se recorta) → hay que subir la base a ≥420. **Bug potencial a corregir.** **[A VERIFICAR]**

---

## 14. Fuentes

Todo lo de este documento sale del **historial de la base de sesiones de opencode**
(`C:\Users\emiliano\.local\share\opencode\opencode.db`, leída en **solo lectura** copiando
`db` + `-wal` + `-shm` a un temporal):

| Sesión | Título | Rango usado | Qué aportó |
|---|---|---|---|
| `ses_eb85516f1ffejxdWSrIJnS07DQ` | "Diseño prueba del jefe" | 2026-10-16 23:22:28 → 23:47:44 | **Informe de diseño completo** (mensaje de texto de 2026-10-16 23:42:44): concepto, §3 ubicación y coordenadas, §4 secuencia, §5 jefe y fases, §6 puerta/anti-trampa, §7 repetibilidad, §8 árbol del datapack + código de funciones, §9 rendimiento, §10 Bedrock, §11 plan de prueba, §12 riesgos, §13 conflictos, §14 decisiones. También el *brief* original del subagente (2026-10-16 23:22:28). |
| `ses_ec34de8a2ffeGSmmea47uEeNc8` | "Servidor Minecraft (Mushoku) - 2026-10-14" | 2026-10-16 21:08 → 2026-10-17 03:45 (y 15:40–16:02) | El usuario delega el diseño (2026-10-16 23:20:44: *"lo de la trial chamber trata de resolverlo vos…"*), resumen del subagente (2026-10-16 23:47:44), **propuesta de las 7 decisiones** (2026-10-17 03:15:46) y **aprobación del usuario** (2026-10-17 03:17:39: *"si claudio, realizalo como lo describiste, mantene un ojo en el export"*), implementación **narrada** de las funciones del datapack, advancements, loot tables y `hitogami.sk` (03:17–03:38). Verificación en disco de que **nada se escribió** (2026-10-17 15:40–15:50). |
| `C:\Dev\OPENCLAUDIO\.opencode\traspaso_2026-10-17_minecraft.md` | Traspaso | — | §5: los **7 puntos aprobados** (versión consolidada). §7: orden de implementación (Bloques C1/C2/C3). |
| `C:\Dev\OPENCLAUDIO\minecraft_server\AGENTS.md` | — | — | Decisiones del usuario (portal tras trial chamber muy difícil), hechos verificados del mundo (borde 21800, altura 511, sin estructuras vanilla). |
| Verificación en disco (2026-10-17) | — | — | `mt_core` **no** tiene `boss/puerta/sellos/prueba`; **no** existe `hitogami.sk`; **no** existe `DISENO_TRIAL_CHAMBER_JEFE.md`; `allow-end: false` en `servidor/bukkit.yml`. |

**Nota de método:** las sesiones se leyeron con sondas propias en
`C:\Users\emiliano\AppData\Local\Temp\opencode\` (copia de `opencode.db` + WAL a temporal,
apertura en solo lectura). El WAL es imprescindible: abrir la base original con `mode=ro` no lo
ve. Los "tool calls" del 2026-10-17 03:17–03:38 aparecen en el historial **sólo como texto**
(`[Assistant tool call]: write(...)`) sin ítems de herramienta estructurados, y el listado de
llamadas `write`/`edit` de esa ventana está vacío → **los archivos nunca se crearon**.

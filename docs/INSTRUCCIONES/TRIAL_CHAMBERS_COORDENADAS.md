# Trial Chambers — coordenadas de pegado

Fuente verificable de las coordenadas para pegar las trial chambers con FAWE.

## Estado (actualizado 2026-10-22 21:30, **verificado sobre los chunks**)

| Elemento | Coordenada | Estado |
|---|---|---|
| **SC1–SC10** (10 secundarias, `compact_trial_chamber.schem`) | tabla de §Coordenadas | **COLOCADAS — VERIFICADO.** Decodificando los voxeles del backup `BACKUP MUNDO 22.10 16.23hs`, las 10 regiones tienen `trial_spawner` + `vault` + `waxed_copper_grate` + `chiseled_tuff`. Cajas exactas en `mundo\COMANDOS_NATURALIZACION_RIKARISU_v2_VERIFICADO_2026-10-22.md` §A.1 |
| **Principal — arena de Hitogami** | caja real `X[−2293,−2188] Y[−12,52] Z[−2994,−2883]`, centro `−2241 20 −2939` | **COLOCADA — VERIFICADO.** Región `r.-5.-6`, 40.950 bloques firma, 4 `trial_spawner`, 9 `vault`. Material **sin encerar** → corresponde a `large_vanilla_trial_chamber.schem` |
| **Arena construida + portal al End** | — | **PENDIENTE.** En `r.-5.-6` no hay ni un `barrier`, `end_portal`, `reinforced_deepslate` ni `mossy_stone_bricks`: lo pegado es la trial chamber vanilla, **no** el complejo con la puerta |

> **El punto `(-2304, -16, -3008)` no es el centro de la cámara**: es el origen de pegado, 11 bloques al oeste y
> 14 al norte de la caja real. Para teleportarse, usar el centro medido.

> **Ojo con el schematic de la arena: hay dos indicaciones contradictorias y no está resuelto.**
>
> - Este archivo (heredado del traspaso 2026-10-18) dice `large_vanilla_trial_chamber.schem` (128×72×128, 14.307 B).
> - `mundo\PLAN_TRIAL_CHAMBER_JEFE_PORTAL_END_2026-10-21.md` §2.1 recomienda `compact_trial_chamber.ofs0.schem` y dice de `large_vanilla`: **"no usar"**.
> - **Medido en disco 2026-10-22:** `compact_trial_chamber.ofs0.schem` **no existe**. En
>   `MT_Server_Base_26.2\plugins\Litematica\schematics\` hay solo `compact_trial_chamber.schem`
>   (37.325 B) y `large_vanilla_trial_chamber.schem` (14.307 B); el `isla-del-end-el-uv87add1.schem`
>   (231.516 B) está en `plugins\FastAsyncWorldEdit\schematics\`.
> - `laberintos\INFORME_LABERINTOS.md` (medido con nbtlib) reporta que `large_vanilla` tiene
>   **0 block entities** con 9 vaults y 4 trial spawners solo en la paleta → al pegarlo esos
>   dispositivos **no dan botín ni invocan mobs**. `compact` trae **23 vaults + 25 trial spawners
>   configurados** (144 block entities).
> - **Decisión pendiente del usuario:** qué schematic se usa para la arena.

## Procedencia (leer esto primero)

- **Origen:** documento de traspaso de la sesión 2026-10-18
  (`.opencode\traspaso_*.md`, sección 6 "Trial Chambers — plan y schematics").
- **NO verificado contra el contenido de los `.schem`.** Son las coordenadas que
  venían en el traspaso; no se abrieron los schematics para confirmar dimensiones
  ni anclaje. Verificar con `//paste -preview` antes de pegar (ver más abajo).
  (Excepción medida: el `Offset` de `compact_trial_chamber.schem` sí se midió el
  2026-10-20 — ver más abajo.)
- El `MAPA_JUGADORES.md` que el traspaso decía haber generado **no existe** en
  `minecraft_server\` (buscado con glob: no está). Por eso este archivo es la
  fuente que queda en disco.
- Fecha de este archivo: **2026-10-18**. **Actualizado: 2026-10-22** (estado
  SC1–SC10 colocadas / arena pendiente; tamaños de `.schem` re-medidos en disco).

## Schematics (ubicación y tamaño en disco)

Carpeta: `MT_Server_Base_26.2\plugins\Litematica\schematics\`

| Archivo | Tamaño | Rol declarado |
|---|---|---|
| `large_vanilla_trial_chamber.schem` | 14.307 B | **Principal / arena** (según el traspaso 2026-10-18) |
| `compact_trial_chamber.schem` | 37.325 B | **Secundarias** (SC1–SC10) — ya pegadas |

Tamaños re-medidos en disco el **2026-10-22**. Son `.schem` (no `.litematic`).
FAWE los lee directo.

## Comandos

**Arena de Hitogami (principal) — PENDIENTE.** El schematic no está decidido
(ver el bloque de advertencia arriba). Forma documentada de FAWE:
```text
//schem load <schematic>
//pos1 -2304,-16,-3008
//paste -o
```
> `//paste at <X> <Y> <Z>` es la forma que traía el traspaso; **no figura en la doc
> oficial de FAWE** [A VERIFICAR en 2.16.1]. Y sin normalizar el `Offset` del
> schematic, `//pos1` no cae exacto.

**Secundarias SC1–SC10 — YA COLOCADAS (2026-10-22).** Se conserva el comando como registro:
```text
//schem load compact_trial_chamber.schem
//pos1 <X>,<Y>,<Z>      # con el Offset [269,191,736] compensado
//paste -o
```

## Coordenadas

### Principal

| X | Y | Z |
|---|---|---|
| -2304 | -16 | -3008 |

### Secundarias (orden SC1 → SC10)

| # | X | Y | Z |
|---|---|---|---|
| SC1 | -3554 | 72 | -842.9 |
| SC2 | -4587.8 | 70 | -1988.7 |
| SC3 | -4749.3 | 68 | -3528.0 |
| SC4 | -3976.8 | 70 | -4865.8 |
| SC5 | -2042.7 | 74 | -5494.3 |
| SC6 | -1054 | 72 | -5173.1 |
| SC7 | -20.2 | 70 | -4027.3 |
| SC8 | 141.3 | 68 | -2488.0 |
| SC9 | -631.2 | 70 | -1150.2 |
| SC10 | -2565.3 | 72 | -521.7 |

Ring: ~2500 bloques del principal, ~1545 entre secundarias, todas fuera de
Perugius + su buffer.

## Portal al End (PENDIENTE — va después de la arena)

Datos tomados de `mundo\PLAN_TRIAL_CHAMBER_JEFE_PORTAL_END_2026-10-21.md` §1.1 y §5.4
(son del **diseño**, no medidos en el mundo):

| Elemento | Coordenadas / medida | Nota |
|---|---|---|
| Cámara del portal | 23×23×13 → `X ∈ [−2315,−2293]`, `Y ∈ [−44,−31]`, `Z ∈ [−2980,−2958]` | Bajo el piso de la arena |
| Marco del portal (5×5) | centro `(−2304, −43, −2970)` | `minecraft:end_portal_frame` |
| Hueco del portal (3×3) | `Y ∈ [−42,−40]`, `X ∈ [−2305,−2303]`, `Z ∈ [−2971,−2969]` | **Sellado con `reinforced_deepslate`** hasta que muera el jefe |
| Vano arena ↔ portal | plano `z = −2984`, `X ∈ [−2312,−2297]`, `Y ∈ [−15,−11]` | `barrier` hasta completar los 3 sellos |
| Estructura decorativa | `MT_Server_Base_26.2\plugins\FastAsyncWorldEdit\schematics\isla-del-end-el-uv87add1.schem` (**231.516 B**, 241×256×241) | Licencia **[A VERIFICAR]** — el plan §6 la marca como posible `All Rights Reserved` |

**Mecánica (la razón por la que no hay portal ahora):** no existe **ningún bloque `end_portal`**
en el mundo hasta que muere Hitogami. El hueco 3×3 está con `reinforced_deepslate`
y el vano con `barrier`. Al morir el jefe, `mt:puerta/desbloquear` hace
`fill -2305 -42 -2971 -2303 -40 -2969 minecraft:end_portal` y pone `mt_end = 1`.
Desde ahí el portal queda abierto **para todos, para siempre**. Fuente:
`PLAN_TRIAL_CHAMBER_JEFE_PORTAL_END_2026-10-21.md` §5.4.

**Verificación al terminar:** `/scoreboard players get global mt_end` debe dar 0 antes
de la pelea y 1 después. Y `settings.allow-end` debe seguir en `true` (ya lo está desde
el 2026-10-17, ver `minecraft_server\AGENTS.md`).

## Cómo pegar (sin romper el mundo)

1. **Ajustar Y a superficie − 10.** Las Y de la tabla son del traspaso, no medidas
   en el mundo actual. En terreno irregular pueden quedar flotando o enterradas.
2. **`//paste -preview` SIEMPRE antes del pegado real.** Muestra dónde cae sin
   escribir nada. Recién cuando te gusta la posición, confirmás.
3. **Ojo con el agua.** ~47 % del mapa es océano (dato de `minecraft_server\AGENTS.md`).
   Una celda en mar deja la estructura flotando o sumergida: bajá la Y hasta el
   fondo, o aceptá la sumersión a conciencia.
4. Si el preview no convence: **movete/ajustá y previsualizá de nuevo.** No pegues
   para "ver cómo queda" — el pegado real es un `//undo` de distancia, pero es
   más limpio no equivocarse.
5. **Nada de `/place structure trial_chamber`:** el mundo se exportó con
   `generate-structures=false` y no hay `.nbt` vanilla. Solo funciona
   `//schem load` + `//paste`.

## Pendiente (fuera de este archivo)

- ~~Pegar SC1–SC10~~ — **hecho (reportado 2026-10-22)**.
- **Pegar la arena de Hitogami** (principal) en `(-2304, -16, -3008)`.
- Colocar el portal al End detrás de la arena (ver `mundo\PLAN_TRIAL_CHAMBER_JEFE_PORTAL_END_2026-10-21.md`).

### ⚠️ El `.schem` trae Offset NO-cero

Medido el 2026-10-20 (`mundo\COORDENADAS_PEGADO_DEFINITIVAS_2026-10-20.md` §Offset):
`compact_trial_chamber.schem` tiene **Offset `[269, 191, 736]`**. Ese offset desplaza el punto
de pegado, así que `//pos1 <coordenada>` **no** cae donde dice la tabla salvo que se normalice
el Offset a `[0,0,0]` o se compense a mano. Vale igual para la arena cuando se pegue.
El `Offset` de `large_vanilla_trial_chamber.schem` **no está medido** — [A VERIFICAR].

### Sobre `//paste at <X> <Y> <Z>`

La forma que este archivo usaba en 2026-10-18 **no figura en la documentación oficial de FAWE**.
La forma documentada es `//pos1 <X>,<Y>,<Z>` + `//paste -o` (ver
`mundo\COORDENADAS_PEGADO_DEFINITIVAS_2026-10-20.md` §Comandos). [A VERIFICAR] en la versión
instalada (FastAsyncWorldEdit 2.16.1, leído de `servidor\logs\latest.log` el 2026-10-22).

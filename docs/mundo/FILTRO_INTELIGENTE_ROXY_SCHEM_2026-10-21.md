# Filtro inteligente de Roxy — re-extracción de 12 `.schem` (2026-10-21)

Documenta **cómo** se generó la carpeta `roxy_village_smart\` y **qué** se midió.
Cada cifra sale de un comando sobre el archivo en disco de este turno.

---

## 1. Qué se pidió y qué se hizo

| Ítem | Estado |
|---|---|
| Modificar `es_natural()` para que elimine **solo terreno crudo** | ✅ hecho en `laberintos\mundo_a_schem.py` |
| Extraer 12 `.schem` de las 12 regiones indicadas | ✅ 12/12 escritos |
| Salida en `…\Litematica\schematics\roxy_village_smart\` | ✅ 12 archivos, **6.813.166 B = 6,81 MB** |
| Castillo/pueblo completo, sin terreno crudo | ✅ **0 bloques crudos residuales** en los 12 |

---

## 2. El cambio de código

`laberintos\mundo_a_schem.py` — el filtro pasó de "lista de nombres" a **lista explícita de
terreno crudo**:

- **Antes**: `NATURAL` (35 entradas) + `NATURAL_PREFIJOS` (39 entradas, incluyendo `*_log`, `*_leaves`,
  `*_grass`, `dandelion`, `poppy`, `moss_carpet`…) + sufijo `_flower`.
  Eso borraba **madera de construcción** (`oak_log`, `birch_log`, `stripped_*_log`) y
  **decoración** (`poppy`, `dandelion`, `cactus_flower`, `potted_*`), que el encargo pide conservar.
- **Ahora**: `TERRENO_CRUDO` (39 entradas exactas, la lista del encargo) + sufijo `_ore`.
  `es_natural()` devuelve `True` **solo** para esos.

```python
def es_natural(nombre):
    if nombre in TERRENO_CRUDO:
        return True
    if nombre.endswith("_ore"):   # minerales
        return True
    return False
```

Comprobado sobre la tabla (mediado, `python -c "import mundo_a_schem"`):

| Llamada | Resultado | Llamada | Resultado |
|---|---|---|---|
| `stone` | `True` (se elimina) | `stone_bricks` | `False` (se conserva) |
| `grass_block` | `True` | `deepslate_bricks` | `False` |
| `packed_ice` | `True` | `polished_blackstone` | `False` |
| `end_stone` | `True` | `blackstone_bricks` | `False` |
| `blackstone` | `True` | `oak_log` / `oak_planks` / `oak_stairs` | `False` |
| `basalt` | `True` | `barrel` / `chest` / `lantern` | `False` |
| `netherrack` | `True` | `poppy` / `dandelion` / `torchflower` | `False` |
| `purpur_block` | `True` | `stone_brick_stairs` / `deepslate_brick_slab` | `False` |
| `quartz_block` | `True` | `stone_brick_wall` | `False` |
| `deepslate_coal_ore` | `True` | `mossy_stone_bricks` | `False` |

> `minecraft:stone_bricks` y `minecraft:deepslate_bricks` **no existen** en el mundo Roxy
> (verificado: 0 apariciones en 184 regiones / 150.427 chunks). El castillo usa
> `mossy_stone_bricks`, `cracked_deepslate_bricks` y `end_stone_bricks`.

---

## 3. Método de extracción

Comando por región (12 corridas secuenciales):

```powershell
cd minecraft_server\laberintos
C:\Dev\OPENCLAUDIO\tesis_neuronal\.venv\Scripts\python.exe mundo_a_schem.py `
  "…\plugins\Litematica\schematics\Roxy village fixed.zip" `
  "…\plugins\Litematica\schematics\roxy_village_smart\roxy_r.<rx>.<rz>.schem" `
  --x0 <X0> --x1 <X1> --z0 <Z0> --z1 <Z1> --solo-construccion
```

Notas de método:

- El flag `--solo-construccion` es lo que activa el filtro; sin él sale el terreno.
- Rango Y: `-64..511` (576 capas) por defecto; el script **recorta** solo al rango realmente
  usado antes de escribir, así que el `.schem` queda ajustado (p. ej. `r.-2.-3` → 320 capas).
- Memoria: `mundo_a_schem.py` usa un **bytearray plano** (2 B por voxel), no dict de tuplas.
  Para 512×512×576 son ~300 MB, viable en esta máquina.
- Cada región tarda ~40–60 s (reabre y re-decomprime el ZIP de 221 MB).

---

## 4. Lo medido en la salida

### 4.1 Integridad de los 12 archivos

`verify_schem.py` (lee el NBT de vuelta): **`blockdata_ok=True` en 12/12**
(los varints de `BlockData` coinciden exactamente con `Width×Height×Length`),
`Version=2` (Sponge v2, el que lee FAWE), `DataVersion=4903`.

| Archivo | Dims (X×Y×Z) | Bytes | Tipos | BEs |
|---|---|---|---|---|
| roxy_r.-1.-10 | 512×216×512 | 142.557 | 4 | 0 |
| roxy_r.-1.-12 | 512×289×512 | 173.770 | 3 | 0 |
| roxy_r.-1.-2 | 512×377×512 | 1.040.085 | 125 | 8.774 |
| roxy_r.-1.-3 | 512×375×512 | 1.343.545 | 162 | 7.057 |
| roxy_r.-2.-11 | 512×175×512 | 144.788 | 3 | 0 |
| roxy_r.-2.-12 | 512×110×512 | 56.100 | 3 | 0 |
| roxy_r.-2.-3 | 512×320×512 | 1.579.820 | 25 | 75 |
| roxy_r.0.-10 | 512×222×512 | 145.788 | 3 | 0 |
| roxy_r.0.-11 | 512×269×512 | 209.417 | 3 | 0 |
| roxy_r.0.-12 | 512×215×512 | 163.868 | 3 | 0 |
| roxy_r.0.-2 | 512×377×512 | 1.146.112 | 65 | 2.359 |
| roxy_r.1.-2 | 512×119×512 | 991.316 | 9 | 29 |

### 4.2 El filtro funciona: 0 terreno crudo residual

`verificar_roxy_smart.py` (nuevo) vuelve a leer los 12 `.schem` y clasifica **cada bloque**
con el mismo `TERRENO_CRUDO`:

```
TOTAL bloques crudos residuales en los 12 schem: 0
```

### 4.3 Lo que sí quedó (construcción, madera, block entities, decoración)

Conteos de la unión de los 12:

| Familia | Bloques | Conteo |
|---|---|---|
| **Madera** | `stripped_birch_wood` | 37.222.521 |
| | `stripped_oak_wood` | 1.176.936 |
| | `stripped_jungle_wood` | 22.506 |
| | `spruce_planks` / `birch_planks` / `jungle_planks` / `oak_planks` / `bamboo_planks` | 4.773 / 4.196 / 579 / 35 / 143 |
| | `oak_fence` / `birch_fence` / `spruce_fence` | 3.384 / 2.422 / 43 |
| | `birch_trapdoor` / `oak_trapdoor` / `spruce_trapdoor` / `iron_trapdoor` | 4.266 / 514 / 1.449 / 252 |
| | `birch_slab` / `oak_slab` / `spruce_slab` | 2.594 / 67 / 8 |
| | `birch_stairs` / `oak_stairs` / `spruce_stairs` | 99 / 18 / 3 |
| | `iron_door` / `oak_fence_gate` / `birch_fence_gate` | 339 / 1.133 / 353 |
| **Bloques trabajados** | `sandstone_stairs` / `sandstone_slab` / `sandstone_wall` | 130.813 / 21.025 / 9.912 |
| | `cut_sandstone_slab` / `smooth_sandstone_stairs` / `smooth_sandstone_slab` | 17.166 / 9.910 / 4.855 |
| | `polished_blackstone` | 2.218 |
| | `end_stone_bricks` | 1.843 |
| | `granite_slab` / `granite_stairs` | 1.639 / 254 |
| | `mud_brick_slab` / `mud_brick_stairs` / `mud_brick_wall` | 1.297 / 57 / 11 |
| | `cracked_deepslate_bricks` | 1.023 |
| | `polished_deepslate_wall` / `polished_diorite` / `diorite_wall` | 50 / 18 / 25 |
| | `prismarine_bricks` / `prismarine_brick_slab` / `stone_stairs` | 126 / 193 / 71 |
| **Block entities (bloque)** | `bookshelf` / `fletching_table` | 1.911 / 1.425 |
| | `campfire` / `decorated_pot` / `barrel` / `chest` / `hopper` / `brewing_stand` / `flower_pot` | 567 / 310 / 230 / 3 / 2 / 6 / 71 |
| | `beehive` | 17.346 (en el mundo) |
| **Decoración** | `white_stained_glass` / `light_gray_stained_glass` / `orange_stained_glass` | 430.387 / 19.480 / 2.206 |
| | `light_blue_wool` / `blackstone_slab` | 1.414.645 / 1.077.368 |
| | `red_carpet` / `light_blue_carpet` / `purple_carpet` / `orange_carpet` / `pale_moss_carpet` | 914 / 178 / 100 / 56 / 331 |
| | `oak_wall_sign` | 1.267 |
| | `waxed_copper_lantern` / `exposed_copper_lantern` / `lantern` / `shroomlight` | 69 / 1 / 1 / 10 |
| | `potted_oxeye_daisy` / `ladder` | 41 / 38 |

**Block entities con datos** (del NBT, no solo el bloque): 18.294 en total en los 12 —
`beehive` 14.398, `sign` 2.602, `campfire` 567, `barrel` 230, `shelf` 1.059,
`decorated_pot` 310, `banner` 116, `chest` 3, `skull` 18, `command_block` 1,
`comparator` 1, `dispenser` 1, `hopper` 2, `brewing_stand` 6, `hanging_sign` 2.

### 4.4 Contraste con el mundo crudo (qué se eliminó de verdad)

Histograma crudo de las 12 cajas juntas (X −1024..1023, Z −6144..−513), 199 tipos:

**Eliminados (12 tipos)** — `stone` 252.061.428, `dirt` 22.245.873, `sand` 18.438.828,
`grass_block` 11.584.132, `bedrock` 11.352.296, `end_stone` 1.444.588, `sandstone` 150.491,
`mud` 13.527, `diorite` 677, `smooth_basalt` 456, `blackstone` 2.241, `water` 10.

**Conservados (187 tipos)**, de los que standout los de construcción listados arriba.

---

## 5. Hallazgos que el encargo dio por hecho y **no** se cumplen

> These are measured facts, not opinions.

### 5.1 `stone_bricks` / `deepslate_bricks` / `blackstone_bricks` NO existen en Roxy

Escaneo de **todas** las paletas del mundo (184 regiones, 150.427 chunks, 245 nombres de bloque):
`minecraft:stone_bricks` → **0 apariciones**; `minecraft:deepslate_bricks` → **0**;
`minecraft:blackstone_bricks` → **0**.

Lo que sí hay, y en qué regiones:

| Bloque | Regiones del mundo | Bloques (12 cajas) |
|---|---|---|
| `mossy_stone_bricks` | **r.-1.1, r.-1.2** | 0 (fuera de las 12) |
| `cracked_deepslate_bricks` | r.-1.-3, r.-1.-2 | 1.023 |
| `end_stone_bricks` | r.-1.-3, r.-1.-2, r.0.-3, r.0.-2 | 1.843 |
| `mud_bricks` | r.-1.1, r.-1.2 | 0 (fuera) |

→ **El castillo de `mossy_stone_bricks` está en r.-1.1 / r.-1.2**, que **no** son de las 12
regiones pedidas. Las 12 regiones pedidas cubren el **pueblo** (madera + sandstone),
no el castillo de ladrillo. Si el encargo era "el castillo", faltan 2 regiones.

### 5.2 Las 6 regiones "vacías" en bloques de construcción

Seis `.schem` (r.-2.-12, r.-2.-11, r.-1.-12, r.-1.-10, r.0.-12, r.0.-11, r.0.-10) tienen
**3–4 tipos de bloque**, casi todo `blackstone_slab`. No es un bug del filtro: el mundo
**real** en esas cajas solo tiene `stone` (15,8 M), `dirt`, `bedrock`, `grass_block`,
`blackstone_slab` (167.291), `black_concrete` (93.782) y 2 `gold_block`.
Es decir: **esas cajas son terreno plano con una vereda de `blackstone_slab`**, no pueblo.
El filtro no puede inventar buildings donde no hay.

### 5.3 El "terreno crudo" de Roxy está pintado con material de construcción

Verificado con perfil de columna (`_columna_roxy.py`, columna x=700 z=−700):

```
y=-64      bedrock
y=-63..-62 dirt
y=-61      grass_block
y=-19..18  stripped_birch_wood   <-- 38 capas seguidas
y=19..46   sand
```

La masa de `stripped_birch_wood` (104 M bloques en las 12 cajas) es **la base de madera del
pueblo**, no ruido de terreno: forma un bloque macizo y continuo. Coincide con lo que
documenta `ALTERNATIVAS_PEGADO_ROXY_SOLO_ESTRUCTURAS_2026-10-21.md` §2.2: en Roxy el
"terreno" está pintado con `smooth_sandstone` / `stripped_birch_wood`, o sea que **el
separador por tipo no alcanza** y haría falta uno geométrico (altura respecto de la
superficie local). Este trabajo implementa **la parte por tipo** del encargo.

---

## 6. Errores y tropiezos durante el trabajo

| Error | Causa | Resolución |
|---|---|---|
| `NameError: name '_iter_regiones' is not defined` al correr `_columna_roxy.py` | **typo real** en el archivo: `from mundo_a_schem import _iter_regiones` (faltaba la `e`). La consola de PowerShell muestra el mensaje con el nombre en cp437 y **parece** idéntico al correcto, por eso costó ver el bug. | Detectado con `ast.parse` + volcado de los `ast.Name` a un archivo y `ascii()`; corregido. **Regla: cuando un `NameError` sugiera un nombre "casi igual", volcar los codepoints, no confiar en la consola.** |
| `NameError: name '_iter_regiones' is not defined` **no se arreglaba re-ejecutando** | Al principio se sospechó caché/compilación; no era eso: el archivo en disco tenía el typo desde el `write`. | Se verificó con `io.open(...).read()` y `repr()` de las líneas 15 y 28. |
| `inspeccionar_region.py` no existe en `laberintos\` | Está en `mundo\`. | Se usan las funciones de `mundo_a_schem.py` directamente (`_iter_regiones`, `_chunks_de_region`). |
| `mundo_a_schem.py` importa `converter` e `inspeccionar_region` desde `mundo\` | Requiere `sys.path` con `laberintos\` y `mundo\`. | Los scripts nuevos replican ese `sys.path.insert`. |
| `localizar_construccion.py` usa `M.NO_SOLIDO` | **`NO_SOLIDO` no existe en `mundo_a_schem.py`** — referencia rota, preexistente, **no** la introduce este trabajo. | **No se tocó** (fuera del alcance). Queda reportado. |
| PowerShell: `&&` no válido | Windows PowerShell 5.1 no soporta `&&`. | Se usó `;` en vez de `&&`. |
| Consola PowerShell con cp437 | `�` en los mensajes de Python, y nombres casi idénticos indistinguibles. | Todo el análisis crítico se hizo con salida a **archivo + `ascii()`/`repr()`**, no con la consola. |

---

## 7. Herramientas nuevas (todas en `laberintos\`, solo lectura sobre el mundo)

| Archivo | Qué hace |
|---|---|
| `verificar_roxy_smart.py` | Verifica los 12 `.schem`: crudos residuales, trabajados, block entities. **El que valida la entrega.** |
| `_hist_roxy.py` | Histograma crudo de bloques de una caja del mundo (sin filtro). |
| `_hist_y_roxy.py` | Distribución en Y de un bloque concreto dentro de una caja. |
| `_slice_roxy.py` | Corte horizontal ASCII de una caja del **mundo crudo**. |
| `_slice_schem.py` | Corte horizontal ASCII de un **`.schem` ya entregado**. |
| `_columna_roxy.py` | Perfil vertical (runs de bloques por Y) de columnas sueltas. |
| `_scan_paletas.py` | Escanea **todas** las paletas del mundo y lista los nombres de bloque. |
| `_donde_bloque.py` | Localiza en qué regiones del mundo está un bloque objetivo. |

Todos con prefijo `_` (convención de "herramienta de trabajo" del proyecto) salvo
`verificar_roxy_smart.py`, que es el verificador de la entrega.

---

## 8. Estado y pendientes

**Hecho y verificado en disco (2026-10-21):**

- 12 `.schem` en `…\plugins\Litematica\schematics\roxy_village_smart\`, 6,81 MB, Sponge v2.
- 0 bloques de terreno crudo residuales.
- Bloques trabajados, madera, block entities y decoración presentes y contados.
- `blockdata_ok=True` en 12/12.

**Pendiente (decisión del usuario, no se hizo):**

1. **¿Faltan las 2 regiones del castillo?** `mossy_stone_bricks` / `mud_bricks` están en
   **r.-1.1** (X −512..−1, Z 16..511) y **r.-1.2** (X −512..−1, Z 512..1007).
   Si el encargo era el castillo, hay que extraerlas también.
2. **Los 6 `.schem` de "3 tipos"** son terreno plano con vereda: no contienen pueblo.
   Si se esperaba un pueblo ahí, la caja de origen está mal elegida.
3. **Separación geométrica** (no solo por tipo): ver
   `mundo\ALTERNATIVAS_PEGADO_ROXY_SOLO_ESTRUCTURAS_2026-10-21.md` §2.2–2.4 y §7.
   El filtro por tipo no puede separar el terreno pintado con material de construcción.
4. **Pegado**: los `.schem` van a `plugins/FastAsyncWorldEdit/schematics/` (la carpeta de
   Litematica es del mod cliente). `//paste` exige cliente conectado o RCON
   (la consola por stdin de Paper 26.2 está defectuosa).
5. Límite de FAWE: `max-changes: 50000000` en `servidor\plugins\FastAsyncWorldEdit\config.yml`.
   Los `.schem` grandes (`r.-2.-3` = 12,9 M de bloques no-aire) quedan por debajo;
   `r.0.-2` (11,3 M) también. **Cada pegado va individual, no todos juntos.**

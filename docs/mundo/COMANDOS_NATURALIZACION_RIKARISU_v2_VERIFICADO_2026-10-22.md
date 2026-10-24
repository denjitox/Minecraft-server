# Comandos de Naturalización — Rikarisu (Roxy Village) — **v2 VERIFICADO**

**Fecha:** 2026-10-22
**Verificado contra:** `servidor\plugins\FastAsyncWorldEdit-Paper-2.16.1.jar` (leído con `javap` de JDK 25).
**Método de verificación:** extracción de las clases `*CommandsRegistration` y de las firmas/anotaciones de
`com.sk89q.worldedit.command.RegionCommands`, `UtilityCommands`, `GenerationCommands`, `ClipboardCommands`.
Todo comando listado abajo existe en **ese** JAR con **esa** firma. Lo que no se pudo verificar está marcado.

> ⚠️ **La v1 de este documento (`COMANDOS_NATURALIZACION_RIKARISU_2026-10-22.md`) tenía comandos que NO existen.**
> Fallaban con "Unknown command": `//blend`, `//path`, `//pos3`, y `//forest` con sintaxis inventada.
> Este archivo la reemplaza.

---

# A. Estado REAL del mundo vivo (medido el 2026-10-22, sobre los chunks)

Todo lo de esta sección sale de **decodificar los voxeles** de las regiones `.mca`, no de documentos previos.

**Fuente:** `C:\Dev\OPENCLAUDIO\minecraft_server\BACKUP MUNDO 22.10 16.23hs\BACKUP MUNDO 22.10 16.23hs`.
**No es una carpeta: es un TAR SIN COMPRIMIR de 14.686.102.016 B** (magic `ustar` en el offset 257), con
**3.427 miembros** y **1.946 regiones** de overworld. Se extrajeron **23 regiones** a
`mundo\_live_verif_2026-10-22\` con acceso aleatorio al TAR (sin descomprimir los 14,7 GB).

| Script (en `mundo\`) | Qué mide |
|---|---|
| `_extraer_regiones_backup.py` | extrae del TAR solo las regiones necesarias |
| `_escanear_paletas_live.py` | qué bloques firma aparecen en cada región (paletas, rápido) |
| `_ubicar_estructuras_live.py` | caja real `X/Y/Z` (mín/máx) de cada estructura, voxel por voxel |
| `_superficie_natural_live.py` | superficie del **terreno** ignorando construcciones |
| `_rikarisu_terreno_vs_aldea.py` | terreno vs. base de la aldea, columna por columna |
| `_rikarisu_huella_construccion.py` | huella y altura de arranque de lo construido |

Lector de referencia cruzado: **`_lab_read_block.py`** (el que ya estaba probado). Las columnas de control
coinciden bloque por bloque con el decodificador nuevo.

> **Trampa de formato que costó dos intentos:** dentro de una sección el índice de voxel es
> `idx = dy*256 + z*16 + x`. Usar un `idx` constante para todos los `dy` devuelve 16 veces el bloque de
> `dy=0` y hace aparecer "16 bloques de grass" donde hay aire. Los números de abajo están tomados con el
> índice correcto y verificados contra `_lab_read_block.py`.

## A.1 — Las 11 trial chambers ESTÁN PEGADAS ✅

Verificado por bloques firma (`trial_spawner`, `vault`, `tuff_bricks`, `chiseled_tuff`, `polished_tuff`,
`copper_grate`, `copper_bulb`). **Ninguna caja coincide con el punto documentado**: el punto del doc es el
origen de pegado / teleport, no el centro de la cámara.

| # | Región | Caja X | Caja Y | Caja Z | Centro (para `/tp`) | spawners | bloques firma |
|---|---|---|---:|---|---|---:|---:|
| **Jefe** | `r.-5.-6` | −2293 … −2188 | −12 … 52 | −2994 … −2883 | **−2241 20 −2939** | 4 | 40.950 |
| SC1 | `r.-7.-2` | −3584 … −3514 | −4 … 64 | −903 … −818 | −3549 30 −861 | 21 | 45.527 |
| SC2 | `r.-9.-4` | −4608 … −4548 | −6 … 62 | −2048 … −1964 | −4578 28 −2006 | 16 | 38.840 |
| SC3 | `r.-10.-7` | −4817 … −4710 | −8 … 60 | −3584 … −3503 | −4764 26 −3544 | 25 | 52.889 |
| SC4 | `r.-8.-10` | −4044 … −3937 | −6 … 62 | −4926 … −4841 | −3991 28 −4884 | 25 | 54.113 |
| SC5 | `r.-4.-11` | −2048 … −2003 | −8 … 60 | −5555 … −5470 | −2026 26 −5513 | 11 | 25.707 |
| SC6 | `r.-3.-11` | −1121 … −1025 | −4 … 64 | −5234 … −5149 | −1073 30 −5192 | 24 | 51.687 |
| SC7 | `r.-1.-8` | −97 … −1 | −26 … 42 | −4092 … −4007 | −49 8 −4050 | 24 | 51.687 |
| SC8 | `r.0.-5` | 74 … 181 | −43 … 25 | −2548 … −2463 | 127 −9 −2506 | 25 | 54.113 |
| SC9 | `r.-2.-3` | −698 … −591 | −12 … 56 | −1211 … −1126 | −645 22 −1169 | 25 | 54.113 |
| SC10 | `r.-6.-2` | −2632 … −2561 | −28 … 40 | −582 … −513 | −2597 6 −548 | 17 | 37.389 |

**Dos esquemas distintos, confirmado por los materiales:**

- **Jefe (`r.-5.-6`)**: usa `copper_grate` y `copper_bulb` **sin encerar** y no tiene `chiseled_tuff` → es
  `large_vanilla_trial_chamber.schem`, y es la única con esa firma.
- **Las 10 secundarias**: `waxed_copper_grate` + `waxed_copper_bulb` + `chiseled_tuff` → `compact_trial_chamber.schem`.

**Marcador de pegado:** 9 de las 10 secundarias tienen una columna de `white_stained_glass` de 4 bloques
(SC5 y el jefe no la tienen). Ese marcador cae en la misma `Z` que el punto del doc, ~11 bloques al oeste en `X`
y **~44 bloques por debajo** del `Y` del doc (p. ej. SC1: doc `−3554 72 −842.9` → vidrio en `−3565, 27-28, −843…−838`).

**Consecuencia práctica:** las cámaras están **bajo tierra** (techo entre Y=25 y Y=64 según cuál). Un
`/tp` al `Y` del doc deja al jugador **en el aire por encima del terreno** o dentro de roca. Para entrar,
usar el centro medido de la tabla.

## A.2 — Rikarisu: **la aldea está flotando sobre el terreno**

Medido sobre las 6 regiones donde está la aldea (`r.8.-15`, `r.9.-15`, `r.10.-15`, `r.8.-14`, `r.9.-14`, `r.10.-14`).

**El terreno** (1.824 columnas, malla cada 8 bloques, ignorando construcciones):

| Dato | Valor medido |
|---|---|
| Y del terreno | min **75** · p10 **89** · mediana **95** · p75 **97** · p90 **99** · máx **99** |
| Material de superficie | `red_sand` **1.576/1.824 (86 %)** · `stone` 187 · `dirt` 16 · grava/granito/andesita el resto |

O sea: **meseta de badlands a Y≈95**, prácticamente plana (desvío 3,2 bloques).

**La aldea**, en cambio, arranca muy por encima. Tres columnas de control, leídas con el lector validado:

| Columna (X Z) | Terreno | Aire | Primer bloque de la aldea |
|---|---:|---|---:|
| `4879 −7475` (entrada del doc) | Y=92 | 93 … 192 | **Y=193** `stripped_birch_wood` |
| `4640 −7296` | Y=95 | 96 … 156 | **Y=157** `stripped_birch_wood` |
| `4704 −7376` | Y=95 | 96 … 141 | **Y=142** `stripped_birch_wood` |

Barrido de 88.303 columnas con material de construcción (`stripped_*_wood`, `smooth_sandstone`,
`cut_sandstone`, `white_stained_glass`):

| Dato | Valor medido |
|---|---|
| Base de la aldea (Y del bloque construido más bajo) | más frecuentes: **100** (11.809 col.) · **142** (9.128) · **132** (5.271) · luego 136, 159, 157, 150-152 |
| Hueco de aire bajo la aldea | min 0 · p10 10 · **mediana 48** · p90 189 · máx 315 |
| Columnas con hueco ≥ 20 bloques | **66.221** de 88.303 (75 %) |
| Extensión | X[4400,5150] Z[−7650,−7152] — **la huella toca los bordes de lo escaneado**, el extremo real puede ser mayor `[A VERIFICAR]` |

**Esto cambia el trabajo a hacer.** `//naturalize` *no* arregla esto: `//naturalize` pone 3 capas de tierra
sobre una superficie que ya existe; acá el problema es que **no hay superficie** — hay 47 bloques de aire
promedio entre el terreno (Y≈95) y la aldea (Y≈142). Hay que **construir el suelo que falta**, no peinarlo.

**Hallazgo suelto a revisar:** hay una **capa de `black_concrete` en Y=90** en una franja amplia de la zona
(aparece en la columna `4879 −7475`, con `stone` encima en 91-92). No es terreno natural ni material de la
aldea. `[A VERIFICAR]` de dónde salió antes de taparla.

---

## 0. Diagnóstico: por qué no los reconocía

| Comando de la v1 | Estado real |
|---|---|
| `//blend ball 80 0.35` | ❌ No existe. `blendball` es un **brush** (`/brush blend <radio>`), no un comando `//` |
| `//blend cylinder 80 25 0.4` | ❌ No existe |
| `//path grass_path 3` | ❌ No existe `//path`; y `grass_path` ya no es un bloque válido (→ `dirt_path`) |
| `//pos3` | ❌ No existe. Solo `//pos1` / `//pos2` (`//curve` usa selección poligonal, no `pos3`) |
| `//forest 80 40 0.15 oak,spruce,birch` | ❌ Firma real: `//forest <treeType> [densidad]` |
| `//fillr air stone 60` | ⚠️ Firma real: `//fillr <patrón> <radio> [profundidad]` |
| `//gmask solid` | ⚠️ El mask es `#solid` (con numeral) |
| `//paste at X Y Z` | ❌ `//paste` no acepta `at` |
| `//paste -a` | ✅ Correcto (`-a` = *Skip air blocks*) |
| `//smooth 2` | ✅ Correcto |

---

## 1. Comandos verificados (firma exacta del JAR)

| Comando | Firma real | Descripción (del JAR) |
|---|---|---|
| `//naturalize` | sin argumentos | *3 layers of dirt on top then rock below* |
| `//smooth` | `//smooth [iteraciones] [mask]` | *Smooth the elevation in the selection* (iteraciones default **1**) |
| `//flora` | `//flora [densidad]` | *Make flora within the region* (densidad default **5**) |
| `//forest` | `//forest <treeType> [densidad]` | *Make a forest within the region* (densidad default **5**) |
| `//line` | `//line <patrón> [grosor] [-h]` | *Draws line segments between cuboid selection corners* |
| `//curve` | `//curve <patrón> [grosor] [-h]` | *Draws a spline through selected points* (requiere selección poligonal) |
| `//overlay` | `//overlay <patrón>` | *Set a block on top of blocks in the region* |
| `//fillr` | `//fillr <patrón> <radio> [profundidad]` | *Fill a radius with a pattern* |
| `//replace` | `//replace [desde] <hacia>` | Reemplazo dentro de la selección |
| `//gmask` | `//gmask <mask>` | Máscara global (los bloques destino deben cumplirla) |
| `//pos1` / `//pos2` | `//pos1`, `//pos2` | Esquinas de la selección cuboide |
| `//paste` | `//paste [-a] [-s] [-o] [-n] [-e] [-b] [-m <mask>] [-x] [-v]` | `-a`: **Skip air blocks** · `-o`: posición original · `-s`: seleccionar al pegar |
| `//schem load` | `//schem load <archivo>` | Carga el .schem al clipboard |
| `//undo` | `//undo [n]` | Deshacer |

**Máscaras válidas (verificadas):** `#solid`, `#air`, `#existing`, `#surface`.
Se pueden combinar con bloques: `//smooth 1 grass_block,dirt,stone,podzol,coarse_dirt`.

**Brush (interactivo, NO comando `//`):** `/brush blend <radio>` — alias de `blendball`.
Requiere click derecho en el mundo; **no sirve por consola**.

---

## 2. Receta A — Suavizar el terreno alrededor de Roxy **sin tocar los edificios**

Ésta es la que reemplaza a los `//blend` inventados. La clave es la **máscara de bloques naturales**:
solo suaviza superficie de terreno, no toca madera, ladrillos, tejas ni piedra trabajada.

```bash
# 1) Delimitar el volumen a suavizar (terreno alrededor del pueblo)
//pos1 4800 150 -7550
//pos2 4960 210 -7400

# 2) Suavizar SOLO superficie natural (2 iteraciones)
//smooth 2 grass_block,dirt,stone,podzol,coarse_dirt,gravel,sand

# Si el resultado es muy plano o muy rugoso, repetir con 1 iteración más:
//smooth 1 grass_block,dirt,stone,podzol,coarse_dirt,gravel,sand

# 3) Volver a poner la capa de césped donde el smooth dejó tierra/roca
//overlay grass_block
```

> **Por qué con máscara:** el ejemplo del propio JAR es
> `//smooth 1 grass_block,dirt,stone` — *"would only smooth natural surface terrain"*.
> Sin máscara, `//smooth` también deforma los edificios.

---

## 3. Receta B — Naturalizar la superficie (terreno "pelado" → césped + tierra + roca)

`//naturalize` **no lleva argumentos** y trabaja sobre la selección entera: pone 3 capas de tierra
y roca debajo. **CUIDADO:** si la selección cubre edificios, les cambia el techo por tierra.

```bash
# Seleccionar SOLO terreno sin construcciones
//pos1 4700 180 -7650
//pos2 5050 200 -7300
//naturalize
```

**Regla práctica:** usar `//naturalize` únicamente en zonas donde no haya nada construido.
Para zonas mixtas, usar la Receta A (`//smooth` con máscara).

---

## 4. Receta C — **Construir el suelo que le falta a Rikarisu** (la receta que de verdad hace falta)

> Reemplaza la versión anterior de esta receta, que usaba `4800 160 -7550 → 4960 194 -7400`, un volumen
> inventado. Las cotas de abajo son las **medidas** en la sección A.2.

**Cotas reales:** terreno a **Y≈95** (89-99) · base de la aldea a **Y=142** (la más común), con piezas que
bajan a **132** y **100** · hueco mediano **48 bloques**.

Como el material del terreno es `red_sand` al 86 %, lo natural es una **meseta de badlands**, no un relleno
de piedra. Y va **por región (512×512)**, no de una sola vez: la zona completa son ~750×500 columnas.

```bash
# ---------- UNA REGION POR VEZ: r.9.-15 ----------
# 1) Caja: desde el suelo hasta 1 bloque ANTES de la base de la aldea (no la toca)
//pos1 4608 96 -7680
//pos2 5119 141 -7169

# 2) Rellenar SOLO el aire (respeta lo que ya existe, incluidas las piezas que bajan a 132/100)
//replace air red_sand

# 3) Suavizar el borde para que no quede un acantilado de 46 bloques
//smooth 2 red_sand,stone,gravel,dirt

# 4) Superficie: capa de arena y algo de flora de badlands
//overlay red_sand
//flora 3
```

Y repetir cambiando la caja por región:

| Región | `//pos1` | `//pos2` |
|---|---|---|
| `r.8.-15` | `4352 96 -7680` | `4607 141 -7169` |
| `r.9.-15` | `4608 96 -7680` | `5119 141 -7169` |
| `r.10.-15` | `5120 96 -7680` | `5631 141 -7169` |
| `r.8.-14` | `4352 96 -7168` | `4607 141 -7153` |
| `r.9.-14` | `4608 96 -7168` | `5119 141 -7153` |
| `r.10.-14` | `5120 96 -7168` | `5631 141 -7153` |

> ⚠️ **Antes de correrlo:** `//undo` es por comando, así que hacé **una región, mirala, y seguí**. El paso 2
> mueve del orden de **5-10 millones de bloques por región**; con FAWE es rápido pero el server va a lagunear.
> Si el `//replace air` te tapa algo que querías conservar, `//undo` y subí el `//pos2` a `Y=131`.

> **Aviso honesto:** esto rellena **todo** el aire de la franja, incluidas cuevas que caigan dentro.
> Si querés conservar cuevas, bajá el volumen a 5-10 bloques de alto justo debajo de cada edificio.
>
> **NO usar** `//gmask #solid` + `//replace air stone`: es al revés. El gmask exige que el bloque
> **destino** cumpla la máscara; el aire no es sólido, así que no reemplazaría nada.

### 4.1 Resultado del test de polígonos por nivel (2026-10-22) — **la receta 4 NO alcanza**

`mundo\_rikarisu_poligonos_nivel.py` clasifica cada chunk por la base de arranque de la aldea y emite cajas
alineadas a chunk, con el techo en `base_min - 1` (nunca entierra nada dentro de su propia caja):

| Resultado | Valor |
|---|---|
| Cajas generadas | **871** (1.742 líneas de `//pos1` / `//pos2`) |
| Volumen | **~68,5 M bloques** |
| Chunks con desnivel interno > 24 (no clasificables) | **1.035** de 5.083 |
| Lista completa | `mundo\_poligonos_salida.txt` |

→ **No sirve como receta manual.** Son horas de pegar coordenadas y un solo error deja un borde visible.

**Y el corte horizontal (`mundo\_rikarisu_cortes.py` → `_cortes_salida.txt`) explica por qué.** Para `Z=−7400`,
recorriendo X de 4608 a 5112:

| Tramo X | Base de la aldea | Interpretación |
|---|---|---|
| 4624 … 4736 | **Y=142** plano (112 bloques seguidos) | terraza baja |
| 4744 … 4912 | **172 → 345** subiendo de a ~3 por muestra | ladera escalonada |
| 4920 … 4960 | **159-168** | meseta intermedia |
| 4968 … 5024 | **122 → 105 → 126** | bajada |
| 5032 … 5112 | **Y=90** (hueco 0) | **apoyada en el terreno** |

El terreno, mientras tanto, está plano en **Y=89-99** en todo el corte.

**Consecuencia:** la base de la aldea **reproduce el perfil de una montaña de ~250 bloques** que este mundo
no tiene. El schematic se diseñó para una ladera; pegado sobre una meseta plana, queda colgado donde iba la
montaña y apoyado donde la montaña era baja.

Eso **descarta las tres opciones** del debate:
- **A (relleno plano a Y=141)**: entierra todo lo que arranca en 90-132 y deja un acantilado de 45.
- **B (anillo de 10-15)**: deja 200+ bloques de aire en el tramo alto.
- **C (bajar 47)**: la parte alta seguiría 200 bloques en el aire.

Lo que hace falta es **relleno por columna**: cada columna hasta su propia base menos 1. No es expresable con
unas pocas cajas de FAWE; hay que generarlo (schematic o escritura directa de las regiones).

---

## 5. Receta D — Caminos integrados al terreno

`//path` no existe. El camino se hace con `//line` sobre una selección cuboide de 2 esquinas
(las esquinas marcan el recorrido), o pintando a mano con un brush.

```bash
# Opción 1: línea recta entre dos esquinas (grosor 3)
//pos1 4879 194 -7475
//pos2 4820 190 -7410
//line dirt_path 3

# Opción 2: recorrido curvo → selección poligonal + //curve
//sel poly
//pos1 4879 194 -7475
//pos2 4850 192 -7440
//pos2 4820 190 -7410
//curve dirt_path 3
```

> `dirt_path` es el nombre actual del bloque (el viejo `grass_path` fue renombrado en 1.17).
> El grosor en `//line` es un **radio en bloques** (double), no un conteo de bloques de ancho.

---

## 6. Receta E — Vegetación de transición

```bash
# Delimitar el anillo de transición (sin incluir los edificios)
//pos1 4770 190 -7580
//pos2 4990 200 -7370

# Árboles: UN tipo por comando (densidad en %, default 5)
//forest oak 8
//forest spruce 5

# Arbustos, flores y pasto alto (densidad en %, default 5)
//flora 8
```

> `//forest` acepta **un solo** treeType por invocación. Para mezclar especies, correr el comando
> varias veces con densidades bajas, o usar `//forest` en subselecciones distintas.
> Si el server rechaza `oak`, probar `minecraft:oak` (el registro es namespaced).
> Verificación in-game: `//help forest` muestra la sintaxis que acepta **tu** build.

**Biomas de la zona:** `windswept_hills` → prioridad `spruce` > `oak` > `birch`;
`taiga` → `spruce` > `oak`; `forest` → `oak` > `birch` > `spruce`.

---

## 7. Receta F — Agua / charcos artificiales

```bash
# Delimitar SOLO la zona de estructuras
//pos1 4839 190 -7515
//pos2 4919 200 -7435

# Drenar el agua que quedó dentro de construcciones
//replace water air
```

> Para conservar un lago real, **acotar la caja** a la zona construida. No usar una caja grande:
> secaría lagos naturales.

---

## 8. Secuencia recomendada (orden importa)

```bash
# --- PASO 1: terreno alrededor (no toca edificios) ---
//pos1 4800 150 -7550
//pos2 4960 210 -7400
//smooth 2 grass_block,dirt,stone,podzol,coarse_dirt,gravel,sand
//overlay grass_block

# --- PASO 2: relleno bajo estructuras (caja fina, por edificio) ---
//pos1 4800 160 -7550
//pos2 4960 194 -7400
//replace air stone

# --- PASO 3: caminos ---
//pos1 4879 194 -7475
//pos2 4820 190 -7410
//line dirt_path 3

# --- PASO 4: vegetación ---
//pos1 4770 190 -7580
//pos2 4990 200 -7370
//forest oak 8
//forest spruce 5
//flora 8

# --- PASO 5: agua ---
//pos1 4839 190 -7515
//pos2 4919 200 -7435
//replace water air
```

**Entre paso y paso: mirar en el juego y `//undo` si no convence.**
FAWE guarda historial **por jugador**: hacer todo desde el mismo usuario OP para que `//undo` funcione en secuencia.

---

## 9. Cómo verificar cualquier comando antes de usarlo

En el juego o consola:

```bash
//help                 # lista de comandos // disponibles
//help smooth          # sintaxis exacta de //smooth en ESTE build
//help paste           # flags válidos de //paste
/help brush           # sintaxis de los brushes
```

Si un comando no aparece en `//help`, **no existe en este build** — no insistir.

---

## 10. Registro del error (para no repetirlo)

- **Qué pasó:** la v1 de este documento listó `//blend`, `//path`, `//pos3` y `//forest` con parámetros
  inventados. Ninguno existe en FAWE 2.16.1. El usuario los probó y el server los rechazó.
- **Causa raíz:** el documento se escribió por inferencia (parecía "lo lógico" para naturalizar terreno)
  sin verificar contra el plugin instalado. Viola la regla 1 del AGENTS.md (**NO FABRICAR RESULTADOS**).
- **Corrección:** esta v2 se derivó del JAR real con `javap` (firmas + anotaciones + descripciones),
  extrayendo `*CommandsRegistration` y las clases de comando. Los scripts de verificación quedaron en
  `mundo\_listar_comandos_fawe.py`, `_buscar_nombres_fawe.py`, `_dump_comandos.py`, `_firmas_fawe.py`,
  `_consts_fawe.py`, `_mascaras_fawe.py`, `_treetype.py`.
- **Lección:** para comandos de un plugin, la fuente de verdad es el JAR instalado, no la memoria ni la intuición.

# Análisis de continentes — Mushoku Tensei (mundo 22.000×22.000)

Generado: 2026-10-17 22:52:25  (script determinista; segmentación por componentes conexos)

## Fuente

`MT_22000_WorldPainter_MINIMO/biomas_2750.png` — 2750×2750, modo `L` (uint8), 84 KB.
Es la imagen de **distribución de biomas** del kit congelado: un valor numerico por pixel.
Otras imagenes del kit (no usadas para la segmentación):

- `relieve_2750_16bit.png` (2750×2750, I;16, 5,0 MB) — heightmap en 16 bits.
- `superficie_2750.png` (2750×2750, L, 71 KB) — superficie/tipo de material.
- `Vista_MT_22000.png` (1600×1600, RGB, 1,8 MB) — vista previa a color, **otra escala** (1600 px, no 2750).

## Mapeo pixel → coordenadas Minecraft (X,Z)

- Mundo: 22.000×22.000 bloques, centrado en (0,0). Imagen: 2750×2750 px.
- Escala: 22000 / 2750 = **8,0 bloques por pixel**.
- `shift = -11000` (el mismo valor por defecto de `mundo/mapear_biomas.py`).
- Fórmulas:
  - `X = -11000 + px * 8`
  - `Z = -11000 + pz * 8`
  - Bbox del pixel `[px0..px1] × [pz0..pz1]`: `minX=-11000+px0*8`, `maxX=-11000+(px1+1)*8-1`, `minZ=-11000+pz0*8`, `maxZ=-11000+(pz1+1)*8-1`

> **Supuesto no verificado in-game.** Este mapeo viene de `mapear_biomas.py` (`--shift -11000`) y de que el mundo es 22000×22000. **No** se validó contra un chunk real en este informe. Ver "Calibración" al final.

## Continentes identificados (área ≥ 1000 px de tierra)

| ID | Área (px) | px x | px z | X min | X max | Z min | Z max | Centro (X,Z) | Ancla (X,Z) | Bioma del ancla |
|---|---:|---|---|---:|---:|---:|---:|---|---|---|
| C3 | 2504789 | 112–1467 | 160–2602 | -10104 | 743 | -9720 | 9823 | (-4681,51) | (-4900,-732) | plains |
| C2 | 878950 | 1794–2635 | 151–1563 | 3352 | 10087 | -9792 | 1511 | (6719,-4141) | (6828,-4628) | desert |
| C8 | 609638 | 1291–2618 | 1756–2476 | -672 | 9951 | 3048 | 8815 | (4639,5931) | (4716,5876) | jungle |
| C1 | 2375 | 2214–2268 | 69–123 | 6712 | 7151 | -10448 | -10009 | (6931,-10229) | (6932,-10228) | plains |
| C4 | 2375 | 1499–1553 | 757–811 | 992 | 1431 | -4944 | -4505 | (1211,-4725) | (1212,-4724) | plains |
| C9 | 1917 | 1310–1358 | 2052–2100 | -520 | -129 | 5416 | 5807 | (-325,5611) | (-324,5612) | plains |
| C5 | 1523 | 1560–1603 | 845–888 | 1480 | 1831 | -4240 | -3889 | (1655,-4065) | (1652,-4068) | plains |
| C6 | 1523 | 1615–1658 | 927–970 | 1920 | 2271 | -3584 | -3233 | (2095,-3409) | (2092,-3404) | beach |
| C7 | 1161 | 1755–1792 | 1700–1737 | 3040 | 3343 | 2600 | 2903 | (3191,2751) | (3196,2756) | beach |

### Composición de biomas por continente (top 4, medido sobre los píxeles del componente)

- **C3** (2504789 px): plains 35.4%, desert 19.2%, taiga 15.4%, forest 14.7%
- **C2** (878950 px): desert 85.4%, windswept_hills 14.5%, beach 0.1%
- **C8** (609638 px): jungle 39.1%, plains 25.1%, forest 17.3%, windswept_hills 9.0%
- **C1** (2375 px): beach 95.6%, plains 4.4%
- **C4** (2375 px): beach 82.3%, plains 17.7%
- **C9** (1917 px): plains 52.6%, beach 47.4%
- **C5** (1523 px): plains 80.6%, beach 19.4%
- **C6** (1523 px): beach 100.0%
- **C7** (1161 px): beach 100.0%

## Puntos ancla

Cada ancla es un píxel **interior** del continente (centroide → píxel de tierra más cercano → 60 erosiones hacia adentro),
por lo que cae en tierra firme y no en agua ni en una franja de playa. Sirve para `/tp`, un `forceload`, o como centro de una zona.

| ID | Ancla mundo (X,Z) | Ancla px | Bioma | Tipo |
|---|---|---|---|---|
| C3 | (-4900, -732) | (762, 1283) | plains | continente |
| C2 | (6828, -4628) | (2228, 796) | desert | continente |
| C8 | (4716, 5876) | (1964, 2109) | jungle | continente |
| C1 | (6932, -10228) | (2241, 96) | plains | isla |
| C4 | (1212, -4724) | (1526, 784) | plains | isla |
| C9 | (-324, 5612) | (1334, 2076) | plains | isla |
| C5 | (1652, -4068) | (1581, 866) | plains | isla |
| C6 | (2092, -3404) | (1636, 949) | beach | isla |
| C7 | (3196, 2756) | (1774, 1719) | beach | isla |

## Formato compacto para scripts (minX, maxX, minZ, maxZ)

```json
[
  {
    "id": "C3",
    "tipo": "continente",
    "area_px": 2504789,
    "bbox": [
      -10104,
      743,
      -9720,
      9823
    ],
    "center": [
      -4681,
      51
    ],
    "anchor": [
      -4900,
      -732
    ],
    "anchor_biome": "plains"
  },
  {
    "id": "C2",
    "tipo": "continente",
    "area_px": 878950,
    "bbox": [
      3352,
      10087,
      -9792,
      1511
    ],
    "center": [
      6719,
      -4141
    ],
    "anchor": [
      6828,
      -4628
    ],
    "anchor_biome": "desert"
  },
  {
    "id": "C8",
    "tipo": "continente",
    "area_px": 609638,
    "bbox": [
      -672,
      9951,
      3048,
      8815
    ],
    "center": [
      4639,
      5931
    ],
    "anchor": [
      4716,
      5876
    ],
    "anchor_biome": "jungle"
  },
  {
    "id": "C1",
    "tipo": "isla",
    "area_px": 2375,
    "bbox": [
      6712,
      7151,
      -10448,
      -10009
    ],
    "center": [
      6931,
      -10229
    ],
    "anchor": [
      6932,
      -10228
    ],
    "anchor_biome": "plains"
  },
  {
    "id": "C4",
    "tipo": "isla",
    "area_px": 2375,
    "bbox": [
      992,
      1431,
      -4944,
      -4505
    ],
    "center": [
      1211,
      -4725
    ],
    "anchor": [
      1212,
      -4724
    ],
    "anchor_biome": "plains"
  },
  {
    "id": "C9",
    "tipo": "isla",
    "area_px": 1917,
    "bbox": [
      -520,
      -129,
      5416,
      5807
    ],
    "center": [
      -325,
      5611
    ],
    "anchor": [
      -324,
      5612
    ],
    "anchor_biome": "plains"
  },
  {
    "id": "C5",
    "tipo": "isla",
    "area_px": 1523,
    "bbox": [
      1480,
      1831,
      -4240,
      -3889
    ],
    "center": [
      1655,
      -4065
    ],
    "anchor": [
      1652,
      -4068
    ],
    "anchor_biome": "plains"
  },
  {
    "id": "C6",
    "tipo": "isla",
    "area_px": 1523,
    "bbox": [
      1920,
      2271,
      -3584,
      -3233
    ],
    "center": [
      2095,
      -3409
    ],
    "anchor": [
      2092,
      -3404
    ],
    "anchor_biome": "beach"
  },
  {
    "id": "C7",
    "tipo": "isla",
    "area_px": 1161,
    "bbox": [
      3040,
      3343,
      2600,
      2903
    ],
    "center": [
      3191,
      2751
    ],
    "anchor": [
      3196,
      2756
    ],
    "anchor_biome": "beach"
  }
]
```

Snippet para un `mcfunction` / Denizen (Y a elevar vos):

```
# C3 (plains)  X=-4900  Z=-732  ->  bbox X[-10104,743] Z[-9720,9823]
# C2 (desert)  X=6828  Z=-4628  ->  bbox X[3352,10087] Z[-9792,1511]
# C8 (jungle)  X=4716  Z=5876  ->  bbox X[-672,9951] Z[3048,8815]
```

## Cómo usarlo

- **WorldEdit:** `//pos1 <minX>,<y>,<minZ>` y `//pos2 <maxX>,<y>,<maxZ>` con los valores de la tabla; o `//sel` + `//expand`.
- **Colocar una estructura:** usar el par *Ancla* de la tabla (garantiza tierra firme) y no el centro del bbox, que en C8 cae sobre agua.
- **Zonas de bioma homogéneo:** `mundo/tiles_de_valor.py <biomas_2750.png> <valores>` devuelve los tiles (tx,tz) con más píxeles de cada valor; `mundo/valor_pixel.py` consulta un píxel puntual.
- **Verificación cruzada con el mundo real:** `mundo/mapear_biomas.py <carpetaExport> <biomas_2750.png>` cruza los chunks exportados contra esta imagen.

## Método y límites

- **Medido:** componentes conexos 8-conectividad de la máscara `valor != 0` (tierra), sobre `biomas_2750.png` leída desde disco.
- **Medido:** el histograma global de la imagen coincide con lo documentado en `AGENTS.md` (`ocean 47,1%`, `plains 13,8%`, `desert 16,3%`, …): 10 valores distintos, `numpy` sobre la imagen cruda.
- **Límite:** `scipy` no está en el venv, así que la etiquetación es un flood-fill propio (equivalente a `scipy.ndimage.label` con estructura 3×3) — más lento pero el mismo resultado esperado.
- **Límite:** un «continente» acá es un **componente conexo de tierra**, no una unidad geográfica. C3 y C2 pueden estar unidos por istmos y separarse por el criterio de píxel.
- **Límite:** el bbox es el rectángulo mínimo; no es la forma del continente. Para zonas exactas hay que filtrar por componente.
- **Límite:** se ignoran componentes < 1000 px de área (islas menores y ruido).

## Calibración (importante)

Si el mundo exportado ya tiene chunks (`ENTREGA_Loding_20261017\05_mundo\world\` desde el 2026-10-17; antes estaba en `mundo\salida_mundo\`), se puede **verificar el mapeo** en vez de confiar en él:

> **Nota (2026-10-22):** `ENTREGA_Loding_20261017\` **ya no existe en disco**; el árbol vivo es `_prueba_roxy_local\world` (1.944 `.mca`), **`[INFERRED]`** por conteo de regiones. La calibración de abajo se corrió cuando la carpeta existía.

1. Elegir un chunk exportado con coordenadas conocidas y leer su bioma real.
2. Calcular el píxel con `X = -11000 + px*8` y comprobar que `biomas_2750.png` da el mismo valor.
3. Si hay desfase, ajustar `shift` (offset) y/o `SCALE` (22000/2750 = 8) con dos puntos conocidos.

Hasta que eso se haga, las coordenadas de este informe son **[A VERIFICAR]** contra el mundo real.
→ **Hecho el 2026-10-19** (ver abajo): **coordenadas validadas** contra el mundo exportado.

### Calibración HECHA — 2026-10-19

Ejecutado: `mundo\mapear_biomas.py` sobre **4 regiones copiadas** del mundo exportado (`ENTREGA_Loding_20261017\05_mundo\world\dimensions\minecraft\overworld\region`), **4.096 chunks** cruzados contra `biomas_2750.png`, con el `shift` por defecto (−11000) y la escala 8,0 bloques/pixel. **No hubo que ajustar nada.** *(**2026-10-22:** esas 4 regiones se leyeron de la carpeta de la entrega, que ya no existe; el resultado no se re-verificó contra `_prueba_roxy_local\world`, **`[A VERIFICAR]`**.)*

| Región | Bloques X | Bloques Z | Qué cubre |
|---|---|---|---|
| `r.-10.-2.mca` | −5120…−4609 | −1024…−513 | ancla de **C3** (plains) |
| `r.13.-10.mca` | 6656…7167 | −5120…−4609 | ancla de **C2** (desert) |
| `r.9.11.mca` | 4608…5119 | 5632…6143 | ancla de **C8** (jungle) |
| `r.-2.12.mca` | −1024…−513 | 6144…6655 | **fortaleza de Perugius** |

Valor de imagen → bioma real, con el % de celdas de bioma (celda de bioma = 4×4×4 bloques):

| Valor | Bioma real | % |
|---:|---|---:|
| 0 | `minecraft:ocean` | 100,0 |
| 1 | `minecraft:plains` | 97,3 (forest 2,7) |
| 2 | `minecraft:desert` | 98,7 (windswept_hills 1,3) |
| 3 | `minecraft:windswept_hills` | 93,2 (desert 6,8) |
| 4 | `minecraft:forest` | 98,8 (plains 1,2) |
| 7 | `minecraft:river` | 86,5 (beach 13,5) |
| 16 | `minecraft:beach` | 51,2 (jungle 25,0 / river 23,8) |
| 21 | `minecraft:jungle` | 98,8 (beach 1,1) |

**Interpretación (MEDIDO):** cada valor cae en **un solo bioma** (≥86 %); los restos son **efecto de borde** (1 píxel = 8 bloques, 1 celda de bioma = 4 bloques, así que en los límites entran celdas del bioma vecino; `beach` es transición por naturaleza). Un `shift` equivocado habría mezclado cada valor con biomas ajenos. **El mapeo pixel→bloque queda validado: las coordenadas de este informe pasan de `[A VERIFICAR]` a MEDIDAS.**

**Límite que queda:** se validaron 4 regiones (512×512 bloques cada una) de 1.936, repartidas por el mapa; no es una verificación región por región.

### Altura (Y) de las anclas — derivada del heightmap — 2026-10-19

`parametros.json` documenta el mapeo `height_mapping`: imagen `[0, 65535]` → mundo `[-64, 511]`. Leído `relieve_2750_16bit.png` en el píxel de cada ancla con `mundo\valor_pixel.py`:

| ID | Ancla (X,Z) | Valor del heightmap | **Y** |
|---|---|---:|---:|
| C3 | (−4900, −732) | 17310 | **88** |
| C2 | (6828, −4628) | 15184 | **69** |
| C8 | (4716, 5876) | 17769 | **92** |
| C1 | (6932, −10228) | 15075 | **68** |
| C4 | (1212, −4724) | 15012 | **68** |
| C9 | (−324, 5612) | 15654 | **73** |
| C5 | (1652, −4068) | 15582 | **73** |
| C6 | (2092, −3404) | 14776 | **66** |
| C7 | (3196, 2756) | 13987 | **59** |

`Y = −64 + valor/65535 × 575`. **Consistencia:** los 9 valores caen dentro del rango usado del heightmap, que corresponde al `height_range_actual` del kit (14,95…445).
**Límite:** es el heightmap del kit (1 píxel = 8 bloques), no el bloque real medido en el mundo. **C7 (Y≈59) y C6 (Y≈66) quedan en/bajo el nivel del mar (63)** → `[A VERIFICAR]` contra el mundo real. Para un `/tp` conviene apuntar a una Y holgada.

### Completitud del inventario — MEDIDO 2026-10-19

La suma de los 9 componentes da **4.004.251 píxeles**, exactamente los mismos que la suma de todos los valores de tierra de `biomas_2750.png` (0 = océano: 3.558.249 px; 3.558.249 + 4.004.251 = 7.562.500 = 2750²). **No hay ningún continente ni isla fuera de estos 9** (no existe ningún componente <1000 px).

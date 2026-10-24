# Ubicación reservada — Fortaleza de Perugius

**Estado: PEGADA (reportado por el usuario, 2026-10-18).** El pegado (Fase 9) lo hizo el usuario sobre el **servidor oficial**. **No verificado en disco local:** en este repo no hay ningún `.mca` con fecha posterior al export del 2026-10-17 17:48, así que el pegado **no** cayó sobre esta copia local (coherente con que desde el 2026-10-18 los cambios van directo al server oficial).

- Análisis del terreno: **2026-10-17** (barrido por ventanas sobre el heightmap del kit).
- Registro de este documento: **2026-10-18**.
- Medición re-ejecutada y confirmada: **2026-10-18** (evidencia de disco de este turno; ver "Método").
- **Punto de aparición (spawn/teleport) dentro de la fortaleza: `−1016 227 6417`** — definido por el usuario el **2026-10-18**. Ver §1.1.

---

## 1. Punto reservado (recomendado)

| Dato | Valor |
|---|---|
| **Centro horizontal** | **X = −916 · Z = 6316** |
| **Ancla de pegado (`//paste`)** | **(−916, 150, 6316)** |
| Región | estrecho de agua abierta entre el continente central y el continente del sur |
| Distancia al spawn (0,0) | **6.382 bloques** |
| Margen al world border | **4.458 bloques** (border a ±10.900 desde 0,0) |

**Por qué acá:** es agua abierta sobre la que la isla flotante no intersecta nada (silueta limpia), el fondo del mar queda muy por debajo (nada sobresale del nivel del mar dentro del footprint), y está lejos del spawn y con holgura al borde.

### 1.1. Punto de aparición — dónde caen los jugadores al teletransportarse

| Dato | Valor |
|---|---|
| **Punto** | **X = −1016 · Y = 227 · Z = 6417** |
| Definido por | el usuario, **2026-10-18** |
| Encaja en el footprint | Sí: X −1016 ∈ [−1041, −791] ✔ · Y 227 ∈ [132, 421] ✔ · Z 6417 ∈ [6191, 6442] ✔ |
| Uso | destino de `/tp` (o warp) cuando un jugador se teletransporta a la fortaleza |

Relativo al ancla de pegado (−916, 150, 6316): **ΔX = −100 · ΔY = +77 · ΔZ = +101** (dentro de la selección, que ocupa ±125 en X y Z).

> **[A VERIFICAR en juego]** No se probó con un `/tp` real que (−1016, 227, 6417) sea aire con piso sólido debajo: es la posición que indicó el usuario. **Medido en el mapa de biomas (2026-10-18):** debajo de ese punto hay **agua** — pixel (1248, 2177) de `MT_22000_WorldPainter_MINIMO\biomas_2750.png` = valor `0` = `ocean`. Está en el estrecho entre el continente central (C3) y el del sur, así que **el piso lo aporta la propia fortaleza pastada, no el terreno**. (Contraste: el spawn **por defecto del mundo**, centro del mapa `0,0`, sí cae en tierra — pixel (1375, 1375) = valor `1` = `plains`, dentro del continente central.)

---

## 2. Geometría de la fortaleza (del schematic)

Fuente: `perugius\fortaleza_perugius_v1.md` y `README_ENTREGA.md`.

| Dato | Valor |
|---|---|
| Bbox sólido (X,Y,Z) | **247 × 286 × 248** |
| Selección (X,Y,Z) | **251 × 290 × 252** |
| Bloques no-aire | 560.892 |
| Origen (en la fuente) | (−499, 48, 379) = centro horizontal + fondo de la isla |
| **Offset** | **[−125, −18, −125]** |

Al pegar con el ancla en `(Xc, Y₀, Zc)`, la **selección** ocupa:

- X: `Xc − 125` … `Xc + 125`  →  con Xc = −916: **−1041 … −791**
- Z: `Zc − 125` … `Zc + 125`  →  con Zc = 6316: **6191 … 6442**
- Y: `Y₀ − 18` … `Y₀ + 272`  →  con Y₀ = 150: **132 … 421** (290 de alto)

El sólido queda un poco más adentro: aproximadamente `Y₀ − 16 … Y₀ + 269` (**134 … 419**).

---

## 3. Elección de la altura (Y₀)

| Referencia | Valor |
|---|---|
| Nivel del mar | **63** (`parametros.json` → `sea_level: 63`) |
| Techo del mundo | **511** (alto de build = 576 desde y=−64) |
| Fondo de la isla (ancla) | **Y₀ = 150** |
| Tope de la selección | **421** (150 + 271), deja **90 bloques** bajo el techo |
| Bloque sólido más bajo | **134** → la fortaleza **flota 71 bloques sobre el mar**, sin tocar el agua |

**Regla aplicada:** `Y₀ ≤ 234` (para que el tope quede bajo 511) y `Y₀ ≥ ~90` (para que flote claro sobre el mar). **150** es el valor recomendado.

---

## 4. Verificación del terreno en el punto (medido 2026-10-18)

Ventanas centradas en (−916, 6316) sobre el heightmap del kit:

| Ventana | % océano | maxY (relieve) | minY (fondo) |
|---|---|---|---|
| ±160 blk | **100,00 %** | 54,4 | 34,2 |
| ±200 blk | **100,00 %** | 58,6 | 33,7 |
| ±260 blk | **99,43 %** | **63,0** | 33,6 |
| ±320 blk | 95,82 % | 65,2 | 33,5 |

Lectura: dentro de ±200 bloques **no hay tierra** y el relieve más alto (58,6) queda **bajo** el nivel del mar. Incluso a ±260 el relieve más alto (63,0) apenas alcanza el nivel del mar: **nada sobresale del agua** en el entorno inmediato. El fondo (~34) está **~29 bloques** por debajo del mar.

Comprobación del footprint real (selección 251×252 → ±126): a ±145 bloques → **100,00 % océano**, maxY 53,1.

Mapa local (800 blk de ancho, `~`=agua, `#`=tierra, `S`=centro):

```
  #####################~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ####################~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ###################~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  #################~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ###################~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  #################~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ###############~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ##############~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ############~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ###########~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ##########~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ######~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~S~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  #####~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ###~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ##~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  #~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
```

El centro queda en aguas abiertas; el continente está al oeste (a ~320 bloques) y hacia el este/sur el océano sigue abierto.

---

## 5. Alternativas (por si se prefiere otro punto)

| Centro (X, Z) | ±160 blk | ±260 blk | Dist. spawn | Margen border |
|---|---|---|---|---|
| **−916, 6316** (recomendado) | 100 % océano, max 54 | 99,4 % océano, **max 63** | 6.382 | **4.458** |
| −7316, 2732 (bahía costa oeste) | 100 % océano, max 53 | 99,9 % océano, max 70 | 7.809 | 3.458 |
| −3860, 8236 (bahía cerrada al SO) | 100 % océano, max 55 | 98,5 % océano, max 74 | 9.096 | 2.538 |

El recomendado es el **único** en el que, dentro de ±260 bloques, **nada supera el nivel del mar** (max 63; los otros llegan a 70 y 74). Es también el que más margen tiene al borde entre los dos que quedan lejos del spawn.

---

## 6. Comprobación del world border

Borde: diámetro **21.800** centrado en (0,0) → límites **±10.900**. Semiancho del footprint = **126** (X y Z). Condición: `|coord| + 126 ≤ 10.900`.

- X: `|−916| + 126 = 1.042` ✔
- Z: `|6316| + 126 = 6.442` ✔  ← eje que manda
- Margen libre: `10.900 − 6.442 = 4.458` bloques.

---

## 7. Cómo se pegó (Fase 9 — ejecutada 2026-10-18)

1. Trabajar sobre el **mundo exportado** (`world`), **con backup previo** y **sin jugadores** conectados ni en la zona.
2. Copiar `perugius\fortaleza_perugius_v1.schem` a `plugins\FastAsyncWorldEdit\schematics\` (o `WorldEdit\schematics\`).
3. Pararse en el ancla: `-916 150 6316`.
4. `//schem load fortaleza_perugius_v1`
5. `//paste`  (FAWE; **no** usar `//paste -a` a ciegas: ignorar aire puede dejar agua/terreno dentro de las habitaciones — probar ambas variantes en una copia).
6. Esperar a que termine, `/relight` y revisar interiores, líquidos, iluminación y compatibilidad Bedrock.
7. Proteger con WorldGuard.

---

## 8. Advertencias y pendientes

- **[A VERIFICAR]** Orientación del frente/entrada del schematic: **no medida**. El agua del estrecho se abre hacia el sur; conviene elegir la rotación (`//rotate`) mirando las capturas (`captura_frontal.png`, `captura_entrada.png`) al momento del pegado.
- **[A VERIFICAR]** Que el **ancla del schematic caiga exactamente en la posición del pegado en FAWE** (el offset `[−125,−18,−125]` se toma del `README_ENTREGA.md`; conviene confirmarlo con un pegado de prueba en una copia).
- Este análisis es sobre el **heightmap del kit** (fuente del terreno). El mundo exportado agrega cuevas y vegetación, pero la **superficie coincide**; sobre mar abierto no hay vegetación.
- **Pegado hecho (2026-10-18, reportado por el usuario).** El mundo quedó completo el 2026-10-17 (1.936 regiones, ver `AGENTS.md`). El pegado se hizo sobre el **server oficial**; en esta copia local no hay evidencia del pegado (ningún `.mca` posterior al export).

---

## 9. Método (reproducible)

- Heightmap: `MT_22000_WorldPainter_MINIMO\relieve_2750_16bit.png` (2750 px = 22.000 bloques → **8 bloques/píxel**).
- Biomas: `MT_22000_WorldPainter_MINIMO\biomas_2750.png` (valor `0` = océano).
- Mapeo de altura: `mundo_y = −64 + (px / 65535) × 575` (de `parametros.json`: imagen `[0,65535]` → mundo `[−64,511]`; `sea_level: 63`).
- Conversión bloque→píxel: `px = bloque / 8 + 1374,5`.
- Scripts: `%TEMP%\opencode\fort_final.py` y `fort_prim.py` (usados el 2026-10-18); el barrido completo original, `fort_candidates.py` / `fort_score.py` / `fort_verify.py`.
- Intérprete: `tesis_neuronal\.venv\Scripts\python.exe` (numpy + PIL).

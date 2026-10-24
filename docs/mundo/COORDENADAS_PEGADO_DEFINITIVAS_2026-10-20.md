# Coordenadas de pegado — estructuras nuevas (2026-10-20)

**Ámbito:** las 9 piezas `.schem` sueltas, el pueblo **Roxy village** (103 `.schem`),
los **laberintos** y las **trial chambers** (estas últimas ya documentadas aparte).
Medido contra el mundo real de la entrega
(`ENTREGA_Loding_20261017\05_mundo\world`, 1.936 `.mca`).

> **Nota al pie (2026-10-22):** `ENTREGA_Loding_20261017\` **ya no existe en disco** (verificado en `C:\Dev\OPENCLAUDIO\minecraft_server\`, `C:\Dev\OPENCLAUDIO\` y `E:\OPENCLAUDIO_archivo\`). Las coordenadas se midieron sobre ese mundo cuando existía y **no se re-midieron** contra el árbol vivo `_prueba_roxy_local\world` (1.944 `.mca`) **`[A VERIFICAR]`**.

> **Este documento NO ejecuta el pegado.** Da las coordenadas para que las use
> quien pegue. El `//paste` en vivo requiere cliente conectado: la consola de
> Paper 26.2 por stdin no-terminal es defectuosa (ver `CALIBRACION_MAPEO_2026-10-19.md`).

---

## Procedencia (leer primero)

- **Mapeo pixel→bloque CONFIRMADO:** `X = -11000 + px*8` sobre `biomas_2750.png` (2750 px),
  99,3 % de aciertos (2026-10-19, `mundo\CALIBRACION_MAPEO_2026-10-19.md`).
- **Mapa visual con los nombres temáticos:** `MT_22000_WorldPainter_MINIMO\Vista_MT_22000.png`
  (1600 px → 13,75 bl/px). Tiene los rótulos **CONTINENTE CENTRAL, CONTINENTE DEMONIACO,
  BEGARITT, MILIS, MAR INTERIOR**. **No está rotada** (IoU 0,85 identidad). El mapeo de sus
  rótulos a las anclas C1–C9 quedó medido el 2026-10-20:

| Continente (rótulo) | Ancla medida (X,Z) | Bioma | Y superficie |
|---|---|---|---|
| CONTINENTE CENTRAL | (−4720, −2224) | plains/forest | 93 |
| CONTINENTE DEMONIACO | (6828, −4628) | desert 100 % | 70 |
| BEGARITT | (−5040, 5360) | desert 93 % | 91 |
| MILIS | (4716, 5876) | jungle 44 % | 97 |

- El mundo es *"interpretación original inspirada en Mushoku Tensei; no copia exacta ni
  mapa oficial"* (`parametros.json`).
- **Asignación decidida por el usuario (2026-10-20):**
  - **7 piezas temáticas** → CONTINENTE CENTRAL (C3), centros propuestos
    (−4900,−732) tomados como definitivos.
  - **Roxy village** → CONTINENTE DEMONIACO (zona virgen).
  - **Laberintos** → BEGARITT.

---

## Comandos (FAWE)

Sintaxis confirmada en la doc oficial de FastAsyncWorldEdit
(`github.com/intellectualsites/fastasyncworldedit/wiki/Commands`):

```text
//schem load <nombre>          # sin extensión; busca en plugins/FastAsyncWorldEdit/schematics/
//pos1 <X>,<Y>,<Z>             # esquina de pegado (formato con comas)
//paste -o                     # pega en la posición ORIGINAL (la de pos1)
```

Flags de `//paste`: `-a` (omite aire), `-b` (biomas), `-e` (entidades), **`-o` (pegar en
origen)**, `-s` (seleccionar lo pegado).

> **Aviso del proyecto sobre `-a`:** *"ignorar el aire puede dejar agua/terreno dentro de
> las habitaciones"*. Para piezas que son puro edificio conviene `-a`; para Roxy (que **es**
> terreno) **NO** usar `-a`.

> La forma `//paste at <X> <Y> <Z>` que usa `TRIAL_CHAMBERS_COORDENADAS.md` no figura en la
> doc oficial — **[A VERIFICAR]**. La documentada es `//pos1 <X>,<Y>,<Z>` + `//paste -o`.

### ⚠️ Los `.schem` traen Offset NO-cero (medido 2026-10-20)

El campo `Offset` (que en WorldEdit viene del "Origin" de la captura) define dónde cae el
bloque (0,0,0) del schematic. **Medido, es distinto en cada pieza:**

| Pieza | Offset |
|---|---|
| `stbasinecathedral.schem` | `[−179, −37, 63]` |
| `eterna-the-grand-libraryupd2.schem` | `[−11, −8, −263]` |
| `trueColors-from-abfielder.schem` | `[−25, −2, 1]` |
| `rangersguild1.schem` | `[172, 1, −1]` |
| `wizard-tower.schem` | `[520, 115, 853]` |
| `fantasy-greenhouse.schem` | `[0, 0, 0]` |
| `medievalbar.schem` | `[−13, 0, −11]` |
| `compact_trial_chamber.schem` | `[269, 191, 736]` |

**Consecuencia:** `//paste` (y también `-o`) desplazan el pegado según el offset, así que
las coordenadas de la tabla NO son las de `//pos1` directo. Dos opciones:
1. **Normalizar el Offset a `[0,0,0]`** reescribiendo los `.schem` (bloque (0,0,0) = esquina
   NW-inferior). Entonces `//pos1 <X>,<Y>,<Z>` + `//paste` pega exacto. **Requiere tocar los archivos.**
2. **Calcular `//pos1 = centro − dims/2 − offset`** por pieza (más frágil).

> **El "centro" de la tabla es el centro deseado en el mundo. La coordenada de `//pos1`
> exacta depende de qué opción se elija.** Pendiente de decisión del usuario.

---

## Coordenadas

### 1. Piezas temáticas — CONTINENTE CENTRAL (C3)

Layout **reajustado el 2026-10-20: 0 solapes entre las 7 cajas**. Los centros viejos
solapaban (stbasine↔wizard-tower 32×36, eterna↔trueColors 50×59, eterna↔medievalbar 14×18);
los de abajo ya están separados. Y medida con huella = dims exactas de cada pieza.

| # | Archivo | Dim (W×H×L) | Centro (X,Z) | Caja X | Caja Z | Y base | Zona |
|---|---|---|---|---|---|---|---|
| 1 | `stbasinecathedral.schem` | 92×107×205 | **(−5620, −745)** | −5666..−5574 | −847..−643 | **84** | C3 SW-lejos |
| 2 | `eterna-the-grand-libraryupd2.schem` | 275×118×275 | **(−4880, −543)** | −5017..−4743 | −680..−406 | **86** | C3 N |
| 3 | `trueColors-from-abfielder.schem` | 51×109×105 | **(−4680, −861)** | −4705..−4655 | −913..−809 | **96** | C3 E |
| 4 | `rangersguild1.schem` | 53×39×39 | **(−4650, −712)** | −4676..−4624 | −731..−693 | **88** | C3 E |
| 5 | `wizard-tower.schem` | 33×85×36 | **(−5620, −560)** | −5636..−5604 | −578..−542 | **89** | C3 SW |
| 6 | `fantasy-greenhouse.schem` | 99×36×73 | **(−4860, −760)** | −4909..−4811 | −796..−724 | **96** | C3 N |
| 7 | `medievalbar.schem` | 14×17×18 | **(−4720, −585)** | −4727..−4713 | −594..−576 | **85** | C3 N |

Separaciones mínimas entre cajas: ≥36 bloques en Z (las piezas de fondo grande),
≥30 entre las demás.

**Notas (MEDIDO):**
- Todas las huellas caen en **tierra seca**. `wizard-tower` (dominante 89 al 75,4 %) y
  `medievalbar` (85 al 79,3 %) son las más planas.
- Hay desnivel dentro de las huellas grandes (p95−p05 ≈ 9–12): va a hacer falta terrazado
  o relleno, sobre todo en `stbasinecathedral` (rango 63–96).
- **Y base** = nivel dominante de la superficie en la huella (para que la base apoye a nivel).

> La Y es del punto medido; re-verificar con `mundo\medir_superficie.py` si se corre el centro.

### 2. Roxy village — CONTINENTE DEMONIACO (C2)

**⚠️ ACTUALIZADO 2026-10-21:** el pegado real se ejecutó por **reescritura NBT offline** (no FAWE `//paste`),
y las coordenadas reales difieren de la fórmula prevista abajo. Ver resultado real más abajo.

---

#### 2a. Fórmula PREVISTA (no ejecutada) — vía FAWE `//paste`

Mosaico contiguo de **3072 (X) × 9216 (Z)** bloques (6×18 regiones de 512).
103 `.schem` en `plugins/Litematica/schematics/roxy_village/`.

**Ancla base prevista:** `(X_base, Y_base, Z_base) = (5500, -64, -9000)`

- El origen de cada `.schem` es Y = −64 (bedrock del mundo original).
- El continente demoníaco (C2, ancla 6828, −4628) tiene tierra desde Z ≈ −9792 hasta 1511.
  El mosaico cubriría **X 5500..8572**, **Z −9000..216** → entra completo en C2.
- Muestreo 200×200 en la zona norte (Z −8200): Y dominante 103 (plano), medan 103.
  La cordillera central (Z −7000..−5000, Y 200–350) la cruzarían los `.schem` del sur.

**Fórmula de pegado prevista (por archivo `roxy_r.<rx>.<rz>.schem`):**
```text
//pos1 <5500 + rx*512>,<-64>,<-9000 + rz*512>
//paste -o
```
Rango rx: **−3..2** (6 valores) · rz: **−12..5** (18 valores). Total 103 operaciones.

**Ejemplos previstos:**
- `roxy_r.-3.-12.schem` → `//pos1 5500,-64,-9000` + `//paste -o`
- `roxy_r.0.0.schem`   → `//pos1 7044,-64,-2916` + `//paste -o`
- `roxy_r.2.5.schem`   → `//pos1 8572,-64,-1476` + `//paste -o`

---

#### 2b. RESULTADO REAL EJECUTADO (2026-10-20/21) — reescritura NBT offline

**Método:** reescritura de `xPos`/`zPos` en 88 archivos `.mca` (script `_a06_reescribir.py` +
`_a08_batch.py`), offset **+12 regiones en X, −6 regiones en Z** desde el staging.
Validado: 88/88 alineados, 0 `Ignoring absolute chunk` en Paper 26.2, SHA256 intacto tras carga.

| Dato | Valor REAL (medido en disco y Paper) |
|---|---|
| **Centro de construcción (bloques)** | **X ≈ 6240, Z ≈ −3256** |
| **Bbox de construcción** | **X 5120..7359, Z −5136..−1377** |
| **Regiones con construcción** | **r.10..14 × r.−11..−3** (28 regiones, 12.625 chunks) |
| **Regiones totales del staging** | r.10..14 × r.−18..−1 (90 regiones, 88 con datos) |
| **Biomas (fuente `biomas_2750.png`)** | **65 % desert, 35 % windswept_hills, 0 % océano** |
| **Y superficie (meseta norte Z −9216..−8400)** | **111 (plana, rango 14–30 blk)** |
| **Y en el centro real (6240,−3256)** | **≈ 288 (lomo nevado, windswept_hills)** |
| **Distancia a ancla C2 (6828,−4628)** | **~1.496 bloques** |
| **Chunks con construcción** | **12.625** |
| **Archivos `.mca` subidos al live** | **88** (desde `%TEMP%\opencode\_exp_A_all`) |
| **Verificación live** | SHA256 remoto = local en 88/88; Paper 26.2 carga sin errores |

> **Nota:** el centro real **(6240, −3256)** está ~3.000 bloques al norte de la ancla prevista
> (−9000), y ~1.400 bloques al este del centro de C2 (6828). Cayó en `windswept_hills`
> (montaña con nieve) y no en la meseta plana del norte. Para moverlo a la meseta haría falta
> un delta **Δregiones = (−1, −11)** (script `_reubicar_roxy.py` listo y validado).

**Archivos de evidencia:**
- Procedimiento: `mundo\PEGADO_ESTRUCTURAS_GRANDES_PROCEDIMIENTO_2026-10-21.md`
- Scripts: `mundo\_herramientas_roxy\` (`_reubicar_roxy.py`, `_auditar_alineacion_mca.py`, etc.)
- Log de subida live: 88 OK, 0 diferentes (2026-10-21)

### 3. Laberintos — BEGARITT

Centro del conjunto: **(−5040, 93, 5360)** — Y base = nivel dominante 93 (medido).

| Archivo | Dim (W×H×L) | Centro (X,Z) | Puntúa | Separación |
|---|---|---|---|---|
| `compact_trial_chamber.schem` | 110×85×91 | **(−5040, 5360)** | Sí (23 vaults, 25 spawners) | Centro |
| `laberinto_mayor_SH_buried_halls_arena.schem` | 32×12×32 | **(−5190, 5360)** | **Sí** (3 vaults, 2 spawners) | 150 W |
| `laberinto_menor_DnT_arena_3x3.schem` | 33×7×33 | **(−4890, 5360)** | No (0 vaults, 0 spawners) | 150 E |

`laberinto_mayor_SH_arena_3x3.schem` es **artefacto descartado, NO pegar.**
`laberinto_menor_DnT_trial_room_cross.schem` (11×7×11) es celda suelta, opcional.

**Fórmula de pegado (cada archivo):**
```text
//schem load <nombre>
//pos1 <centro_X - W/2>,<93>,<centro_Z - L/2>
//paste -o
```
Begaritt (Z 5260..5460, X -5600..-4400) es plano Y 81–93 (dominante 84–93); los 3 caben sin solape.

> **Nota 2026-10-22.** No hay evidencia de que este pegado de laberintos se haya ejecutado.
> Además, `compact_trial_chamber.schem` es hoy **candidato para la arena de Hitogami** en
> `(−2304, −16, −3008)`: un `.schem` se puede pegar en más de un lugar, pero **la decisión de
> qué se hace con él sigue abierta** (decisión del usuario). Ver `laberintos\INFORME_LABERINTOS.md`
> §(j): el "laberinto mayor" no es tileable con las piezas de Skyhunters.

### 4. Trial chambers — ya documentadas

Ver `TRIAL_CHAMBERS_COORDENADAS.md`: principal `(−2304, −16, −3008)` + SC1–SC10.

**Estado 2026-10-22:** **SC1–SC10 colocadas** (reportado por el usuario; sin evidencia
verificable en este repo). **Pendiente: la arena de Hitogami** (principal). Schematic sin
decidir — ver la advertencia al inicio de `TRIAL_CHAMBERS_COORDENADAS.md`.

### 5. Fortaleza de Perugius — ya pegada

`(−916, 150, 6316)`; spawn/tp `(−1016, 227, 6417)`. Ver
`perugius\UBICACION_RESERVADA_FORTALEZA_PERUGIUS.md`.

---

## Cómo pegar (sin romper el mundo)

1. Backup del mundo / trabajar en `server-test` primero.
2. Sin jugadores en la zona (o `//paste -preview` para ver sin escribir).
3. Cargar el `.schem`, fijar `//pos1` con la coordenada, `//paste -o`.
4. `/relight` y proteger la zona con WorldGuard.

---

## Pendiente (fuera de este archivo)

- **Pegado real**: ejecutado **parcialmente**. Medido/registrado:
  - Roxy village → hecho por reescritura NBT offline (88 `.mca`), ver §2b.
  - Trial chambers **SC1–SC10** → **colocadas (reportado 2026-10-22)**.
  - **Arena de Hitogami (trial chamber principal) → PENDIENTE.**
  - Las 7 piezas temáticas de C3 y los laberintos de Begaritt: **no verificados** en este turno.
- **Normalizar Offset de los .schem v2/v3** a `[0,0,0]` si se quiere `//pos1` directo = esquina. Ver sección ⚠️.
- **Licencias**: varias piezas prohíben redistribuir o exigen atribución (ver `INSTRUCCIONES\MT_Propuestas_de_Estructuras.md`).
- **Proteger zonas** con WorldGuard tras pegar.

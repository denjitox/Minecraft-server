# Calibración del mapeo y viabilidad de añadir estructuras

**Fecha:** 2026-10-19
**Ámbito:** mundo 22.000×22.000 exportado (`ENTREGA_Loding_20261017\05_mundo\world`) y las
9 estructuras nuevas dejadas en `MT_Server_Base_26.2\plugins\Litematica\schematics\`.

> **Nota (2026-10-22):** `ENTREGA_Loding_20261017\` **ya no existe en disco**; el árbol vivo es `_prueba_roxy_local\world` (1.944 `.mca`), **`[INFERRED]`** por conteo de regiones. Lo medido acá se tomó sobre el mundo cuando existía.

---

## 1. El mapeo pixel → bloque quedó CONFIRMADO (ya no es [A VERIFICAR])

El `analisis_continentes_mushoku_tensei.md` daba `X = -11000 + px*8` y avisaba
explícitamente: *"Hasta que eso se haga, las coordenadas de este informe son
[A VERIFICAR] contra el mundo real."* **Eso está resuelto.**

Método: se leyó el bioma REAL de chunks del mundo exportado (paleta de
`sections[i].biomes`) y se cruzó contra `biomas_2750.png` con 6 hipótesis de
escala/shift. Herramienta: `mundo\calibrar_mapeo3.py`.

| Muestra | Aciertos con `X = -11000 + px*8` |
|---|---|
| 6 regiones, 480 chunks | **469/475 = 98,7 %** |
| 20 regiones, 2400 chunks | **2384/2400 = 99,3 %** |

Las demás hipótesis quedaron descartadas: 7 b/px 50,9 %, 8 b/px shift −11008
97,2 %, shift −11024 95,7 %, 16 b/px 25,2 %, 4 b/px 30,0 %.

**Consecuencia: las anclas y bboxes del `analisis_continentes_mushoku_tensei.md`
son válidos.** C3 = plains/forest, C2 = desert, C8 = jungle. Salida cruda en
`mundo\calibracion_mapeo_amplia.json`.

### Lo que NO se calibró: el heightmap

`relieve_2750_16bit.png` (I;16) trae valores **8999–58013**, no 0–575. La imagen
de relieve **sigue sin calibrar**: los intentos (`calibrar_relieve.py`,
`calibrar_relieve2.py`) dan correlación inútil y quedan **descartados**. El
heightmap del chunk en el mundo exportado (`Heightmaps.WORLD_SURFACE`) viene
**vacío** en los chunks muestreados (verificado), así que no sirve para calibrar.

**Para la cota real se usa `mundo\medir_superficie.py`**, que lee los `.mca` de
la zona y da la Y de superficie medida. Es más directo y no depende del PNG.

---

## 2. Herramientas

| Herramienta | Estado | Función |
|---|---|---|
| `mundo\calibrar_mapeo3.py` | **válida** | Calibra pixel→bloque contra el mundo real. Salida JSON |
| `mundo\medir_superficie.py` | **válida** | Y de superficie real en una huella (X,Z), leyendo los `.mca` |
| `mundo\verificar_ubicacion.py` | válida para bioma; **el relieve de su salida NO sirve** | Composicion de biomas de una huella |

**Descartadas** (se conservan como registro del intento, no usar): `calibrar_mapeo.py`,
`calibrar_mapeo2.py`, `calibrar_relieve.py`, `calibrar_relieve2.py`,
`diag_region.py`, `diag_mapeo.py`.

### Trampas medidas (para no repetirlas)

1. **La clave del bioma es `biomes` (plural), no `biome`.** Buscar `biome`
   devuelve 0 chunks sin error visible.
2. **`palette` es una `TAG_List` de `TAG_String`**, no de compuestos. Un lector
   que solo materializa listas de compuestos devuelve `[None]` y el bioma sale
   `None`.
3. **Los bits de una entrada NUNCA cruzan un long** (`epb = 64 // bits`). Decodificar
   cruzando longs da valores basura. La convención validada está en
   `inspeccionar_region.desempacar` — **reusarla, no reimplementarla.**
4. **`Heightmaps.WORLD_SURFACE` viene vacío** en los chunks de este mundo.
5. Inferir la superficie recorriendo secciones por "sección más alta con algún
   bloque" da siempre el tope del rango (Y=−4 o Y=−49): **no es la superficie.**

---

## 3. ¿Caben las estructuras en el mundo? — por ubicación

Dimensiones medidas de los `.schem` (ver el inventario de schematics) y
composición de bioma de la huella.

### C3 — continente central (ancla X=−4900 Z=−732)

| Estructura | Huella | Bioma | ¿Cabe? |
|---|---|---|---|
| Ranoa (campus 500×420) | 500×420 | **forest 56 % / plains 44 %, 100 % tierra** | **sí** |
| Milis (St. Basine 92×205) | 100×210 | plains 50 % / forest 50 %, 100 % tierra | **sí** |
| Eterna (275×118×275) | 280×280 | a verificar en su punto exacto | probable |

**C3 es la zona correcta para Ranoa y Milis**: 100 % tierra en la huella, sin
océano ni río. La superficie medida sube a Y≈34–61, así que el paste va con Y
elegida (no hay que enterrar a nivel del mar).

### Otras ubicaciones muestreadas

| Zona | Bioma | Nota |
|---|---|---|
| C2 (desierto, X=6828 Z=−4628) | desert | candidata para algo desértico |
| C8 (jungla, X=4716 Z=5876) | jungle | candidata para templo/ruina en selva |
| X=4500 Z=3000 | **océano 100 %** | **no pegar nada acá** |

---

## 4. Veredicto sobre "¿podemos añadirlo al mundo?"

**Sí, técnicamente sí — y no requiere resubir el mundo.** Las tres vías:

1. **Pegar en vivo en el server oficial** con FAWE y un cliente conectado
   (`//schem load` + `//paste`), que es como se pegó Perugius. Cero resubida.
   Requiere: cliente Java o Bedrock conectado (la consola por stdin no sirve).
2. **Pegar en la copia local y subir SOLO las regiones `.mca` que cambiaron**,
   verificadas por SHA256 contra `SHA256MUNDO.txt`, con
   `mundo\reemplazar_sftp_verificado.py`. Es el camino verificable y reanudable.
3. **Probar primero en `server-test`** (que tiene borde propio y mundo vacío) y
   después decidir el traslado.

**El cuello de botella nunca fue técnico:** era el mapeo sin calibrar (ya resuelto)
y los permisos (que el usuario dice tener).

### Limitaciones que siguen en pie

- **`Roxy village fixed.zip` (221 MB) es un MUNDO, no un `.schem`.** No se puede
  `//paste` directo: hay que extraer la parte del castillo y exportarla a `.schem`
  (o pegar su región). 1044 `.mca`, extensión X[−6656..5631] Z[−8704..6655].
- **`st-sayers-east-wing.zip` (3,3 MB) también es un mundo** (1.14.4, 15 regiones).
  Mismo caso.
- **`fantasy-greenhouse.schem` tiene 241 block entities con `Id=""`** →
  los contenedores quedan rotos al pegar. Hay que arreglar el NBT antes.
- **Ninguno de los 7 `.schem` es celda tileable**: son salas terminadas. Sirven
  como piezas sueltas, que es lo que se decidió.
- **3 `.schem` son Sponge v3** (`trueColors`, `eterna`, `medievalbar`):
  `inspeccionar_schem.py` **falla con ellos** (busca las dims en la raíz; en v3
  viven dentro de `Schematic`). Hay que extenderlo si se quiere medir más.

---

## 5. Pendientes que abre esto

1. Extender `laberintos\inspeccionar_schem.py` a Sponge v3 (hoy falla).
2. Definir coordenadas exactas de pegado por estructura (centro y Y) usando
   `medir_superficie.py` en cada punto.
3. Resolver cómo extraer el castillo de `Roxy village` (mundo) a un `.schem`.
4. Arreglar los block entities de `fantasy-greenhouse.schem`.
5. Calibrar el heightmap del kit — **opcional**, porque `medir_superficie.py`
   ya da la cota real sin él.

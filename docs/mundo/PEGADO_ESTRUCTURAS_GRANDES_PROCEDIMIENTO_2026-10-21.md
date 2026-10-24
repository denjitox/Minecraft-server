# Pegado de estructuras GRANDES en el mundo 22k — procedimiento verificado

**Fecha:** 2026-10-20/21
**Ámbito:** cómo pegar una estructura grande (mundo completo, no `.schem`) en el mundo 22k,
y las **trampas medidas** que hay que evitar. Documento de referencia para futuras
estructuras de este tipo (el caso que lo motivó: **pueblo Roxy**, `Roxy village fixed.zip`).

> **Todo lo de acá está medido en disco en la sesión del 2026-10-20/21.** Lo que no se
> verificó se marca `[A VERIFICAR]`.

---

## 1. La trampa central: renombrar un `.mca` NO reubica los chunks

**Medido, causa raíz del fallo del pegado de Roxy.**

Cada chunk guarda en su NBT la posición **absoluta** `xPos` / `zPos`. El archivo `.mca`
(región) es solo un contenedor cuyo **nombre** indica qué región debería contener.

Paper (y Minecraft en general) valida, al cargar cada chunk, que:

```
xPos // 32 == región_X_del_nombre_del_archivo
zPos // 32 == región_Z_del_nombre_del_archivo
```

Si no coincide, **descarta el chunk y regenera la región**. Log típico:

```
Ignoring absolute chunk [38, -159] in regionfile .../region/r.13.-11.mca
as it is not contained in the bounds of the regionfile.
It should be in regionfile (1,-5)
```

**Consecuencia medida:** renombrar `r.-2.5.mca` → `r.10.-1.mca` cambia el nombre pero
los 1024 chunks siguen declarando región `(−2,5)`. Resultado: **0/90 regiones alineadas,
88 desalineadas, 2 vacías**. Paper regeneró todo y el pueblo **nunca se pegó**. Nada se
corrompió, pero el pegado fue silenciosamente nulo.

> **Regla:** para reubicar chunks hay que **reescribir el NBT**, no el nombre. El nombre
> debe acompañar, pero no alcanza.

---

## 2. Campos NBT absolutos que hay que desplazar (medidos)

Reescribir **solamente** estos campos del chunk. Todo lo demás es relativo.

| Campo | Ubicación | Tipo | Offset por región |
|---|---|---|---|
| `xPos` | raíz | TAG_Int (4 B fijo) | `+dRX*32` (chunks) |
| `zPos` | raíz | TAG_Int (4 B fijo) | `+dRZ*32` (chunks) |
| `block_entities[].x` | raíz (**TAG_List**, no compound) | TAG_Int | `+dRX*512` (bloques) |
| `block_entities[].z` | raíz (**TAG_List**) | TAG_Int | `+dRZ*512` (bloques) |

**NO hay que tocar** (verificado que no contienen coordenadas absolutas en este dataset):
`sections[]` (Y y paletas son relativas), `Heightmaps` (relativo al chunk),
`PostProcessing` (vacío en todos), `structures` (`{}` en todos), `entities` (ausentes —
los chunks son `minecraft:full`), componentes de block entities (`target`, `lodestone`, etc.:
0 apariciones reales).

### ⚠️ Trampa medida: `block_entities` es TAG_List en la RAÍZ

Un reescritor que asuma `block_entities` como TAG_Compound **deja los 21.580 block
entities sin desplazar** (verificado: 75/75, 8774/8774 con la condición correcta).
La condición correcta es `tag == LIST and name == b"block_entities"` en la raíz.

---

## 3. Formato `.mca` (Anvil) — layout verificado

```
Offset        Tamaño      Contenido
0             4096 B      Tabla de ubicaciones: 1024 × 4 B big-endian
                           → 3 B offset en SECTORES + 1 B cantidad de sectores
4096          4096 B      Tabla de timestamps: 1024 × 4 B big-endian (epoch s)
8192          resto       Sectores de 4096 B
```

**Índice de chunk:** `i = x_local + 32*z_local`, con `x_local = xPos % 32`, `z_local = zPos % 32`.

**Payload por chunk** (desde `offset_sectores × 4096`):
```
+0   4 B  longitud (big-endian int) = compresión + datos comprimidos
+4   1 B  tipo de compresión (1=gzip, 2=zlib, 3=sin comprimir, 4=LZ4, 127=custom)
+5   N B  datos comprimidos (N = longitud − 1)
```
El último chunk se rellena a múltiplo de 4096 B (el padding no cuenta en `longitud`).
Flag oversized: si el chunk supera 255 sectores, el byte de cantidad se pone en `0x80`.

**Medido en Roxy:** 86.131/86.131 chunks tipo 2 (zlib), cero `.mcc`, cero oversized.

---

## 4. ⚠️ Trampa medida: el tamaño COMPRIMIDO cambia → hay que repackear

- **El descomprimido NO cambia:** `xPos`/`zPos`/`x`/`z` son TAG_Int de 4 B fijos.
  Medido: 10.505 → 10.505 bytes. Confirmado.
- **El comprimido SÍ cambia:** zlib puede emitir otra longitud tras reescribir los ints.
  Medido en los 90 archivos: **delta máximo +15 bytes por chunk**; **3 de 86.131 chunks**
  cruzaron de **1 a 2 sectores** al pasar el límite de 4096 B.

**Consecuencia:** **NO** se puede reusar el header tal cual. Hay que **reconstruir el
header y repackear sectores**. Un enfoque que asuma tamaño constante **corrompe esos 3 chunks**.

**Verificación de no-daño** (medida, recomendada siempre): comparar el NBT descomprimido
original vs reescrito → la diferencia debe ser **4 u 8 bytes por chunk** (uno o dos ints) y
el tamaño total descomprimido debe ser **idéntico**. Medido: máx 8 bytes/chunk, `len_igual=True`.

---

## 5. Procedimiento (offline, sin server vivo) — RECOMENDADO

Verificado end-to-end en el mundo local. **No necesita server, es determinista.**

1. **Backup.** Copiar el mundo destino completo a una carpeta de prueba aislada
   (el pegado **pisa** el terreno existente: medido en Roxy, las 90 regiones destino tenían
   9.300–17.400 bloques no-aire c/u). Nunca trabajar sobre la copia canónica.
2. **Reescribir** con el reubicador (`%TEMP%\opencode\_reubicar_roxy.py` o equivalente):
   `xPos += dRX*32`, `zPos += dRZ*32`, `block_entities[].x/.z += dRX*512/dRZ*512`.
   Repackear sectores y reconstruir header; copiar timestamps.
3. **Verificar alineación (obligatorio, ANTES de subir):**
   correr `_auditar_alineacion_mca.py <carpeta> --todos` → debe dar **`DESALINEADOS: 0`**.
4. **Verificar no-daño:** diff del NBT descomprimido → solo 4/8 bytes por chunk.
5. **Smoke test en Paper local** con el mundo de prueba: arrancar y confirmar en el log
   **cero** `Ignoring absolute chunk` / `should be in regionfile`.
6. **Confirmar que Paper no regeneró:** SHA256 de los `.mca` idéntico antes/después de cargar.
7. **Solo entonces**, subir al server (con el server **apagado**).

### Resultado medido del smoke test (Roxy, 2026-10-20/21)

| Prueba | Resultado |
|---|---|
| `Ignoring absolute chunk` / `should be in regionfile` | **0** (antes: 90 regiones rechazadas) |
| Paper regeneró las regiones reescritas | **0** — SHA256 idéntico en 88/88 |
| Chunks del pueblo cargados | ✅ 12.625 chunks con construcción |
| Warning nuevo | ⚠️ `Ignoring heightmap data ... expected: 43, got: 37` (ver §6) |

---

## 6. ⚠️ Warning benigno: `Ignoring heightmap data`

Al cargar chunks de un mundo de **versión vieja** (el mundo fuente de Roxy es DataVersion
antiguo), Paper avisa:

```
Ignoring heightmap data for chunk [414, -576], size does not match; expected: 43, got: 37
```

- **Qué es:** `Heightmaps` guarda mapas de altura precalculados. El formato cambió de
  tamaño entre versiones (37 longs vs 43). Paper **descarta el heightmap guardado y lo
  RECALCULA** al cargar. **No se pierde terreno** — solo el mapa de alturas precalculado.
- **Medido:** el pueblo trae 37 longs por clave; los chunks del mundo 22k tienen
  `Heightmaps` **vacío** (`{}`) — por eso no avisan.
- **Volumen:** 180.104 warnings sobre 45.017 chunks únicos en el bbox de Roxy.
- **Cuidado operativo:** ese volumen de warnings **inunda el log** si se redirige todo el
  stdout a un archivo, y el watchdog de Paper puede disparar un thread dump
  (`The server has not responded for 10 seconds!`) al serializar el texto. No es un cuelgue
  del mundo: es I/O de consola. **Para el smoke test, conviene subir el nivel de log o
  no redirigir todo el stdout.**

---

## 7. Alternativa soportada: WorldEdit / FAWE

- **FAWE 2.16.1 está instalado** (mismo SHA en local y hosting). `//paste` **respeta las
  coordenadas absolutas por construcción** (usa el origin del clipboard, no el nombre).
- **Por qué se descartó originalmente:** NO por técnica — porque `//paste` **exige un actor
  con posición** y la consola de Paper 26.2 por stdin es defectuosa (`NullPointerException:
  CommandSourceStack.getLevel() is null`). Se resolvió copiando `.mca`, y **ahí nació el bug**
  del rename.
- **Caminos viables:** cliente conectado (como se pegó Perugius, funcionó), o RCON
  (**no probado** en el hosting; `enable-rcon=false` por defecto).
- **`//schem`:** los `.schem` con `Offset=[0,0,0]` permiten `//pos1 <X>,<Y>,<Z>` + `//paste`.
  OJO: los `.schem` pueden traer orígenes Y distintos (recorte de altura variable), así que
  **fijar la Y explícita** por pieza. **NO usar `-a`** si la estructura *es* terreno (deja huecos).

---

## 8. Reubicar una estructura ya reescrita

Si hay que mover una estructura ya reescrita (p. ej. porque la coordenada elegida no era la
correcta), **no hay que rehacer desde el origen**: basta con aplicar un **delta adicional**
con el mismo procedimiento de §5.

Script: `%TEMP%\opencode\_reubicar_roxy.py` (parametrizado):

```powershell
.\.venv\Scripts\python.exe _reubicar_roxy.py `
  --entrada <carpeta.mca> --salida <carpeta_salida> `
  --drx <delta_regiones_X> --drz <delta_regiones_Z> [--dry-run]
```

Renombra cada `r.<rx>.<rz>.mca` → `r.<rx+drx>.<rz+drz>.mca` y reescribe el NBT con el delta.

---

## 9. Caso Roxy — estado y coordenadas (2026-10-21)

| Dato | Valor (medido) |
|---|---|
| Fuente | `MT_Server_Base_26.2\plugins\Litematica\schematics\Roxy village fixed.zip` (221.275.318 B, SHA256 `89D469E8…`) — **es un MUNDO completo**, no `.schem` |
| Regiones del pueblo (con construcción) | **12.625 chunks** |
| Offset aplicado en el staging | **+12 regiones X, −6 regiones Z** (= +384/+384? no: +384 chunks X, −192 chunks Z) |
| **Bbox REAL donde quedó** | **X 5120..7359, Z −5136..−1377** (centro ~6232, −3256) |
| Ancla declarada en la doc | (5500, −64, −9000) — **no coincide** con dónde quedó |
| Terreno (fuente `biomas_2750.png`) | 65,4% desert + 34,6% windswept_hills, **0% océano** |
| Continente asignado | C2 (demoníaco, ancla 6828, −4628, Y 69) |

### Discrepancia de coordenadas (a resolver)

- La doc `COORDENADAS_PEGADO_DEFINITIVAS_2026-10-20.md` (§2) declara
  `//pos1 <5500 + rx*512>,-64,<-9000 + rz*512>`, con rx −3..2, rz −12..5.
- El rename **realmente ejecutado** (`_preparar_roxy_staging.py`) usó ancla 5360, −8976 y
  offset `r.<rx>.<rz> → r.<rx+12>.<rz−6>`.
- Son **dos esquemas distintos**. El pueblo quedó donde el segundo lo puso.
- **El ancla documentada (5360, −8976) cae en periferia sin construcción** (chunk del ancla:
  solo air/plains/bedrock/dirt/grass_block).

### Opciones

1. **Aceptar dónde quedó** (X 5120..7359, Z −5136..−1377) — está más cerca de C2 que la zona
   prevista; ambas son tierra seca. Solo habría que corregir la doc.
2. **Reubicar** a la zona prevista u otra — usar `_reubicar_roxy.py` con el delta.

> **Costura de terreno:** el pueblo trae su propio terreno (grass_block, maderas) y el
> destino es desierto → habrá un borde visible de terreno verde/construido en medio del
> desierto. Considerarlo en la decisión.

---

## 10. Herramientas (todas en `%TEMP%\opencode\`)

| Herramienta | Función |
|---|---|
| `_reubicar_roxy.py` | Reubicador parametrizado (reescribe NBT + renombra + repackea) |
| `_auditar_alineacion_mca.py` | Audita una carpeta: ALINEADOS vs DESALINEADOS (`--todos` para los 1024 chunks) |
| `_diag_region_abs.py` | Mide `xPos`/`zPos` declarados vs la región del nombre |
| `_diag_header_mca.py` | Valida header de sectores y parseo NBT |
| `_a06_reescribir.py` / `_a08_batch.py` | Reescribir (versión original del caso Roxy) |

> Estas herramientas viven en `%TEMP%` (se pueden borrar). **Si se van a usar de nuevo,
> copiarlas a `mundo\` antes de que se limpien.**

---

## 11. Reglas de oro (resumen)

1. **Renombrar no reubica.** Reescribir `xPos`/`zPos` siempre.
2. **`block_entities` es TAG_List en la raíz.**
3. **El comprimido cambia → repackear sectores, no reusar header.**
4. **Verificar alineación (0 DESALINEADOS) y no-daño (4/8 bytes/chunk) antes de subir.**
5. **Smoke test en Paper local:** 0 errores de chunk + SHA256 intacto = funcionó.
6. **El warning de heightmap es benigno** (Paper recalcula) pero inunda el log.
7. **Backup siempre.** El pegado pisa el terreno destino.
8. **Server apagado** para subir regiones.
9. Medir, no inferir. Cada cifra con su fuente.

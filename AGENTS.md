# AGENTS.md — Proyecto Minecraft Mushoku Tensei
Contexto operativo exportado para continuar el proyecto desde `C:\Dev\Minecraft-server`.
Los archivos pesados y el árbol de trabajo original permanecen en `C:\Dev\OPENCLAUDIO\minecraft_server`; este repositorio contiene solo documentación y skills.
Todo lo de acá está medido o leído de los archivos del proyecto. Si algo no está, **leelo del archivo fuente**, no lo supongas.
el uso de sub agentes se promueve activamente
ante una decision critica enviar a sub agentes a debatir
documentar errores y metodos probados
para tareas que se espera duren varias horas monitorear cada minimo 1 hora
---
> **Design Read:** Reading this as: operational AGENTS.md for Minecraft server automation, no visual language, dial ENERGY 1 / RHYTHM 1 / MOTION 1.

## Qué es esto

Mundo Minecraft **22.000 × 22.000** inspirado en *Mushoku Tensei*, generado con **WorldPainter** (scripting headless *ejecución sin interfaz gráfica*) a partir del kit congelado `MT_22000_WorldPainter_MINIMO`, para montar en un hosting con **Paper 26.2 + Geyser/Floodgate** (crossplay Java + Bedrock *jugadores de ambas ediciones en el mismo servidor*).

| Componente | Ruta | Qué es |
|---|---|---|
| Kit WorldPainter (congelado) | `MT_22000_WorldPainter_MINIMO\` | Imágenes 2750 px, `crear_mundo.js`, `parametros.json` y el `MT_22000.world` generado (419 MB) |
| Paquete base del servidor | `MT_Server_Base_26.2\` (+ `MT_Server_Base_26.2_Geyser.zip`) | Configs, datapack `mt_core`, resource packs, docs |
| Servidor probado local | `servidor\` | Paper 26.2 build 134, 19 plugins, Java 25, ViaVersion. Copia para el panel |
| Instrucciones | `INSTRUCCIONES\` | Los 3 documentos operativos (Manual, Requerimientos, Instructivo MushokuCore) |
| Herramientas de mundo | `mundo\` | Scripts WorldPainter (.js) y análisis (.py) |
| Fortaleza de Perugius | `perugius\`, `Celestara_Reduced_1.21.4\`, `_perugius_work\` | Material del `.schem`. **Pegada en el server oficial (2026-10-18).** Punto y spawn/teleport (`−1016 227 6417`) en `perugius\UBICACION_RESERVADA_FORTALEZA_PERUGIUS.md` |
| Credenciales | archivo local excluido del repositorio | **No tocar, no imprimir, no transcribir**. El endpoint y las credenciales de producción no se publican; se consultan únicamente en el gestor autorizado. |

El núcleo temático es el datapack/plugin propio **`mt_core` / `MushokuCore`** (Siete Potencias, enfermedades, reliquias, facciones). La especificación está en `INSTRUCCIONES\Instructivo_Diseno_MushokuCore.md`.

---

## Reglas invariables / límites duros
> **Razón de estructura:** Plantilla operativa compartida con tesis_neuronal para consistencia de disciplina; secciones fijas porque la regla 1 exige verificación en disco en cada turno.

1. **NO FABRICAR RESULTADOS.** Prohibido afirmar que algo existe, que se escribió un archivo o que un proceso avanza **sin la evidencia tomada del disco en ese mismo turno**. Las reglas concretas:
   - **Verificá antes de afirmar.** Antes de decir "existe", "está hecho" o "va bien": `Get-ChildItem`, `Get-Content`, contar archivos, leer el log. Si no lo verificaste en este turno, se escribe **"no verificado"** y punto.
   - **Un resumen, checkpoint o compactación NO es evidencia.** Si un resumen dice que algo está terminado, se re-verifica en disco antes de actuar sobre esa afirmación.
   - **Un proceso "que está corriendo" no es un proceso que esté haciendo su trabajo.** Hay que probar que produce salida (un archivo nuevo, un log que avanza). Sin salida nueva, se reporta que no se pudo confirmar el avance.
   - **Nunca rellenes huecos.** Si una salida de herramienta vino truncada, no completás las partes faltantes por deducción: volvés a pedir acotado.
   - **Distinguí lo MEDIDO de lo INFERIDO** y marcá lo inferido con `[A VERIFICAR]`. Ninguna cifra se reporta sin su fuente (archivo y hora).
   - **No inventes cifras** de rendimiento, conteos ni estimaciones "razonables". Si no hay medición, se dice que no la hay.
   - **Si algo falla, se conserva el error completo**; jamás se ajustan parámetros para que "pase".
   - **Ante un pedido que no se pudo cumplir, la respuesta empieza por lo que NO se hizo.**
   - **Incidente real (2026-10-17).** Un resumen de compactación de opencode describió como completado el export de este mundo (1936 regiones, ~13 GB) y la existencia de la carpeta de entrega, de `subir_sftp.py`, del datapack del jefe, de `hitogami.sk`, del documento de diseño de la trial chamber y de un `MushokuCore.jar`. Verificado en disco: **nada de eso existía** (fuente: `Get-ChildItem ENTREGA_Loding_20261017 -ErrorAction SilentlyContinue`, `Get-ChildItem salida_mundo -Recurse -Filter *.mca | Measure-Object`, 2026-10-17 17:48); el export real estaba en **376/1936 regiones** (fuente: `Get-ChildItem salida_mundo\_prueba\world\*.mca | Measure-Object`, 2026-10-17 17:48), y una tarea reportada como "export corriendo" entre 10:54 y 15:08 **nunca escribió un solo archivo**.
2. **El kit `MT_22000_WorldPainter_MINIMO\` NO se modifica.** Es el original congelado. Si tiene afirmaciones obsoletas, se reportan; no se editan.
3. **La instalación de WorldPainter (`%LOCALAPPDATA%\Programs\WorldPainter`) NO se modifica.**
4. **Nunca imprimir ni escribir credenciales** (SFTP, tokens, API keys). Los archivos locales de acceso y algunas configuraciones del servidor contienen contraseñas y no forman parte de este repositorio. Si hay que referirse a ellas: "la credencial de SFTP".
5. **No tocar `C:\Dev\OPENCLAUDIO\tesis_neuronal\`** (es otro proyecto).
6. Si se toca el paquete del servidor (`MT_Server_Base_26.2\`), hay que **regenerar `mt_core_26.2.zip` y `SHA256SUMS.txt`**: el manifiesto deja de valer.
7. **El export completo tarda ~2–2,5 h** (medido: ~340 chunks/s en zonas grandes de 1024 tiles; fuente: `python estimar_peso.py --json out.json` + logs de `exportar_por_zonas.ps1`, 2026-10-17). Antes de lanzarlo, exportar una zona chica y verificar.
8. **El árbol original no tiene git.** Este repositorio documental sí usa git; no confundirlo con una copia completa ni asumir que contiene los artefactos citados.
9. No resolver solo las decisiones listadas como **requieren decisión del usuario** (ver abajo).

---

## Hechos verificados (fuente de verdad)

### Generación del mundo

| Hecho | Dato medido |
|---|---|
> **Razón de tabla uniforme:** Formato tabla permite verificación rápida de hecho↔fuente; no es feature card visual.
| **Minerales automáticos** | No hay que pintar `Resources`: WorldPainter la trata como *minimum layer* y el post-proceso `RESOURCES` corre siempre. Medido: ~1,85 % del subsuelo con coal, iron, copper, gold, redstone, lapis, diamond, emerald y variantes `deepslate_*` |
| **`MT_22000.world` está pelado** | `isPopulate()=false`, 0 cuevas (`Caves`/`Caverns`/`Chasms` en 0 sobre 400 puntos), sin vegetación. Los biomas SÍ están pintados (52% de los puntos) |
| **`populate=true` NO da árboles** | Solo cobertura baja: `grass`, `tall_grass`, `fern`, `large_fern`, `sunflower`. Para árboles hay que pintar las capas de árboles |
| **Nombres reales de capas** | Árboles: `Deciduous`, `Pine`, `Jungle`, `Swamp`. Cuevas: `Caves`, `Caverns`, `Chasms`. Otras: `Populate`, `Resources`, `River`, `Frost`, `Void` |
| **`Deciduous Forest` / `Pine Forest` NO existen** | Las clases son `DeciduousForest`/`PineForest` pero su `getName()` devuelve `"Deciduous"`/`"Pine"`. `withName('Deciduous Forest')` falla |
| **Etapas del export** (post-procesos, salteables) | `CAVES`, `RESOURCES`, `LIGHTING`, `LEAVES` |

### Altura, formato y estructuras

| Hecho | Dato medido |
|---|---|
| **Techo y=511 confirmado** | Datapack sonda sobre Paper 26.2: `min_y=-64`, `height=576`; `setblock` funciona en y=300/400/500 y falla en 520 y 600 |
| **Formato de mapa** | `org.pepsoft.anvil.26.1`. WorldPainter escribe su datapack dentro del mundo; Paper lo acepta aunque el `pack_format` sea 101.1 contra 107.1 |
| **Bedrock ve hasta y=511 (caveat viejo, resuelto)** | Geyser 2.12.0 extiende por sí solo el overworld de Bedrock: si el `dimension_type` de Java es más alto que 320, le manda un `DimensionDataPacket` y lo redefine a `-64..512` (tope del protocolo Bedrock). Verificado leyendo el bytecode del jar instalado (fuente: `javap -c servidor\plugins\Geyser-Spigot.jar | FindStr DimensionDataPacket`, 2026-10-17; no con un cliente); **falta** el test con cliente Bedrock real. Detalle: `INVESTIGACION_Bedrock_Altura_511.md` |
| **`generate-structures=false` NO se aplica a un mundo importado** | `world_gen_settings.dat` queda con `generate_structures: 1`, generador `minecraft:noise`, seed 27594263. Consecuencia: **cero estructuras** (ni aldeas, ni minas, ni strongholds) salvo que se coloquen a mano |

### Tamaño, tiempos y export por zonas

| Hecho | Dato medido |
|---|---|
| **Peso real estimado** | **10,1–13,7 GB** (5,62 KB/chunk; fuente: `python estimar_peso.py _prueba_roxy_local\world --mapa 22000 --json out.json`, 2026-10-17). El kit/repo no tiene una cifra mayor válida |
| **Tamaño del mundo** | 29.584 tiles = 1.891.625 chunks |
| **Velocidad de export** | **~340 chunks/s** en zonas de 1024 tiles (medido: 211 s por 65.536 chunks; fuente: `exportar_por_zonas.ps1` log zona base, 2026-10-17 15:51). Las pruebas chicas daban 57–73 chunks/s porque el arranque de WorldPainter cuesta ~15 s fijos (fuente: `exportar_por_zona.js` runs, 2026-10-16). El mundo completo sale en **~2–2,5 h**, no en 7–10 |
| **Export por zonas: funciona** | `WorldExportSettings` (clase concreta) + `setTilesToExport(Set<Point>)` + `setDimensionsToExport({0})` → `w.setExportSettings(es)` → `wp.exportWorld(w).toDirectory(dir).go()`. Medido: 81 tiles → 81 regiones en 23 s (+14 s de carga; fuente: `exportar_por_zona.js` run, 2026-10-16) |
| **El directorio destino debe existir** | Antes de exportar |
| **La carpeta exportada se llama como el heightmap** | `relieve_2750_16bit` → hay que **renombrarla a `world`**. **Resuelto:** el watcher la movió el 2026-10-17 a `ENTREGA_Loding_20261017\05_mundo\` (con el nombre `mushoku_world`) y el **2026-10-18** se renombró a `...\05_mundo\world` (fuente: `mundo\watch_export_y_mover.ps1` log, 2026-10-17). **Actualizado 2026-10-22:** `ENTREGA_Loding_20261017\` **ya no existe en disco** (verificado en `C:\Dev\OPENCLAUDIO\minecraft_server\`, `C:\Dev\OPENCLAUDIO\` y `E:\OPENCLAUDIO_archivo\`; fuente: `Test-Path` en PowerShell, 2026-10-22 10:15); el rename histórico ocurrió, la ruta de hoy es `_prueba_roxy_local\world` **`[INFERRED]`** |

### Biomas (tabla medida sobre 5.184 chunks de 81 tiles; fuente: `python mapear_biomas.py mundo\salida_mundo\relieve_2750_16bit mundo\biomas_2750.png`, 2026-10-16)

| Valor | Bioma | % mapa |
|---:|---|---:|
| 0 | ocean | 47,1 (fuente: `python mapear_biomas.py mundo\salida_mundo\relieve_2750_16bit mundo\biomas_2750.png`, 2026-10-16) |
| 1 | plains | 13,8 (fuente: `python mapear_biomas.py mundo\salida_mundo\relieve_2750_16bit mundo\biomas_2750.png`, 2026-10-16) |
| 2 | desert | 16,3 (fuente: `python mapear_biomas.py mundo\salida_mundo\relieve_2750_16bit mundo\biomas_2750.png`, 2026-10-16) |
| 3 | windswept_hills | 4,7 (fuente: `python mapear_biomas.py mundo\salida_mundo\relieve_2750_16bit mundo\biomas_2750.png`, 2026-10-16) |
| 4 | forest | 6,3 (fuente: `python mapear_biomas.py mundo\salida_mundo\relieve_2750_16bit mundo\biomas_2750.png`, 2026-10-16) |
| 5 | taiga | 5,1 (fuente: `python mapear_biomas.py mundo\salida_mundo\relieve_2750_16bit mundo\biomas_2750.png`, 2026-10-16) |
| 16 | beach | 2,1 (fuente: `python mapear_biomas.py mundo\salida_mundo\relieve_2750_16bit mundo\biomas_2750.png`, 2026-10-16) |
| 21 | jungle | 3,2 (fuente: `python mapear_biomas.py mundo\salida_mundo\relieve_2750_16bit mundo\biomas_2750.png`, 2026-10-16) |
| 7 | sin identificar | 0,16 (fuente: `python mapear_biomas.py mundo\salida_mundo\relieve_2750_16bit mundo\biomas_2750.png`, 2026-10-16) |
| 12 | sin identificar | 1,42 (fuente: `python mapear_biomas.py mundo\salida_mundo\relieve_2750_16bit mundo\biomas_2750.png`, 2026-10-16) |

Tierra = 52,87 % (coincide con `land_fraction: 0.529` del kit). **47% del mapa es océano**: verificar que el spawn caiga en tierra.

### Filtros y paquete

| Hecho | Detalle |
|---|---|
| **API de filtros** | `wp.createFilter().onlyOnBiome(id)` / `.onlyOnLand()` / `.onlyOnWater()` / `.exceptOnBiome(id)` … `.go()`, aplicado con `.withFilter(filtro)`. Las condiciones `onlyOn*`/`exceptOn*` son mutuamente excluyentes: **un filtro admite UNA sola condición** |
| **Paquete verificado** | `resource-pack-sha1` coincide; `mt_core_26.2.zip` byte-idéntico *igualdad binaria exacta, SHA256 coincidente* a su carpeta; los 19 jars locales tienen las versiones declaradas (Geyser 2.12.0-SNAPSHOT, Floodgate 2.2.5-SNAPSHOT, ViaVersion 5.12.2-SNAPSHOT); 3 SHA256 verificados (fuente: `Get-FileHash MT_Server_Base_26.2\*.jar`, `Get-FileHash servidor\plugins\*.jar`, 2026-10-17) |
| **`mt_core` duplicado** | En `servidor\datapacks\mt_core` y `servidor\world\datapacks\mt_core`: dejar uno |
| **Consola de Paper 26.2** | Por stdin no-terminal tira `NullPointerException: …CommandSourceStack.getLevel() is null` en todo comando con contexto de mundo, incluido `/stop`. **No verificado** contra el panel del hosting: pendiente RCON |
| **ViaVersion es obligatorio** | Geyser 2.12.0 exige Java MC 26.3 y el server corre Paper 26.2. `README_PRIMERO.md` decía que "no es necesario": **obsoleto** (lo corrige `servidor\LEEME_PANEL.txt`) |
| **URL pública del pack visual (2026-10-18)** | Java `MT_Void_Java.zip` (11.583 B, sha1 `305d8816df472d1fa0f7141eff688725a3a6df3f`) → **`https://files.catbox.moe/y5vmna.zip`**; Bedrock `MT_Void_Bedrock.mcpack` (7.587 B, sha1 `fac7a034b1d8d5b3be3c244328b9b8095d5a3af8`) → **`https://files.catbox.moe/cc04pr.mcpack`**. Subidos a **catbox.moe (host público anónimo, provisorio)** y **verificados bajándolos de vuelta** (sha1 idéntico al local; fuente: `Get-FileHash` local vs `Invoke-WebRequest` + `Get-FileHash` remoto, 2026-10-18). **(subido 2026-10-18, host provisorio catbox.moe, **aprobación pendiente de registrar**)**. Falta: decidir si se cablea en `resource-pack=` — **YA RESUELTO el 2026-10-18: cableado en las 3 configs** (decisión del usuario). Host catbox = **provisorio**; para producción conviene moverlo al hosting web propio. **⚠️ Ver `PENDIENTE_SINCRONIZACION_ENTREGA_2026-10-18.md`:** hay otra sesión escribiendo en el árbol y el datapack de la entrega quedó desactualizado (la decisión es esperar a que termine) |

---

## Trampas de la API de scripting de WorldPainter

Se aprenden a golpes. El motor es **Nashorn** (soporta `Java.type`), no Rhino.

| Trampa | Qué hacer |
|---|---|
| `Java.type('x.Y').class` | Sirve para **reflexión**, NO para instanciar. Para `new` va el tipo directo, sin `.class` |
| Una sola operación `wp.*` pendiente a la vez | No se puede meter `wp.getLayer()` dentro del argumento de una cadena ya iniciada. Es el bug del `crear_mundo.js` del kit: **precargar las capas/heightmaps en variables** antes de abrir cualquier builder |
| `f.call(obj)` no funciona | Usar llamada directa al método |
| `Java.type('org.pepsoft.worldpainter.layers.Layers')` no existe | `ClassNotFoundException`. Las capas se obtienen por nombre (`wp.getLayer().withName('Caves').go()`) o por el campo estático `INSTANCE` de la clase de la capa (`DeciduousForest.INSTANCE.getName() == "Deciduous"`) |
| Selección de tiles activa | WorldPainter exige fijar **exactamente una** dimensión: `setDimensionsToExport({0})` |

---

## Herramientas ya escritas (`mundo\`)

Intérpretes: `wpscript.exe <script.js> <args>` para los `.js` (los logs se leen con `-Encoding Default`); `tesis_neuronal\.venv\Scripts\python.exe` para los `.py` (tienen numpy/PIL).

| Herramienta | Función | Invocación |
|---|---|---|
| `crear_mundo_headless.js` | Versión corregida de `crear_mundo.js` (precarga heightmaps, formato `org.pepsoft.anvil.26.1`) | `wpscript.exe crear_mundo_headless.js <carpetaKit> [formato]` |
| `exportar_por_zona.js` | Exporta una zona por coordenadas de tile (128×128 bloques) | `wpscript.exe exportar_por_zona.js <mundo.world> <destino> [tx0 tz0 tx1 tz1]` |
| `muestrear_mundo.js` | Exporta una muestra dispersa de tiles | `wpscript.exe muestrear_mundo.js <mundo.world> <destino> <paso>` |
| `crear_prueba.js` | Mundo de prueba 512×512 con `Caverns`/`Chasms`/`populate` opcionales | `wpscript.exe crear_prueba.js <recortes> <sx> <sy> <salida.world> [cavernas] [populate] [chasms]` |
| `consultar_mundo2.js` / `consultar_capas.js` / `consultar_capas2.js` | Introspección del `.world`: populate, altura, semilla, capas, histograma de biomas | `wpscript.exe consultar_mundo2.js <mundo.world>` |
| `consultar_export.js` / `consultar_exportsettings.js` | Métodos de la clase de export settings | `wpscript.exe consultar_exportsettings.js` |
| `inspeccionar_region.py` | Resume bloques reales de los `.mca` (ores, cuevas, árboles, agua) | `python inspeccionar_region.py <archivo o carpeta> [--secciones]` |
| `mapear_biomas.py` | Cruza imagen de biomas + chunks exportados → tabla valor→bioma | `python mapear_biomas.py <export> <imagenBiomas>` |
| `valor_pixel.py` | Valor e histograma de un PNG del kit | `python valor_pixel.py <imagen> [x y …]` |
| `tiles_de_valor.py` | Tiles con más píxeles de cada valor de bioma → formato `tx,tz` | `python tiles_de_valor.py <imagenBiomas> <valores>` |
| `elegir_recorte.py` | Elige una ventana de tierra y exporta los 3 recortes para el mundo de prueba | `python elegir_recorte.py [--ventana 64] [--salida DIR]` |
| `estimar_peso.py` | Estima el peso del mundo (sectores usados del header Anvil) | `python estimar_peso.py <carpetaExport> [--mapa 22000] [--json out.json]` |
| `inspeccionar_world.py`, `leer_nbt.py` | Lectura de `.world` / lector NBT reutilizable | `python <script> …` |
| `parchear_datapack_altura.py` | Parchea el `pack.mcmeta` del `worldpainter.zip` (`min_format [101,1]`, `max_format [107,1]`) | `python parchear_datapack_altura.py <mundo o zip> [--dry-run]` |
| `pintar_capas.js` | Pinta cuevas y árboles sobre un `.world` y **guarda en otro archivo** (nunca toca el original). Opciones: `caves=,cavernas=,chasms=,forest=,plains=,taiga=,hills=,jungle=,populate=` | `wpscript.exe pintar_capas.js <entrada.world> <salida.world> <opciones>` |
| `exportar_tiles.js` | Exporta una **lista de tiles leída de un archivo** (`tx,tz` por línea). Hace falta porque el lanzador nativo se come los argumentos que empiezan con `-` y las coordenadas de tile de este mundo son negativas | `wpscript.exe exportar_tiles.js <mundo.world> <destino> <archivoDeTiles>` |
| `medir_subsuelo.py` | **Mide cuevas**: aire por debajo de la superficie. El histograma de bloques no sirve para esto (el aire del cielo domina el total). Como control, el mismo terreno sin cuevas da 0,000 % | `python medir_subsuelo.py <carpetaRegiones\|archivo.mca> [--max-chunks N] [--area x0 x1 z0 z1]` |
| `exportar_por_zonas.ps1` | Orquestador *controlador central que coordina las tareas* del export completo **por zonas**, reanudable *puede retomarse desde donde quedó tras interrupción* (marcas en `_control\marcas`), con control de colisiones de región y merge automático de los `.mca` al mundo final. **Parcheado 2026-10-17** (ver "Estado y pendientes") | `powershell -File exportar_por_zonas.ps1 -Mundo <w.world> -Destino <dir> -Lado 32 [ -Prueba \| -SoloPlan \| -Forzar ]` |

---

## Estado y pendientes

> **Server oficial (2026-10-18).** Desde esta fecha, **todos los cambios van directo al servidor oficial**: lo de este repo es la copia de trabajo/entrega, no un entorno de prueba aparte. Aplica a mundo, configs y decisiones (p. ej. `level-name=world`, el pegado de la fortaleza).

`MT_22000.world` (419 MB) → pintado como **`mundo\prueba\C_mundo_real.world`** (422 MB; pintar tarda 4,6 min). Niveles validados: `caves=6,cavernas=3,chasms=0,forest=8,taiga=8,jungle=6,plains=3,hills=4,populate=1`. Verificado además que el **spawn por defecto (centro del mapa, `0,0`) cae en `plains`**, o sea en tierra, **dentro del continente central (C3)** (medido 2026-10-18: pixel (1375,1375) de `biomas_2750.png` = valor 1; fuente: `python valor_pixel.py mundo\biomas_2750.png 1375 1375`, 2026-10-18). El 47 % del mapa es océano (fuente: tabla de biomas `python mapear_biomas.py mundo\salida_mundo\relieve_2750_16bit mundo\biomas_2750.png`, 2026-10-16: ocean 47,1%).

### Estado del export — COMPLETO (verificado 2026-10-18)

**El export terminó.** Verificado en disco el **2026-10-18** (fuente: `Get-ChildItem ENTREGA_Loding_20261017\05_mundo\world\*.mca | Measure-Object` + `Get-Content ENTREGA_Loding_20261017\05_mundo\SHA256MUNDO.txt`): el mundo final está en `ENTREGA_Loding_20261017\05_mundo\world\` — **1.936 `.mca`**, **11,49 GB**, `level.dat` en la raíz, y `datapacks\mt_core_26.2.zip` + `datapacks\worldpainter.zip` adentro. 36/36 marcas `.ok` en `salida_mundo\_control\marcas`, sin zonas fallidas y sin choques de región; `SHA256MUNDO.txt` con 1.941 archivos. Informe: `05_mundo\INFORME_MUNDO.txt`.

> **Actualizado 2026-10-22 — la carpeta de entrega ya no existe.** `ENTREGA_Loding_20261017\` **no está en disco**: `Test-Path` = `False` en las tres rutas probadas (`C:\Dev\OPENCLAUDIO\minecraft_server\ENTREGA_Loding_20261017`, `C:\Dev\OPENCLAUDIO\ENTREGA_Loding_20261017`, `E:\OPENCLAUDIO_archivo\ENTREGA_Loding_20261017`), medido 2026-10-22 (fuente: `Test-Path` en PowerShell, 2026-10-22 10:15). El **hecho histórico** del 2026-10-17/18 (movimiento del mundo, copia del datapack, `SHA256MUNDO.txt`, `INFORME_MUNDO.txt`) **ocurrió y no se niega**; lo que cambió es el disco de hoy: ni la carpeta ni `SHA256MUNDO.txt` están (búsqueda recursiva en `minecraft_server\`, 0 resultados, 2026-10-22; fuente: `Get-ChildItem -Recurse -Filter ENTREGA_Loding*`, 2026-10-22).
>
> - **Único árbol local con 1.936 regiones de overworld:** `minecraft_server\_prueba_roxy_local\world` — medido 2026-10-22 (fuente: `Get-ChildItem _prueba_roxy_local\world\*.mca | Measure-Object` + `(Get-ChildItem _prueba_roxy_local\world | Measure-Object -Sum Length).Sum / 1MB`): **1.944 `.mca`** (1.936 overworld + 4 nether + 4 end), **1.991 archivos**, **11.303,01 MB**. Que sea el ex-`05_mundo\world` es **`[INFERRED]`**: coincide el conteo de regiones, **no se comparó byte a byte** (el `SHA256MUNDO.txt` de la entrega no está en disco: no hay contra qué comparar).
> - **No confundir con `servidor\world`**, que tiene **16 `.mca`** (medido 2026-10-22; fuente: `Get-ChildItem servidor\world\*.mca | Measure-Object`): son **mundos distintos**.

- El orquestador cerró el **2026-10-17 17:48:48**: `TERMINADO. Zonas hechas en esta corrida: 28   regiones en el mundo final: 1936` (tiempo total 7.009 s). El watcher `mundo\watch_export_y_mover.ps1` verificó (1.936 regiones, sin fallidas, sin choques) y **movió** `salida_mundo\relieve_2750_16bit` → `ENTREGA_Loding_20261017\05_mundo\world`, copió el datapack adentro y generó `SHA256MUNDO.txt` (log: `mundo\salida_mundo\_control\watcher.log`). **El rename que faltaba (`relieve_2750_16bit` → `world`) lo hizo el watcher.** *(Registro histórico: ese destino ya no existe al 2026-10-22 — ver el bloque de arriba.)*
- Después del movimiento, `mundo\salida_mundo\` ya **no** contiene el mundo: quedan solo `_control\` (logs y marcas) y `_zonas\` (los 36 exports por zona, 1.872 `.mca`). Por eso `mundo\salida_mundo\relieve_2750_16bit\` **no existe** (si una referencia apunta ahí, quedó vieja).
- **1 región = 16 tiles**; el plan total es 29.584 tiles → 1.936 regiones.

**Historia del relanzado (2026-10-17) — se conserva como registro:**
- 376 → 440 regiones al 2026-10-17 15:52. **Export relanzado 2026-10-17 15:51** en modo reanudado sobre `mundo\salida_mundo\`. **Los dos parches funcionaron:** `z_-64_-64` (la que murió anoche) terminó en 405 s y el log muestra `REANUDO: zona base ya marcada, mergeo sobre ...`.
- **ETA realista: ~3–4 h, no 2–2,5 h.** El ritmo medido es ~136–150 chunks/s contra los ~340 chunks/s de la medición original (~2,4× más lento; fuente: `exportar_por_zonas.ps1` log zona 28, 2026-10-17 17:48), probablemente por presión de memoria: el JVM del export está en ~9,9 GB de working set y quedan ~1 GB de RAM libre de 13,9 GB (fuente: Task Manager / `Get-Process java | Select-Object WorkingSet`, 2026-10-17 17:30). El orquestador proyectaba `promedio 406 s/zona, restante 237 min`. No se interrumpe: reiniciar cuesta la zona en curso entera (el export borra el directorio destino de la zona).
- **Dos bugs del orquestador, parcheados ese mismo día** (eran la causa de que el export quedara trabado):
  1. **Fallo de zona fatal → ahora no fatal.** Un `exit=1` en una sola zona cortaba las 35 restantes. Medido: el 2026-10-16 23:36 la zona grande `z_-64_-64` (1024 tiles) falló con `exit=1` y el export se detuvo en 8/36. Ahora se loguea `FALLO <zona> ... -> se sigue con la proxima`, se acumula en `$fallidas` y **no** se escribe la marca `.ok` (queda para reintentar). Al final se lista `ZONAS FALLIDAS (sin marca, reintentar en otra corrida): ...`.
  2. **Bug de reanudado.** Al saltear la zona base (ya marcada), `$regionFinal` quedaba `$null` y la primera zona no salteada moría en `throw 'La zona base tiene que ir primero'`: se exportaba una zona y se perdía. Ahora, si no hay `$regionFinal`, se toma de `<Destino>\relieve_2750_16bit\dimensions\minecraft\overworld\region` cuando existe (loguea `REANUDO: zona base ya marcada, mergeo sobre ...`).
- **Verificado después del parche:** `-SoloPlan` sigue dando 36 zonas / 29584 tiles / 1936 regiones / 0 compartidas.
- **Un "export corriendo" que no crea `.mca` nuevos no está exportando.** Medido el 2026-10-17: entre 10:54 y 15:08 se reportó un "export directo" en curso que nunca escribió nada (su destino `mundo\salida_mundo_directo\` no existe). Verificar siempre el conteo de `.mca` antes de confiar en el proceso.
- **Heap:** `wpscript.vmoptions` trae `-Xmx12159M` (default del instalador; **no se toca**, regla 3). La máquina tiene 13,9 GB de RAM (fuente: `systeminfo | FindStr "Total Physical Memory"`, 2026-10-17) y ~8,6 GB libres: el working set del export llega a ~8,2 GB (fuente: `Get-Process java | Select-Object WorkingSet`, 2026-10-17 17:30). Ojo con la presión de memoria en zonas grandes.

**Hecho:** el export (36/36 zonas, 1.936 regiones completas) y la carpeta de entrega `ENTREGA_Loding_20261017\` (01_plugins, 02_datapack, 03_configuracion, 04_resourcepacks y `05_mundo\world`). **Actualizado 2026-10-22:** esa carpeta **ya no existe en disco**; ver el bloque "la carpeta de entrega ya no existe" al inicio de esta sección.

Falta: colocar la **arena de Hitogami** (trial chamber principal, `(-2304, -16, -3008)`) + el portal al End (detrás de esa arena) dentro del mundo ya exportado, subir por SFTP (ver `mundo\COMO_SUBIR.md`) y probar con cliente Bedrock real.

**Actualizado 2026-10-22:** las **10 trial chambers secundarias (SC1–SC10)** están **colocadas** — dato reportado por el usuario; **no verificable desde este repo** (el pegado se hizo sobre el servidor oficial; los `.log` de texto de `servidor\logs\` no registran `//paste` ni el schematic). Lo único pendiente de ese conjunto es la **arena de Hitogami**, y **no está decidido con qué `.schem` se arma**: `mundo\PLAN_TRIAL_CHAMBER_JEFE_PORTAL_END_2026-10-21.md` §2.1 recomienda `compact_trial_chamber.ofs0.schem`, que **no existe en disco** (medido 2026-10-22; fuente: `Get-ChildItem -Recurse -Filter compact_trial_chamber*`); `INSTRUCCIONES\TRIAL_CHAMBERS_COORDENADAS.md` dice `large_vanilla_trial_chamber.schem`, que según `laberintos\INFORME_LABERINTOS.md` tiene **0 block entities** (vaults/spawners solo en la paleta → no funcionan).

La **Fase 9** (Fortaleza de Perugius) ya está **pegada en el server oficial (2026-10-18)**; punto y spawn/teleport `−1016 227 6417` en `perugius\UBICACION_RESERVADA_FORTALEZA_PERUGIUS.md`.

Traspaso completo de la sesión: `.opencode\traspaso_2026-10-17_minecraft.md` (incluye el detalle de que el resumen de compactación describía como hecho trabajo que no existe en disco).

### Server local — `level-name` inconsistente (2026-10-22) `[A VERIFICAR]`

Medido en este turno, sin resolver (fuente: `Get-ChildItem` y `Get-Content` en PowerShell, 2026-10-22 10:30):

| Dato | Valor medido (2026-10-22) |
|---|---|
| `servidor\server.properties` | `level-name=.._prueba_roxy_localworld` — **sin separadores de ruta** (`.._prueba_roxy_localworld`, no `..\_prueba_roxy_local\world`) |
| Directorio literal que existe | `servidor\.._prueba_roxy_localworld\` — **41 `.mca`**, creado **2026-10-22 01:17:03** |
| `servidor\world\` | **16 `.mca`** |
| `_prueba_roxy_local\world\` | **1.944 `.mca`** (1.991 archivos, 11.303,01 MB) |

Son **tres ubicaciones distintas**: `servidor\world` y `_prueba_roxy_local\world` son **mundos distintos**, y el directorio literal con puntos es un tercer lugar que **parece** haber creado Paper al usar el valor tal cual (`[A VERIFICAR]`: se infiere de la fecha de creación, no de un log). **La intención de esa configuración está bajo investigación** — no se explica acá ni se corrige; queda marcada como `[A VERIFICAR]`. **No modificar `server.properties`** sin decisión del usuario.

### MushokuCore — Fase 2 (wiring) y prueba de arranque real (2026-10-18)

**Primera prueba de runtime del plugin** (hasta entonces solo había compilación): arranque del servidor local completo (19 plugins) con el jar instalado, comandos por stdin vía un script clonado de `_consola.ps1` (log propio en `%TEMP%\opencode\prueba_mushokucore_*.log`, para no pisar `consola.log`), y baja con `stop`. Tres corridas: en frío, con wiring, y de reinicio.

| Hecho | Dato |
|---|---|
| Wiring de Fase 2 | `PluginBootstrap.enable()` ahora llama `InfraCore.instalar(plugin, contexto)` antes de los módulos, y `disable()` llama `InfraCore.cerrar(infra)`. Sin eso, `ranks` y `diseases` quedaban apagados: medido — `IllegalStateException: PluginConfig sin proveer todavia` y el plugin arrancaba en modo degradado (fuente: `prueba_mushokucore_20261018_00*.log` corridas 1–2, 2026-10-18) |
| Bug de migraciones (**arreglado**) | `MigrationRunner.versionDeNombre` exigía `VNNN__…` anclado al inicio, pero el listado del JAR entrega `migrations/V001__initial.sql`. Resultado: el plugin **se auto-deshabilitaba entero** al arrancar (`IllegalArgumentException: Nombre de migracion invalido`). El test de A usaba el directorio en disco (sin prefijo), así que no lo cubría. Fix: se calcula la versión sobre el nombre base. Verificado: aplica la V001 |
| Bug de shutdown (**arreglado y re-verificado por el integrador**) | `PotenciasModule.disable()` (L146) programaba una tarea async en `onDisable` → `IllegalPluginAccessException: Plugin attempted to register task while disabled`. No perdía datos (el cierre de infra hace `flushAll()` después), pero tiraba excepción. **Fix de B, verificado en disco 2026-10-18** (fuente: `Get-Content MushokuCore\src\main\java\...\PotenciasModule.java`, 2026-10-18 00:58:04): `PotenciasModule.disable()` ya no programa nada (`PotenciasModule.java` 8.176 B, 00:58:04) y los `catch` de registro en el scheduler pasaron de `IllegalStateException` a `RuntimeException` (`IllegalPluginAccessException extends RuntimeException`) en `PotenciasService.java` L654/L675 (32.993 B, 00:57:51) y `LuckPermsOutbox.java` L139 (10.954 B, 00:58:52). **Medido en la 4.ª corrida** (log `prueba_mushokucore_20261018_010112_Bfix.log`, L425): `Potencias: tareas canceladas; el vaciado final lo hace db.flushAll()...` y **cero** `IllegalPluginAccessException`; después `Colas de escritura vaciadas (flushAll)` + `SQLite cerrado (mushoku.db)`, `exit=0` |
| Arranque OK (verificado) | `Infra-core instalado: SQLite=mushoku.db, floodgate=si, luckperms=si`; `Modulos habilitados: [ranks, diseases, commands]`; `mushoku.db` 102.400 B con las 7 potencias sembradas; `/mushoku estado` y `/mushoku diagnostico` completos (Config schema 2, IPotenciasService OK, IEnfermedadesService OK, LuckPerms/PlaceholderAPI/floodgate/Towny OK, BD OK) (fuente: `prueba_mushokucore_20261018_010112_Bfix.log`, 2026-10-18) |
| Persistencia OK (verificado) | Segunda corrida: `SQLite listo: mushoku.db (esquema al dia, 1 migraciones en el JAR)` → **no re-aplica** la migración; `cache de puestos cargada (7 filas)` (fuente: `prueba_mushokucore_20261018_010112_Bfix.log`, 2026-10-18) |
| Deshabilitado limpio | `Disabling MushokuCore` + los 3 módulos deshabilitados, `exit=0` en las tres corridas |
| Hallazgo externo (**solo en el server local, requiere tu decisión**) | En `servidor\plugins\` hay dos jars de Duels: `Duels-Optimised-8.2.jar` (10-14) y `Duels-Optimised-8.3.jar` (10-18 00:50:25, 4.845.214 B) + un archivo `Duels` de 1 B; Paper avisa `Ambiguous plugin name 'Duels'`. No los creé yo: es un auto-update del plugin o una descarga en curso `[A VERIFICAR]`. **En el hosting NO pasa**: `ls /plugins` (2026-10-18 01:11, SFTP) muestra un solo `Duels-Optimised-8.3.jar` |
| Cómo se compila (**trampa**) | `build.ps1` **tiene que correr en un proceso PowerShell hijo**: `powershell -NoProfile -ExecutionPolicy Bypass -File .\MushokuCore\build.ps1 *> log`. Invocado en línea (`& .\build.ps1`) el `Note: Some input files use or override a deprecated API.` que javac manda a **stderr** se convierte en `NativeCommandError` y, con el `$ErrorActionPreference='Stop'` del script, **corta la corrida justo después de `== javac ==`**: las clases quedan escritas pero **no se arma el jar**. Síntoma: el log termina en `== javac ==` y no hay `build\libs\*.jar` nuevo |

Pendiente de la prueba (no cubierto): jugador real conectado (Bedrock/Java), potencias por daño a jefe, enfermedades en vivo, y el registro de la expansión PAPI (V3).

#### Revisión de mensajes y `/mushoku reload` (2026-10-18)

- **Auditoría de claves de `messages.yml` (re-hecha por el integrador).** Se cruzaron las claves literales usadas en el código contra el YAML: **0 claves sin definir**. La lista de "15 claves faltantes" que había reportado el agente D **no se pudo reproducir**; lo único real era `mtestado.solo-jugadores`, que estaba hardcodeada con un comentario `[PROPUESTA D … pendiente para Fase 2]`. **Resuelto**: la clave se agregó a `messages.yml` (29 claves) y `MtestadoCommand.java` L37 ahora la usa. Ojo con dos falsos positivos al auditar: `enfermedad.aplicar/curar/expirar`, `import.skript`, `potencia.asignar` son **acciones de `audit_log`**, no claves de mensaje; y `DiseasesService` resuelve texto por el helper `avisar(...)`, así que un `grep` de `texto(` los da por no usados.
- **`/mushoku reload` es honesto desde 2026-10-18.** Recarga **solo `messages.yml`**; el texto `generic.reload-ok` decía "Configuración recargada" y ahora dice `Recargado messages.yml (config.yml requiere reinicio). Módulos: {modulos}`. Recargar `config.yml` en caliente sigue **pendiente a propósito**: `PluginContextImpl.proveerConfig()` existe, pero los módulos leen `ctx.config()` 20 veces entre `enable()` y operación (p. ej. `PotenciasModule` 7, `PotenciasService` 5), así que una recarga parcial dejaría valores viejos en unos caminos y nuevos en otros. La decisión (recargar en caliente vs. exigir reinicio) es del usuario.
- **Ojo al probar cambios en `messages.yml`:** Paper **no** vuelve a extraer los recursos del jar si `plugins/MushokuCore/messages.yml` ya existe. Un cambio en `src\main\resources\messages.yml` **no se ve** hasta copiar el archivo a `servidor\plugins\MushokuCore\` a mano (o sobre una instalación nueva). Medido: la primera corrida con el jar nuevo seguía mostrando el texto viejo hasta copiar el YAML.

#### Primera subida del plugin al hosting (2026-10-18)

| Hecho | Dato |
|---|---|
| Artefacto | `MushokuCore\build\libs\MushokuCore-1.0.0.jar` — **188.503 B**, SHA256 `A2D4855F90984BEBBD08BD2EA75A206C9A79D9AE8F2E9D6C3D48ACAC2A7CA9FD`. **Actualizado 2026-10-22:** `MushokuCore\build\libs\` está **vacío** (medido 2026-10-22; fuente: `Get-ChildItem MushokuCore\build\libs`, 2026-10-22 10:20): ese artefacto no está hoy en esa ruta |
| Destino | `/plugins/MushokuCore-1.0.0.jar` del hosting (`<HOST_SFTP>:<PUERTO_SFTP>`) |
| Cómo | `mundo\subir_sftp.py` (paramiko, reanudable) invocado por un wrapper que lee la credencial desde una fuente local excluida y **enmascara usuario/contraseña** en la salida. `--dry-run` primero |
| Verificación | El jar remoto se bajó y se hasheó: **tamaño 188.503 B y SHA256 idénticos** al local. `/plugins` pasó de 22 a **23 jars**; `/plugins/MushokuCore` todavía no existe (lo crea el server al arrancar) |
| Falta (al 2026-10-18) | **Reiniciar el servidor del panel** para que Paper cargue el plugin (por SFTP no se puede). Después, en consola: `/mushoku diagnostico`. **→ resuelto el 2026-10-22, ver abajo** |
| Nota WinSCP | El sitio guardado en WinSCP (`<usuario>@<HOST_SFTP>`) **no tiene la contraseña guardada**, así que en modo script pide prompt y aborta (`No se facilitaron credenciales.`). Para subir sin intervención humana hay que usar `subir_sftp.py` (paramiko) o tipear la contraseña en la GUI |

#### Segunda subida — JAR nuevo y primera carga real en producción (2026-10-22)

| Hecho | Dato |
|---|---|
| **Había 3 JAR distintos** | `servidor\plugins\` **200.688 B** SHA256 `cc2140d5…90d0dd` (20/10) · entrega **200.444 B** `088940a7…` (18/10) · hosting **191.237 B** `2a090a47…`. Ninguno coincidía con el otro |
| Cuál vale | El del servidor local (200.688 B, 20/10) es el más nuevo: se copió a la entrega (`ENTREGA_Loding_20261017\01_plugins\MushokuCore-1.0.0.jar` **ya no existe en disco; copia viva: `servidor\plugins\MushokuCore-1.0.0.jar`**, hash verificado) y se regeneró `SHA256SUMS.txt` (19 entradas). **Actualizado 2026-10-22:** la copia de la entrega **ya no existe**; la copia viva del JAR es `servidor\plugins\MushokuCore-1.0.0.jar` (200.688 B, SHA256 `cc2140d5…90d0dd`, medido 2026-10-22; fuente: `Get-FileHash servidor\plugins\MushokuCore-1.0.0.jar -Algorithm SHA256`, 2026-10-22 10:25) |
| Subida al hosting | `mundo\reemplazar_sftp_verificado.py` (**no** `subir_sftp.py`: ése reanuda por tamaño y corrompe un archivo distinto con el mismo nombre parcial) |
| Verificación de subida | El JAR remoto se bajó y se hasheó: **200.688 B y SHA256 `cc2140d5ec439ca5afee1e42d4776f4ad4a6633646a53f7d9d1ab3950917d0dd`**, idénticos al local |
| **Carga real en producción** | **CONFIRMADO POR EL USUARIO el 2026-10-22:** reinició el panel y corrió `/mushoku diagnostico` → *"funciona perfectamente"*. **Es reporte del usuario, NO medido por el agente:** no leí la salida de la consola del hosting ni el `logs/latest.log` remoto (fuente: conversación con usuario, 2026-10-22), así que el detalle línea por línea queda `[A VERIFICAR]` |
| Consecuencia | El plugin corre en producción con el JAR del 20/10. Lo que sigue pendiente **no es el plugin**: es la **arena de Hitogami + portal al End** (no construidos) y la **naturalización de Rikarisu** (ver `mundo\COMANDOS_NATURALIZACION_RIKARISU_v2_VERIFICADO_2026-10-22.md`) |

### Medición de las capas pintadas (sobre el mundo real, un tile puro por bioma; fuente: `python medir_subsuelo.py _prueba_roxy_local\world\region`, 2026-10-17)

| Capa | Nivel | Verificación por export |
|---|---|---|
| Cuevas `Caves` / `Caverns` | 6 / 3 | **7,9 %** de aire bajo la superficie; el mismo terreno sin cuevas da **0,000 %** (fuente: `python medir_subsuelo.py _prueba_roxy_local\world\region`, 2026-10-17). Repartido en todas las franjas de y |
| `forest` (valor 4) | 8 | `oak_leaves` + `birch_leaves` (roble y abedul) |
| `taiga` (5) | 8 | `spruce_leaves` (pinos) |
| `jungle` (21) | 6 | `jungle_leaves` + algunos `oak_leaves` |
| `windswept_hills` (3) | 4 | `spruce` disperso |
| `plains` (1) | 3 | `oak` disperso |
| `beach` (16), `snowy_plains` (12) | — | sin árboles. En `snowy_plains` es lo correcto (vanilla tampoco tiene) |

**Tabla valor→bioma cerrada** (los dos últimos se resolvieron el 2026-10-16): `0=ocean`, `1=plains`, `2=desert`, `3=windswept_hills`, `4=forest`, `5=taiga`, `7=river`, `12=snowy_plains`, `16=beach`, `21=jungle`.

**Dos riesgos medidos que condicionan el export:**
1. **El export BORRA el directorio destino.** Exportar dos veces al mismo directorio deja solo la última corrida (medido: 81 regiones → 4; fuente: `exportar_por_zona.js` prueba zona 81 tiles, 2026-10-16). Por eso cada zona va a su propio directorio.
2. **Las zonas tienen que estar alineadas a `Lado`** (múltiplo de 4). Si dos zonas comparten una región, las dos escriben el mismo `r.X.Z.mca` y el merge pisa chunks. `exportar_por_zonas.ps1` aborta si detecta regiones compartidas (medido con `-SoloPlan`: 1.936 regiones distintas, 0 compartidas; fuente: `exportar_por_zonas.ps1 -SoloPlan`, 2026-10-17).

Decisiones ya tomadas por el usuario: nombre del mundo `world` (2026-10-18; antes `mushoku_world`, descartado); altura **y=511**; portal al End personalizado, oculto tras una trial chamber muy difícil; contraseña de SFTP actualizada. **Actualizado 2026-10-17:** el End queda **permitido** (`settings.allow-end: true`) y hay **un único** portal del End, bloqueado hasta completar los 3 sellos.

### Decisiones resueltas por el usuario (2026-10-16 / 2026-10-17)
> **Razón de clonación:** Misma disciplina de "no fabricar resultados" (regla 1) exige misma plantilla; no es copia sin pensamiento.

| Tema | Decisión | Aplicado en |
|---|---|---|
| Borde del mundo | **21.800** de diámetro, centrado en 0,0 (dentro de un mapa de 22.000) | `setup.mcfunction` (3 copias), `MushokuCore\config.yml`, `README_PRIMERO`, `PERFIL_RENDIMIENTO` |
| `max-players` | **24** | `servidor\server.properties`, `server.properties.inicial`, `README_PRIMERO`, `PERFIL_RENDIMIENTO` |
| `simulation-distance` | **6** | Ya estaba en `server.properties` y `PERFIL_RENDIMIENTO`; se corrigió el Manual, que decía 5 |
| Portal al End + trial chamber | Personalizado, oculto tras una trial chamber **muy difícil**: hay que **derrotar a un jefe** para pasar | `MT_Server_Base_26.2\FORTALEZA_END_DISENO.md` (recorrido de 6 pasos) + `DISENO_TRIAL_CHAMBER_JEFE.md` (**531 líneas totales / 440 no vacías**, 30,9 KB, reconstruido 2026-10-17) |
| **End permitido (2026-10-17)** | **`settings.allow-end: true`.** Es seguro porque **no existe ningún bloque `end_portal` hasta que muere el jefe**: nadie llega al End. **Verificado en disco 2026-10-17** (fuente: `Get-Content MT_Server_Base_26.2\configuracion\bukkit.yml.fragmento-inicial`, `Get-Content servidor\bukkit.yml`, 2026-10-17) | `MT_Server_Base_26.2\configuracion\bukkit.yml.fragmento-inicial` y `servidor\bukkit.yml` |
| **Un solo portal del End (2026-10-17)** | **Un único** portal, **bloqueado hasta su desbloqueo** (completar los 3 sellos). Recién entonces se coloca el bloque `end_portal` | `MT_Server_Base_26.2\FORTALEZA_END_DISENO.md` + `DISENO_TRIAL_CHAMBER_JEFE.md` |
| **`level-name` (2026-10-18)** | **`level-name=world`**. Nombre del mundo = `world` (reemplaza a `mushoku_world`, descartado por el usuario el 2026-10-18). **Verificado en disco 2026-10-18** (fuente: `Get-Content MT_Server_Base_26.2\configuracion\server.properties.inicial`, `Get-Content MushokuCore\src\main\resources\config.yml`, 2026-10-18) | `MT_Server_Base_26.2\configuracion\server.properties.inicial`, `ENTREGA_Loding_20261017\03_configuracion\server.properties.inicial` **(ya no existe en disco; copia viva: `MT_Server_Base_26.2\configuracion\server.properties.inicial`)**, `MushokuCore\src\main\resources\config.yml`, carpeta `05_mundo\world\` **(ya no existe en disco; copia viva: `_prueba_roxy_local\world` `[INFERRED]`)** |
| **`online-mode` (2026-10-18)** | **`online-mode=false` por defecto** — decisión del usuario. Lo que protege es `white-list=true` + `enforce-whitelist=true` (Bedrock no tiene sesión Mojang). **Verificado en disco 2026-10-18** (fuente: `Get-Content MT_Server_Base_26.2\configuracion\server.properties.inicial`, `Get-Content servidor\server.properties`, 2026-10-18; las 3 copias alineadas; antes el `.inicial` del paquete traía `true` por drift) | `MT_Server_Base_26.2\configuracion\server.properties.inicial`, `ENTREGA_Loding_20261017\03_configuracion\server.properties.inicial` **(ya no existe en disco; copia viva: `MT_Server_Base_26.2\configuracion\server.properties.inicial`)**, `servidor\server.properties` |

> **Actualizado 2026-10-22 — las rutas de la entrega en esta tabla ya no existen.** `ENTREGA_Loding_20261017\` **no está en disco** (verificado en C: y E:, 2026-10-22; fuente: `Test-Path` en PowerShell, 2026-10-22 10:15), así que las celdas que la citan (`03_configuracion\server.properties.inicial`, `05_mundo\world\`) describen dónde se **aplicó** la decisión en su momento, no un archivo consultable hoy. Las **copias vivas** de esas decisiones son `MT_Server_Base_26.2\configuracion\` (`server.properties.inicial`, `bukkit.yml.fragmento-inicial`) y `servidor\` (`server.properties`, `bukkit.yml`); el mundo vivo es `_prueba_roxy_local\world` **`[INFERRED]`**.

> **Diseño del jefe:** `DISENO_TRIAL_CHAMBER_JEFE.md` (**531 líneas totales / 440 no vacías**, reconstruido el 2026-10-17 desde el historial de opencode; nunca existió en disco antes). El nombre del jefe ("Hitogami") **NO tiene aprobación explícita del usuario** — está marcado **[A VERIFICAR]**. **Ojo:** varios pasajes del doc afirman que `allow-end` está en `false` o "no aplicado"; son un **snapshot anterior** al cambio del 2026-10-17 (ver el bloque "ACTUALIZACIÓN" al inicio del doc).
> **Ojo (verificado 2026-10-17; fuente: `Get-Content MT_Server_Base_26.2\configuracion\bukkit.yml.fragmento-inicial`, `Get-Content ENTREGA_Loding_20261017\03_configuracion\bukkit.yml`, `Get-Content servidor\bukkit.yml`, 2026-10-17):** las **tres** copias del fragmento estaban alineadas en `allow-end: true` — `MT_Server_Base_26.2\configuracion\bukkit.yml.fragmento-inicial`, su copia en `ENTREGA_Loding_20261017\03_configuracion\` y `servidor\bukkit.yml`. (Un subagente reportó el 2026-10-17 que la copia de la entrega seguía en `false`: era una **lectura previa** al arreglo. Falso al momento de reportarlo — la regla 1 existe exactamente para esto.) **Actualizado 2026-10-22:** la copia de la entrega ya no existe, así que hoy las copias comparables son **dos**, no tres: `MT_Server_Base_26.2\configuracion\bukkit.yml.fragmento-inicial` y `servidor\bukkit.yml`.

> Al tocar el paquete del servidor se **regeneraron** `datapacks\mt_core_26.2.zip`, `SHA256SUMS.txt` (3/3 verifican) y `MT_Server_Base_26.2_Geyser.zip`. Los scripts de regeneración están en `servidor\_backups\regen_mt_core_zip.py` y `regen_package_zip.py`.

### ⚠️ Requieren decisión del usuario — un agente NO las resuelve solo

| Tema | Opción A | Opción B | Dónde aparece cada una |
|---|---|---|---|
| Geodas | capa nativa: no hay | evaluar `pockets/UndergroundPocketsLayer` con amatista | Requerimientos, ESTRUCTURAS_SUBTERRANEAS |
| Estructuras (minas, Trial Chambers, Ancient Cities, dungeons) | colocar a mano | — | `ESTRUCTURAS_SUBTERRANEAS.md` |

Otros pendientes conocidos: probar la consola del hosting por RCON; test con cliente Bedrock real.

<!-- antislop:start -->
## antislop
For UI, copy, people, mobile layout, or code comments work, read these installed skill files directly (use these paths even if a same-named global skill exists):
- Core filter, always on: `antislop`: `C:/Dev/Minecraft-server/.opencode/skills/antislop/SKILL.md`
- Copy & text: `antislop-copywriting`: `C:/Dev/Minecraft-server/.opencode/skills/antislop-copywriting/SKILL.md`
- Code comments: `antislop-code`: `C:/Dev/Minecraft-server/.opencode/skills/antislop-code/SKILL.md`
Before starting, follow the core's "Two Usage Modes" section in strict order: explicit session instruction first, then global preference, then ask. A session instruction always wins. For a resolved mode, say `antislop active: <mode> (session override).` or `antislop active: <mode> (global preference).` once before presenting findings or making edits, using the actual mode and source. Acknowledging the user's request without naming the source does not replace this notice.
Only an explicit choice of antislop during or after selects a session mode. A request to review, audit, or avoid file edits does not select a mode; read the global preference in that case. Another skill's mode does not select antislop's mode.
If the mode is unresolved, ask during/after and end the response; wait for the answer before any UI review, planning, or concept. For read-only tasks, put the active-mode notice only at the start of the final answer, never in progress messages. For editing tasks, announce before the first edit and omit it from the final answer.
To update antislop later: `npx antislop-ai --update`, or run `npx antislop-ai` and pick Overwrite them.
<!-- antislop:end -->

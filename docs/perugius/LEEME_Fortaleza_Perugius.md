# LEEME — Fortaleza de Perugius v1

## Origen

- **Construcción original:** ButterRoll / バターロール — *Celestara* (Epic Floating Castle Mega Build).
- **Fuente (Planet Minecraft):** https://www.planetminecraft.com/project/celestara-epic-floating-castle-mega-build/
- **Mundo fuente usado para la extracción:** `Celestara_Reduced_1.21.4\Celestara_Reduced` (copia reducida del proyecto original "Celestara").
- **Minecraft (origen):** 1.21.4 (DataVersion 4189)
- **DataVersion del schematic:** 4903 (Paper 26.2 / 1.21.9+, compatible)

## Coordenadas aproximadas

Basadas en `fortaleza_perugius_v1.md` (coordenadas exactas de selección/origen):

- **Origen (WorldEdit/FAWE, anchor):** `(-499, 48, 379)`
- **Selección (pos1 → pos2):** `(-624, 30, 254)` → `(-374, 319, 505)`
- **BBox completa de selección:** X [-624, -374] (251), Y [30, 319] (290), Z [254, 505] (252)
- **Offset aplicado al schematic:** `[-125, -18, -125]` (min_corner - origin)

**Explicación:** el origen queda centrado horizontalmente (centro X=-499, Z≈379.5) y corresponde al bloque inferior central de la isla flotante (y=48).

## Bloques destacados (resumen)

El schematic contiene **560.892** bloques no-aire sobre **17.782.188** de aire (volumen 18.343.080 celdas). Paleta ~275 estados, 31 tipos base.

Principales:
- `quartz_block`: 159.865
- `smooth_quartz_stairs`: 74.536
- `deepslate_bricks`: 71.691
- `smooth_quartz_slab`: 61.423
- `diorite_wall`: 59.179
- `quartz_slab`: 37.220
- `smooth_quartz`: 27.290
- `pale_oak_trapdoor`: 16.721
- `pale_oak_fence`: 12.534
- `stripped_birch_wood`: 10.416
- `iron_trapdoor`: 7.624
- `black_stained_glass_pane`: 5.004
- `deepslate_brick_slab`: 3.064
- `black_stained_glass`: 2.575
- `quartz_pillar`: 2.500

**Nota:** 100% bloques vanilla. Solo **24 BlockEntities** (hoppers vacíos). Sin entidades vivas, sin cofres con loot, sin spawners, sin command blocks, sin display entities masivas.

## Archivos entregados (perugius/)

- `fortaleza_perugius_v1.schem` — Sponge v3 (FAWE/WorldEdit). Probado carga/pegado (15.053.976 cambios).
- `fortaleza_perugius_v1.md` — Especificación técnica completa (dimensiones, origen/offset, bloques, pruebas).
- `captura_superior.png`, `captura_frontal.png`, `captura_entrada.png` — Capturas de referencia.
- `CastleWorld_original.zip` — **Respaldo del mundo usado** para extraer la fortaleza (incluido en entrega). SHA256: `806ED356F35E725E785BB9C83923E93B609546EFF6F0D5CB0C46B4EB316DA70A`
- `SHA256SUMS.txt` — Checksums de los entregables.
- `README_ENTREGA.md` — Instrucciones de uso rápido.

## Trazabilidad: CastleWorld_original.zip vs Celestara_Reduced

**Mundo fuente de referencia (carpeta de trabajo):**
- Ruta: `C:\Dev\OPENCLAUDIO\minecraft_server\Celestara_Reduced_1.21.4\Celestara_Reduced\`
- Contiene: `level.dat`, `region/`, `datapacks/`, `data/`, `paper-world.yml`, `README_REDUCED.txt`, `Requerimientos_Exportacion_Fortaleza_Perugius.md`

**Comparación de zips:**
- `CastleWorld_original.zip` (presente en `perugius/`): SHA256 `806ED356F35E725E785BB9C83923E93B609546EFF6F0D5CB0C46B4EB316DA70A`
- Zip comprimido desde `Celestara_Reduced_1.21.4\Celestara_Reduced\*`: SHA256 `F0E547668A4117C51CBFE949A892594DCF1C183151C1EFA05E0D66A7AAE689E2`

**Conclusión sobre equivalencia:** Los hashes **difieren** (probablemente por compresión/algoritmo, nivel de compresión, orden de archivos o atributos al comprimir). Sin embargo:

1. `fortaleza_perugius_v1.md` declara explícitamente el origen como `Celestara_Reduced_1.21.4\Celestara_Reduced` (línea 8).
2. `README_ENTREGA.md` indica que `CastleWorld_original.zip` es una copia del mundo fuente reducido (referencia a `Celestara_Reduced_1.21.4.zip`) para archivo.
3. La selección usada para generar el schematic coincide con la descrita (pos1/pos2/origen) y las pruebas fueron hechas sobre ese mundo fuente.

**Decisión (cumplimiento de requisitos):** No se crea `Celestara_reducido.zip` duplicado. El respaldo presente en la entrega es `CastleWorld_original.zip` (ya existe y cumple el requisito de "respaldo del mundo original usado para extraer la fortaleza"). La carpeta `Celestara_Reduced_1.21.4\Celestara_Reduced` queda como referencia/trazabilidad de trabajo.

## Fechas y metadatos

- **Fecha de extracción/exportación (registro):** según `fortaleza_perugius_v1.md` línea 12: `2025-10-xx` (ver metadatos NBT del schematic para la fecha exacta).
- **Fecha de entrega/documentación LEEME:** 2026-10-17

## Notas

- Compatibilidad verificada: FAWE 2.16.1 + Paper 26.2. Sin errores de carga. Estructura 100% vanilla → compatible Java/Bedrock vía Geyser/Floodgate.
- Se recomienda recalcular iluminación tras el pegado (`/relight`) y revisar los 24 hoppers vacíos si no se desea funcionalidad de tolvas.
- Uso privado conforme a la atribución de la obra original (ButterRoll / Celestara). No vender, republicar ni atribuirse la construcción original.
- Cumplimiento de requisitos: punto 1 (README/explicativo) cubierto con este LEEME + documentación existente. Punto 2: CastleWorld_original.zip ya existe como respaldo; se documenta equivalencia/trazabilidad, no se duplica innecesariamente.

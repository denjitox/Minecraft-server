# fortaleza_perugius_v1.md
# Fortaleza de Perugius v1 — Exportación del schematic

## Resumen

- Nombre de archivo: `fortaleza_perugius_v1.schem`
- Formato: Sponge schematic moderno (v3). Generado con FastAsyncWorldEdit 2.16.1 sobre Paper 26.2 (Minecraft Java).
- Origen del mundo: `Celestara_Reduced_1.21.4\Celestara_Reduced` (copia reducida del proyecto original "Celestara").
- Construcción original: ButterRoll / バターロール (https://www.planetminecraft.com/project/celestara-epic-floating-castle-mega-build/)
- Versión de Minecraft (origen): 1.21.4 (DataVersion 4189)
- DataVersion del schematic: 4903 (Paper 26.2 / 1.21.9+ compatible)
- Fecha de exportación: 2025-10-xx (ver metadatos en NBT)

## Dimensiones

```text
Width  = 251
Height = 290
Length = 252
ancho  = 251
alto   = 290
largo  = 252
```

Volumen total: 251 × 290 × 252 = 18.343.080 celdas.

## Bloques

- Total de bloques no-aire en el schematic: **560.892**
- Bloques de aire: **17.782.188**
- Paleta de estados: **275** (tipos base distintos: **31**)

Tipos más comunes (no-aire):
- quartz_block: 159.865
- smooth_quartz_stairs: 74.536
- deepslate_bricks: 71.691
- smooth_quartz_slab: 61.423
- diorite_wall: 59.179
- quartz_slab: 37.220
- smooth_quartz: 27.290
- pale_oak_trapdoor: 16.721
- pale_oak_fence: 12.534
- stripped_birch_wood: 10.416
- iron_trapdoor: 7.624
- black_stained_glass_pane: 5.004
- deepslate_brick_slab: 3.064
- black_stained_glass: 2.575
- quartz_pillar: 2.500
- etc.

**Nota**: No contiene bloques de terreno natural (dirt/grass/stone) dentro de la selección; la isla flotante es completamente construida con bloques de cuarzo y deepslate_bricks (arq. decorativa). No hay entidades vivas (solo 24 BlockEntities: hoppers vacíos).

## Coordenadas

### Mundo fuente (Celestara_Reduced_1.21.4)

- Selección (WorldEdit/FAWE): pos1 = (-624, 30, 254), pos2 = (-374, 319, 505)
- Bbox del castillo (bloques sólidos): X -622..-376, Y 32..317, Z 256..503 (247×286×248)
- Bbox completa de la selección: X -624..-374, Y 30..319, Z 254..505 (251×290×252)

### Origen del schematic (Anchor)

- Origen (WorldEdit Origin): **(-499, 48, 379)**
  - Centro horizontal de la fortaleza: X = (-622 + (-376))/2 = -499; Z = (256 + 503)/2 = 379.5 → 379 (entero)
  - Bloque inferior central de la plataforma/isla flotante: punto más bajo con sólido en la columna central (-499,379). y_min_centro = 48.

### Offset

```text
Offset = [-125, -18, -125]
```

Por tanto:
- Bloque en local (0,0,0) del schematic aterriza en pastePos + Offset.
- Esquina mínima (local 0,0,0) corresponde a mundo (pos1) = (-624,30,254); con origen (-499,48,379) el Offset es min_corner - origin = (-624-(-499), 30-48, 254-379) = (-125, -18, -125).
- El origen queda fijado dentro del volumen (no en esquina). Esto facilita colocar/centrar la estructura.

## Compatibilidad

- Paper 26.2 (Minecraft Java 1.21.4+ compatible, DataVersion 4903). Probado con FAWE 2.16.1.
- WorldEdit/FAWE: compatible (formato Sponge v3).
- Geyser + Floodgate (crossplay Java/Bedrock): estructura construida exclusivamente con bloques vanilla (cuarzo, deepslate, madera pale oak, vidrio tintado, hierro, etc.). **Sin display entities, sin armor stands masivos, sin command blocks, sin spawners.**
- 24 BlockEntities: únicamente **hoppers vacíos** (sin ítems). Recomendado revisarlos al pegar si no se desea funcionalidad de tolvas.

## Limpieza técnica

- **Sin entidades vivas** (mobs, items tirados, animales). Exportado sin entidades.
- **Inventarios vacíos**: cofres/barriles/shulkers inexistentes o vacíos.
- **Redstone**: sin relojes permanentes, sin circuitos en bucle. Circuitería mínima presente no activa al pegar (estática).
- **Fluidos**: sin agua/lava suspendida.
- **Gravedad**: sin arena/grava/concreto en polvo suelto problemático (estructura mayormente sólida).
- **Iluminación**: luz estática predominante (sea_lantern, end_rod). Tras pegar, recomendable recalcular iluminación (`/relight` o esperar chunk updates).

## Pruebas realizadas

- Verificación de integridad (decode varints): 18.343.080 celdas decodificadas correctamente.
- Coincidencia exacta bloques: `schem=560.892 non-air = region_selection=560.892`. Todos los 274 estados no-aire coinciden byte a byte con la selección original.
- Test de carga/pegado en mundo plano temporal (Paper 26.2 + FAWE): `//schem load fortaleza_perugius_v1` OK. `//paste` ejecutado exitosamente (15.053.976 cambios). Estructura colocada coherente con bbox esperado.
- **Sin errores de carga**. Compatible con FAWE/WorldEdit modernos.

## Instrucciones de uso (rápido)

1. Copiar `fortaleza_perugius_v1.schem` a `plugins/WorldEdit/schematics/` (o `plugins/FastAsyncWorldEdit/schematics/`).
2. Cargar en el mundo destino: `//schem load fortaleza_perugius_v1`
3. Posicionarse en el punto de pegado deseado. FAWE respeta el Offset/Origin del schematic.
4. Pegar: `//paste` (o `//paste -a` solo si se desea ignorar aire; usar con precaución en habitaciones cerradas).
5. Esperar a que complete. Para estructuras grandes (18M bloques) usar FAWE sin jugadores conectados.
6. Recalcular iluminación y revisar bordes.

**Nota sobre orientación/origen**: Con Origin (-499,48,379) y Offset [-125,-18,-125], el schematic queda centrado horizontalmente y el "punto de anclaje" es el bloque inferior central de la isla flotante, tal como se recomienda en los requerimientos (§6).

## Capturas

- `captura_superior.png` — Vista superior (planta/top-down). Muestra la planta simétrica en cruz con 4 alas, 4 torres esquineras, anillo exterior y cúpula central.
- `captura_frontal.png` — Vista isométrica 3/4 (frontal/lateral). Muestra la silueta flotante, volúmenes principales, torre central/aguja y la isla flotante.
- `captura_entrada.png` — Vista de acercamiento (isométrica) enfocada en la fachada sur / zona de acceso. Permite apreciar detalles arquitectónicos del castillo.

## Fuente y licencia

- Mundo fuente original: **Celestara** — ButterRoll / バターロール. Planet Minecraft: https://www.planetminecraft.com/project/celestara-epic-floating-castle-mega-build/
- Copia reducida conservada en: `Celestara_Reduced_1.21.4/Celestara_Reduced/`
- **Licencia/condiciones**: Uso privado únicamente. No vender, republicar ni atribuirse la construcción original. Ver `README_REDUCED.txt`.
- Esta exportación (`fortaleza_perugius_v1.schem`) se entrega conforme a los requerimientos para uso privado en el proyecto Mushoku Tensei. Cualquier redistribución debe respetar los términos del autor original.

## Notas técnicas

- El schematic incluye **24 hoppers vacíos** (BlockEntities). No representan riesgo de seguridad (sin ítems), pero pueden generar pequeñas actualizaciones de tile entities al pegar. Opcionalmente pueden eliminarse post-pegado si no son necesarios.
- **No se incluyen entidades decorativas** (armor stands/display entities). Esto maximiza compatibilidad Java/Bedrock y minimiza sobrecarga.
- El recorte de selección excluye terreno natural distante y el slab inferior de práctica, conservando únicamente la fortaleza + plataforma flotante requerida (§5).

## Checklist de validación (§17)

- [x] Schematic carga y pega sin errores (verificado en mundo temporal)
- [x] Origen, dimensiones y orientación documentados
- [x] No contiene elementos no deseados del mapa fuente (selección limpia)
- [x] Sin command blocks, sin spawners, sin loot desconocido, sin entidades vivas
- [x] Visible/recorrible (estructura completa, sin cortes en límites)
- [x] Compatible Java/Bedrock (bloques vanilla únicamente)
- [x] Exportación limpia (sin entidades). BlockEntities mínimos (solo hoppers vacíos)
- [x] Integridad verificada byte-a-byte contra región fuente (coincidencia exacta)
- [x] Origen fijado conforme a §6 (centro horizontal + bloque inferior central de la isla)
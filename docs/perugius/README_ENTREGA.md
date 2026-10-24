# Entrega: Fortaleza de Perugius v1

## Contenido

- `fortaleza_perugius_v1.schem` — Schematic Sponge v3 listo para pegar con WorldEdit/FAWE (Paper 26.2 / 1.21.9+ compatible). DataVersion 4903. 251×290×252, 560.892 bloques no-aire.
- `fortaleza_perugius_v1.md` — Especificación completa (dimensiones, origen/offset, coordenadas, bloques, compatibilidad, pruebas).
- `captura_superior.png` — Vista superior (planta).
- `captura_frontal.png` — Vista isométrica 3/4 (frontal/lateral).
- `captura_entrada.png` — Vista de acercamiento isométrica (zona de acceso).
- `CastleWorld_original.zip` — Copia del mundo fuente reducido (`Celestara_Reduced_1.21.4.zip`) para archivo.
- `SHA256SUMS.txt` — Checksums SHA256 de los archivos entregables.

## Uso rápido

1. Copiar `fortaleza_perugius_v1.schem` a `plugins/FastAsyncWorldEdit/schematics/` (o `WorldEdit/schematics/`).
2. En el servidor: `//schem load fortaleza_perugius_v1`
3. Posicionarse en el punto de pegado. `//paste` (usar FAWE sin jugadores conectados para estructuras grandes).
4. Esperar a que termine, recalcular iluminación (`/relight`) y revisar.

## Notas

- Exportado sin entidades vivas. Solo 24 hoppers vacíos como BlockEntities.
- Bloques 100% vanilla. Sin command blocks, spawners, display entities masivas.
- Verificado: coincidencia exacta entre schematic y selección original (560.892 bloques).
- Origen (-499,48,379), Offset [-125,-18,-125) conforme a los requerimientos.

## Origen

Construcción original: ButterRoll / バターロール — Celestara (Planet Minecraft). Uso privado únicamente. No vender, republicar ni atribuirse la construcción original.
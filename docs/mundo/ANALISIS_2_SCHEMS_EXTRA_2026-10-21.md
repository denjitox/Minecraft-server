# Análisis de 2 Schematics Extra — 2026-10-21

Fuente: `C:\Dev\OPENCLAUDIO\minecraft_server\MT_Server_Base_26.2\plugins\FastAsyncWorldEdit\schematics\`

---

## 1. `netherfortress.schem`

| Propiedad | Valor |
|---|---|
| **Dimensiones** | 185 × 148 × 141 (W × H × L) |
| **Bloques no-aire** | 160.650 |
| **Tipos de bloque** | 127 |
| **Block Entities** | 2 |

### Composición principal (Top 10)

| Bloque | Cuenta | % del total |
|---|---:|---:|
| `red_nether_bricks` | 28.020 | 17,4% |
| `cracked_deepslate_bricks` | 11.240 | 7,0% |
| `warped_hyphae` | 11.209 | 7,0% |
| `cracked_deepslate_tiles` | 11.204 | 7,0% |
| `cracked_polished_blackstone_bricks` | 11.071 | 6,9% |
| `dirt` | 10.855 | 6,8% |
| `grass_block` | 6.363 | 4,0% |
| `nether_wart_block` | 5.618 | 3,5% |
| `shroomlight` | 4.329 | 2,7% |
| `soul_sand` | 3.080 | 1,9% |

**Observaciones:**
- Mezcla de materiales **Nether** (red_nether_bricks, nether_wart_block, shroomlight, soul_sand, warped/crimson nylium) y **Overworld/Deepslate** (cracked_deepslate_*, cracked_polished_blackstone_bricks, basalt, bone_block, dirt, grass_block).
- Presencia de **dirt (10.855) y grass_block (6.363)**: sugiere que el schematic incluye **terreno base** (posiblemente el suelo sobre el que se construyó la fortaleza), no solo la estructura.
- 2 Block Entities: probablemente cofres o spawners.

### Propósito temático
Fortaleza estilo **Nether** con influencia de deepslate/overworld — encaja con una **fortaleza del Nether personalizada** o una **ruina en el Nether** que combina ladrillos rojos del Nether con piedra profunda.

### Ubicación recomendada en mundo 22k
- **Bioma objetivo**: Nether (Dimensión -1). El mundo 22k es Overworld; para colocar esto haría falta:
  - Opción A: Generar/usar el Nether del servidor y pegar allí.
  - Opción B: Si el servidor tiene un portal al Nether personalizado, colocar cerca del portal de entrada.
- **Coordenadas sugeridas (Nether)**: X ~0, Z ~0 (cerca del spawn del Nether) o en una zona plana de *Basalt Deltas* / *Warped Forest*.
- **Y recomendado**: Y ≈ 60–70 (altura típica de fortalezas Nether).
- **Pegado**: `//paste -a` **NO** (trae dirt/grass_block propio → se vería raro en Nether). Usar `//paste` normal y **limpiar dirt/grass_block a mano** o editar el schematic antes (quitar bloques Y < fundación).

### Licencias / Origen
- Nombre de archivo genérico (`netherfortress.schem`). **Sin atribución clara**.
- **Acción requerida**: buscar origen (PlanetMinecraft, CurseForge, autor conocido) antes de usar en servidor público. Si no hay licencia clara, **no usar** o reconstruir desde cero.

### Veredicto
- **Requiere adaptación**: quitar dirt/grass_block (terreno) o confirmar que es parte intencional del diseño.
- **Pendiente**: confirmar licencia / autor.
- **No es prioridad** para el mundo Overworld 22k; pertenece al Nether.

---

## 2. `isla-del-end-el-uv87add1.schem`

| Propiedad | Valor |
|---|---|
| **Dimensiones** | 241 × 256 × 241 (W × H × L) |
| **Bloques no-aire** | 285.652 |
| **Tipos de bloque** | 66 |
| **Block Entities** | 21 |

### Composición principal (Top 10)

| Bloque | Cuenta | % del total |
|---|---:|---:|
| `obsidian` | 78.678 | 27,5% |
| `crying_obsidian` | 76.138 | 26,6% |
| `end_rod` (down/up) | 28.598 | 10,0% |
| `end_stone_brick_slab` | 23.591 | 8,3% |
| `end_stone_brick_wall` | 9.663 | 3,4% |
| `end_stone_brick_stairs` | ~34.000 | 11,9% |
| `end_stone` | 6.046 | 2,1% |
| `end_stone_bricks` | 2.364 | 0,8% |

**Observaciones:**
- Paleta **100% End**: obsidian, crying_obsidian, end_rod, end_stone, end_stone_bricks. **Cero bloques de Overworld/Nether**.
- 21 Block Entities: probablemente cofres, shulkers, spawners, end_portal_frame, lecterns.
- Muy limpio — **sin terreno residual**.

### Propósito temático
**Isla del End personalizada** — estructura mayor (posiblemente torre, ciudadela o altar central) con mucha obsidian/crying_obsidian y end_rods como iluminación. Encaja con la **Fortaleza del End / Portal al End** documentada en `FORTALEZA_END_DISENO.md`.

### Ubicación recomendada en mundo 22k
- **Dimensión**: The End (Dimensión 1).
- **Coordenadas**: Isla principal del End (0,0) o isla secundaria alejada (ej. X 1000, Z 1000).
- **Y recomendado**: Y ≈ 60–80 (altura típica de islas End).
- **Pegado**: `//paste -a` **SÍ** (no trae terreno, solo estructura). Alinear base de la isla con Y del terreno End (end_stone).
- **Rotación**: Probar 0° y 90° para orientar entrada hacia el portal principal.

### Licencias / Origen
- Nombre sugiere origen: **"isla-del-end-el-uv87add1"** → probable autor `uv87add1` (usuario de PlanetMinecraft / CurseForge / Discord).
- **Acción requerida**: contactar a `uv87add1` o buscar la publicación original para confirmar licencia (¿CC-BY? ¿Solo uso personal? ¿Redistribución permitida?).
- Si es de **PlanetMinecraft**, usualmente permite uso en servidores con crédito.

### Veredicto
- **Listo para usar** (paleta limpia, sin terreno).
- **Candidato ideal** para la **Fortaleza del End / Sala del Portal** del diseño `FORTALEZA_END_DISENO.md`.
- **Pendiente**: confirmar licencia y creditar a `uv87add1` / `sheluvjuli` si aplica.

---

## Resumen de acciones

| Schem | Acción inmediata | Prioridad |
|---|---|---|
| `netherfortress.schem` | Buscar licencia; decidir si quitar dirt/grass o usar en Nether | Baja (Nether) |
| `isla-del-end-el-uv87add1.schem` | Contactar a `uv87add1`; integrar en Fortaleza End / Portal End | **Alta** (End) |

---

## Próximos pasos recomendados

1. **Licencia `isla-del-end-el-uv87add1`**: buscar en PlanetMinecraft "isla del end uv87add1" o contactar al usuario.
2. **Integrar en `FORTALEZA_END_DISENO.md`**: añadir esta isla como base de la "Sala del portal" (paso 6 del recorrido).
3. **Preparar pegado en End**: cuando el mundo 22k esté subido y el End accesible, `//schem load isla-del-end-el-uv87add1; //tp @p <x> <y> <z>; //paste -a`.
4. **`netherfortress`**: dejar en espera hasta que se trabaje la dimensión Nether.
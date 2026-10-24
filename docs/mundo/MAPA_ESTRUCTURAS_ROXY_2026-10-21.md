# MAPA DEFINITIVO DE ESTRUCTURAS — Mundo Roxy Village Fixed
**Fecha:** 2026-10-21  
**Fuente:** `Roxy village fixed.zip` (184 regiones .mca)  
**Método:** Escaneo de PALETAS de todas las secciones de todos los chunks (no decodificación voxel-a-voxel).  
**Script:** `laberintos/_mapear_estructuras_roxy.py` → salida JSON `mundo/MAPA_ESTRUCTURAS_ROXY_2026-10-21.json`

---

## 1. RESUMEN EJECUTIVO

| Estructura | Regiones (.mca) | BBox bloques (X,Z) | Tipos clave |
|---|---|---|---|
| **CASTILLO RIKARISU** (núcleo) | **r.-1.1, r.-1.2** | X -512..-1, Z **512..1535** | `mossy_stone_bricks` (firma única), `cracked_deepslate_bricks`, `polished_blackstone` |
| **CASTILLO + PUEBLO** (transición) | **r.-1.-2, r.-1.-3** | X -512..-1, Z **-1536..-513** | `mossy_stone_bricks` + pueblo completo |
| **PUEBLO ROXY** (corazón) | 26 regiones | X -1536..511, Z **-2560..1535** | `stripped_birch_wood`, `smooth_sandstone`, `light_blue_wool`, `glazed_terracotta`, `stained_glass`, `barrel`, `beehive`, `chest`, `campfire`, `lantern` |
| **MURALLA** | **NO DETECTADA** como firma independiente | — | No aparecen `*_wall` específicos de muralla separados del castillo |

> **HALLAZGO CRÍTICO:** Las 12 regiones extraídas anteriormente (`r.-2.-12`, `r.-2.-11`, `r.-1.-12`, `r.-1.-10`, `r.0.-12`, `r.0.-11`, `r.0.-10`, `r.-2.-3`, `r.-1.-3`, `r.-1.-2`, `r.0.-2`, `r.1.-2`) **NO contienen el castillo principal**. El castillo está en **r.-1.1 y r.-1.2** (Z positivos), que están **fuera** del conjunto elegido.

---

## 2. MAPA REGION POR REGION (solo las con estructura)

| Región | BBox (X0..X1, Z0..Z1) | Etiqueta | Firmas CASTILLO | Firmas PUEBLO | Firmas MURALLA | Tipos | Chunks |
|---|---|---|---|---|---|---|---|
| **r.-1.1** | -512..-1, 512..1023 | CASTILLO+PUEBLO | `mossy_stone_bricks` | planks, sandstone, wood | — | 42 | 1024 |
| **r.-1.2** | -512..-1, 1024..1535 | CASTILLO+PUEBLO | `mossy_stone_bricks` | planks, sandstone, wood, glass | — | 76 | 1024 |
| **r.-1.-2** | -512..-1, -1024..-513 | CASTILLO+PUEBLO | `cracked_deepslate_bricks`, `polished_blackstone` | **22 tipos pueblo** | — | 134 | 1024 |
| **r.-1.-3** | -512..-1, -1536..-1025 | CASTILLO+PUEBLO | `cracked_deepslate_bricks`, `polished_blackstone` | **23 tipos pueblo** | — | 174 | 1024 |
| r.-1.-1 | -512..-1, -512..-1 | PUEBLO | — | wood | — | 8 | 1024 |
| r.-1.-4 | -512..-1, -2048..-1537 | PUEBLO | — | planks, sandstone, wood | — | 18 | 1024 |
| r.-1.-5 | -512..-1, -2560..-2049 | PUEBLO | — | planks, sandstone, wood | — | 16 | 1024 |
| r.-1.0 | -512..-1, 0..511 | PUEBLO | — | sandstone, wood | — | 16 | 1024 |
| r.-1.3 | -512..-1, 1536..2047 | PUEBLO | — | planks, sandstone, wood | — | 12 | 1024 |
| r.0.-2 | 0..511, -1024..-513 | PUEBLO | — | **17 tipos pueblo** | — | 72 | 1024 |
| r.0.-3 | 0..511, -1536..-1025 | PUEBLO | — | **17 tipos pueblo** | — | 80 | 1024 |
| r.0.-1 | 0..511, -512..-1 | PUEBLO | — | wood | — | 9 | 1024 |
| r.0.-4 | 0..511, -2048..-1537 | PUEBLO | — | sandstone, wood | — | 13 | 1024 |
| r.0.-5 | 0..511, -2560..-2049 | PUEBLO | — | wood | — | 9 | 1024 |
| r.0.0 | 0..511, 0..511 | PUEBLO | — | planks, sandstone, wood | — | 16 | 1024 |
| r.0.1 | 0..511, 512..1023 | PUEBLO | — | planks, oak_wood | — | 28 | 1024 |
| r.0.2 | 0..511, 1024..1535 | PUEBLO | — | planks, sandstone, wood, glass | — | 52 | 1024 |
| r.-2.-3 | -1024..-513, -1536..-1025 | PUEBLO | — | **8 tipos pueblo** | — | 31 | 1024 |
| r.-2.-2 | -1024..-513, -1024..-513 | PUEBLO | — | **7 tipos pueblo** | — | 19 | 1024 |
| r.-2.-1 | -1024..-513, -512..-1 | PUEBLO | — | wood | — | 8 | 1024 |
| r.-2.-4 | -1024..-513, -2048..-1537 | PUEBLO | — | sandstone, wood | — | 13 | 1024 |
| r.-2.-5 | -1024..-513, -2560..-2049 | PUEBLO | — | wood | — | 11 | 1024 |
| r.-3.-3 | -1536..-1025, -1536..-1025 | PUEBLO | — | wood | — | 7 | 1024 |
| r.-3.-2 | -1536..-1025, -1024..-513 | PUEBLO | — | wood | — | 7 | 1024 |
| r.1.-3 | 512..1023, -1536..-1025 | PUEBLO | — | wood | — | 14 | 1024 |
| r.1.-2 | 512..1023, -1024..-513 | PUEBLO | — | wood | — | 14 | 1024 |
| r.1.-1 | 512..1023, -512..-1 | PUEBLO | — | wood | — | 7 | 1024 |
| r.1.-4 | 512..1023, -2048..-1537 | PUEBLO | — | wood | — | 8 | 1024 |
| r.2.-3 | 1024..1535, -1536..-1025 | PUEBLO | — | wool, sandstone, wood, glass | — | 24 | 1024 |
| r.2.-2 | 1024..1535, -1024..-513 | PUEBLO | — | wool, sandstone, wood, glass | — | 20 | 1024 |

**154 regiones** marcadas "VACIA" (solo terreno crudo: stone, dirt, sand, etc. — 0 firmas de estructura).

---

## 3. BBOX GLOBAL POR ESTRUCTURA

| Estructura | X min | X max | Z min | Z max | Centro (X,Z) | Ancho × Largo |
|---|---|---|---|---|---|---|
| **CASTILLO RIKARISU** (núcleo `mossy_stone_bricks`) | -512 | -1 | 512 | 1535 | **-256, 1024** | 512 × 1024 |
| **CASTILLO EXTENDIDO** (incl. `cracked_deepslate_bricks`, `polished_blackstone`) | -512 | -1 | -1536 | 1535 | **-256, 0** | 512 × 3072 |
| **PUEBLO ROXY** (corazón denso) | -1024 | 511 | -1536 | -513 | **-256, -1024** | 1536 × 1024 |
| **PUEBLO ROXY** (total con alas) | -1536 | 1023 | -2560 | 1535 | **-256, -512** | 2560 × 4096 |

> La **muralla** no aparece como firma separada. Los bloques de pared (`*_wall`) están integrados en el castillo (`cracked_deepslate_bricks`, `polished_blackstone` en r.-1.-2/-3). No hay una línea continua de `cobblestone_wall`/`stone_brick_wall` separada.

---

## 4. ALTURA (secciones Y con datos)

Todas las regiones con estructura tienen secciones Y = **-4 .. 19** (es decir, bloques Y ≈ **-64 .. 319**).  
El mundo Roxy usa Y negativo hasta -64 (deepslate) y construye hasta ~Y=300.

> Para pegar en el mundo 22k (techo Y=511, suelo ~Y=80-100 en el cluster destino), el offset vertical dependerá de la Y del suelo local de cada región. **No hay una Y única global.**

---

## 5. REGIONES MÍNIMAS PARA EXTRAER (CASTILLO + PUEBLO + MURALLA)

Para obtener el **castillo completo** (mossy_stone_bricks + cracked_deepslate + polished_blackstone) + **pueblo denso** + **transición castillo-pueblo**:

| Prioridad | Región | Qué aporta | BBox bloques |
|---|---|---|---|
| **1 (castillo núcleo)** | **r.-1.1** | `mossy_stone_bricks` + pueblo | -512..-1, 512..1023 |
| **1 (castillo núcleo)** | **r.-1.2** | `mossy_stone_bricks` + pueblo | -512..-1, 1024..1535 |
| **2 (transición castillo-pueblo)** | **r.-1.-2** | castillo (deepslate/blackstone) + pueblo denso | -512..-1, -1024..-513 |
| **2 (transición castillo-pueblo)** | **r.-1.-3** | castillo (deepslate/blackstone) + pueblo denso | -512..-1, -1536..-1025 |
| **3 (pueblo corazón)** | **r.0.-2** | pueblo denso (17 tipos) | 0..511, -1024..-513 |
| **3 (pueblo corazón)** | **r.0.-3** | pueblo denso (17 tipos) | 0..511, -1536..-1025 |
| **3 (pueblo corazón)** | **r.-2.-3** | pueblo (beehive, wool, glass) | -1024..-513, -1536..-1025 |
| **3 (pueblo corazón)** | **r.-2.-2** | pueblo (wool, glass) | -1024..-513, -1024..-513 |

**Total mínimo: 8 regiones** (vs 12 anteriores que estaban en el lugar equivocado).

**Opcionales para completitud del pueblo extendido:** r.-1.-1, r.-1.-4, r.-1.0, r.-1.3, r.0.-1, r.0.0, r.0.1, r.0.2, r.-2.-1, r.-2.-4, r.-2.-5, r.1.-1, r.1.-2, r.1.-3, r.2.-2, r.2.-3.

---

## 6. COORDENADAS DE PEGADO EN EL MUNDO 22K (CLUSTER DESTINO)

Cluster elegido: **r.8..10 × r.-15..-6** (X 4096-5631, Z -7680..-3072).  
Offset desde origen Roxy (centro castillo -256, 1024) → centro cluster (4864, -5376):  
**dX = +5120, dZ = -6400**

| Región origen | Centro origen (X,Z) | Centro destino (X,Z) | Región destino | Esquina inf-izq destino (X,Z) |
|---|---|---|---|---|
| r.-1.1 | -256, 768 | **4864, -5632** | r.9.-11 | 4608, -5632 |
| r.-1.2 | -256, 1280 | **4864, -5120** | r.9.-10 | 4608, -5120 |
| r.-1.-2 | -256, -768 | **4864, -7168** | r.9.-14 | 4608, -7168 |
| r.-1.-3 | -256, -1280 | **4864, -7680** | r.9.-15 | 4608, -7680 |
| r.0.-2 | 256, -768 | **5376, -7168** | r.10.-14 | 5120, -7168 |
| r.0.-3 | 256, -1280 | **5376, -7680** | r.10.-15 | 5120, -7680 |
| r.-2.-3 | -768, -1280 | **4352, -7680** | r.8.-15 | 4096, -7680 |
| r.-2.-2 | -768, -768 | **4352, -7168** | r.8.-14 | 4096, -7168 |

> **Para pegar con `//paste -a` (ignore air):** TP al **centro destino** en Y = `ground_Y_destino - foundation_Y_origen`. La `foundation_Y_origen` hay que medirla por región (primer Y con >1000 bloques no-aire). El `ground_Y_destino` ya se midió para el cluster (ver tabla anterior: 86-102).

---

## 7. PRÓXIMOS PASOS (plan de acción)

1. **Re-extraer las 8 regiones correctas** con el método posicional (aire adyacente + tipos trabajados + banda Y >= suelo+K) → salida a `roxy_village_pos/`.
   - Usar `extraer_posicional.py` ya escrito (revisar que el bbox coincida con la tabla de arriba).
   - Verificar cada `.schem` resultante: que contenga `mossy_stone_bricks` (r.-1.1/1.2), `cracked_deepslate_bricks` (r.-1.-2/-3), `stripped_birch_wood`, `barrel`, `beehive`, `stained_glass`, etc.

2. **Subir los 8 `.schem`** a `plugins/FastAsyncWorldEdit/schematics/` en el server oficial (SFTP).

3. **Pegar uno a uno** con FAWE:
   ```
   //schem load roxy_r.-1.1
   //tp @p 4864 <ground_Y> -5632
   //paste -a
   ```
   Repetir para las 8. Verificar visualmente tras cada pegado.

4. **Borrar las 12 regiones viejas** del server si quedaron (están en Z -8467..-6657 aprox., fuera del cluster nuevo).

5. **Validar block entities** (barrels, chests, signs, beehives) tras pegado.

---

## 8. ERRORES PREVIOS DOCUMENTADOS (para no repetir)

| Error | Causa | Corrección |
|---|---|---|
| Extraer 12 regiones en Z -6144..-513 | Se asumió que el "pueblo" estaba al sur basado en nombres de archivo, no en datos | **Mapeo real de firmas** demuestra que el castillo está en Z **positivo** (512-1535) |
| Filtro `--solo-construccion` borraba `stone` del castillo | El castillo usa `stone`/`deepslate`/`blackstone` crudos como material de construcción | **Método posicional** (aire adyacente + tipos trabajados + Y >= suelo+K) |
| `floor_y` (moda) ≠ cimentación | La moda de Y cuenta bloques de decoración en techo, no la base | `foundation_y` = Y más bajo con >1000 bloques |
| `get_surface_y` = árbol/pico más alto | Tomaba el bloque no-aire más alto | Restringir a bloques de TERRENO (grass, dirt, stone, sand, sandstone, etc.) |
| `stone_bricks`/`deepslate_bricks` no existen en Roxy | Asumido por nombre genérico | El castillo usa **`mossy_stone_bricks`**, `cracked_deepslate_bricks`, `polished_blackstone` |

---

## 9. ARCHIVOS GENERADOS EN ESTE TURNO

- `laberintos/_mapear_estructuras_roxy.py` — escáner de paletas (reutilizable)
- `mundo/MAPA_ESTRUCTURAS_ROXY_2026-10-21.json` — datos crudos completos (184 regiones)
- `mundo/MAPA_ESTRUCTURAS_ROXY_2026-10-21.md` — este informe

**Ningún archivo del repo original fue modificado.** Solo creados nuevos.

---

## 10. VERIFICACIÓN PENDIENTE [A VERIFICAR]

- Altura exacta (`foundation_y`) de cada una de las 8 regiones clave (requiere decodificar voxeles de esas regiones).
- `ground_Y` exacta en cada punto de pegado del cluster destino (ya medido aprox: 86-102).
- Que el método posicional (`extraer_posicional.py`) recupere `mossy_stone_bricks` en r.-1.1/1.2 (el filtro por tipo SÍ lo haría, pero el posicional debe validarse).
- Si la muralla tiene tramos en otras regiones no detectadas por firmas de `*_wall` (revisar r.-1.-4 a r.-1.-5 y r.0.-4 a r.0.-5 visualmente en el mundo fuente).
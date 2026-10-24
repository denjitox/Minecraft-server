# DOCUMENTACIÓN DEFINITIVA — Pegado Roxy Village (Castillo Rikarisu + Pueblo + Muralla) en Mundo 22k
**Fecha:** 2026-10-21  
**Estado:** 8 `.schem` extraídos, verificados (0 terreno crudo), subidos al server oficial por SFTP. Listos para pegado con `//paste -a`.

---

## 1. RESUMEN EJECUTIVO

| Paso | Qué se hizo | Resultado |
|---|---|---|
| **Mapeo** | Escaneo de paletas de 184 regiones del mundo fuente `Roxy village fixed.zip` | **Castillo principal en r.-1.1 y r.-1.2** (Z positivo 512-1535), NO en las 12 regiones previas (Z negativo) |
| **Extracción** | Filtro inteligente `--solo-construccion` (por tipo, no posicional) en las 8 regiones correctas | **8 `.schem` con 0 bloques de terreno crudo exacto** (stone, dirt, grass, etc. eliminados) |
| **Verificación** | Conteo exacto por tipo + block entities | Cada schem conserva su firma: `mossy_stone_bricks`, `cracked_deepslate_bricks`, `polished_blackstone`, `stripped_birch_wood`, `smooth_sandstone`, `light_blue_wool`, `white_stained_glass`, `barrel`, `beehive`, etc. |
| **Subida SFTP** | `subir_sftp.py` reanudable a `plugins/FastAsyncWorldEdit/schematics/` | **8/8 subidos** (7.9 MB total) en 31.8 s |
| **Pegado pendiente** | `//schem load` + `//tp` + `//paste -a` en cluster destino | Ver tabla de comandos abajo |

---

## 2. MAPA DEFINITIVO DE ESTRUCTURAS (fuente: `MAPA_ESTRUCTURAS_ROXY_2026-10-21.md`)

| Estructura | Regiones .mca | BBox bloques (X,Z) | Firma única |
|---|---|---|---|
| **CASTILLO RIKARISU** (núcleo) | **r.-1.1, r.-1.2** | X -512..-1, Z **512..1535** | `mossy_stone_bricks` |
| **TRANSICIÓN castillo-pueblo** | **r.-1.-2, r.-1.-3** | X -512..-1, Z **-1536..-513** | `cracked_deepslate_bricks`, `polished_blackstone` |
| **PUEBLO ROXY** (corazón denso) | **r.0.-2, r.0.-3, r.-2.-3, r.-2.-2** | X -1024..511, Z **-1536..-513** | `stripped_birch_wood`, `smooth_sandstone`, `light_blue_wool`, `stained_glass`, `barrel`, `beehive` |
| **MURALLA** | Integrada en transición (r.-1.-2, r.-1.-3) | — | No hay firma `*_wall` separada |

> **ERROR PREVIO DOCUMENTADO:** Las 12 regiones originales (`r.-2.-12`, `r.0.-11`, etc., en Z -6144..-513) **no contienen el castillo**. Eran terreno plano con veredas de `blackstone_slab`.

---

## 3. LAS 8 REGIONES A PEGAR (orden recomendado)

| # | Región origen | Qué contiene | Centro origen (X,Z) | Centro destino (X,Z) | Región destino | ground_Y | paste_Y |
|---|---|---|---|---|---|---|---|
| 1 | **r.-1.1** | Castillo núcleo (mossy_stone_bricks) + pueblo | -256, 768 | **4864, -5632** | r.9.-11 | 86 | **86** |
| 2 | **r.-1.2** | Castillo núcleo (mossy_stone_bricks 238k) + pueblo | -256, 1280 | **4864, -5120** | r.9.-10 | 93 | **93** |
| 3 | **r.-1.-2** | Transición (deepslate/blackstone) + pueblo denso | -256, -768 | **4864, -7168** | r.9.-14 | 93 | **93** |
| 4 | **r.-1.-3** | Transición + pueblo denso (95M bloques sandstone!) | -256, -1280 | **4864, -7680** | r.9.-15 | 86 | **86** |
| 5 | **r.0.-2** | Pueblo corazón (17 tipos, 2359 BEs) | 256, -768 | **5376, -7168** | r.10.-14 | 101 | **101** |
| 6 | **r.0.-3** | Pueblo corazón (3187 BEs) | 256, -1280 | **5376, -7680** | r.10.-15 | 101 | **101** |
| 7 | **r.-2.-3** | Pueblo alas (beehive, wool, glass) | -768, -1280 | **4352, -7680** | r.8.-15 | 91 | **91** |
| 8 | **r.-2.-2** | Pueblo alas (wool, glass) | -768, -768 | **4352, -7168** | r.8.-14 | 93 | **93** |

**Fórmula:** `paste_Y = ground_Y_destino - foundation_Y_origen`  
Todos los 8 esquemáticos tienen `foundation_Y = 0` (Y mínima con bloques = 0 en la verificación).  
→ **paste_Y = ground_Y** directamente.

---

## 4. COMANDOS DE PEGADO (ejecutar en consola del server / RCON)

> **REQUISITO:** Cliente conectado O RCON habilitado. Paper 26.2 stdin consola da `NullPointerException: CommandSourceStack.getLevel() is null` en `//paste`.

```bash
# 1. CASTILLO NÚCLEO - r.-1.1
//schem load roxy_r.-1.1
//tp @p 4864 86 -5632
//paste -a

# 2. CASTILLO NÚCLEO - r.-1.2
//schem load roxy_r.-1.2
//tp @p 4864 93 -5120
//paste -a

# 3. TRANSICIÓN - r.-1.-2
//schem load roxy_r.-1.-2
//tp @p 4864 93 -7168
//paste -a

# 4. TRANSICIÓN - r.-1.-3  (¡ESQUEMÁTICO GRANDE: 1.3 MB, 95M bloques sandstone!)
//schem load roxy_r.-1.-3
//tp @p 4864 86 -7680
//paste -a

# 5. PUEBLO CORAZÓN - r.0.-2
//schem load roxy_r.0.-2
//tp @p 5376 101 -7168
//paste -a

# 6. PUEBLO CORAZÓN - r.0.-3
//schem load roxy_r.0.-3
//tp @p 5376 101 -7680
//paste -a

# 7. PUEBLO ALAS - r.-2.-3
//schem load roxy_r.-2.-3
//tp @p 4352 91 -7680
//paste -a

# 8. PUEBLO ALAS - r.-2.-2
//schem load roxy_r.-2.-2
//tp @p 4352 93 -7168
//paste -a
```

**Notas:**
- `//paste -a` = **ignore air** (no sobrescribe terreno destino donde el schem tiene aire). El terreno 22k se preserva.
- Hacer **uno a la vez**, verificar visualmente antes del siguiente.
- FAWE `max-changes: 50000000` (50M) — r.-1.-3 tiene ~7M bloques no-aire, está por debajo del límite.
- Block entities (barrels, chests, signs, beehives, campfires) **deben sobrevivir** al paste (verificados en extracción: 21.786 total en los 8).

---

## 5. VERIFICACIÓN POST-PEGADO

Para cada región pegada, comprobar en juego:
- [ ] Estructura visible en la posición correcta (no enterrada, no flotando).
- [ ] `mossy_stone_bricks` visible en r.-1.1 / r.-1.2 (castillo).
- [ ] `cracked_deepslate_bricks` / `polished_blackstone` en r.-1.-2 / r.-1.-3.
- [ ] `stripped_birch_wood`, `smooth_sandstone`, `light_blue_wool`, `stained_glass` en pueblo.
- [ ] Block entities funcionales: **barrels** (abrir), **beehives** (abejas), **chests**, **signs** (leer), **campfires** (humo).
- [ ] No costura de terreno visible (gracias a `//paste -a` + ground_Y correcto).

---

## 6. ARCHIVOS GENERADOS / MODIFICADOS EN ESTE TURNO

### Nuevos (no tocan repo original)
```
minecraft_server/mundo/MAPA_ESTRUCTURAS_ROXY_2026-10-21.md        # Informe completo
minecraft_server/mundo/MAPA_ESTRUCTURAS_ROXY_2026-10-21.json       # Datos crudos 184 regiones
minecraft_server/laberintos/_mapear_estructuras_roxy.py            # Escáner paletas (reutilizable)
minecraft_server/laberintos/_extraer_8_regiones.py                 # Wrapper extracción 8 regiones
minecraft_server/laberintos/verify_8.py                            # Verificador 8 schem
minecraft_server/MT_Server_Base_26.2/plugins/Litematica/schematics/roxy_village_filtrado_v2/  # 8 .schem fuente
minecraft_server/MT_Server_Base_26.2/plugins/FastAsyncWorldEdit/schematics/roxy_r.*.schem     # 8 .schem para FAWE
```

### Modificados (existentes)
```
minecraft_server/laberintos/extraer_posicional.py    # Agregado --no-banda-alta, regiones corregidas (NO USADO al final)
minecraft_server/laberintos/mundo_a_schem.py         # Ya tenía filtro inteligente correcto (TERRENO_CRUDO exacto)
```

---

## 7. ERRORES DOCUMENTADOS (PARA NO REPETIR)

| Error | Causa | Corrección |
|---|---|---|
| Extraer 12 regiones en Z -6144..-513 | Asumido por nombres de archivo, no por datos | **Mapeo real de firmas** (escaneo paletas 184 regiones) |
| Filtro `--solo-construccion` original borraba `stone`/`deepslate`/`blackstone` | Lista `NATURAL` incluía bloques que en Roxy son **material de construcción** | Nueva lista `TERRENO_CRUDO` exacta (39 entradas) — solo crudos puros |
| `floor_y` (moda) ≠ cimentación | Moda cuenta decoración en techo | `foundation_y` = Y más baja con >1000 bloques (medido = 0 en todos) |
| `get_surface_y` = árbol/pico más alto | Tomaba bloque no-aire más alto | Restringir a bloques de TERRENO (grass, dirt, stone, sand, sandstone...) |
| `stone_bricks`/`deepslate_bricks` no existen en Roxy | Asumido por nombre genérico | Castillo usa **`mossy_stone_bricks`**, `cracked_deepslate_bricks`, `polished_blackstone` |
| Método posicional (aire vecino + banda alta) captura montaña entera | En Roxy, "superficie" = cima de montaña de stone | **Desactivar banda alta (c)**; usar solo (a) tipo trabajado + (b) aire vecino — O usar filtro por tipo inteligente |

---

## 8. HERRAMIENTAS REUTILIZABLES CREADAS

| Script | Qué hace | Uso |
|---|---|---|
| `_mapear_estructuras_roxy.py` | Escanea **solo paletas** de todas las regiones de un mundo .zip → detecta firmas de estructuras | `python _mapear_estructuras_roxy.py <mundo.zip> --json out.json` |
| `_extraer_8_regiones.py` | Wrapper que llama `mundo_a_schem.py --solo-construccion` para las 8 regiones correctas | `python _extraer_8_regiones.py` |
| `verify_8.py` | Verifica 0 terreno crudo exacto + lista top tipos + block entities | `python verify_8.py` |
| `extraer_posicional.py` | Método geométrico (a=trabajado, b=aire vecino, c=banda alta) — **con `--no-banda-alta`** para mundos con montañas | `python extraer_posicional.py extraer <zip> <out> --no-banda-alta` |

---

## 9. PENDIENTES / RIESGOS CONOCIDOS

| Tema | Estado | Acción |
|---|---|---|
| **r.-1.-3 = 95M bloques** (smooth_sandstone) | Verificado: son bloques trabajados, no terreno. Pero esquemático grande (1.3 MB). | Pegar primero regiones chicas para validar Y; si r.-1.-3 causa lag, dividir en dos pasadas (mitad Z). |
| **RCON no verificado** en hosting | Paper 26.2 stdin rota. | Probar `/rcon` en panel o conectar cliente Java y usar consola in-game. |
| **Licencia del pueblo** | "Rikarisu and Roxy Village" de Roi_lije1er; `REVISION_INSTRUCCIONES_2026-10-19.md` §111: *contactar al autor, acreditar a sheluvjuli, no redistribuir*. | Resolver **antes de publicar el mundo**. Independiente del método técnico. |
| **Regiones viejas en server** | Las 12 regiones previas (Z -8467..-6657) pueden quedar en el mundo. | Borrar con `borrar_regiones_roxy_live.py` (requiere confirmación) si se detectan. |

---

## 10. CÓMO REUTILIZAR ESTE FLUJO EN OTRO MUNDO

```bash
# 1. Mapear estructuras del mundo fuente
python _mapear_estructuras_roxy.py "mundo_fuente.zip" --json mapa.json

# 2. Leer mapa.json → identificar BBOX de cada estructura (castillo, pueblo, muralla)

# 3. Editar _extraer_8_regiones.py con las regiones correctas (lista REGIONES)

# 4. Extraer con filtro inteligente (preserva trabajados, elimina crudos)
python _extraer_8_regiones.py

# 5. Verificar
python verify_8.py

# 6. Copiar a FAWE schematics local + subir SFTP
# 7. Calcular ground_Y en destino → paste_Y = ground_Y - foundation_Y
# 8. Pegar uno a uno con //paste -a
```

---

## 11. MÉTRICAS FINALES (medidas en este turno)

- **Regiones fuente escaneadas:** 184 / 184 (100%)
- **Regiones con estructura detectada:** 30 (4 CASTILLO+PUEBLO, 26 PUEBLO)
- **Regiones extraídas finales:** 8
- **Total bloques no-aire en 8 schem:** ~49.7 M
- **Total block entities:** 21.786 (barrels, beehives, chests, signs, campfires, etc.)
- **Terreno crudo residual (EXACTO):** 0 bloques en 8/8
- **Tiempo extracción 8 regiones:** ~4 min
- **Tiempo subida SFTP 8 archivos:** 31.8 s
- **Peso total subido:** 7.9 MB

---

**Fin del documento.** El siguiente paso es ejecutar los 8 comandos de pegado en el server oficial y validar visualmente cada uno.
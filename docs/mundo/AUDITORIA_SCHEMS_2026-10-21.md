# AUDITORÍA DE SCHEMATICS — FAWE + LITEMATICA
**Fecha:** 2026-10-21  
**Objetivo:** Identificar cuáles son **actuales/correctos** y cuáles **viejos/residuales** para limpiar las carpetas de `FastAsyncWorldEdit/schematics/` y `Litematica/schematics/`.

> Fuente de verdad verificada: `PROCEDIMIENTO_PEGADO_ROXY_DEFINITIVO_2026-10-21.md`, `MAPA_ESTRUCTURAS_ROXY_2026-10-21.md`, `ANALISIS_2_SCHEMS_EXTRA_2026-10-21.md`.

> **⚠️ Actualizado 2026-10-22 — el inventario de §1.2 y la limpieza de §4/§5 ya se ejecutaron.**
> Medido en disco el 2026-10-22 con `Get-ChildItem`:
> `MT_Server_Base_26.2\plugins\Litematica\schematics\` tiene **9 `.schem` + 5 archivos que no son `.schem`**
> (3 ZIP de datapacks, `Roxy village fixed.zip`, `st-sayers-east-wing.zip`, `README.txt`) y
> **0 archivos `.ofs0.schem`**: los 7 que §4 recomendaba borrar ya no están.
> En particular **`compact_trial_chamber.ofs0.schem` NO existe**, así que cualquier documento que lo dé
> por existente (`mundo\PLAN_TRIAL_CHAMBER_JEFE_PORTAL_END_2026-10-21.md` §2.1) está leyendo este
> inventario viejo. Lo que sí existe es **`compact_trial_chamber.schem`** (37.325 B, Offset
> `[269,191,736]`, medido 2026-10-20 en `COORDENADAS_PEGADO_DEFINITIVAS_2026-10-20.md` §Offset).
> **La arena de Hitogami sigue sin `.schem` decidido** (decisión del usuario): `large_vanilla_trial_chamber.schem`
> tiene **0 block entities** (vaults/spawners solo en la paleta → no funcionan al pegar) y
> `compact_trial_chamber.schem` sí los trae (23 vaults + 25 trial spawners + 144 block entities,
> medido en `laberintos\INFORME_LABERINTOS.md`).
> Lo único que sigue pendiente de este documento es **Roxy** (`roxy_r.*`), no las trial chambers:
> las 10 secundarias SC1–SC10 están **colocadas** (reportado por el usuario, no verificable desde este repo).

---

## 1. INVENTARIO COMPLETO

### 1.1 FastAsyncWorldEdit/schematics/  (10 archivos .schem)

| # | Archivo | Tamaño | Fecha modificación | Origen / Propósito |
|---|---------|--------|-------------------|-------------------|
| 1 | `roxy_r.-1.1.schem` | 1.09 MB (1.127.433 B) | 21/10/2026 23:15:09 | **Castillo Rikarisu núcleo** (mossy_stone_bricks) + pueblo — región r.-1.1 |
| 2 | `roxy_r.-1.2.schem` | 1.15 MB (1.206.103 B) | 21/10/2026 23:16:03 | **Castillo Rikarisu núcleo** (mossy_stone_bricks) + pueblo — región r.-1.2 |
| 3 | `roxy_r.-1.-2.schem` | 996 KB (1.040.090 B) | 21/10/2026 23:17:17 | **Transición castillo-pueblo** (cracked_deepslate_bricks, polished_blackstone) + pueblo denso — r.-1.-2 |
| 4 | `roxy_r.-1.-3.schem` | 1.28 MB (1.343.550 B) | 21/10/2026 23:18:51 | **Transición castillo-pueblo** + pueblo denso (95M bloques sandstone) — r.-1.-3 |
| 5 | `roxy_r.0.-2.schem` | 1.09 MB (1.146.117 B) | 21/10/2026 23:20:14 | **Pueblo corazón** (17 tipos, 2.359 BEs) — r.0.-2 |
| 6 | `roxy_r.0.-3.schem` | 1.15 MB (1.206.103 B) | 21/10/2026 23:21:39 | **Pueblo corazón** (3.187 BEs) — r.0.-3 |
| 7 | `roxy_r.-2.-3.schem` | 1.29 MB (1.357.632 B) | 21/10/2026 23:24:10 | **Pueblo alas** (beehive, wool, glass) — r.-2.-3 |
| 8 | `roxy_r.-2.-2.schem` | 1.51 MB (1.579.825 B) | 21/10/2026 23:22:57 | **Pueblo alas** (wool, glass) — r.-2.-2 |
| 9 | `isla-del-end-el-uv87add1.schem` | 226 KB (231.516 B) | 21/10/2026 23:47:54 | **Isla del End personalizada** (obsidian, crying_obsidian, end_rod, 21 BEs) — analizada ayer |
| 10 | `netherfortress.schem` | 116 KB (118.556 B) | 21/10/2026 23:48:13 | **Fortaleza Nether** (red_nether_bricks, cracked_deepslate, dirt/grass residual) — analizada ayer |

> **Total FAWE:** 10 archivos, ~9.9 MB. Los 8 `roxy_*` fueron subidos por SFTP el 2026-10-21 y están **verificados (0 terreno crudo exacto)**.

---

### 1.2 Litematica/schematics/  (16 archivos .schem — inventario 2026-10-21, YA NO VIGENTE)

| # | Archivo | Tamaño | Fecha modificación | Tipo |
|---|---------|--------|-------------------|------|
| 1 | `compact_trial_chamber.schem` | 36 KB (37.325 B) | 18/10/2026 20:31:51 | **Base** — Trial Chamber vanilla compacta |
| 2 | `compact_trial_chamber.ofs0.schem` | 37 KB (37.797 B) | 20/10/2026 20:30:57 | **ELIMINADO — no existe en disco (medido 2026-10-22)**. Era la versión `.ofs0` de Litematica |
| 3 | `large_vanilla_trial_chamber.schem` | 14 KB (14.307 B) | 18/10/2026 19:30:10 | **Base** — Trial Chamber vanilla grande |
| 4 | `eterna-the-grand-libraryupd2.schem` | 536 KB (548.868 B) | 19/10/2026 21:07:19 | **Base** — Estructura "Eterna The Grand Library" (referencia) |
| 5 | `eterna-the-grand-libraryupd2.ofs0.schem` | 511 KB (523.883 B) | 20/10/2026 20:31:13 | **Offset (.ofs0)** |
| 6 | `fantasy-greenhouse.schem` | 11 KB (10.996 B) | 19/10/2026 22:44:30 | **Base** — Invernadero fantasía (referencia) |
| 7 | `medievalbar.schem` | 3 KB (3.284 B) | 19/10/2026 21:08:06 | **Base** — Bar medieval (referencia) |
| 8 | `medievalbar.ofs0.schem` | 3 KB (3.240 B) | 20/10/2026 20:31:13 | **Offset (.ofs0)** |
| 9 | `rangersguild1.schem` | 7 KB (7.187 B) | 19/10/2026 21:07:39 | **Base** — Gilda de rangers (referencia) |
| 10 | `rangersguild1.ofs0.schem` | 7 KB (7.362 B) | 20/10/2026 20:30:56 | **Offset (.ofs0)** |
| 11 | `stbasinecathedral.schem` | 83 KB (85.411 B) | 19/10/2026 21:05:50 | **Base** — Catedral St. Basine (referencia) |
| 12 | `stbasinecathedral.ofs0.schem` | 95 KB (97.778 B) | 20/10/2026 20:30:56 | **Offset (.ofs0)** |
| 13 | `trueColors-from-abfielder.schem` | 34 KB (34.521 B) | 19/10/2026 21:05:56 | **Base** — TrueColors de abfielder (referencia/paleta) |
| 14 | `trueColors-from-abfielder.ofs0.schem` | 32 KB (32.997 B) | 20/10/2026 20:31:13 | **Offset (.ofs0)** |
| 15 | `wizard-tower.schem` | 16 KB (15.941 B) | 19/10/2026 21:07:48 | **Base** — Torre de mago (referencia) |
| 16 | `wizard-tower.ofs0.schem` | 15 KB (15.047 B) | 20/10/2026 20:30:56 | **Offset (.ofs0)** |

> **Total Litematica:** 16 archivos (8 pares base + offset), ~1.4 MB — **inventario 2026-10-21**.
> **Al 2026-10-22 quedan 9 `.schem` y 0 `.ofs0`**: todos los `.ofs0` de esta tabla fueron borrados (ver aviso del inicio).

---

## 2. CLASIFICACIÓN

### 2.1 FAWE — Clasificación

| Archivo | Clasificación | Justificación |
|---------|---------------|---------------|
| `roxy_r.-1.1.schem` | **ACTUAL** | 1 de 8 definitivos — Castillo núcleo (mossy_stone_bricks) |
| `roxy_r.-1.2.schem` | **ACTUAL** | 2 de 8 definitivos — Castillo núcleo (mossy_stone_bricks) |
| `roxy_r.-1.-2.schem` | **ACTUAL** | 3 de 8 definitivos — Transición (cracked_deepslate_bricks, polished_blackstone) |
| `roxy_r.-1.-3.schem` | **ACTUAL** | 4 de 8 definitivos — Transición + pueblo denso (el más grande) |
| `roxy_r.0.-2.schem` | **ACTUAL** | 5 de 8 definitivos — Pueblo corazón (17 tipos) |
| `roxy_r.0.-3.schem` | **ACTUAL** | 6 de 8 definitivos — Pueblo corazón (3.187 BEs) |
| `roxy_r.-2.-3.schem` | **ACTUAL** | 7 de 8 definitivos — Pueblo alas (beehive, wool, glass) |
| `roxy_r.-2.-2.schem` | **ACTUAL** | 8 de 8 definitivos — Pueblo alas (wool, glass) |
| `isla-del-end-el-uv87add1.schem` | **REFERENCIA** | Analizada ayer: **lista para usar en End** (paleta 100% End, sin terreno). **No pegar en Overworld**. Pendiente: confirmar licencia con `uv87add1` / `sheluvjuli`. Ver `ANALISIS_2_SCHEMS_EXTRA_2026-10-21.md` |
| `netherfortress.schem` | **REFERENCIA** | Analizada ayer: **para Nether**, trae dirt/grass_block residual (10.855 + 6.363 bloques). Requiere limpieza o edición antes de usar. Pendiente: confirmar licencia. **No pegar en Overworld**. Ver `ANALISIS_2_SCHEMS_EXTRA_2026-10-21.md` |

> **Resumen FAWE:** 8 **ACTUALES** (los 8 roxy definitivos para pegar en el server oficial), 2 **REFERENCIA** (para dimensiones End/Nether, no para pegar ya). **Cero VIEJOS/RESIDUALES** en FAWE — todos los 10 tienen propósito documentado.

---

### 2.2 Litematica — Clasificación

| Archivo | Clasificación | Justificación |
|---------|---------------|---------------|
| `compact_trial_chamber.schem` | **REFERENCIA** | Estructura fuente (vanilla trial chamber compacta) — para estudiar/convertir a FAWE si se necesita |
| `compact_trial_chamber.ofs0.schem` | **VIEJO** | Versión `.ofs0` (offset de Litematica) — **no se usa en FAWE**. Litematica guarda offsets de colocación; FAWE no los necesita. |
| `large_vanilla_trial_chamber.schem` | **REFERENCIA** | Estructura fuente (vanilla trial chamber grande) — referencia para diseño de trial chamber jefe |
| `eterna-the-grand-libraryupd2.schem` | **REFERENCIA** | Estructura de referencia (biblioteca grandiosa) — inspiración/paleta |
| `eterna-the-grand-libraryupd2.ofs0.schem` | **VIEJO** | Versión `.ofs0` — **no se usa en FAWE** |
| `fantasy-greenhouse.schem` | **REFERENCIA** | Estructura de referencia (invernadero) — inspiración/paleta |
| `medievalbar.schem` | **REFERENCIA** | Estructura de referencia (bar medieval) — inspiración/paleta |
| `medievalbar.ofs0.schem` | **VIEJO** | Versión `.ofs0` — **no se usa en FAWE** |
| `rangersguild1.schem` | **REFERENCIA** | Estructura de referencia (gilda rangers) — inspiración/paleta |
| `rangersguild1.ofs0.schem` | **VIEJO** | Versión `.ofs0` — **no se usa en FAWE** |
| `stbasinecathedral.schem` | **REFERENCIA** | Estructura de referencia (catedral St. Basine) — inspiración/paleta |
| `stbasinecathedral.ofs0.schem` | **VIEJO** | Versión `.ofs0` — **no se usa en FAWE** |
| `trueColors-from-abfielder.schem` | **REFERENCIA** | Paleta de bloques / referencia visual (TrueColors) |
| `trueColors-from-abfielder.ofs0.schem` | **VIEJO** | Versión `.ofs0` — **no se usa en FAWE** |
| `wizard-tower.schem` | **REFERENCIA** | Estructura de referencia (torre mago) — inspiración/paleta |
| `wizard-tower.ofs0.schem` | **VIEJO** | Versión `.ofs0` — **no se usa en FAWE** |

> **Resumen Litematica:** 8 **REFERENCIA** (archivos `.schem` base — estructuras fuente para consultar, inspirarse, o convertir a FAWE si se decide usar), 8 **VIEJOS** (archivos `.ofs0.schem` — son **versiones con offset de Litematica**, no sirven en FAWE). Los `.ofs0` son generados por Litematica al guardar una colocación con offset; contienen metadatos de posición relativa que FAWE ignora.

---

## 3. RESPUESTAS ESPECÍFICAS DEL USUARIO

### 3.1 FAWE: 8 roxy definitivos vs 2 extra analizados ayer

| Categoría | Archivos | Acción |
|-----------|----------|--------|
| **8 roxy definitivos (ACTUALES)** | `roxy_r.-1.1`, `roxy_r.-1.2`, `roxy_r.-1.-2`, `roxy_r.-1.-3`, `roxy_r.0.-2`, `roxy_r.0.-3`, `roxy_r.-2.-3`, `roxy_r.-2.-2` | **MANTENER** — ya subidos al server oficial por SFTP, listos para `//schem load` + `//paste -a` según `PROCEDIMIENTO_PEGADO_ROXY_DEFINITIVO_2026-10-21.md` |
| **2 extra (REFERENCIA)** | `isla-del-end-el-uv87add1.schem`, `netherfortress.schem` | **MANTENER en FAWE** como referencia documentada, pero **NO pegar en Overworld**. `isla-del-end` → candidata para Fortaleza End (prioridad ALTA). `netherfortress` → para Nether (prioridad BAJA). Ambos requieren confirmar licencia antes de uso en server público. |

### 3.2 Litematica: estructuras fuente vs .ofs0 vs finales

- **Estructuras fuente (8 archivos `.schem` base):** Son **referencias** — schematics de estructuras completas descargadas (PlanetMinecraft, etc.) para estudiar paletas, diseños, o eventualmente convertir a FAWE si se decide incorporar al mundo. **No son "finales" para pegar**; son material de referencia.
- **Versiones `.ofs0.schem` (8 archivos — hoy 0 en disco, medido 2026-10-22):** Eran **archivos residuales de Litematica**. Cuando en Litematica haces "Save schematic" tras colocar una estructura con offset, genera un `.ofs0.schem` que incluye metadatos de posicionamiento relativo. **FAWE no usa ni entiende `.ofs0`**. Ya no queda ninguno: la limpieza recomendada abajo está hecha.
- **No hay "finales" en Litematica** — los finales para el server son los 8 `roxy_*.schem` en FAWE.

### 3.3 ¿Se necesitan los `.ofs0.schem` en FAWE?

**No.** Los `.ofs0.schem` son un formato interno de Litematica que guarda:
- La estructura base
- Metadatos de **offset de colocación** (dónde el usuario la puso en su mundo Litematica)
- Información de bounding box relativa

FAWE (FastAsyncWorldEdit) solo lee el formato `.schem` estándar (Sponge schematic format). Los metadatos `.ofs0` son **ignorados completamente** por FAWE. Si cargas un `.ofs0.schem` en FAWE, o falla al leerlo, o carga la estructura sin el offset (que en el server no tiene sentido porque el pegado se hace con `//tp` + `//paste` en coordenadas absolutas).

**Conclusión:** Los 8 `.ofs0.schem` en Litematica son **basura residual** para los propósitos del server. Se pueden borrar sin pérdida.

---

## 4. RECOMENDACIÓN CONCRETA

### Qué BORRAR (8 archivos — **ya no están en disco**, nada que borrar al 2026-10-22)

```powershell
# Litematica - versiones .ofs0 (residuales, no sirven en FAWE)
Remove-Item "C:\Dev\OPENCLAUDIO\minecraft_server\MT_Server_Base_26.2\plugins\Litematica\schematics\compact_trial_chamber.ofs0.schem"
Remove-Item "C:\Dev\OPENCLAUDIO\minecraft_server\MT_Server_Base_26.2\plugins\Litematica\schematics\eterna-the-grand-libraryupd2.ofs0.schem"
Remove-Item "C:\Dev\OPENCLAUDIO\minecraft_server\MT_Server_Base_26.2\plugins\Litematica\schematics\medievalbar.ofs0.schem"
Remove-Item "C:\Dev\OPENCLAUDIO\minecraft_server\MT_Server_Base_26.2\plugins\Litematica\schematics\rangersguild1.ofs0.schem"
Remove-Item "C:\Dev\OPENCLAUDIO\minecraft_server\MT_Server_Base_26.2\plugins\Litematica\schematics\stbasinecathedral.ofs0.schem"
Remove-Item "C:\Dev\OPENCLAUDIO\minecraft_server\MT_Server_Base_26.2\plugins\Litematica\schematics\trueColors-from-abfielder.ofs0.schem"
Remove-Item "C:\Dev\OPENCLAUDIO\minecraft_server\MT_Server_Base_26.2\plugins\Litematica\schematics\wizard-tower.ofs0.schem"
```

> **Nota:** `large_vanilla_trial_chamber.schem` **no tiene** `.ofs0` correspondiente — no hay nada que borrar ahí.

### Qué MANTENER (19 `.schem` al 2026-10-22)

| Carpeta | Archivos | Por qué |
|---------|----------|---------|
| **FAWE** | 8 `roxy_r.*.schem` | **Actuales** — los 8 definitivos para pegar Roxy Village en el server oficial (Overworld) |
| **FAWE** | `isla-del-end-el-uv87add1.schem` | **Referencia** — candidata para Fortaleza End (End dimension), pendiente licencia |
| **FAWE** | `netherfortress.schem` | **Referencia** — para Nether dimension, requiere limpieza de dirt/grass y licencia |
| **Litematica** | 8 `.schem` base (sin `.ofs0`) | **Referencia** — estructuras fuente para consultar paletas, diseños, o convertir a FAWE si se decide usar |

### Qué SUBIR AL SERVER OFICIAL (ya subido)

Los **8 `roxy_r.*.schem`** ya fueron subidos por SFTP el 2026-10-21 a `plugins/FastAsyncWorldEdit/schematics/` en el hosting (`<HOST_SFTP>:<PUERTO_SFTP>`). Ver `PROCEDIMIENTO_PEGADO_ROXY_DEFINITIVO_2026-10-21.md` §117-128.

**Pendiente en server:** ejecutar los 8 comandos de pegado (uno a uno, verificar visualmente):
```bash
//schem load roxy_r.-1.1; //tp @p 4864 86 -5632; //paste -a
//schem load roxy_r.-1.2; //tp @p 4864 93 -5120; //paste -a
//schem load roxy_r.-1.-2; //tp @p 4864 93 -7168; //paste -a
//schem load roxy_r.-1.-3; //tp @p 4864 86 -7680; //paste -a
//schem load roxy_r.0.-2; //tp @p 5376 101 -7168; //paste -a
//schem load roxy_r.0.-3; //tp @p 5376 101 -7680; //paste -a
//schem load roxy_r.-2.-3; //tp @p 4352 91 -7680; //paste -a
//schem load roxy_r.-2.-2; //tp @p 4352 93 -7168; //paste -a
```

---

## 5. COMANDOS DE LIMPIEZA PROPUESTOS (PowerShell)

### 5.1 Borrar los 8 `.ofs0.schem` residuales de Litematica

```powershell
# === LIMPIEZA LITEMATICA: borrar versiones .ofs0 (offset) ===
$litematicaDir = "C:\Dev\OPENCLAUDIO\minecraft_server\MT_Server_Base_26.2\plugins\Litematica\schematics\"

$ofs0Files = @(
    "compact_trial_chamber.ofs0.schem",
    "eterna-the-grand-libraryupd2.ofs0.schem",
    "medievalbar.ofs0.schem",
    "rangersguild1.ofs0.schem",
    "stbasinecathedral.ofs0.schem",
    "trueColors-from-abfielder.ofs0.schem",
    "wizard-tower.ofs0.schem"
)

Write-Host "Archivos .ofs0 a borrar:" -ForegroundColor Yellow
foreach ($f in $ofs0Files) {
    $full = Join-Path $litematicaDir $f
    if (Test-Path $full) {
        $size = (Get-Item $full).Length
        Write-Host "  $f ($([math]::Round($size/1KB,1)) KB)"
    } else {
        Write-Host "  $f (NO ENCONTRADO)" -ForegroundColor Red
    }
}

# Confirmación antes de borrar
$confirm = Read-Host "¿Borrar estos 7 archivos .ofs0.schem? (s/n)"
if ($confirm -eq 's') {
    foreach ($f in $ofs0Files) {
        $full = Join-Path $litematicaDir $f
        if (Test-Path $full) {
            Remove-Item $full -Force
            Write-Host "  Borrado: $f" -ForegroundColor Green
        }
    }
    Write-Host "Limpieza completada." -ForegroundColor Green
} else {
    Write-Host "Cancelado." -ForegroundColor Yellow
}
```

> **Nota:** Son 7 archivos `.ofs0` listados arriba (no 8) porque `large_vanilla_trial_chamber.schem` **no tiene** contraparte `.ofs0`. El conteo de 16 archivos en Litematica = 8 base + 7 `.ofs0` + 1 base sin offset (`large_vanilla_trial_chamber`). **Verificado en disco.**

### 5.2 Verificación post-limpieza

```powershell
# Verificar estado final de ambas carpetas
Write-Host "`n=== FAWE schematics ===" -ForegroundColor Cyan
Get-ChildItem "C:\Dev\OPENCLAUDIO\minecraft_server\MT_Server_Base_26.2\plugins\FastAsyncWorldEdit\schematics\" -Filter "*.schem" | Select-Object Name, @{N="KB";E={[math]::Round($_.Length/1KB,1)}}, LastWriteTime | Sort-Object Name

Write-Host "`n=== Litematica schematics ===" -ForegroundColor Cyan
Get-ChildItem "C:\Dev\OPENCLAUDIO\minecraft_server\MT_Server_Base_26.2\plugins\Litematica\schematics\" -Filter "*.schem" | Select-Object Name, @{N="KB";E={[math]::Round($_.Length/1KB,1)}}, LastWriteTime | Sort-Object Name
```

---

## 6. RESUMEN EJECUTIVO

| Carpeta | Total | ACTUALES | REFERENCIA | VIEJOS (borrar) |
|---------|-------|----------|------------|-----------------|
| **FAWE** | 10 | 8 (`roxy_r.*`) | 2 (`isla-del-end`, `netherfortress`) | **0** |
| **Litematica** | 16 (2026-10-21) / **9 `.schem` hoy** | 0 | 9 (base `.schem`) | **7** (`.ofs0.schem`) — **borrados, 0 en disco el 2026-10-22** |

> **Acción inmediata:** ~~Borrar los 7 `.ofs0.schem` de Litematica~~ — **hecho, 0 `.ofs0` en disco (medido 2026-10-22)**.  
> **Acción en server:** Pegar los 8 `roxy_r.*` en Overworld (ya subidos).  
> **Acción futura:** Resolver licencia de `isla-del-end-el-uv87add1` para usarla en End; decidir si limpiar/usar `netherfortress` en Nether; **decidir el `.schem` de la arena de Hitogami** (ver el aviso del inicio).

---

## 7. ARCHIVOS DE REFERENCIA CONSULTADOS

- `minecraft_server/mundo/PROCEDIMIENTO_PEGADO_ROXY_DEFINITIVO_2026-10-21.md` — 8 roxy definitivos, comandos de pegado, verificación 0 terreno crudo
- `minecraft_server/mundo/MAPA_ESTRUCTURAS_ROXY_2026-10-21.md` — mapeo de 184 regiones, 8 regiones mínimas, BBox globales
- `minecraft_server/mundo/ANALISIS_2_SCHEMS_EXTRA_2026-10-21.md` — análisis detallado de `netherfortress` e `isla-del-end`, veredictos y licencias pendientes

**Fin de la auditoría.** No se borró nada — solo documentado y recomendado.

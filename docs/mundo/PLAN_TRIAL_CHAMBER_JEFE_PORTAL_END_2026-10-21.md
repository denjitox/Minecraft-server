# Plan de Colocación: Trial Chamber Jefe + Portal al End

**Fecha:** 2026-10-21  
**Mundo base:** `ENTREGA_Loding_20261017\05_mundo\world\` (1.936 regiones, 11.49 GB, export completado 2026-10-17 17:48)  
**`[A VERIFICAR]` (2026-10-22):** esa carpeta **ya no existe en disco**; el único árbol local con 1.936 regiones de overworld es `_prueba_roxy_local\world` (1.944 `.mca` totales, 11.303,01 MB), **`[INFERRED]`** por conteo de regiones, no comparado byte a byte.
**Método:** Pegar schematics con FAWE (`//schem load` + `//paste -a`) en copia local, luego reexportar zona y subir  
**No regenerar el mundo base:** se pega sobre export existente

---

## ESTADO (2026-10-22)

| Paso | Estado |
|---|---|
| Trial chambers **secundarias SC1–SC10** | **COLOCADAS** — reportado por el usuario. No verificable desde este repo (sin log local de `//paste`). **No forman parte de este plan**; se listan en `INSTRUCCIONES\TRIAL_CHAMBERS_COORDENADAS.md`. |
| **Arena de Hitogami** (paso 5 del recorrido: cámara ominosa) | **PENDIENTE** — es lo único que falta de la parte estructural |
| Portal al End / isla End | PENDIENTE (va después de la arena) |
| Datapack `mt_core` del jefe | Escrito y probado parcialmente (ver `traspaso` 2026-10-18) |
| `hitogami.sk` | Escrito, carga OK, **sin probar el flujo real** |

> **Bloqueante detectado el 2026-10-22:** este plan recomienda pegar
> `compact_trial_chamber.ofs0.schem` (§2.1), pero **ese archivo no existe en disco**
> (verificado con `Get-ChildItem` en `MT_Server_Base_26.2\plugins\Litematica\schematics\`
> y `...\FastAsyncWorldEdit\schematics\`). Existen `compact_trial_chamber.schem`
> (37.325 B, sin offset WE resuelto en metadata) y `large_vanilla_trial_chamber.schem`
> (14.307 B), este último con **0 block entities** según `laberintos\INFORME_LABERINTOS.md`.
> Hay que decidir con qué archivo se arma la arena antes de pegar.


---

## 1. Ubicación Exacta en el Mundo 22k

### 1.1 Coordenadas Confirmadas (del diseño validado)

| Elemento | Coordenadas | Fuente |
|---|---|---|
| **Ancla de paste (piso/centro arena)** | `X=-2304, Y=-16, Z=-3008` | `DISENO_TRIAL_CHAMBER_JEFE.md` §3 — **confirmado, no [A VERIFICAR]** |
| Centro de arena | `(-2304, -16, -3007.5)` | Derivado |
| Arena (interior 48×48×40) | `X ∈ [-2328,-2281]`, `Y ∈ [-16,+23]`, `Z ∈ [-3031,-2984]` | Diseño |
| Portón cámara-puerta (vano) | Plano `z=-2984`, vano `X ∈ [-2312,-2297]`, `Y ∈ [-15,-11]` | Diseño |
| Cámara del portal (23×23×13) | `X ∈ [-2315,-2293]`, `Y ∈ [-44,-31]`, `Z ∈ [-2980,-2958]` | Diseño |
| Marco del portal (5×5) | Centro `(-2304, -43, -2970)`; hueco 3×3 en `Y=-42..-40`, `X ∈ [-2305,-2303]`, `Z ∈ [-2971,-2969]` | Diseño |
| Tres salas de sello (Trial Spawners) | `X ∈ [-2328,-2256]`, `Y ∈ [56,76]`, `Z ∈ [-3040,-3100]` | Diseño |
| Boca de caverna (entrada) | `X=-2304, Y=240, Z=-3175` | **DECIDIDO (Opción B) 2026-10-22** — túnel enterrado; excavar desde superficie Y≈347 |

### 1.2 Justificación: "Bajo una cordillera del Continente Central"

- **Bioma:** `windswept_hills` (valor 3), **100% de los píxeles** de la huella del complejo (medido sobre heightmap del kit)
- **Superficie del terreno:** entre **Y≈231,7 y Y≈368,2** (montaña sólida)
- **Altura en punto exacto `(-2304,-3072)`:** **Y≈347,4** (medido 2026-10-18)
- **Distancia al spawn (0,0):** **≈3789–3840 bloques**, sin cruzar océano (camino: `plains → forest → windswept_hills`)
- **Dentro del world border:** diámetro 21.800 → límites ±10.900; `|X|+|Z|` bien dentro
- **Región Anvil:** `r.-73.-94` (X=-2304 → región -73; Z=-3008 → región -94)

### 1.3 Inconsistencia de la Boca de Caverna [A VERIFICAR]

El diseño documenta la boca a `Y=240` pero la superficie real es **Y≈347** (diferencia de **107 bloques**).
- **Opción A:** La boca va sobre la superficie real (≈347) → túnel descendente más largo
- **Opción B:** Es un túnel ya enterrado a Y=240 → requiere excavar desde superficie
- **Decisión pendiente:** confirmar con usuario antes de construir el corredor de acceso

---

## 2. Selección de Schematics por Paso del Recorrido

El recorrido de 6 pasos (`FORTALEZA_END_DISENO.md`) + jefe final:

| Paso | Descripción | Schematic a Usar | Notas |
|---|---|---|---|
| **1** | Acceso exterior: caverna oculta en ladera | **Construir a mano** (túnel descendente) | No hay schematic; excavar desde superficie Y≈347 hasta Y≈76 (salas de sello) |
| **2** | Galería de guardianes | **Parte de `compact_trial_chamber.ofs0.schem`** | Corredores con spawners de esqueleto/zombie/silverfish |
| **3** | Biblioteca quebrada | **Parte de `compact_trial_chamber.ofs0.schem`** | Zona con evocadores/vex; adaptar cofres a loot `mt:sellos` |
| **4** | Tres salas de sello (Trial Spawners) | **Parte de `compact_trial_chamber.ofs0.schem`** | **CRÍTICO:** 3 Trial Spawners con loot tables `mt:sellos/sello_N` + advancements |
| **5** | Cámara ominosa (arena jefe) | **`compact_trial_chamber.ofs0.schem`** (arena central 48×48×40) | Ancla de paste en centro arena `(-2304, -16, -3008)` |
| **6** | Sala del portal | **`isla-del-end-el-uv87add1.schem`** (241×256×241, paleta 100% End) | **NUEVO:** Reemplaza cámara del portal vanilla; ver §3 |

### 2.1 Qué Schematic Usar para la Trial Chamber

| Schematic | Dimensiones | Paleta | Bloques | Uso Recomendado |
|---|---|---|---|---|
| `compact_trial_chamber.schem` | 110×85×91 | 182 | ~849k | Versión base (sin offset) |
| `compact_trial_chamber.ofs0.schem` | 110×85×91 | 181 | ~1,63M | **RECOMENDADA** — incluye offset WE (`Metadata.WEOffsetX/Y/Z`), entities, block entities |
| `large_vanilla_trial_chamber.schem` | 128×72×128 | 54 | ~1,18M | Referencia vanilla; no usar (demasiado grande, paleta distinta) |

**Decisión:** Usar **`compact_trial_chamber.ofs0.schem`** como base completa. Contiene:
- Offset WorldEdit: `WEOffsetX=-?, WEOffsetY=-?, WEOffsetZ=-?` (leer al pegar)
- 181 tipos de bloques (paleta completa trial chamber: copper, tuff, deepslate, trial_spawner, vault, etc.)
- BlockEntities (trial_spawners, vaults, chests con loot)
- Entities (spawners pre-configurados)

### 2.2 Combinación/Modificación Necesaria

1. **Recortar** el schematic a la bbox real del diseño (arena + 3 salas de sello + galerías + biblioteca)
2. **Reemplazar** la cámara del portal vanilla (del diseño original) por `isla-del-end-el-uv87add1.schem`
3. **Configurar Trial Spawners:** NBT debe copiarse de vanilla generado (`/data get block`) — **[A VERIFICAR]**
4. **Loot tables:** Inyectar `mt:sellos/sello_1/2/3.json` en los 3 cofres de los Trial Spawners
5. **Advancements:** `data/mt/advancement/sello_N.json` con trigger `player_generates_container_loot`

---

## 3. Integración de `isla-del-end-el-uv87add1.schem` como Sala del Portal (Paso 6)

### 3.1 Especificaciones del Schematic

| Propiedad | Valor |
|---|---|
| Archivo | `MT_Server_Base_26.2\plugins\FastAsyncWorldEdit\schematics\isla-del-end-el-uv87add1.schem` |
| Dimensiones | **241 × 256 × 241** (X × Y × Z) |
| Paleta | **67 bloques** (100% temática End: end_stone, purpur, chorus, shulker, end_rod, etc.) |
| BlockEntities | **21** (probablemente shulkers, end_portal_frame, cofres) |
| Entities | Incluidos |
| Origen | Autor `uv87add1` (PlanetMinecraft/Modrinth) — **verificar licencia §6** |

### 3.2 Alineación con el Diseño

**Diseño original (cámara del portal):** 23×23×13 en `X ∈ [-2315,-2293]`, `Y ∈ [-44,-31]`, `Z ∈ [-2980,-2958]`

**Isla End schematic:** 241×256×241 → **10× más grande** en X/Z, 20× en Y.

**Estrategia de integración:**

1. **No pegar la isla completa** (rompería la montaña y sobresaldría a superficie Y≈347)
2. **Extraer solo la "cámara central" del portal** del schematic (área ~23×23 alrededor del portal frame)
3. **Opción alternativa (recomendada):** Usar la isla completa PERO:
   - Rotar/posicionar para que el **portal frame quede en `(-2304, -43, -2970)`** (centro marco 5×5)
   - La isla flota en Y alto; el hueco del portal queda a Y=-42..-40
   - Rellenar hacia arriba con `end_stone`/`barrier` hasta conectar con el vano (z=-2984, Y=-15..-11)

### 3.3 Cálculo de Offset Y para Pegado

| Referencia | Y |
|---|---|
| Suelo arena (ancla paste) | -16 |
| Vano (puerta arena→portal) | -15 .. -11 |
| Centro marco portal (hueco 3×3) | -43 (hueco en -42..-40) |
| Fondo cámara portal (diseño) | -44 |
| Tope isla End schematic (Height=256) | OffsetY + 255 |

**Para que el portal frame quede en Y=-43:**
- El schematic tiene su portal frame en alguna Y interna (medir con `/data get` o inspección)
- Asumiendo que el portal frame está centrado verticalmente en la isla: `OffsetY_isla = -43 - (Y_frame_interno - Y_base_isla)`
- **Método práctico:** Pegar en copia de prueba, medir coordenadas del frame resultante, ajustar offset

### 3.4 Rotación

- La isla End es simétrica; rotación **no crítica** para el portal frame (5×5)
- Alinear para que la "entrada" de la isla mire hacia el vano (dirección +Z, hacia z=-2984)
- Comando: `//rotate 0` (sin rotación) o `//rotate 180` según orientación del schematic

### 3.5 Conexión con Trial Chamber

```
[Arena Y=-16] → [Vano z=-2984, Y=-15..-11, barrier] → [Cámara portal Y=-44..-31] → [Portal frame Y=-43, hueco Y=-42..-40]
```

La isla End debe **empezar en Y=-44** (techo de la cámara) y **bajar** hasta incluir el portal frame.
- Si la isla tiene portal frame en su base: `OffsetY = -44 - (Y_base_isla)`
- Si la isla tiene portal frame en su centro: `OffsetY = -43 - (Y_frame_isla - 128)`

**Verificación obligatoria en copia de prueba** antes de pegar en mundo final.

---

## 4. Secuencia de Pegado (Orden, Comandos FAWE, Offsets Y)

### 4.1 Preparación (en copia local del mundo)

```bash
# 1. Copiar mundo exportado a carpeta de prueba
#    [A VERIFICAR] 2026-10-22: el origen original ya no existe. Ruta viva probable:
robocopy "..\_prueba_roxy_local\world" "mundo_prueba_trial" /E /MT:8
#    (la línea histórica era: robocopy "ENTREGA_Loding_20261017\05_mundo\world" "mundo_prueba_trial" /E /MT:8)

# 2. Iniciar servidor local Paper 26.2 con mundo_prueba_trial
# 3. Conectar con op/consola, cargar FAWE
```

### 4.2 Copiar Schematics a Carpeta FAWE

```bash
copy "MT_Server_Base_26.2\plugins\Litematica\schematics\compact_trial_chamber.ofs0.schem" \
     "servidor\plugins\FastAsyncWorldEdit\schematics\mt_trial_chamber_hitogami_v1.schem"

copy "MT_Server_Base_26.2\plugins\FastAsyncWorldEdit\schematics\isla-del-end-el-uv87add1.schem" \
     "servidor\plugins\FastAsyncWorldEdit\schematics\mt_end_portal_room_v1.schem"
```

### 4.3 Secuencia de Pegado (ORDEN CRÍTICO)

#### PASO 1: Trial Chamber Principal (base completa)

```mcfunction
// Posicionarse en ancla de paste (centro arena, piso)
/tp -2304 -16 -3008

// Cargar schematic principal
//schem load mt_trial_chamber_hitogami_v1

// Pegar SIN -a (con aire) para que la estructura cave la montaña
//paste

// Verificar bbox: /execute if block -2328 -16 -3031 minecraft:tuff run say "OK esquina"
// Verificar trial spawners: /execute if block -2304 64 -3072 minecraft:trial_spawner run say "Spawner OK"
```

**Offset Y del schematic:** Leer `Metadata.WEOffsetY` del archivo. Si el schematic tiene origen en Y=0 y el ancla es piso arena (Y=-16), el offset interno ya debería compensar. **Probar en copia.**

#### PASO 2: Sala del Portal (Isla End) — SOLO si paso 1 OK

```mcfunction
// Posicionarse en centro marco portal (diseño)
/tp -2304 -43 -2970

// Cargar schematic isla End
//schem load mt_end_portal_room_v1

// Pegar CON -a (ignorar aire) para no borrar estructura alrededor
//paste -a

// Si el portal frame NO queda en (-2304, -43, -2970):
//  1. //undo
//  2. Ajustar posición: /tp <nueva_ancla>
//  3. //paste -a
// Repetir hasta que /execute if block -2304 -42 -2970 minecraft:end_portal_frame run say "FRAME OK"
```

#### PASO 3: Corregir Hueco del Portal (si el schematic no lo trae exacto)

```mcfunction
// Asegurar hueco 3×3 de end_portal (solo al morir el jefe; ahora reinforced_deepslate)
// fill -2305 -42 -2971 -2303 -40 -2969 minecraft:reinforced_deepslate

// Vano (puerta arena↔portal) — barrier inicial
// fill -2312 -15 -2984 -2297 -11 -2984 minecraft:barrier
```

#### PASO 4: Excavar Acceso (Caverna + Túnel Descendente) — MANUAL

```mcfunction
// Desde superficie Y≈347 en X=-2304, Z=-3175 hasta Y≈76 (salas de sello)
// Usar //cyl air 3 100 para túnel vertical + //line para rampas
// Colocar iluminación, escaleras, decoración
```

### 4.4 Resumen de Offsets Y Calculados

| Elemento | Ancla Paste (X,Y,Z) | Offset Y Interno (estimado) | Y Final Objetivo |
|---|---|---|---|
| Trial Chamber (compact.ofs0) | -2304, **-16**, -3008 | Leer `WEOffsetY` | Arena piso = -16 ✓ |
| Isla End Portal | -2304, **CALCULAR**, -2970 | Leer `WEOffsetY` + medir frame | Frame center = -43 ✓ |
| Vano (barrier) | -2312, -15, -2984 a -2297, -11, -2984 | N/A (fill manual) | Conecta arena ↔ portal |

---

## 5. Configuración Post-Pegado

### 5.1 Trial Spawners (3 salas de sello)

**Ubicación aprox (diseño):** `X ∈ [-2328,-2256]`, `Y ∈ [56,76]`, `Z ∈ [-3040,-3100]`

**Acciones requeridas:**

1. **Verificar NBT de cada `trial_spawner`** — copiar de vanilla generado en 26.2:
   ```mcfunction
   /data get block <x> <y> <z>
   ```
   Campos clave: `SpawnData`, `SpawnPotentials`, `max_nearby_entities`, `required_player_range`, `spawn_range`, `cooldowns`, `CustomName`

2. **Asignar loot tables personalizadas** a los cofres/vaults asociados:
   - Sello 1: `mt:sellos/sello_1.json` (hierro/oro/lapislázuli + libro sharpness)
   - Sello 2: `mt:sellos/sello_2.json` (escalado intermedio)
   - Sello 3: `mt:sellos/sello_3.json` (diamond/netherite_scrap/enchanted_golden_apple)

3. **Configurar advancements** `data/mt/advancement/sello_N.json`:
   ```json
   {
     "criteria": {
       "loot": {
         "trigger": "minecraft:player_generates_container_loot",
         "conditions": { "loot_table": "mt:sellos/sello_N" }
       }
     },
     "rewards": { "function": "mt:sellos/sello_N" }
   }
   ```

4. **Función `mt:sellos/sello_N`** incrementa `mt_seal` y al llegar a 3 ejecuta `mt:sellos/abrir_arena` (quita barrier del vano)

### 5.2 Loot Personalizado (Reliquias)

| Origen | Loot Table | Tipo | Contenido Clave |
|---|---|---|---|
| 3 Sellos | `mt:sellos/sello_1/2/3.json` | `chest` | Escalonado; advancement dispara función de sello |
| Jefe (Hitogami) | `mt:boss/hitogami.json` | `entity` | 2 rolls: diamond 2-5, netherite_scrap 1-3, emerald_block 1-2; 1 roll enchanted_book |
| **Ítem único (1ª muerte)** | **Skript `hitogami.sk`** | `netherite_sword` | Nombre `&5Hoja de Hitogami`, lore con jugador + fecha |

**El ítem único NO sale de loot table** — lo entrega Skript al top damage de la primera muerte mundial.

### 5.3 Protección WorldGuard

```mcfunction
// Definir región cubriendo TODO el complejo
/rg define prueba_hitogami -2328 76 -3100 -2256 -44 -2958

// Flags
/rg flag prueba_hitogami build deny
/rg flag prueba_hitogami chest-access allow  // solo donde corresponda (cofres sellos)
/rg flag prueba_hitogami interact allow     // botones, palancas
/rg flag prueba_hitogami pvp allow
/rg flag prueba_hitogami mob-spawning allow // para trial spawners
/rg flag prueba_hitogami entry deny         // opcional: requerir permiso para entrar
/rg flag prueba_hitogami exit allow

// Solo admins con worldguard.bypass pueden romper/colocar
```

### 5.4 Bloqueo del Portal Hasta 3 Sellos

**Mecánica (datapack `mt_core`):**

1. `mt_end = 0` → portal **no existe** (hueco 3×3 = `reinforced_deepslate`)
2. Vano arena→portal cerrado con `barrier` (invisible + indestructible)
3. Completar 3 sellos → `mt_seal=3` → `mt:sellos/abrir_arena` → quita barrier del vano
4. Entrar a arena → cuenta atrás 10s → invoca Hitogami
5. **Derrotar a Hitogami** → `mt:puerta/desbloquear`:
   - Quita barrier del vano (si no se quitó)
   - `fill -2305 -42 -2971 -2303 -40 -2969 minecraft:end_portal`
   - `mt_end = 1` (global, permanente)
6. **Una vez `mt_end=1`, el portal queda abierto para TODOS, para siempre**

**Verificación:** `/scoreboard players get global mt_end` debe devolver 0 hasta la derrota, 1 después.

---

## 6. Licencias / Permisos

| Recurso | Autor | Licencia | Estado | Acción Requerida |
|---|---|---|---|---|
| **St. Basine Cathedral** (`stbasinecathedral.schem`) | Desconocido (Litematica) | **Ya verificado** — usuario confirmó tener permiso | ✅ OK | Ninguna |
| **`isla-del-end-el-uv87add1.schem`** | `uv87add1` | **PlanetMinecraft / Modrinth** — típicamente "CC BY-NC-SA" o "All Rights Reserved" | **[A VERIFICAR]** | 1. Buscar página original (PlanetMinecraft `uv87add1` o Modrinth) 2. Leer licencia exacta 3. Si CC BY-NC-SA: dar crédito, no uso comercial 4. Si All Rights Reserved: **no usar** sin permiso escrito |
| `compact_trial_chamber.schem` | Comunidad (Litematica export) | Domino público / uso libre típico | ✅ OK | Ninguna |
| `large_vanilla_trial_chamber.schem` | Vanilla export | Domino público | ✅ OK | Ninguna |

**Verificación urgente para `uv87add1`:**
- Buscar en: `https://www.planetminecraft.com/member/uv87add1/` o `https://modrinth.com/user/uv87add1`
- Descargar página original → leer "License" o "Terms of Use"
- Documentar hallazgo aquí antes de usar en producción

---

## 7. Pruebas en Copia Local (Antes de Subir a Producción)

### 7.1 Checklist de Validación Estructural

| Test | Comando / Método | Criterio de Pasa |
|---|---|---|
| **BBox trial chamber** | `/execute if block -2328 -16 -3031 minecraft:tuff` | Estructura completa, sin huecos |
| **3 Trial Spawners presentes** | `/execute if block <x1> <y1> <z1> minecraft:trial_spawner` ×3 | 3 spawners en salas de sello |
| **Cofres con loot tables** | `/data get block <x> <y> <z> Items[0].tag.LootTable` | `mt:sellos/sello_N` |
| **Portal frame en posición** | `/execute if block -2304 -43 -2970 minecraft:end_portal_frame` | Frame 5×5 centrado correcto |
| **Hueco 3×3 sellado** | `/execute if block -2304 -42 -2970 minecraft:reinforced_deepslate` | No `end_portal` todavía |
| **Vano con barrier** | `/execute if block -2304 -13 -2984 minecraft:barrier` | Cerrado hermético |
| **Isla End integrada** | Visual + `/execute if block -2304 -42 -2970 minecraft:end_stone` | Bloques End rodean portal |
| **Sin huecos de 1 bloque** | Recorrer con elytron/creative; `/execute if block ... air` | Shell sellado (anti ender pearl/chorus) |

### 7.2 Checklist Funcional (Datapack + Skript)

| Test | Comando | Criterio de Pasa |
|---|---|---|
| **Datapack carga** | `/function mt:load` | Sin errores en log; scoreboards creados |
| **Tick function registrada** | `/function mt:tick` | Se ejecuta cada tick (ver log) |
| **Sello 1 → mt_seal=1** | Activar trial spawner 1, abrir cofre | `scoreboard players get global mt_seal` = 1 |
| **Sello 2 → mt_seal=2** | Idem | `mt_seal` = 2 |
| **Sello 3 → mt_seal=3 + vano abre** | Idem | `mt_seal` = 3; barrier vano removido |
| **Entrar arena → prep + cuenta atrás** | Caminar a arena (volumen x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64) | Bossbar aparece, títulos "HITOGAMI", timer 200→0 |
| **Invocación Hitogami** | Esperar cuenta atrás | Ravager con tags `mt_boss`, `mt_add_protegido`, CustomName, Health escalada |
| **Fases de combate** | Golpear jefe; observar HP | Transiciones en 66% y 33%; adds spawnean; partículas/sonidos |
| **Derrota → portal** | Matar jefe (creativo o daño) | `mt_end=1`; `end_portal` en hueco 3×3; vano abierto; título "VICTORIA" |
| **Teleporte al End** | Entrar en portal | Teletransporta a End (allow-end=true) |
| **Reset al salir** | Salir del volumen de arena | `mt_fight=0`, bossbar oculta, jefe muerto |
| **Reinicio server mid-fight** | `stop` → `start` → entrar arena | `mt_fight=0`, sin jefe, **portal NO abierto** (mt_end persiste) |
| **Skript hitogami.sk** | `/sk reload hitogami` + pelea | Top damage 1ª muerte → netherite sword con lore; muertes posteriores → no ítem único |
| **Cooldown re-pelea** | Esperar cooldown (20-30 min) | Jefe re-invocable; sin ítem único |
| **LevelledMobs exclusión** | `/lm mob check @e[tag=mt_boss]` | Sin escalado/renombrado en jefe ni adds |
| **Bedrock (cliente real)** | Conectar Bedrock; repetir tests | Bossbar visible, títulos, partículas, ítems OK |

### 7.3 Rendimiento

```mcfunction
// 4 jugadores en pelea continua 3-5 min
/spark profiler start --timeout 300
// ... pelea ...
/spark profiler stop
/spark report
```
**Objetivo:** TPS ≥ 18.0, MSPT ≤ 50 ms promedio

### 7.4 Reexportar Zona y Subir

```bash
# 1. Detener servidor de prueba
# 2. Identificar regiones afectadas (bbox completo: X -2328..-2256, Z -3100..-2958 → regiones r.-73.-97 a r.-71.-93)
# 3. Copiar solo esas .mca al mundo de producción
# 4. Subir por SFTP (mundo\subir_sftp.py --dry-run primero)
# 5. Reiniciar servidor hosting
# 6. Verificar en vivo: /scoreboard players get global mt_end (debe ser 0)
```

---

## 8. Resumen de Decisiones Pendientes [A VERIFICAR CON USUARIO]

| # | Decisión | Opciones | Impacto |
|---|---|---|---|
| 1 | **Nombre del jefe** | "Hitogami, el Dios Humano" vs otro | Lore, anuncios, Skript |
| 2 | **Cooldown re-pelea exacto** | 20 min vs 25 min vs 30 min | Balance |
| 3 | **Boca de caverna Y** | Superficie real (≈347) vs Y=240 (diseño) | Longitud túnel, estética |
| 4 | **NBT Trial Spawner exacto** | Copiar de vanilla generado en 26.2 | Funcionalidad sellos |
| 5 | **Licencia `uv87add1`** | Verificar en PlanetMinecraft/Modrinth | Legal — **bloqueante si no libre** |
| 6 | **Empate daño top** | Primer atacante vs azar vs repartir | Skript logic |
| 7 | **max_health base jefe** | 400 (diseño) vs ≥420 (para no clamp) | Bug potencial si base=340 |
| 8 | **Rotación isla End** | 0° vs 90° vs 180° vs 270° | Orientación entrada vs vano |

---

## 9. Archivos a Crear/Modificar en `mt_core` (Datapack)

```
data/minecraft/tags/function/
  load.json          ← agregar "mt:boss/reset"
  tick.json          ← NUEVO: ["mt:tick"]

data/mt/function/
  tick.mcfunction
  stage_end_open.mcfunction
  boss/
    reset.mcfunction
    tick.mcfunction
    preparar.mcfunction
    cuenta_atras.mcfunction
    iniciar.mcfunction
    combate.mcfunction
    barra.mcfunction
    fase_1.mcfunction
    fase_2.mcfunction
    fase_3.mcfunction
    _fangs.mcfunction
    detectar_fin.mcfunction
    derrotar.mcfunction
    limpiar.mcfunction
    estado.mcfunction
  puerta/
    vano_cerrar.mcfunction
    vano_abrir.mcfunction
    desbloquear.mcfunction
    _desbloquear_real.mcfunction
  sellos/
    sello_1.mcfunction
    sello_2.mcfunction
    sello_3.mcfunction
    abrir_arena.mcfunction
  prueba/
    combate.mcfunction
    terminar.mcfunction
    estado.mcfunction

data/mt/advancement/
  sello_1.json, sello_2.json, sello_3.json

data/mt/loot_table/
  boss/hitogami.json
  sellos/sello_1.json, sello_2.json, sello_3.json
```

**Scoreboards nuevos:** `mt_fight` (0/1/2), `mt_prep` (0/1), `mt_end` (0/1), `mt_seal` (0-3), `mt_timer`, `mt_calc`, `mt_boss_first_kill` (0/1)

**Tags:** `mt_boss`, `mt_add_protegido`, `mt_add`

---

## 10. Archivo Skript Separado: `hitogami.sk`

**Ruta:** `servidor/plugins/Skript/scripts/hitogami.sk`  
**NO tocar `potencias.sk`**

```skript
options:
  boss_tag: "mt_boss"
  first_flag_obj: "mt_boss_first_kill"

on damage:
  if victim has scoreboard tag {@boss_tag}:
    add damage to {hitogami.dmg::%uuid of attacker%}
    add damage to {hitogami.total::%uuid of attacker%}

on death of ravager:
  if victim has scoreboard tag {@boss_tag}:
    # Verificar flag primera muerte (scoreboard global mt_boss_first_kill)
    # Encontrar top damage en {hitogami.dmg::*}
    # Dar netherite sword "Hoja de Hitogami" con lore personalizado
    # Setear mt_boss_first_kill = 1
    # Anunciar título + sonido
    # Limpiar {hitogami.dmg::*}

command /hitogami_reset_dmg:
  permission: op
  trigger:
    clear {hitogami.dmg::*}
    send "Daño de Hitogami reseteado"
```

---

## 11. Referencias y Fuentes

- `MT_Server_Base_26.2\FORTALEZA_END_DISENO.md` — Recorrido 6 pasos
- `DISENO_TRIAL_CHAMBER_JEFE.md` (531 líneas) — Diseño completo reconstruido 2026-10-17
- `AGENTS.md` (proyecto minecraft_server) — Hechos verificados, decisiones usuario
- `perugius\UBICACION_RESERVADA_FORTALEZA_PERUGIUS.md` — Método de pegado FAWE probado
- `ENTREGA_Loding_20261017\05_mundo\INFORME_MUNDO.txt` — Export verificado 1936 regiones *(**`[A VERIFICAR]` 2026-10-22:** esa carpeta ya no existe en disco; el informe no está disponible)*
- Schematics en `MT_Server_Base_26.2\plugins\Litematica\schematics\` y `FastAsyncWorldEdit\schematics\`

---

## 12. Próximos Pasos Inmediatos

**Actualizado 2026-10-22.** El orden ahora es:

1. **[Usuario] Decidir el schematic de la arena** — `compact_trial_chamber.ofs0.schem` no existe;
   ver el bloqueante de §ESTADO. Es lo que desbloquea el paso 2.
2. **Pegar la arena de Hitogami** en el ancla `(-2304, -16, -3008)` → verificar bbox.
   Compensar el `Offset` del `.schem` (para `compact_trial_chamber.schem` es `[269,191,736]`,
   medido el 2026-10-20).
3. **Pegar `isla-del-end-el-uv87add1.schem`** ajustando Y para alinear el portal frame → verificar.
4. **[Usuario] Confirmar decisiones pendientes §8** (licencia `uv87add1`, nombre del jefe,
   boca de caverna, `max_health` base, cooldown de re-pelea, empate de daño top, rotación isla).
5. **Excavar acceso** desde superficie Y≈347 (decisión de boca: Opción B registrada en §1.3).
6. **Completar el datapack `mt_core`** (§9: faltan `puerta/*`, `sellos/*`, `advancement/*`,
   `loot_table/*` según el plan) y **probar `hitogami.sk`** en runtime.
7. **Ejecutar checklist §7.1 y §7.2** completo, con **un jugador real**.
8. **Reexportar regiones afectadas** y subir por SFTP (`mundo\reemplazar_sftp_verificado.py`
   o `subir_sftp.py`; verificar por **SHA256**, no por tamaño — incidente 2026-10-18).
9. **Test Bedrock real** en hosting.

> Ya hecho: **SC1–SC10 colocadas** (reportado 2026-10-22). No requiere acción de este plan.

---

**Documento generado:** 2026-10-21  
**Actualizado:** 2026-10-22 (estado SC1–SC10 colocadas / arena pendiente; bloqueante del `.ofs0`).  
**Estado:** plan parcialmente ejecutado — el datapack `mt_core` del jefe y `hitogami.sk` **sí existen en disco**; falta la estructura (arena + portal) y la prueba con jugador real.
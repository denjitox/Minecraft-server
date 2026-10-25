# Lista de desarrollo para reanudar en PC principal

Fecha: 2026-10-25  
Repo: `C:\Dev\Minecraft_server\Minecraft server`  
Rama actual: (sin git verificado desde este entorno)

## Estado actual

### Completado
- [x] **Hitogami - Datapack**: `datapacks/mt_core/` con flujo completo (sellos, arena, invocación, combate, derrota). Al derrotarlo quita bedrock de `-2305 -42 -2971` a `-2303 -40 -2969`, coloca `end_portal`, abre vano `-2312 -15 -2984` a `-2297 -11 -2984`, setea `mt_end=1`.
- [x] **Hitogami - Skript**: `Skript/scripts/hitogami.sk` con tracking de daño por UUID, detección de primera muerte mundial (`mt_boss_first_kill`), entrega de `Hoja de Hitogami` al mayor daño con nombre/fecha/lore.
- [x] **Loot repetible**: `datapacks/mt_core/data/mt/loot_table/boss/hitogami.json` con Fragmento del Vacío, Reliquia del Trono Roto, diamantes, netherite scrap, emerald blocks, libro encantado.
- [x] **Regional Difficulty**: Pulido en `MushokuCore/.../regionaldifficulty/RegionalDifficultyModule.java` (distancia, interpolación suave).
- [x] **MushokuCore**: Plugin funcional en producción (cargado y diagnosticado OK el 2026-10-22).

### En progreso / Pendiente crítico
- [ ] **Pegar estructura física Hitogami** (OBLIGATORIO): Pegar trial chamber/arena en `(-2304, -16, -3008)`, integrar isla End con `isla-del-end-el-uv87add1.schem`, construir jaula de bedrock rodeando cámara del portal dejando SOLO el vano libre. 
- [ ] **Probar flujo Hitogami**: `/reload`, `/function mt:prueba/combate`, verificar spawn, derrota abre portal, bedrock removido, `mt_end=1`. `/function mt:prueba/terminar` para pruebas rápidas.
- [ ] **Ajustar coordenadas si difieren**: Si el pegado desplaza, ajustar en `datapacks/mt_core/data/mt/function/*` (área arena `-2330 -20 -3046 dx52 dy44 dz64`, vano `z=-2984`, hueco portal `-2305 -42 -2971` a `-2303 -40 -2969`).
- [ ] **Verificar ítem único**: Probar con `/hitogami_top`, matar con distintos jugadores, confirmar solo primera muerte da `Hoja de Hitogami`.

### Pendiente secundario
- [ ] **Fases Hitogami**: Añadir fases, adds, evoker_fangs, teletransporte (actualmente solo invocación + derrota).
- [ ] **Reliquias config**: Crear config lista para pegar en servidor live basado en `docs/INSTRUCCIONES/MT_Estadisticas_Tecnicas_Reliquias.md`. Implementar las 7 reliquias (suppression_ring, sara_earring, migurd_pendant, paul_sword, superd_spear, aqua_heartia, magic_armor_mk1) con sus valores PvE/PvP, cooldowns, límites globales, ranuras virtuales.
- [ ] **Nether personalizado**: Decidir biomas/ambiente (datapack) o bloqueo por progreso (módulo `dimensions`).
- [ ] **Aldeanos con skins**: Usar skins de `docs/perugius/skins/`, Citizens + Mineskin.
- [ ] **Regional difficulty runtime test**: Probar con spawns reales.

## Compilación (leer antes de buildear)

`MushokuCore\build.ps1` fue endurecido el 2026-10-25. Ver `MushokuCore\COMPILAR.md`.

- [x] **`$LibRoot` relativo**: ya no apunta fijo a `C:\Dev\OPENCLAUDIO\...`. Busca `..\servidor\libraries`, después `..\..\servidor\libraries`, y por último la ruta histórica. Se puede forzar con `-LibRoot`.
- [x] **Dependencias por patrón**: se descubren por nombre (`paper-api-*.jar`, `guava-*.jar`, `PlaceholderAPI-*.jar`, …) en vez de versión exacta. Si hay dos versiones, falla nombrando ambos candidatos.
- [x] **`-WhatIf` completo**: informa y no toca el disco (antes igual compilaba y escribía el jar).
- [x] **Limpieza correcta**: `build\` se borra solo si el build no terminó; un jar parcial ya no queda como si fuera bueno.
- [x] **Errores con contexto**: la salida de `javac`/`jar` se captura y se adjunta al error.

**Pendiente en la PC principal:**

- [ ] Instalar JDK 25 (Temurin) y verificar `javac -version` / `jar --version`. Esta notebook no tiene Java.
- [ ] Confirmar que existe `C:\Dev\OPENCLAUDIO\minecraft_server\servidor\libraries` (o pasar `-LibRoot`).
- [ ] Correr `cd MushokuCore; .\build.ps1 -Verbose` y conservar la salida. **La compilación nunca se ejecutó desde acá**: no hay JDK ni jars en esta máquina.
- [ ] Copiar `build\libs\MushokuCore-1.0.0.jar` a `servidor\plugins\` y reiniciar el servidor (Paper no re extrae `messages.yml` si el archivo ya existe en `plugins\MushokuCore\`).

## Archivos clave
- `datapacks/mt_core/` - Todo el sistema Hitogami
- `Skript/scripts/hitogami.sk` - Tracking daño + ítem único
- `docs/DISENO_TRIAL_CHAMBER_JEFE.md` - Diseño completo
- `docs/mundo/PLAN_TRIAL_CHAMBER_JEFE_PORTAL_END_2026-10-21.md` - Plan y coordenadas
- `docs/INSTRUCCIONES/MT_Estadisticas_Tecnicas_Reliquias.md` - Especificación reliquias
- `MushokuCore/src/main/java/.../regionaldifficulty/RegionalDifficultyModule.java` - Dificultad regional

## Notas
- Sin git en este entorno; cambios deben commitearse/pushearse manualmente (GitHub Desktop).
- No tocar `potencias.sk`.
- Portal solo se desbloquea al matar Hitogami (primera vez o cualquier vez, pero bedrock solo se quita una vez con `mt_end=1`).
- El ítem único SOLO va al mayor daño de la PRIMERA muerte mundial.

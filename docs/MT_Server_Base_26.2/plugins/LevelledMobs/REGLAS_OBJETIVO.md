# Perfil de dificultad a implementar

No copiar este documento como `rules.yml`: la sintaxis exacta se adaptará al
archivo generado por la versión de LevelledMobs instalada en el hosting.

## Escalado buscado

| Zona | Vida | Daño |
| --- | ---: | ---: |
| Radio 0–2.000 | +10 % | +10 % |
| Radio 2.001–6.000 | +20 % | +15 % |
| Radio 6.001–11.000 | +35 % | +25 % |
| Nether | +40 % | +30 % |
| End | +50 % | +35 % |

## Exclusiones

- Warden: excluir del escalado automático.
- Ender Dragon: configurar como encuentro separado.
- Mobs de Trial Spawner: limitar el aumento de vida a 25 % para evitar combates
  demasiado largos cuando haya muchos jugadores.
- Animales, aldeanos, mascotas y armor stands: excluir.

Evitar niveles visibles sobre todos los mobs hasta comprobar que los nombres no
generen ruido visual o diferencias molestas para jugadores Bedrock.

---

# APLICADO 2026-10-18

Implementado en **LevelledMobs 4.5.3.2 (LM4)** sobre `rules.yml`. Fuente editada:
`servidor\plugins\LevelledMobs\rules.yml` (config real del server); copiada a
`MT_Server_Base_26.2\plugins\LevelledMobs\rules.yml` (paquete de entrega).
Backup previo: `servidor\plugins\LevelledMobs\rules.yml.pre-regional` (16.181 B).

## Qué se cambió

1. **`default-rule` sin escalado por nivel.** `use-preset` pasó a
   `[challenge-vanilla, nametag-disabled]`; se quitaron `challenge-silver`,
   las 2 estrategias (`lvlstrategy-weighted-random`, `lvlstrategy-distance-from-origin`)
   y los 2 modificadores. `minLevel: 1`, `maxLevel: 1`, `construct-level: '1'`,
   `nametag-visibility-method: ['DISABLED']`.
   Consecuencia buscada: el nivel no afecta atributos y no hay nametags.
   (`challenge-vanilla` deja los bonos de atributo en 0; `item-drop`/`xp-drop`
   en 0.0 significan "sin boost", no "sin drops".)

2. **Exclusiones (primeras, cortan con `stop-processing` + `maxLevel: 0`):**
   - `Exclusion - Jefe Hitogami`: `conditions.scoreboard-tags: ['mt_boss','mt_add']`
     + `stop-processing: true`; `settings.maxLevel: 0`.
   - `Exclusion - Warden, animales, aldeanos y mascotas`: `entities.included-groups:
     ['all_passive_mobs']` + `included-list: ['WARDEN','ARMOR_STAND']` +
     `stop-processing: true`; `settings.maxLevel: 0`.

3. **Dificultad regional (al final, con `merge: false` para pisar cualquier
   `attribute-modifier` anterior; el % regional es el único efecto):**
   - `Regional - Overworld radio 0-2000`: vida +10 %, daño +10 %
   - `Regional - Overworld radio 2001-6000`: vida +20 %, daño +15 %
   - `Regional - Overworld radio 6001-10900`: vida +35 %, daño +25 %
   - `Regional - Nether` (`world_nether`): vida +40 %, daño +30 %
   - `Regional - End` (`world_the_end`): vida +50 %, daño +35 %
   Las distancias usan `min-distance-from-spawn` / `max-distance-from-spawn`
   (círculos desde el spawn del mundo). El % se aplica a `max-health`,
   `attack-damage` y `ranged-attack-damage` con la sintaxis de fórmula
   `'(%max-health% * 0.XX)'`.
   - `Trial Spawner - vida limitada`: `spawn-reasons: ['TRIAL_SPAWNER']`,
     `max-health: '(%max-health% * 0.25)'`. Va **después** de las regionales y
     **sin** `merge: false`, así pisa solo la vida y conserva el daño regional.

4. **Warden sacado de las listas que lo tomaban:** quitado de
   `Bronze Challenge for Specific Mobs` y de `Minimized Nametag for Specific Mobs`.
   Ahora WARDEN solo aparece en la regla de exclusión.

## Verificación hecha

- `rules.yml` parsea con PyYAML (`YAML OK`), 19 `custom-rules`.
- No se levantó el servidor (no se probó en runtime).

## [A VERIFICAR] — pendientes explícitos

- **[A VERIFICAR]** Que el spawn del mundo sea **(0,0)**: los anillos de
  `min/max-distance-from-spawn` se miden desde ahí. No verificado en runtime
  (no se levantó el server).
- **[A VERIFICAR]** Que el jefe Hitogami realmente lleve los scoreboard tags
  `mt_boss` y `mt_add`: si no los tiene, la condición `scoreboard-tags` no
  matchea y **no** queda excluido. No verificado en el mundo.
- **[A VERIFICAR]** Que `TRIAL_SPAWNER` sea el spawn reason real de los mobs de
  trial spawner en este server (Paper 26.2). Confirmado en la javadoc de
  Paper 1.21+ (`CreatureSpawnEvent.SpawnReason.TRIAL_SPAWNER`), no en este server.
- **[A VERIFICAR]** Semántica del tope de trial spawner: es un **valor fijo
  +25 %**, no un `min(regional, 25 %)`. En el anillo 0-2000 (regional +10 %) esto
  daría +25 %, que supera el 10 % regional. A confirmar si molesta.
- **[A VERIFICAR]** Nombres de mundo: se asume overworld = `world`,
  nether = `world_nether`, end = `world_the_end` (coinciden con las reglas ya
  presentes en el `rules.yml` original).
- **Ender Dragon "aparte":** no se definió un encuentro propio en esta tarea; hoy
  queda alcanzado por la regla del End (+50 %/+35 %). Confirmar si debe tener
  otro valor o quedar sin escalado.
- **[A VERIFICAR]** El método fórmula se aplica como **delta** (`ADD_NUMBER`),
  o sea `(%max-health% * 0.10)` = +10 % de la vida base. Verificado en el código
  fuente de LM4 (`MobDataManager.getAdditionsForLevel`, `AttributePreMod`), no en
  este server.



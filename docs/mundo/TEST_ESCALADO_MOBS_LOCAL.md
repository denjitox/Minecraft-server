# Test Local: Escalado Regional de Mobs (LevelledMobs)
**Fecha:** 2026-10-22  
**Objetivo:** Verificar que el escalado de vida/daño por distancia del spawn funciona en server local antes de pushear a live.

---

## 1. Estado actual

**LevelledMobs 4.5.3.2** ya instalado y configurado en `servidor\plugins\LevelledMobs\rules.yml`.

Reglas activas (custom-rules, orden de prioridad = orden en archivo):

| # | Regla | Condición | Efecto |
|---|---|---|---|
| 1 | Exclusion - Jefe Hitogami | tags `mt_boss`, `mt_add` | `maxLevel: 0` (sin escalado) |
| 2 | Exclusion - Warden, animales, aldeanos, mascotas | grupos `all_passive_mobs` + `WARDEN`, `ARMOR_STAND` | `maxLevel: 0` |
| 3 | Nether World | mundo `world_nether` | estrategia Y-coordinate + custom_nether |
| 4 | End World | mundo `world_the_end` | estrategia distance-from-origin + custom_end |
| 5 | Custom Attributes | mobs específicos | bonos fijos (enderman +0.5 salud, etc.) |
| 6 | Bronze Challenge | voladores + bosses | challenge-bronze |
| 7 | Vanilla Challenge | pasivos + babies | challenge-vanilla (sin bonos) |
| 8 | **Regional Overworld 0-2000** | mundo `world`, dist ≤ 2000 | **+10% vida, +10% daño** |
| 9 | **Regional Overworld 2001-6000** | mundo `world`, dist 2001-6000 | **+20% vida, +15% daño** |
| 10 | **Regional Overworld 6001-10900** | mundo `world`, dist 6001-10900 | **+35% vida, +25% daño** |
| 11 | **Regional Nether** | mundo `world_nether` | **+40% vida, +30% daño** |
| 12 | **Regional End** | mundo `world_the_end` | **+50% vida, +35% daño** |
| 13 | **Trial Spawner** | spawn-reason `TRIAL_SPAWNER` | **+25% vida** (pisa solo max-health) |

> **Clave:** `merge: false` en reglas 8-12 → el % regional es el **ÚNICO** efecto sobre atributos (pisa cualquier bono previo). Verificado en código LM4.

---

## 2. Iniciar server local para test

```powershell
cd C:\Dev\OPENCLAUDIO\minecraft_server\servidor
# Java 25 ya en PATH (Temurin 25.0.4.1)
java -Xms2G -Xmx4G -jar paper-26.2-134.jar --nogui
```

**Verificar arranque limpio:**
- `[LevelledMobs] Enabling LevelledMobs v4.5.3.2-b159`
- `[LevelledMobs] Loaded 13 custom rules`
- Sin errores de config

---

## 3. Comandos de test (ejecutar en consola del server local)

### 3.1 Verificar reglas cargadas
```
/lm rules
/lm debug
```

### 3.2 Spawnear mobs de prueba en distintas distancias

**Zona 0-2000 (cerca del spawn 0,0):**
```
/tp @p 0 100 0
/summon zombie ~5 ~ ~ {Tags:["test_mob"]}
/summon skeleton ~10 ~ ~ {Tags:["test_mob"]}
/summon creeper ~15 ~ ~ {Tags:["test_mob"]}
```

**Zona 2001-6000:**
```
/tp @p 3000 100 0
/summon zombie ~5 ~ ~ {Tags:["test_mob"]}
/summon skeleton ~10 ~ ~ {Tags:["test_mob"]}
```

**Zona 6001-10900:**
```
/tp @p 8000 100 0
/summon zombie ~5 ~ ~ {Tags:["test_mob"]}
/summon skeleton ~10 ~ ~ {Tags:["test_mob"]}
```

**Nether (requiere portal o /execute):**
```
/execute in world_nether run tp @p 0 100 0
/execute in world_nether run summon zombie ~5 ~ ~ {Tags:["test_mob"]}
```

**End:**
```
/execute in world_the_end run tp @p 0 100 0
/execute in world_the_end run summon zombie ~5 ~ ~ {Tags:["test_mob"]}
```

### 3.3 Verificar atributos aplicados

Para cada mob spawneado:
```
/data get entity @e[tag=test_mob,limit=1,sort=nearest] Attributes
```

**Valores base vanilla (referencia):**
| Mob | Salud base | Daño melee base |
|---|---:|---:|
| Zombie | 20.0 (10 corazones) | 3.0 |
| Skeleton | 20.0 | 2.0 (melee) / variable (ranged) |
| Creeper | 20.0 | N/A (explosión) |

**Valores esperados con escalado:**

| Zona | Zombie Salud | Zombie Daño | Skeleton Salud | Skeleton Daño melee |
|---|---:|---:|---:|---:|
| 0-2000 (+10%) | **22.0** | **3.3** | **22.0** | **2.2** |
| 2001-6000 (+20%/+15%) | **24.0** | **3.45** | **24.0** | **2.3** |
| 6001-10900 (+35%/+25%) | **27.0** | **3.75** | **27.0** | **2.5** |
| Nether (+40%/+30%) | **28.0** | **3.9** | **28.0** | **2.6** |
| End (+50%/+35%) | **30.0** | **4.05** | **30.0** | **2.7** |

> **Fórmula:** `max-health: '(%max-health% * X)'` en LM4 = **ADD_NUMBER** del X*100% de la vida base. O sea: `base + base*X = base*(1+X)`.

### 3.4 Verificar EXCLUSIONES (no deben escalar)

```bash
# Warden (excluido)
/tp @p 0 100 0
/summon warden ~5 ~ ~ {Tags:["test_excl"]}
/data get entity @e[tag=test_excl,limit=1] Attributes
# Esperado: Salud 500 (base), Daño 30 (base) — SIN escalado

# Vaca (pasivo, excluido)
/summon cow ~10 ~ ~ {Tags:["test_excl"]}
/data get entity @e[tag=test_excl,limit=1,sort=nearest] Attributes
# Esperado: Salud 10 (base), sin bonos

# Aldeano (excluido)
/summon villager ~15 ~ ~ {Tags:["test_excl"]}
/data get entity @e[tag=test_excl,limit=1,sort=nearest] Attributes

# Trial Spawner mob (debe tener +25% vida MAX, no regional)
# Requiere trial spawner real o /setblock trial_spawner
```

### 3.5 Verificar nametags (opcional)
```
/lm nametag on
# Golpear mob y ver nametag con salud
```

---

## 4. Checklist de verificación

| Test | Resultado esperado | ✅/❌ | Notas |
|---|---|---|---|
| Zombie en 0,0 tiene 22.0 salud | 20.0 * 1.10 = 22.0 | | |
| Zombie en 3000,0 tiene 24.0 salud | 20.0 * 1.20 = 24.0 | | |
| Zombie en 8000,0 tiene 27.0 salud | 20.0 * 1.35 = 27.0 | | |
| Zombie en Nether tiene 28.0 salud | 20.0 * 1.40 = 28.0 | | |
| Zombie en End tiene 30.0 salud | 20.0 * 1.50 = 30.0 | | |
| Warden SIN escalado (500 salud) | maxLevel: 0 | | |
| Vaca/aldeano SIN escalado | grupos pasivos | | |
| Trial Spawner mob +25% vida | 20.0 * 1.25 = 25.0 | | |
| Daño melee escala correctamente | Ver tabla arriba | | |
| Daño ranged escala correctamente | Ver tabla arriba | | |
| `/lm rules` muestra 13 custom rules | | | |
| Sin errores en consola al spawnear | | | |

---

## 5. Push a Live (cuando tests pasen)

### 5.1 Archivos a subir (SFTP `<HOST_SFTP>:<PUERTO_SFTP>`)
```
servidor/plugins/LevelledMobs/rules.yml
servidor/plugins/LevelledMobs/settings.yml
servidor/plugins/LevelledMobs/customdrops.yml
servidor/plugins/LevelledMobs/externalplugins.yml
servidor/plugins/LevelledMobs/messages.yml
```
> El `.jar` ya está en live (mismo version 4.5.3.2-b159).

### 5.2 Procedimiento
1. **Backup world live** (por si acaso)
2. **Subir configs** vía `mundo\subir_sftp.py` o WinSCP a `/plugins/LevelledMobs/`
3. **Reiniciar server live** (panel hosting o RCON `/stop` + start)
4. **Verificar en live:** `/lm rules`, spawnear 1 mob cerca de spawn, `/data get` atributos
5. **Confirmar** que valores coinciden con local

### 5.3 Rollback si falla
- Restaurar configs anteriores del backup
- Reiniciar server

---

## 6. Notas técnicas

- **Distancia medida desde spawn del mundo (0,0)** — confirmar que `server.properties` tiene `spawn-protection=0` y spawn real es 0,0 (medido 2026-10-18: pixel (1375,1375) biomas_2750.png = plains = tierra)
- **LevelledMobs usa `min/max-distance-from-spawn`** = distancia euclídea 2D desde (0,0) en el mundo `world`
- **`merge: false`** crítico: sin él, los bonos se sumarían (ej. regional + bronze challenge = doble escalado)
- **Orden de reglas importa:** exclusiones PRIMERO (`stop-processing: true`), regionales DESPUÉS, Trial Spawner AL FINAL
- **Performance:** `async-task-update-period: 3`, `async-task-max-blocks-from-player: 320` — OK para 24 jugadores

---

## 7. Próximos pasos tras confirmar

- [ ] Test local completo ✅
- [ ] Push configs a live
- [ ] Verificar en live con 1-2 mobs
- [ ] Documentar en `BITACORA_ESCALADO_MOBS_2026-10-22.md`
- [ ] Continuar con Trial Chamber jefe + Portal End (la **arena de Hitogami** en `(-2304, -16, -3008)` y el portal detrás; las secundarias **SC1–SC10 ya están colocadas**, reportado 2026-10-22)
- [ ] Naturalización estructuras (plan pendiente resultados descarga mundo live)

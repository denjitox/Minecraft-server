# Informe — Enfermedades de MushokuCore en producción

**Fecha:** 27/10/2026 · **Alcance:** solo lectura · **Servidor:** producción (Paper 26.2, MushokuCore 1.0.0)
**Pregunta:** ¿por qué aparentemente nadie se enfermó nunca?

---

## 0. Qué NO se hizo

- No se modificó ninguna config, base de datos, mundo ni JAR de producción.
- No se reinició ni se recargó el servidor.
- No se volvieron a descargar los archivos en este turno. Los datos de producción son los de la copia tomada el **27/10/2026 00:11 (UTC-3)**; el código y los YAML del repo sí se releyeron en disco hoy.
- No se aplicó el fix propuesto (sección 5): está descrito, no ejecutado.

**Procedencia de cada dato**

| Tipo de dato | Fuente | Cuándo |
|---|---|---|
| Código Java, `diseases.yml`, `config.yml`, `plugin.yml`, migraciones | repo local `MushokuCore\src\...` | releído en disco el 27/10/2026 |
| Config, catálogo, JAR, base y logs de producción | copia por WinSCP en `mushoku-main` (`...\Temp\opencode\live2\` y `...\live\`) | descargada el 27/10/2026 00:11 (UTC-3) |
| Logs archivados 16–26/10 | `...\live\` | descargados en la misma ventana |

> Un archivo de resumen no es evidencia. Las cifras de producción de este informe se midieron sobre esas copias; no se re-midieron en este turno porque las copias no están en esta máquina.

---

## 1. Respuesta corta

**El sistema no está roto y sí contagió: 4 infecciones naturales en la ventana observada (~3 días).**

El "nadie se enfermó" tiene tres piezas, en orden de peso:

1. **Cobertura de fuentes.** Solo 4 tipos de mob pueden contagiar: husk, enderman, cave spider y silverfish (`DiseaseInfectionListener.java` L29-33). Los mobs que más daño hacen en la práctica —**zombie, spider, skeleton y drowned**— no están en el mapa. El 26/10 el log no registra **ningún** husk/enderman/cave spider/silverfish, y las muertes PvE fueron de zombie (2), spider (2) y skeleton (2): ese día no hubo fuentes elegibles registradas.
2. **4 % por golpe recibido.** No es por mob ni por pelea: es una tirada por cada golpe (`DiseasesService.java` L326). Con 4 % hacen falta ~17 golpes para llegar al 50 % y ~60 para el 91 %.
3. **Falso negativo de medición.** La tabla `diseases` solo contiene filas mientras la enfermedad está activa (30–180 s) y la tarea de expiración las borra (`DiseaseRepository.java` L95-104). La foto del 27/10 00:11 se tomó **23 h después** de la última aplicación (26/10 04:09Z = 26/10 01:09 local): tenía que estar vacía. Además, el `.db` sin el `-wal` muestra el estado viejo.

La prueba directa de que funciona: el 25/10 16:43:39 (local) se aplicó `laplace` a **PAOLI1404** por daño de enderman, y a las 16:46:55 el jugador escribió en el chat *"me hizo daño un enderman y me dio fiebre de Laplace"*.

---

## 2. Evidencia medida

### 2.1 El módulo está encendido y el catálogo cargó

| Hecho | Valor | Fuente |
|---|---|---|
| `enabled` | `true` | `/plugins/MushokuCore/config.yml` L7 (copia 27/10 00:11); repo `config.yml` L7 |
| `modules.diseases` | `true` | mismo archivo, L17 |
| Catálogo cargado | `diseases.yml: 4 enfermedad(es), 5 tipo(s) de efecto gestionado(s)` | `/logs/2026-10-25-3.log.gz` L308-317, 25/10 23:52:07 |
| Módulo habilitado | `Modulo 'diseases' habilitado...`; `Modulos habilitados: [ranks, diseases, factions, regional-difficulty, commands]` | mismo log, 25/10 23:52:07 |
| Catálogo vivo = catálogo del JAR | `diseases.yml` 1.604 B, SHA256 `DE68FC4F80ECF1B7798B71EF3842C4F3F01CBF590CFDED38DB16F1E848DA808B`, idéntico a la entrada del JAR | copia 27/10 00:11 |

### 2.2 Sí hubo infecciones

`audit_log`: **44 filas**, de las cuales **8 `enfermedad.aplicar`**, **8 `enfermedad.expirar`** y **0 `enfermedad.curar`**. Fuente: copia de `mushoku.db` del 27/10 00:11, leída **con `-wal` y `-shm`**.

| Fecha/hora UTC | Hora local (UTC-3) | Enfermedad | Fuente | Nota |
|---|---|---|---|---|
| 24/10 03:36:35 | 24/10 00:36:35 | dryne | `HUSK_DAMAGE` | natural |
| 24/10 03:39:10 | 24/10 00:39:10 | laplace | `ENDERMAN_DAMAGE` | natural; 19 s después el log registra `ItzKayn was slain by Enderman` |
| 24/10 05:56:52 | 24/10 02:56:52 | dryne | `HUSK_DAMAGE` | natural |
| 25/10 19:43:39 | 25/10 16:43:39 | laplace | `ENDERMAN_DAMAGE` | natural; aplicada a PAOLI1404; chat a las 16:46:55 (3 min después, ya expirada) |
| 26/10 04:09 | 26/10 01:09 | 4 aplicaciones | `admin` | prueba administrativa |

Las 8 expiraciones son consistentes con las 8 aplicaciones (4 naturales + 4 admin). Duraciones observadas entre aplicación y expiración: **47 s, 182 s, 46 s, 182 s**, coherentes con laplace (45 s) y dryne (180 s) más la granularidad del barrido.

**Tiempo natural acumulado en los 4 pares identificados:** 47 + 182 + 46 + 182 = **457 s ≈ 7 min 37 s**.

### 2.3 El 26/10 no hubo ni un mob elegible

| Dato | Valor | Fuente |
|---|---|---|
| Menciones de Enderman / Husk / Cave Spider / Silverfish en el log del 26/10 | **0** | `/logs/2026-10-26-1.log` |
| Muertes PvE | Zombie 2, Spider 2, Skeleton 2 | mismo log |
| Muertes PvP | 32 | mismo log |

Los tres mobs que mataron ese día (**zombie, spider, skeleton**) no están en `FUENTES`. Aunque hubieran golpeado 100 veces, la probabilidad de contagio era exactamente 0.

### 2.4 El JAR que corre es el mismo que ya contagió

| Hecho | Valor |
|---|---|
| JAR en producción | `MushokuCore-1.0.0.jar`, **246.367 B**, 25/10 23:12:25, SHA256 `9F813CFF73167838E608F802A8359157B16289DCF8274E01068F43D2620BE26E` |
| JAR del 20/10 que produjo las infecciones naturales | 200.688 B, SHA256 `CC2140D5...` |
| Clases de enfermedades + listener + `diseases.yml` | **byte-idénticas** entre ambos JAR |
| `javap` sobre el JAR desplegado | mapa `FUENTES` de 4 entradas, 3 listeners registrados, lógica `nextDouble(100.0) >= chance` |

No hubo drift entre el JAR que funciona y el que está en producción.

---

## 3. Causas raíz, por probabilidad

### 3.1 Cobertura de fuentes — **alta**

`DiseaseInfectionListener.java` L29-33:

```java
private static final Map<EntityType, String> FUENTES = Map.of(
        EntityType.HUSK, "HUSK_DAMAGE",
        EntityType.CAVE_SPIDER, "CAVE_SPIDER_DAMAGE",
        EntityType.SILVERFISH, "SILVERFISH_DAMAGE",
        EntityType.ENDERMAN, "ENDERMAN_DAMAGE");
```

`diseases.yml`:

| Enfermedad | Fuentes declaradas | Líneas |
|---|---|---|
| dryne | `HUSK_DAMAGE` | L11-12 |
| miasma | `CAVE_SPIDER_DAMAGE`, `SILVERFISH_DAMAGE` | L24-26 |
| laplace | `ENDERMAN_DAMAGE` | L35-36 |
| desplazamiento | `[]` (solo admin) | L48 |

Consecuencias:

- **Zombie, spider, skeleton y drowned no contagian nada.** Son los mobs hostiles más frecuentes del overworld. El drowned es especialmente relevante: el 47,1 % del mapa es océano (fuente: tabla de biomas del proyecto, `mapear_biomas.py`, 16/10/2026), y ningún drowned puede infectar.
- **Miasma es, en la práctica, contenido muerto.** Cave spider solo aparece por spawners de minas; silverfish, por strongholds o bloques infestados. Si el mundo no tiene estructuras, no hay fuente natural. *(La ausencia de estructuras está documentada en `AGENTS.md` y en `world_gen_settings.dat`, pero no se recorrió el mundo vivo: **NO VERIFICADO**.)*
- Las 4 infecciones naturales observadas usan exactamente las dos fuentes "alcanzables": husk (2) y enderman (2). Cero de miasma. Eso encaja con el modelo.

**El 26/10 es el caso testigo:** 0 mobs elegibles registrados en el log, 6 muertes PvE de mobs no elegibles. No es mala suerte; es cobertura.

### 3.2 4 % por golpe + muestra chica — **alta**

`diseases.yml` L13/L27/L37: `base-chance-percent: 4.0`.
`DiseasesService.java` L326: `if (azar.nextDouble(100.0) >= chance) continue;` → 4 % de éxito por golpe.

Probabilidad de al menos un contagio tras *n* golpes: `P(n) = 1 − 0.96^n`.

| Golpes recibidos | Probabilidad |
|---:|---:|
| 10 | 33,5 % |
| 17 | 50,0 % |
| 20 | 55,8 % |
| 25 | 64,0 % |
| 30 | 70,6 % |
| 60 | 91,4 % |
| 90 | 97,5 % |
| 100 | 98,3 % |

(Calculado, no medido: no hay log de daño por golpe en producción.)

Atenuantes que reducen aún más la frecuencia:

- **No hay reinfección de la misma enfermedad mientras está activa** (`DiseasesService.java` L229-230): los golpes siguientes no vuelven a tirar para esa enfermedad.
- **Inmunidad post-cura de 300 s** para dryne (`diseases.yml` L20; `DiseasesService.java` L233-235).
- **Alianza Orsted:** si el jugador tiene `mt.alianza.orsted`, la chance se multiplica por 0,5 antes de la tirada (`DiseasesService.java` L315-325). No se consultó quién tiene el permiso: **NO VERIFICADO**.

Con 4 % por golpe, un jugador puede pelear toda una noche y no contagiarse; y al revés, dos golpes seguidos pueden bastar. Con 4 infecciones en ~3 días, el sistema se comporta como estaba diseñado, no como se esperaba.

### 3.3 Falso negativo de medición — **media**

La tabla `diseases` no es un historial: es estado transitorio.

- PK `(player_uuid, disease_id)` (`V001__initial.sql` L64-71).
- La fila se borra al expirar (`DiseaseRepository.java` L95-104) y al curar (L72-92).
- El barrido corre, por defecto, cada 100 ticks = 5 s (`PluginConfigImpl.java` L129) y la purga SQL cada 12 barridos ≈ 60 s (`DiseaseExpiryTask.java` L21, L39-45). Al vencer, `procesarExpiraciones` manda a borrar la fila en el siguiente barrido (L434-439); la purga de 60 s es la red de seguridad para filas huérfanas.

Entonces, para un jugador **online**, una fila de laplace (45 s) vive como máximo ~50 s y una de dryne (180 s), ~185 s. Si el jugador se desconectó, la fila puede quedar hasta ~60 s más, hasta que la purga la borre.

La foto del **27/10 00:11** se tomó **23 h después** de la última aplicación (26/10 04:09Z = 01:09 local). La probabilidad de encontrar una fila era prácticamente cero. Que `diseases` tenga 0 filas **no significa que nunca se aplicó nada**.

A esto se suma el error clásico de SQLite: `mushoku.db` tenía mtime **25/10 23:51**, mientras que `mushoku.db-wal` tenía mtime **27/10 00:11**. Abrir solo el `.db` (sin `-wal`/`-shm`) devuelve el último checkpoint, no el estado vivo. Cualquier consulta hecha así es un falso negativo.

### 3.4 Hipótesis descartadas

| Hipótesis | Por qué se descarta |
|---|---|
| El módulo estaba apagado | `enabled: true` y `modules.diseases: true` (config L7/L17); log 25/10 23:52:07 "Modulo 'diseases' habilitado" |
| El catálogo no cargó | log "4 enfermedad(es), 5 tipo(s)"; `cargarCatalogo()` devuelve 4 (`DiseasesService.java` L108-131) |
| El YAML vivo estaba desincronizado | 1.604 B, SHA256 idéntico a la entrada del JAR |
| Había una excepción silenciosa | sin warnings de `DiseasesService` ni `IllegalStateException` en los logs 25–26/10; los stack traces de `purgarExpiradas` son del 18/10 y de un build anterior |
| Corría otro JAR | clases de enfermedades, listener y YAML byte-idénticos al JAR del 20/10 que contagió |
| "Nadie se enfermó nunca" | 4 `enfermedad.aplicar` naturales + chat de PAOLI1404 |

---

## 4. Prueba en vivo (< 1 minuto)

Objetivo: probar **las dos vías** —la tirada real por daño y la aplicación determinista— sin tocar producción más allá de la prueba.

### Condiciones exactas

- Dificultad **easy, normal o hard**. En `peaceful` no spawnean hostiles y la prueba no sirve.
- **Noche** (`/time set night`) o bajo techo: el husk se quema de día.
- Jugador en **survival**, en un lugar sin protección de región/Towny que cancele el daño. El listener es `ignoreCancelled = true` (`DiseaseInfectionListener.java` L41): si otro plugin cancela el evento, **no hay tirada**.
- Sin la enfermedad ya activa. **No cures justo antes**: curar dryne activa 300 s de inmunidad (`diseases.yml` L20).
- Sin `mt.alianza.orsted` (o aceptar 2 % en vez de 4 %).
- La armadura no cambia la tirada (solo evita que el jugador muera): la tirada depende de que el husk golpee, no del daño final.
- El jugador objetivo tiene que estar **online**: `/enfermedad` resuelve el nick con `Bukkit.getPlayerExact` (`EnfermedadCommand.java` L56).

### Paso 1 — tirada real por daño (~30 s)

```
/summon minecraft:husk ~ ~ ~5
/summon minecraft:husk ~ ~ ~5
/summon minecraft:husk ~ ~ ~5
```

Quedarse quieto en un hueco de 1×1 o contra una esquina para que los tres lleguen. 30 s.

**Resultado esperado (si contagia):**

```
[MushokuCore] Fiebre de Dryne: weakness, mining_fatigue durante 3 minutos.
```

(el texto sale de `messages.yml` L27 + `DiseasesService.textoEfectos` L591-597 y `textoDuracion` L600-608)

**Confirmación:**

- Iconos de **Weakness** y **Mining Fatigue** en el inventario, ~3:00.
- `/data get entity <nick> active_effects` → `minecraft:weakness` y `minecraft:mining_fatigue` con ~180 s (si la versión expone ese tag para jugadores).
- En la base: fila `enfermedad.aplicar` con `details = dryne fuente=HUSK_DAMAGE`.

**Probabilidad:** con 3 husks y 30 s, si quedan en rango son ~60–90 golpes → **91–97 %**. Si no contagia, **no prueba que esté roto**: queda el 3–9 %. Repetir o pasar al paso 2.

### Paso 2 — aplicación determinista (15 s)

```
/enfermedad aplicar <nick> laplace
```

**Resultado esperado:**

```
[MushokuCore] Fiebre de Laplace: darkness, nausea durante 45 segundos.
Enfermedad laplace aplicada a <nick>.
```

**Efectos:** Darkness + Nausea, 45 s.

**Limpieza:**

```
/enfermedad curar <nick> todas
```

→ `Enfermedades curadas de <nick>. (N enfermedad(es))`. La cura solo marca inmunidad a las enfermedades con `immunity-after-cure-seconds > 0` (dryne); laplace no tiene, así que no bloquea nada.

**Total: 30 + 15 = 45 s.**

El comando requiere `mushoku.admin.enfermedades` (`plugin.yml` L63-64; `EnfermedadCommand.java` L33, L47-50); la consola lo tiene.

### Consulta de confirmación

Sobre una **copia** de `plugins/MushokuCore/mushoku.db` **junto con `mushoku.db-wal` y `mushoku.db-shm`**, o con el servidor detenido:

```sql
SELECT occurred_at, action, details
FROM audit_log
WHERE action LIKE 'enfermedad.%'
ORDER BY id DESC
LIMIT 10;
```

Esquema: `audit_log(id, occurred_at, actor_uuid, action, target, details)` (`V001__initial.sql` L82-89). En `enfermedad.aplicar`, `target` es el UUID del jugador y `details` es `dryne fuente=HUSK_DAMAGE`. El `audit_log` sí es historial; la tabla `diseases`, no.

---

## 5. Fix propuesto (NO aplicado)

### 5.1 Cambio mínimo: mapear los mobs que faltan

**Archivo 1 — `DiseaseInfectionListener.java` L29-33**

```java
private static final Map<EntityType, String> FUENTES = Map.of(
        EntityType.HUSK, "HUSK_DAMAGE",
        EntityType.ZOMBIE, "ZOMBIE_DAMAGE",
        EntityType.DROWNED, "DROWNED_DAMAGE",
        EntityType.SPIDER, "SPIDER_DAMAGE",
        EntityType.CAVE_SPIDER, "CAVE_SPIDER_DAMAGE",
        EntityType.SILVERFISH, "SILVERFISH_DAMAGE",
        EntityType.ENDERMAN, "ENDERMAN_DAMAGE");
```

(`Map.of` admite hasta 10 pares; 7 no tienen problema.)

**Archivo 2 — `src/main/resources/diseases.yml`**

```yaml
dryne:
  sources:
    - HUSK_DAMAGE
    - ZOMBIE_DAMAGE
    - DROWNED_DAMAGE

miasma:
  sources:
    - CAVE_SPIDER_DAMAGE
    - SILVERFISH_DAMAGE
    - SPIDER_DAMAGE
```

**Archivo 3 — el YAML del data folder, obligatorio.** `DiseasesService.leerYaml()` prefiere `plugins/MushokuCore/diseases.yml` y solo cae al recurso del JAR si no existe (`DiseasesService.java` L133-137). El archivo vivo (1.604 B, SHA256 `DE68...`) hay que editarlo **también**; si solo se cambia el del JAR, el servidor sigue leyendo el viejo.

**Reinicio, no `/mushoku reload`.** `cargarCatalogo()` solo corre en `DiseasesModule.enable()` (`DiseasesModule.java` L54); `/mushoku reload` recarga únicamente `messages.yml` (`MushokuCommand.java` L181-206).

**Después del reinicio, confirmar en el log:**

```
diseases.yml: 4 enfermedad(es), 5 tipo(s) de efecto gestionado(s).
Modulo 'diseases' habilitado: 4 enfermedad(es), tarea cada <N> ticks.
```

(`<N>` es el valor de `diseases.tick-check-ticks`; por defecto 100, `PluginConfigImpl.java` L129.)

### 5.2 Decisión de balance que NO puedo tomar solo

Agregar `ZOMBIE` cambia el juego: el zombie es el mob hostil más común. Con 4 % por golpe, un jugador que recibe 10 golpes de zombie en una noche tiene **33,5 %** de contagiarse; con 30 golpes, **70,6 %** (calculado con la tabla de 3.2).

Opciones, para que elija el dueño:

| Opción | Efecto | Costo |
|---|---|---|
| A. Agregar zombie/drowned/spider y **bajar** la chance a 2,0–2,5 % | Más fuentes, misma presión total | Editar 3 líneas de `diseases.yml` |
| B. Agregar fuentes y **mantener** 4 % | Mucho más contagio; probablemente demasiado | Ninguno extra |
| C. Chance por fuente (mapa `fuente → %`) | Control fino (p. ej. zombie 2 %, husk 6 %) | Cambio de código + YAML |
| D. Dejar el catálogo como está y solo documentar | Cero riesgo; el sistema sigue siendo raro | Nada |

Recomendación: **A**, medir 24–48 h con el log de aplicación que se propone en 5.3, y recién después ajustar.

### 5.3 Instrumentación opcional (para no volver a medir a ciegas)

En `DiseasesService.intentarContagio`, después de `aplicada = true` (L330):

```java
log.info("Contagio natural: " + def.id() + " -> " + victima.getName()
        + " (" + fuente + ")");
```

Es una línea INFO por infección (no por golpe), en el hilo principal del evento. Permite contar contagios en `latest.log` sin abrir la base.

### 5.4 Validación del fix

1. Reiniciar y verificar la línea del catálogo (5.1).
2. Correr la prueba de la sección 4.
3. Revisar `audit_log` con el `-wal` incluido.
4. A las 24–48 h, contar `enfermedad.aplicar` por `details` y comparar con la tabla de probabilidades.

---

## 6. NO VERIFICADO

1. **JAR de producción vs HEAD `a50342e`.** No se compiló HEAD ni se comparó byte a byte; solo se comparó contra el JAR del 20/10. Que las clases de enfermedades coincidan no prueba que todo el JAR sea HEAD.
2. **Golpes reales por mob en producción.** No hay log de daño; la tabla de probabilidades es calculada, no medida.
3. **Poseedores de `mt.alianza.orsted`.** No se consultó LuckPerms. Si alguien lo tiene, su chance es la mitad.
4. **Cero estructuras en el mundo vivo.** Está documentado en `AGENTS.md` (a partir de `world_gen_settings.dat`), pero no se recorrió el mundo. Si hubiera minas o strongholds, miasma tendría fuente.
5. **Cancelación de `EntityDamageByEntityEvent` por plugins de región/Towny.** No se midió. Si cancelan, el listener no tira (`ignoreCancelled = true`).
6. **La consulta exacta del dueño.** No está registrada la hora ni el SQL que produjo el "0".
7. **Las 4 aplicaciones admin del 26/10 04:09Z.** No se transcribió en la evidencia disponible qué enfermedades fueron ni si eran una prueba.
8. **Jugadores Bedrock (Geyser).** No se probó si el evento de daño y la tirada se comportan igual en clientes Bedrock.
9. **Cura con leche.** No hay ninguna fila `enfermedad.curar` en la ventana retenida (24–27/10): el camino existe (`DiseasesService.curarConLeche` L342-376) pero no se ejercitó en esa ventana. `audit_log` archiva a CSV y borra a los 7 días (`config.yml` L145-149), así que una cura anterior al 24/10 no se descarta.
10. **Re-descarga de los archivos de producción.** No se hizo en este turno; los datos son de la copia del 27/10 00:11.

---

## 7. Anexo — Cronología y estado

| Momento | Hecho |
|---|---|
| 20/10 | JAR 200.688 B (`CC2140D5...`) que después produjo las 4 infecciones naturales |
| 24/10 00:36–02:56 (local) | 2 dryne por husk |
| 24/10 00:39 (local) | laplace a ItzKayn; 19 s después, muere por enderman |
| 25/10 16:43 (local) | laplace a PAOLI1404; 16:46:55, el jugador lo reporta en el chat |
| 25/10 23:12:25 | mtime del JAR actual (246.367 B, `9F813C...`) |
| 25/10 23:51:37 | arranque; uptime medido hasta 27/10 00:11:07 = **24 h 19 min** (no "más de dos días") |
| 25/10 23:52:07 | catálogo cargado: 4 enfermedades, 5 tipos de efecto |
| 26/10 01:09 (local) | 4 aplicaciones admin |
| 26/10 | 0 husk/enderman/cave spider/silverfish; muertes PvE: zombie 2, spider 2, skeleton 2; PvP 32 |
| 27/10 00:11 | foto: `diseases` 0 filas, `audit_log` 44, `players` 18; `.db` mtime 25/10 23:51, `-wal` 27/10 00:11 |

**Conclusión de una línea:** el módulo funciona, pero solo reacciona a 4 mobs; los más comunes no contagian, la tirada es del 4 % por golpe y la tabla de estado se vacía sola. La sensación de "nunca pasa nada" es el resultado de esas tres cosas juntas, no de un bug.

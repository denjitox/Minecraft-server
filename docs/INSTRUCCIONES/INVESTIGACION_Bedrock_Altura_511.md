# ¿Pueden los jugadores de Bedrock ver y usar el mundo hasta y=511?

**Investigación técnica — 2026-10-16**
Alcance: determinar si un cliente **Bedrock** conectado por **Geyser/Floodgate** a este
servidor (Paper 26.2 + mundo WorldPainter con `min_y=-64`, `height=576`, techo **y=511**)
puede ver y construir hasta y=511, o si el límite real es y=320.

**Método:** solo lectura. No se modificó el servidor, los datapacks, las configs ni el mundo.
Lo único escrito es este informe. Todo dato va marcado como **[VERIFICADO]** (leído en un
archivo o fuente, o medido) o **[INFERIDO]** (deducido, no observado directamente).

---

## 1. Veredicto

**Sí. Geyser 2.12.0 puede servir el overworld completo hasta y=511 y no hace falta ningún
pack de Bedrock.**

Geyser ya no tiene el techo de y=320: lo toma por defecto (es el overworld vanilla de
Bedrock), pero **si el dimension_type de Java declara una altura mayor, Geyser extiende el
overworld de Bedrock automáticamente** hasta el tope del protocolo, que es **y=512**
(es decir, último bloque colocable y=511). Nuestro mundo declara `min_y=-64` y `height=576`
→ `maxY = -64 + 576 = 512`, **justo en el tope**. Entra sin margen.

El pack de Bedrock **no es necesario** para Geyser. Geyser actúa como servidor Bedrock y
manda él mismo el `DimensionDataPacket` con los límites extendidos. **No hay ninguna opción
en `config.yml` de Geyser que activar: es automático.**

La afirmación de `minecraft_server\AGENTS.md` — *"Bedrock no soporta overworld por encima
de y=320"* — **era verdadera históricamente y hoy está obsoleta**. El soporte se agregó en
Geyser en **octubre de 2024** (PR #5002) y el build instalado lo tiene.

---

## 2. Dónde vive el límite de y=320

El 320 **no es un límite de Java ni del mundo**, y **no es un límite duro del cliente
Bedrock**. Es el **techo por defecto del overworld de Bedrock**, codificado como constante
en Geyser.

**[VERIFICADO — bytecode del jar instalado]** En
`org/geysermc/geyser/level/BedrockDimension.class` del jar que corre el servidor
(`servidor\plugins\Geyser-Spigot.jar`, 50,09 MB):

```
public static final org.geysermc.geyser.level.BedrockDimension OVERWORLD;
    ...
    8: bipush  -64          // minY
   10: sipush  384          // height
   18: putstatic OVERWORLD  // new BedrockDimension(-64, 384, true, OVERWORLD_ID)
```

`BedrockDimension(-64, 384, ...)` ⇒ `minY=-64`, `maxY=-64+384=**320**`. Es el **valor por
defecto**, no una imposición. En el mismo jar, `JavaDimension` y `GeyserSession` pueden
reemplazarlo (ver sección 4).

En Java, en cambio, no hay techo fijo: el `dimension_type` del datapack del mundo
(`worldpainter.zip → data/minecraft/dimension_type/overworld.json`) declara
`min_y: -64`, `height: 576`, `logical_height: 576`, `has_ceiling: false` **[VERIFICADO]**, y
Paper lo respeta —`setblock` funciona en y=300/400/500 y falla en 520 y 600 **[VERIFICADO,
registrado en AGENTS.md del proyecto]**—, con lo que el techo efectivo en Java es
`-64 + 576 - 1 = 511`.

**Resumen:** el 320 sólo existe del lado Bedrock, como valor por defecto de Geyser.

---

## 3. Qué admite Bedrock: `minecraft:dimension_bounds`

**[VERIFICADO — Microsoft Learn, página oficial "Data Driven Overworld Height and Void
Generation"]** Bedrock admite cambiar la altura del overworld con un **pack de
comportamiento** que incluya un `minecraft:dimension_bounds` con `min` y `max`:

```json
{
  "format_version": "1.18.0",
  "minecraft:dimension": {
    "description": { "identifier": "minecraft:overworld" },
    "components": {
      "minecraft:dimension_bounds": { "min": -64, "max": 512 },
      "minecraft:generation": { "generator_type": "void" }
    }
  }
}
```

Cita textual de la página: *"You can change the values for min and max to **any multiples of
16 between -512 and 512** that you like."*

Puntos clave de la fuente:
- Rango máximo del overworld en Bedrock: **-512 a 512**, en pasos de 16.
- El manejador es `minecraft:overworld`; **Nether y End sólo soportan generación de vacío,
  no altura personalizada**.
- El pack **no regenera** el mundo: recorta la vista a `min`/`max` y lo de afuera queda
  guardado pero invisible. Para un mundo importado (como el nuestro) eso es justamente lo
  que se quiere: hacer visible -64..512.

**Consecuencia:** aunque Bedrock *puede* definir la altura con un pack, **para Geyser no
hace falta**, porque Geyser lo hace por paquete de red. El pack sería el **plan B** sólo si
alguna vez se corriera un servidor Bedrock dedicado (BDS) en vez de Geyser.

**[VERIFICADO]** El archivo `plugins\Geyser-Spigot\packs\MT_Void_Bedrock.mcpack` que hay en
el servidor es un pack de **recursos** ("Mundo Vacío", niebla del End y texturas de
reliquias), **no** un pack de altura ni de comportamiento. No tiene nada que ver con esto.

---

## 4. Qué hace Geyser con y>320

**[VERIFICADO — bytecode del jar instalado]** El método `GeyserSession.connect()` del jar
instalado contiene la lógica completa de extensión. Desensamblado (resumido):

```
public void connect():
    minY = BedrockDimension.OVERWORLD.minY();      // -64
    maxY = BedrockDimension.OVERWORLD.maxY();      //  320
    reg  = javaRegistries.registry(DIMENSION_TYPE);
    for (JavaDimension d : reg)
        if (d.bedrockId() == OVERWORLD_ID) {       // bedrockId 0 = overworld
            minY = min(minY, d.minY());
            maxY = max(maxY, d.minY() + d.height());
        }
    minY = max(minY, -512);                        // clamp inferior
    maxY = min(maxY,  512);                        // clamp superior
    if (minY < OVERWORLD.minY() || maxY > OVERWORLD.maxY()) {   // -64 / 320
        bedrockOverworldDimension = new BedrockDimension(minY, maxY - minY, true, false);
        logger.debug("Extending overworld dimension to " + minY + " - " + maxY);
        pkt = new DimensionDataPacket();
        pkt.definitions.add(new DimensionDefinition(
            "minecraft:overworld", maxY, minY, 5, 3,
            GeyserIntegratedPackUtil.INTEGRATED_PACK_UUID, "", 192, true));
        upstream.sendPacket(pkt);
    }
    startGame();
```

**Aplicado a nuestro mundo** (`min_y=-64`, `height=576`) **[INFERIDO a partir del bytecode
verificado; el cálculo es determinista]**:
- `minY = min(-64, -64) = -64`
- `maxY = max(320, -64 + 576) = max(320, 512) = 512`
- clamp: `minY = max(-64, -512) = -64`; `maxY = min(512, 512) = 512`
- `512 > 320` ⇒ se entra al `if` ⇒ `new BedrockDimension(-64, 576, true, false)` y se manda
  el `DimensionDataPacket` con `minecraft:overworld` entre **-64 y 512**.

Es decir: Geyser le dice al cliente Bedrock que el overworld va de **-64 a 512**, y el
cliente renderiza/acepta bloques hasta **y=511**. La cadena `"Extending overworld dimension"`
está presente en **exactamente 1 clase del jar instalado** (`GeyserSession`) **[VERIFICADO
por escaneo de strings del jar]**.

Complementos verificados en el código de Geyser (fuente master, coincidente con el
comportamiento del bytecode):
- `JavaDimension.read()` **lanza excepción** si `min_y` o `max_y` no son múltiplos de 16.
  Los nuestros (-64, 512) sí lo son **[VERIFICADO en fuente master]**.
- `JavaLevelChunkWithLightTranslator` dimensiona el arreglo de secciones del chunk Bedrock
  con `maxBedrockSectionY = (bedrockDimension.height() >> 4) - 1`, para que la altura
  extendida viaje bien **[VERIFICADO en fuente master]**.
- `ChunkUtils.loadDimension` **advierte** si `minY < bedrockDimension.minY()` o
  `maxY > bedrockDimension.maxY()` **[VERIFICADO en fuente master]**.

**Historia (por qué AGENTS.md decía lo contrario):** antes de **PR #5002** ("Add support for
extended world height", mergeado **2024-10-03**), Geyser ignoraba alturas de Java mayores a
320 y los bloques de arriba quedaban invisibles. PR #5002 introdujo exactamente este código
(*"min Y limit of -512 and a max Y limit of 512"*). **PR #5492** ("Fix issues with custom
dimension heights", mergeado **2025-04-17**) corrigió dos bugs: interpretaba `height` de Java
como `maxY`, y calculaba mal el tamaño del arreglo de secciones.

**[VERIFICADO — log del servidor]** El servidor instalado corre:
```
[Geyser-Spigot] Loading Geyser version 2.12.0-b1256 (git-master-fc2170c)
```
y el jar local contiene la lógica extendida (bytecode arriba). O sea: **el build instalado es
posterior a ambos PRs y sí extiende la altura.** En `latest.log` **no aparece** la línea
"Extending overworld dimension" porque `debug-mode: false` y porque en esa corrida no se
conectó un cliente Bedrock a un mundo ya exportado **[VERIFICADO]**.

---

## 5. Medición del terreno por encima de y=320

Fuente: `MT_22000_WorldPainter_MINIMO\relieve_2750_16bit.png` — imagen I;16 de **2750×2750 px**,
**8 bloques/px**, que cubre los **22.000×22.000** bloques del mapa, con origen en el bloque
**(-11000, -11000)** (confirmado por `crear_mundo.js`: `.scale(800).shift(-11000,-11000)`,
*"Compact map equivalent to a 22k map at 800% scale"*) **[VERIFICADO]**.

**Mapeo altura↔valor** (de `crear_mundo.js`: `.fromLevels(0,65535).toLevels(-64,511)`):
`altura = -64 + valor × 575 / 65535`.

**[VERIFICADO — validación cruzada]** Se decodificó la superficie real de los chunks
exportados (81 regiones, 1.327.104 columnas muestreadas) y se comparó columna a columna
contra el PNG con este mapeo: **correlación 0,9998, sesgo 0,12 bloques, error absoluto medio
0,42 bloques**, en el offset esperado (dx=dz=0). El PNG reproduce la altura real del terreno.
(La muestra de chunks por sí sola no sirve para el máximo: son **81 regiones cada 5 tiles, y
dentro de cada una sólo el bloque de 8×8 chunks en (16..23, 16..23)** —una grilla dispersa—,
por eso su máximo local era 265 y no el máximo real.)

### Números del mapa completo

| Umbral | Píxeles | % del mapa | Área (bloques²) |
|---|---:|---:|---:|
| > y=256 | 88.321 | 1,1679 % | 5.652.544 |
| > y=300 | 34.809 | 0,4603 % | 2.227.776 |
| **> y=320** | **20.246** | **0,2677 %** | **1.295.744** |
| > y=340 | 11.988 | 0,1585 % | 767.232 |
| > y=384 | 2.901 | 0,0384 % | 185.664 |
| > y=448 | 0 | 0,0000 % | 0 |

- **Altura máxima del mapa: y=445**, en el bloque **x=-3840, z=-5904** (coincide con
  `parametros.json`: `height_range_actual: [14.953, 445.0]`) **[VERIFICADO]**.
- **Altura mínima: y=15** (el agua está en y=63).
- Percentiles de la **tierra seca** (>63): p50=88,9 · p90=135,7 · p99=**295,3** ·
  **p99,5=321,1** · p99,9=374,0 · máx=445.
- Sobre y=320 hay **0,27 % del área total**, que es **≈0,51 % de la superficie de tierra**
  (la tierra es 52,87 % del mapa, medido sobre los chunks en el AGENTS.md del proyecto).
- No es una meseta continua: son **36 manchas conectadas** por encima de 320; la mayor mide
  3.754 px (**0,24 km²**, bbox de bloques x −4336..−3616, z −6376..−5568, más de 400 bloques
  de lado) y concentra el 18,5 % del área >320. El resto son picos y macizos aislados.
- Reparto por cuadrante del mapa: NO **0,52 %**, NE 0,32 %, SO 0,17 %, SE 0,06 %.

**Interpretación:** lo que quedaría invisible en Bedrock si el límite fuera 320 son unas pocas
decenas de macizos aislados en el norte del mapa, con picos hasta 445. No es una fracción
grande del terreno, pero **hay picos de más de 400 bloques que sí se notarían** (y el spawn
está cerca: x −3576, z −5880, dentro de la zona alta del norte).

---

## 6. Diseño propuesto

**Acción principal: ninguna.** Con Geyser 2.12.0 el overworld se extiende solo hasta y=511.

Lo único que hay que **garantizar** (son precondiciones, no cambios):

1. **[VERIFICADO]** El datapack de altura debe estar **activo y aceptado** por Paper:
   `world/datapacks/worldpainter.zip` con `data/minecraft/dimension_type/overworld.json`
   (`min_y=-64`, `height=576`). Está presente en todos los exports medidos y Java lo toma
   (`setblock` a 500 OK). Si el datapack no estuviera activo, Java mismo se limitaría a 320
   y no existiría terreno arriba: el problema no se plantearía.
2. **[VERIFICADO]** `maxY = min_y + height = 512 ≤ 512`: **entra justo**. No hay margen para
   subir la altura del mundo: cualquier `maxY > 512` **no es representable en Bedrock**
   (Geyser lo recortaría a 512 y lo de arriba quedaría invisible).
3. **[VERIFICADO]** `min_y` y `height` deben ser múltiplos de 16 (Geyser lanza excepción si
   no). -64 y 576 lo son.

**Qué NO hay que hacer:**
- **No** agregar ningún pack de Bedrock `dimension_bounds`. Geyser manda el
  `DimensionDataPacket` por red; un pack sería redundante.
- **No** buscar opciones de altura en `plugins/Geyser-Spigot/config.yml`: no existen
  (verificado; la única clave cercana es `nether-roof-workaround: false`, que es del Nether).
- **No** tocar el mundo ni el datapack.

**Plan B (sólo si algún día se corriera un BDS real en vez de Geyser):** ahí sí haría falta el
pack de comportamiento de la sección 3, con `min: -64`, `max: 512` (múltiplos de 16 ✓), en
`dimensions/overworld.json` + `manifest.json`. Mismo archivo ya citado.

---

## 7. Plan de prueba

1. Arrancar el servidor con el mundo de prueba ya exportado (`expB\relieve_recorte` o el
   mundo final) y el datapack de altura activo.
2. **Activar el log de debug de Geyser**: en `plugins/Geyser-Spigot/config.yml`, `debug-mode:
   false` (línea 336) → ponerlo en `true` y reiniciar.
3. Conectar un **cliente Bedrock real** (teléfono/Win10) por Geyser.
4. En la consola/log buscar exactamente:
   ```
   [Geyser-Spigot] Extending overworld dimension to -64 - 512
   ```
   Si aparece esa línea → Geyser extendió el overworld a -64..512. **[Ésta es la prueba
   directa.]**
5. En el cliente Bedrock, teletransportarse a un pico alto (p. ej. `/tp` a x=-3840, z=-5904)
   y verificar que:
   - el terreno por encima de y=320 **se ve** (no está cortado a 320);
   - se **puede colocar** un bloque por encima de 320 (p. ej. `/setblock ~ 330 ~ stone`) y
     **falla** por encima de 511.
6. Como control cruzado en Java (ya verificado antes): `setblock` funciona a 300/400/500 y
   falla a 520/600.
7. Si la línea del paso 4 **no aparece**, revisar (a) que el datapack de altura esté activo,
   (b) que Geyser haya leído el registro `minecraft:dimension_type` al conectar.

---

## 8. Riesgos y límites

1. **Tope duro de Bedrock en y=512.** El mundo ya está pegado al máximo (maxY=512). Si en el
   futuro se sube `height` por encima de 576 (con `min_y=-64`), **Bedrock no lo va a poder
   mostrar**: Geyser recorta a 512 y lo de arriba queda invisible. Cualquier plan de altura
   futura debe quedarse en ≤512. **[VERIFICADO en la fuente de Bedrock y en el bytecode]**
2. **Depende del datapack activo.** La extensión de Geyser se dispara sólo si el
   `dimension_type` de Java declara la altura. Si el datapack se cae (por ejemplo al
   renombrar/copiar el mundo y perder `world/datapacks/`), Java vuelve a 320 y el terreno
   alto deja de existir. Verificar que el datapack viaje con el mundo en el deploy por SFTP.
3. **Interacción no probada con Paper 26.2 (versión muy nueva).** La extensión de altura de
   Geyser se diseñó y probó contra versiones de Java anteriores; acá Java es 26.2 y Geyser
   necesita **ViaVersion** (5.12.2-SNAPSHOT) para puentear. La combinación *altura extendida
   + Paper 26.2 + ViaVersion* **no está verificada en este servidor**: hay que hacer el paso 4
   del plan de prueba. **[INFERIDO / no observado]**
4. **Clientes Bedrock viejos o de plataformas raras.** La feature depende de que el cliente
   soporte `dimension_bounds`. Bedrock 1.18+ en general lo hace, pero en consolas/versiones
   viejas podría haber glitches de renderizado en los extremos del rango. **[INFERIDO]**
5. **Timing del registro de dimensiones.** `connect()` lee el registro `DIMENSION_TYPE` al
   conectar; si por algún motivo el registro no estuviera poblado todavía, la extensión no se
   aplicaría. Se detecta con el paso 4 del plan de prueba.
6. **Artefactos en los bordes del rango.** Alturas personalizadas tuvieron bugs de secciones
   (corregidos en PR #5492, ya presente). Riesgo bajo en 2.12.0, pero conviene mirar los
   extremos (y≈511 y y≈-64) en el cliente Bedrock.

---

## 9. Fuentes

**Documentación y código de Geyser (verificados hoy):**
- Geyser — *Current Limitations*: <https://geysermc.org/wiki/geyser/current-limitations/>
  (cita: *"Heights lower than -512 or higher than 512 in the overworld … and heights lower
  than 0 or higher than 256 in other dimensions"*).
- Issue #3804 (*High and low coordinates break the game*): <https://github.com/GeyserMC/Geyser/issues/3804>
- PR #5002 (*Add support for extended world height*, merged 2024-10-03, commit `26c4c52`):
  <https://github.com/GeyserMC/Geyser/pull/5002>
- PR #5492 (*Fix issues with custom dimension heights*, merged 2025-04-17, commit `efd4dcc`):
  <https://github.com/GeyserMC/Geyser/pull/5492>
- `BedrockDimension.java` (master): <https://github.com/GeyserMC/Geyser/blob/master/core/src/main/java/org/geysermc/geyser/level/BedrockDimension.java>
- `GeyserSession.java` (master): <https://github.com/GeyserMC/Geyser/blob/master/core/src/main/java/org/geysermc/geyser/session/GeyserSession.java>

**Documentación de Bedrock (verificada hoy):**
- Microsoft Learn — *Data Driven Overworld Height and Void Generation*:
  <https://learn.microsoft.com/en-us/minecraft/creator/documents/datadrivenoverworldheight>
  (`minecraft:dimension_bounds`, *"any multiples of 16 between -512 and 512"*, Nether/End sin
  altura personalizada).

**Archivos locales del proyecto (verificados):**
- `servidor\plugins\Geyser-Spigot.jar` (50,09 MB) — bytecode de `BedrockDimension.class` y
  `GeyserSession.class`; string `"Extending overworld dimension"` presente en `GeyserSession`.
- `servidor\plugins\Geyser-Spigot\config.yml` — sin opción de altura; `debug-mode: false`
  (línea 336); `nether-roof-workaround: false` (línea 143).
- `servidor\logs\latest.log` — `Loading Geyser version 2.12.0-b1256 (git-master-fc2170c)`;
  Floodgate 2.2.5-SNAPSHOT; ViaVersion 5.12.2-SNAPSHOT.
- `mundo\prueba\expB\relieve_recorte\datapacks\worldpainter.zip` →
  `data/minecraft/dimension_type/overworld.json` (`min_y: -64`, `height: 576`).
- `MT_22000_WorldPainter_MINIMO\crear_mundo.js`, `parametros.json`,
  `relieve_2750_16bit.png` (medición de altura y validación contra chunks).
- `mundo\prueba\backups\relieve_2750_16bit.20261016225159\dimensions\minecraft\overworld\region\*.mca`
  (81 regiones, 1.327.104 columnas) — validación del mapeo PNG↔altura real.

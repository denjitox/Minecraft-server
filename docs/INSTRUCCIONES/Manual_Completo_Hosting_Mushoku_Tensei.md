# Manual completo de instalación — Servidor Mushoku Tensei

> Documento maestro para montar el proyecto en el hosting. Reúne las decisiones tomadas, el orden de instalación, las verificaciones y el procedimiento de recuperación. No reemplaces configuraciones enteras a ciegas: conserva siempre las claves que genere la versión instalada de Paper o de cada plugin.

## 1. Resumen del proyecto

El objetivo es un servidor **survival vanilla con plugins**, inspirado en *Mushoku Tensei*, accesible desde Minecraft Java y Bedrock sin que los jugadores tengan que instalar mods.

| Elemento | Decisión actual |
| --- | --- |
| Hosting | LodingHosting Brasil, plan Ouro |
| Recursos | 12 GB RAM, 160 % de CPU Ryzen 9 7950X, 80 GB NVMe |
| Comunidad inicial | 14 jugadores de Latinoamérica y unos 4 de España |
| Límite inicial recomendado | 24 jugadores |
| Software | Paper 26.2 o la revisión estable compatible que ofrezca el panel |
| Crossplay | Geyser-Spigot + Floodgate-Spigot |
| Mundo principal | WorldPainter, 22.000 × 22.000 bloques |
| Área jugable | Borde de 21.800 bloques de diámetro, centrado en 0,0 |
| Estilo | Geografía inspirada en *Mushoku Tensei*, sin mods obligatorios |
| Superficie | Sin estructuras vanilla generadas al azar |
| Subsuelo deseado | Cuevas (requieren capa pintada + post-proceso `CAVES`), minerales (se generan solos), minas, Trial Chambers, Ancient Cities/Warden y strongholds (estos hay que colocarlos a mano) |
| Dificultad | Difícil, reforzada posteriormente con plugins y eventos |
| Nether y End | Bloqueados durante la primera semana; apertura mediante evento |
| End | Mecánicas vanilla y ambientación de cielo blanco/niebla gris |
| Fortaleza de Perugius | Se añadirá posteriormente mediante `.schem` |

La prioridad del primer lanzamiento es estabilidad. Facciones, enfermedades, reliquias, Siete Potencias, NPC y estructuras especiales deben probarse en etapas, sin impedir que Java y Bedrock puedan jugar juntos.

## 2. Archivos preparados

### Paquete de servidor

Archivo principal:

```text
MT_Server_Base_26.2_Geyser.zip
```

Incluye configuraciones, documentación, datapack y dos resource packs. **No incluye los JAR de los plugins**; deben instalarse desde el panel o sus fuentes oficiales para usar versiones compatibles con el build exacto de Paper.

Componentes importantes:

```text
MT_Server_Base_26.2/
├── configuracion/
├── datapacks/mt_core_26.2.zip
├── plugins/
├── resourcepacks/MT_Void_Java.zip
├── resourcepacks/MT_Void_Bedrock.mcpack
├── comandos/
└── documentación adicional
```

### Kit de WorldPainter

Usar únicamente:

```text
MT_22000_WorldPainter_MINIMO.zip
```

No usar `MT_22000_WorldPainter.zip`: la copia anterior quedó incompleta/corrupta.

Dentro del kit, ejecutar `crear_mundo.js` desde WorldPainter mediante **Tools > Run script**. Como ruta de salida puede utilizarse:

```text
C:/MT_22000_WorldPainter_MINIMO
```

El script genera el proyecto editable:

```text
MT_22000.world
```

El archivo `.world` es para editar en WorldPainter. **No es el mundo que se sube al servidor.** Primero hay que abrirlo y exportarlo como mapa de Minecraft.

### Fortaleza de Perugius

El mundo fuente actual es:

```text
CastleWorld.zip
```

El resultado que se colocará en el mundo definitivo debe ser, idealmente:

```text
fortaleza_perugius_v1.schem
```

La extracción, limpieza y exportación se realiza por separado siguiendo `Requerimientos_Exportacion_Fortaleza_Perugius.md`.

## 3. Estado actual y tareas pendientes

| Área | Estado |
| --- | --- |
| Hosting | Contratado |
| Plugins principales | Mayormente configurados por los administradores |
| Geyser/Floodgate | Debe terminarse y probarse desde Bedrock |
| Proyecto de WorldPainter | Preparado para generar y revisar localmente |
| Exportación final del mundo | **Completada (2026-10-17)**: mundo `world`, 1.936 regiones (11,49 GB) |
| Subida del mundo | Pendiente |
| World border | Pendiente |
| Estructuras subterráneas | **Actualizado 2026-10-22:** trial chambers secundarias **SC1–SC10 colocadas** (reportado por el usuario; no verificable desde este repo). **Falta la arena de Hitogami** en `(-2304, -16, -3008)` y el portal al End detrás de ella; el `.schem` de la arena sigue **sin decidir**. Ver `INSTRUCCIONES\TRIAL_CHAMBERS_COORDENADAS.md` |
| Bloqueo de Nether y End | Pendiente de prueba completa |
| Resource packs Java/Bedrock | Preparados, pendientes de instalación y prueba |
| Facciones y Siete Potencias | Configuración base preparada; falta validación |
| Fortaleza de Perugius | Fase posterior al mundo base (ya exportado). Punto reservado: X=−916, Z=6316, Y₀=150 |
| Apertura pública | Pendiente de prueba de carga y backup restaurable |

## 4. Orden correcto de instalación

El orden recomendado es:

1. Inventariar y respaldar lo que ya existe en el hosting.
2. Confirmar Paper y la versión de Java requerida por ese build.
3. Comprobar Geyser y Floodgate con un mundo temporal.
4. Exportar y validar el mundo de WorldPainter localmente.
5. Subir el mundo y hacer que Paper lo cargue.
6. Instalar el datapack y configurar el borde.
7. Verificar estructuras, dimensiones y superficie.
8. Instalar/probar los packs de recursos.
9. Activar plugins de jugabilidad en grupos pequeños.
10. Realizar una prueba de carga Java + Bedrock.
11. Crear un backup restaurable y recién entonces abrir el servidor.

No conviene instalar o actualizar todo y abrir inmediatamente: si aparece un error, será difícil identificar qué componente lo introdujo.

## 5. Fase 0 — Inventario y copias de seguridad

1. Detener completamente el servidor desde el panel.
2. Descargar o crear un backup completo de:
   - mundo actual;
   - `server.properties`;
   - `bukkit.yml`, `spigot.yml` y configuraciones de Paper;
   - carpeta `plugins/`;
   - bases de datos de Towny, LuckPerms, CoreProtect y otros plugins;
   - claves y configuración de Floodgate.
3. Guardar una lista de todos los JAR instalados y sus versiones.
4. Confirmar cuánto espacio libre queda en los 80 GB NVMe.
5. Mantener al menos tres puntos de restauración:
   - antes de subir el mapa;
   - con el mapa cargado y verificado;
   - inmediatamente antes de la apertura pública.

No borrar el mundo generado por defecto. Renombrarlo, por ejemplo, como `world_respaldo_pre_mushoku` hasta confirmar que el nuevo mundo funciona.

## 6. Fase 1 — Paper y configuración base

### 6.1 Software

Seleccionar en el panel:

- Paper para la versión objetivo del servidor.
- La versión de Java que Paper indique para ese build.
- Inicio mediante el JAR y comando administrados por el panel.

Si el panel todavía no ofrece exactamente 26.2, no forzar un JAR incompatible. Usar la revisión estable compatible y revisar datapack, plugins y resource packs antes de actualizar.

### 6.2 Primer inicio controlado

1. Iniciar una vez con el mundo temporal para que Paper genere todos sus archivos.
2. Esperar el mensaje de servidor listo.
3. Detenerlo con el botón de parada o el comando `stop`.
4. No usar `/reload`; para cambios de plugins, datapacks o Geyser, reiniciar limpiamente.

### 6.3 Valores recomendados de `server.properties`

Fusionar estos valores con el archivo generado por el hosting:

```properties
level-name=world
max-players=24
view-distance=8
simulation-distance=6
difficulty=hard
hardcore=false
gamemode=survival
pvp=true
white-list=true
enforce-whitelist=true
spawn-protection=16
player-idle-timeout=15
generate-structures=false
motd=Servidor Mushoku — Java y Bedrock
require-resource-pack=false
```

Consideraciones importantes:

- No cambiar `online-mode` a ciegas. Si Geyser/Floodgate ya funciona, conservar el valor y la instalación recomendada por la versión actual del plugin.
- `generate-structures=false` **no se aplica a un mundo importado**: `world_gen_settings.dat` queda con `generate_structures: 1`, generador `minecraft:noise`, seed 27594263. En la práctica el mundo igual queda con cero estructuras (WorldPainter no las escribe), así que minas, Trial Chambers, Ancient Cities y strongholds hay que colocarlas a mano.
- El límite de 24 jugadores permite margen para eventos, entidades y conexiones Bedrock. Se puede ampliar después de medir TPS/MSPT.
- Durante la instalación, mantener la whitelist activa.

## 7. Fase 2 — Geyser y Floodgate

Instalar las últimas versiones compatibles de:

- Geyser-Spigot.
- Floodgate-Spigot.

Procedimiento:

1. Instalar ambos JAR con el servidor detenido.
2. Iniciar una vez para generar sus carpetas.
3. Detener nuevamente.
4. En la configuración de Geyser, colocar el puerto UDP Bedrock asignado por el hosting.
5. Confirmar que ese puerto sea UDP y no esté ocupado por Query, voice chat u otro servicio.
6. Configurar la autenticación de Geyser para Floodgate siguiendo el esquema generado por la versión instalada.
7. No compartir ni publicar la clave privada de Floodgate.
8. Iniciar y probar una cuenta Java y una Bedrock.

Verificaciones mínimas:

- Java entra por la dirección/puerto Java del hosting.
- Bedrock entra por la dirección y el puerto UDP asignado.
- El jugador Bedrock conserva UUID, inventario y ubicación al reconectar.
- Los comandos básicos, chat, inventario y movimiento funcionan.
- No hay errores de Floodgate en `logs/latest.log`.

Si Bedrock no conecta, revisar primero el puerto UDP y el firewall/regla del panel; reinstalar plugins rara vez soluciona un puerto incorrecto.

## 8. Fase 3 — Generar y exportar el mundo

### 8.1 Generación local

1. Descomprimir `MT_22000_WorldPainter_MINIMO.zip` en la PC.
2. Abrir WorldPainter.
3. Ejecutar `crear_mundo.js` desde **Tools > Run script**.
4. Elegir como salida una carpeta simple y con espacio suficiente.
5. Abrir `MT_22000.world`.
6. Revisar continentes, costas, spawn, biomas, alturas y límites.
7. Pintar las capas que el proyecto **no** trae: **cuevas** (`Caves`/`Caverns`/`Chasms`) y **árboles** (`Deciduous`, `Pine`, `Jungle`, `Swamp`). El `.world` del kit viene pelado: `isPopulate()=false`, sin cuevas y sin vegetación. `populate=true` solo da cobertura baja (`grass`, `tall_grass`, `fern`, `large_fern`, `sunflower`), **nunca árboles**. Los nombres `Deciduous Forest`/`Pine Forest` no existen; los reales son `Deciduous`/`Pine`. Las capas se aplican con filtros (`onlyOnLand`, `onlyOnBiome(id)`, …); un filtro admite una sola condición.

El proyecto trabaja con un mapa de 22.000 × 22.000 (29.584 tiles, 1.891.625 chunks). Medido sobre el mundo real: **~340 chunks/s** en zonas de 1024 tiles (211 s por 65.536 chunks), así que el export completo tarda **~2–2,5 h**. Ojo: **el export borra el contenido del directorio destino**, por lo que hay que usar un directorio por tanda y fusionar las regiones después (`mundo\exportar_por_zonas.ps1`, reanudable, con control de colisiones). El directorio destino debe existir antes de lanzarlo.

### 8.2 Decisiones de exportación

- Nombre técnico del mundo: `world`.
- Centro: X 0, Z 0.
- Tamaño total: 22.000 × 22.000.
- Área jugable final: 21.800 de diámetro.
- Superficie: sin aldeas, templos, puestos, mansiones ni estructuras aleatorias visibles.
- Altura: techo **y=511** confirmado con un datapack sonda sobre Paper 26.2 (`min_y=-64`, `height=576`; `setblock` funciona en y=300/400/500 y falla en 520 y 600). Bedrock también llega a y=511: Geyser 2.12.0 extiende solo el overworld de Bedrock a `-64..512` cuando el `dimension_type` de Java supera y=320 (verificado en el bytecode del jar; falta el test con cliente Bedrock real — ver `INVESTIGACION_Bedrock_Altura_511.md`).

### 8.3 Cuevas, minerales y estructuras subterráneas

WorldPainter **no genera estructuras** (minas, Trial Chambers, Ancient Cities, strongholds): `generate-structures` no se aplica al mundo importado y el post-proceso no las escribe. Un `/locate structure` sobre el mundo exportado no encuentra ninguna. Para conseguirlas hay tres opciones:

1. colocar estructuras mediante schematics;
2. reservar regiones vanilla controladas;
3. usar un datapack específicamente probado para esa versión.

Las **cuevas** tampoco aparecen solas: solo se generan donde hay una capa de cuevas pintada (`Caves`, `Caverns`, `Chasms`) y luego corre el post-proceso `CAVES`. Los **minerales sí son automáticos**: WorldPainter trata `Resources` como capa mínima y el post-proceso `RESOURCES` corre siempre (medido: ~1,85 % del subsuelo con coal, iron, copper, gold, redstone, lapis, diamond, emerald y las variantes `deepslate_*`).

Las etapas del export son `CAVES`, `RESOURCES`, `LIGHTING` y `LEAVES`; se pueden saltear. Los identificadores de `/locate` pueden variar en la versión instalada; usar el autocompletado del servidor si un comando no coincide.

### 8.4 Resultado exportado

WorldPainter nombra la carpeta exportada con el nombre del heightmap (`relieve_2750_16bit`); hay que **renombrarla a `world`** antes de subirla.

La carpeta que se subirá debe contener directamente:

```text
world/
├── level.dat
├── region/
├── entities/
├── poi/
├── data/
└── datapacks/     (si ya se añadió)
```

Evitar el error de doble carpeta:

```text
world/world/level.dat
```

Antes de subir, medir el tamaño descomprimido. Peso medido del mundo completo: **10,1–13,7 GB** (5,62 KB/chunk sobre 1.891.625 chunks). En los 80 GB deben caber simultáneamente el mundo, plugins, bases de datos, logs y al menos una copia de seguridad. Como criterio operativo, intentar conservar 15–20 GB libres.

## 9. Fase 4 — Subir y activar el mundo

1. Con el servidor detenido, comprimir la carpeta exportada en ZIP.
2. Subir el archivo preferentemente mediante SFTP/WinSCP; el gestor web puede fallar con archivos grandes.
3. Extraer el ZIP en la raíz del servidor.
4. Confirmar que `world/level.dat` existe directamente.
5. Dejar el mundo anterior renombrado como respaldo.
6. Configurar:

```properties
level-name=world
```

7. Iniciar el servidor con whitelist.
8. Leer `logs/latest.log` desde el comienzo, buscando errores de conversión, región, altura o datapack.
9. Confirmar que Paper cargó `world` y no creó una carpeta vacía con otro nombre.
10. Entrar como administrador y verificar spawn, coordenadas y terreno.

No ejecutar Chunky sobre todo el mundo si WorldPainter ya exportó todos los chunks: podría generar o alterar zonas con terreno que no corresponde al diseño. Solo debe usarse en márgenes realmente no generados y después de una prueba separada.

## 10. Fase 5 — Datapack y world border

Copiar el datapack a:

```text
world/datapacks/mt_core_26.2.zip
```

Iniciar y comprobar:

```text
datapack list
```

### Corrección obligatoria antes de ejecutar `mt:setup`

La función preparada originalmente establece un borde de 22.000. El criterio final es **21.800** para dejar 100 bloques de margen por lado. Por lo tanto, se puede:

- corregir `data/mt/function/setup.mcfunction` antes de subir el datapack; o
- ejecutar `function mt:setup` y aplicar inmediatamente los comandos definitivos siguientes.

Configuración final:

```mcfunction
/worldborder center 0 0
/worldborder set 21800
/worldborder damage buffer 5
/worldborder damage amount 1
/worldborder warning distance 20
/worldborder warning time 15
```

Luego verificar:

```text
worldborder get
difficulty
```

El número usado por `/worldborder set` es el diámetro, no el radio. Si el centro real del mapa exportado no coincide con 0,0, corregirlo antes de abrir.

## 11. Fase 6 — Bloqueo inicial de Nether y End

Durante la primera semana:

```properties
allow-nether=false
```

Y en `bukkit.yml`:

```yaml
settings:
  allow-end: false
```

Además, revisar plugins de teleportación, homes, RTP, portales y comandos para que no permitan el acceso indirecto. Los administradores pueden preparar dimensiones en una copia de ensayo; no deben depender de un bypass no comprobado en producción.

Para el día 1, con el datapack activo:

```text
function mt:setup
function mt:stage_launch
whitelist on
save-all flush
```

Después de `mt:setup`, volver a aplicar o comprobar el borde de 21.800 debido al valor antiguo incluido en la función.

### Apertura del día 8

1. Avisar a los jugadores.
2. Ejecutar `save-all flush`.
3. Crear un backup completo.
4. Detener el servidor.
5. Cambiar `allow-nether=true`.
6. Cambiar `settings.allow-end: true` en `bukkit.yml`.
7. Iniciar y ejecutar:

```text
function mt:stage_dimensions_open
save-all flush
```

8. Probar portales y retorno desde Java y Bedrock.

No borrar ni regenerar `world_nether` o `world_the_end` después de que empiece la progresión.

## 12. Fase 7 — Resource packs

### Java

El pack preparado es:

```text
MT_Void_Java.zip
```

1. Publicarlo en una URL HTTPS de descarga directa.
2. Calcular el SHA-1 del archivo final. Si se modifica o recomprime, el hash cambia.
3. Completar en `server.properties`:

```properties
resource-pack=https://URL-DIRECTA/MT_Void_Java.zip
resource-pack-sha1=HASH_SHA1_REAL
require-resource-pack=false
```

Mantenerlo opcional durante las primeras pruebas. Solo hacerlo obligatorio cuando la descarga funcione de forma consistente.

### Bedrock mediante Geyser

El pack preparado es:

```text
MT_Void_Bedrock.mcpack
```

Copiarlo, según la estructura generada por el build instalado, normalmente a:

```text
plugins/Geyser-Spigot/packs/
```

Reiniciar completamente y aceptar la descarga desde un cliente Bedrock. Comprobar el cielo/niebla del End en ambas ediciones: el renderizado no es idéntico y una actualización de Bedrock puede requerir adaptar el pack.

Si el pack falla, se desactiva temporalmente la parte visual; nunca debe bloquearse o perderse la progresión de los jugadores por una textura.

## 13. Fase 8 — Plugins y sistemas del servidor

### Núcleo recomendado

- Geyser-Spigot y Floodgate-Spigot: acceso Bedrock.
- LuckPerms: permisos y grupos.
- Towny + TownyChat: pueblos/facciones de jugadores y dos naciones fijas.
- CoreProtect: auditoría y rollback de bloques.
- InventoryRollbackPlus: recuperación de inventarios.
- spark: diagnóstico de rendimiento.
- FastAsyncWorldEdit y WorldGuard: colocación y protección de estructuras.
- PlaceholderAPI, TAB, DecentHolograms: títulos, prefijos y presentación.
- Skript: implementación inicial de Potencias y enfermedades.
- LevelledMobs: dificultad adicional controlada.
- ajLeaderboards: rankings secundarios.
- Plugin de duelos compatible: ranking competitivo si se mantiene esa mecánica.

No instalar dos sistemas de claims en paralelo. Instalar los plugins de a uno o por grupos pequeños, reiniciar y leer el log después de cada grupo.

### Facciones

- Los jugadores pueden crear pueblos que funcionan como facciones.
- **Orsted Corp** y **Apóstoles del Dios Humano** son dos naciones fijas administradas por el servidor.
- LuckPerms gestiona rangos y beneficios.
- Towny gestiona territorio, invitaciones, guerras o alianzas.
- Los beneficios no deben dar una ventaja imposible de compensar; conviene usar identidad, acceso, misiones, descuentos o utilidades moderadas.

### Siete Potencias Mundiales

Asignación prevista de los tres primeros puestos:

| Puesto | Condición inicial |
| --- | --- |
| 1 | Primer jugador que derrote al Warden bajo las reglas del evento |
| 2 | Primer jugador que derrote al Ender Dragon |
| 3 | Primer jugador que derrote al Wither |

Otros títulos pueden basarse en estadísticas, por ejemplo **Dios de la Muerte** para quien tenga más bajas válidas. Se deben definir reglas contra boosting, cuentas alternativas y muertes pactadas antes de activar recompensas.

### Enfermedades y reliquias

- Las enfermedades usan efectos vanilla para conservar compatibilidad Bedrock.
- Las reliquias de Trial Chambers deben conservar un objeto vanilla subyacente, nombre y lore aunque el modelo visual falle.
- Probar recompensas normales y ominosas desde ambas ediciones.
- El datapack preparado contiene recompensas temáticas, pero su `pack_format` y la sintaxis de componentes deben comprobarse con la versión exacta del servidor.

### NPC y skins

- Los NPC son viables si el plugin elegido los presenta correctamente mediante Geyser; probar interacción, nombre y skin desde Bedrock.
- SkinsRestorer es opcional. Java premium ya muestra sus skins y Floodgate gestiona las de Bedrock.
- Instalar SkinsRestorer únicamente si se necesitan comandos `/skin` o soporte adicional para perfiles sin skin.
- No instalar simultáneamente otro gestor de skins de Geyser durante la primera prueba.

## 14. Fase 9 — Fortaleza de Perugius

La fortaleza se añade **después** de que el mundo definitivo funcione. **Estado (2026-10-18):** el mundo base ya está **exportado** (`world`, 1.936 regiones, 2026-10-17) y el **punto de pegado está reservado**: centro `X=−916, Z=6316`, ancla `(−916, 150, 6316)`. Con eso, el paso 7 de abajo (elegir coordenadas) ya está resuelto. Detalle y verificación del terreno: `perugius\UBICACION_RESERVADA_FORTALEZA_PERUGIUS.md`.

Flujo resumido:

1. Abrir una copia local de `CastleWorld.zip`.
2. Localizar el castillo y limpiar entidades, cofres, command blocks, redstone activa y bloques ajenos.
3. Seleccionar la estructura completa con WorldEdit/FAWE.
4. Definir un origen central bajo la isla o plataforma.
5. Guardar `fortaleza_perugius_v1.schem`.
6. Probar el schematic en una copia del mundo definitivo.
7. Elegir coordenadas lejos del spawn, facciones y world border.
8. Comprobar que su altura total quede dentro del límite vertical real.
9. Crear un backup del servidor.
10. Pegar con FAWE sin jugadores en la zona.
11. Revisar interiores, iluminación, líquidos, gravedad y compatibilidad Bedrock.
12. Protegerla con WorldGuard.
13. Añadir NPC, enemigos y loot después; no incrustarlos sin auditar dentro del schematic base.

No usar `//paste -a` automáticamente: ignorar aire puede dejar terreno dentro de habitaciones. Probar ambas variantes en una copia.

## 15. Rendimiento y capacidad

Para 12 GB de RAM y 24 plazas:

- `view-distance=8`.
- `simulation-distance=6`.
- Evitar granjas con miles de entidades, aldeanos o hoppers.
- Limitar armor stands y display entities decorativas.
- Evitar relojes de redstone permanentes.
- No cargar todos los chunks del mapa con force-load.
- Mantener una política de backups que no llene los 80 GB.

Realizar una prueba con al menos 10–14 jugadores, algunos explorando en regiones diferentes y otros usando granjas o combates. Medir con spark.

Objetivos:

| Métrica | Objetivo |
| --- | --- |
| TPS | Cerca de 20 de forma sostenida |
| MSPT | Menor a 50 ms; idealmente con margen |
| Disco libre | 15–20 GB como margen operativo deseable |
| Reinicios | Limpios, sin `/reload` |

Si hay lag, medir antes de quitar plugins al azar. Revisar entidades, generación de chunks, bases de datos, tareas de plugins y distancia de simulación.

## 16. Seguridad y administración

El servidor tendrá dos administradores.

- Crear grupos de LuckPerms separados para administrador y moderador.
- Evitar usar OP para tareas rutinarias.
- No conceder WorldEdit, FAWE, WorldGuard o comandos de consola a jugadores.
- Mantener whitelist hasta terminar todas las pruebas.
- Instalar CoreProtect antes de la apertura.
- Probar una restauración real de backup, no solo su creación.
- Guardar la clave de Floodgate y credenciales SFTP fuera de archivos compartidos.
- Revisar JAR y descargas desde fuentes oficiales.
- Hacer backup antes de actualizar Paper, Geyser, Floodgate, Towny o el datapack.

## 17. Lista de pruebas antes de abrir

### Mundo

- [ ] `world/level.dat` está en la ruta correcta.
- [ ] Paper carga el mundo sin crear otro vacío.
- [ ] Spawn seguro y accesible.
- [ ] Bordes y cuatro esquinas revisados.
- [ ] World border centrado y con diámetro 21.800.
- [ ] No hay estructuras de superficie no deseadas.
- [ ] Existe al menos una mina abandonada.
- [ ] Existe al menos una Trial Chamber. *(Cumplido según reporte del usuario 2026-10-22: SC1–SC10 colocadas; no verificable desde este repo.)*
- [ ] Existe al menos una Ancient City.
- [ ] Existe al menos un stronghold funcional.

### Crossplay

- [ ] Cuenta Java entra, sale y reconecta.
- [ ] Cuenta Bedrock entra por UDP, sale y reconecta.
- [ ] Inventario, UUID y ubicación Bedrock se conservan.
- [ ] Chat, comandos, claims y menús esenciales funcionan en móvil/mando.
- [ ] Packs Java y Bedrock se descargan correctamente.

### Progresión

- [ ] Nether bloqueado.
- [ ] End bloqueado.
- [ ] Homes, RTP y teleports no evaden el bloqueo.
- [ ] Procedimiento del día 8 probado en una copia.
- [ ] Reliquias de Trial Chambers probadas.
- [ ] Potencias y enfermedades cargan sin errores.
- [ ] Towny y LuckPerms conservan datos tras reiniciar.

### Rendimiento y recuperación

- [ ] Prueba de carga con 10–14 personas realizada.
- [ ] TPS/MSPT dentro de objetivos.
- [ ] Espacio libre suficiente.
- [ ] No hay errores repetitivos en `latest.log`.
- [ ] Backup previo a la apertura creado.
- [ ] Restauración de una copia verificada.

## 18. Diagnóstico rápido

| Problema | Revisar primero |
| --- | --- |
| Paper crea un mundo vacío | `level-name`, nombre de carpeta y doble anidación del ZIP |
| Error al cargar el mapa | versión/formato de exportación, `level.dat`, altura y log completo |
| Bedrock no conecta | puerto UDP, asignación del hosting y configuración de Geyser |
| Bedrock entra como usuario distinto | configuración Floodgate/UUID; no cambiarla en producción |
| No aparecen estructuras subterráneas | limitación de WorldPainter; añadirlas de forma controlada |
| El borde queda mal ubicado | centro real del mapa y diferencia entre diámetro/radio |
| El pack Java no descarga | URL no directa, HTTPS o SHA-1 incorrecto |
| El pack Bedrock no aparece | carpeta `packs`, formato/manifest y reinicio completo |
| TPS bajo | informe spark, entidades, chunks, hoppers y tareas de plugins |
| Disco lleno | backups duplicados, ZIP ya extraídos, logs y tamaño del mapa |
| Error del datapack | `pack_format`, sintaxis de comandos y versión exacta de Paper |

## 19. Plan de reversión

Si el mundo nuevo falla:

1. Detener el servidor.
2. Guardar `latest.log` y no sobrescribir la evidencia.
3. Renombrar `world` como `world_fallido_fecha`.
4. Restaurar el nombre del mundo anterior.
5. Volver a colocar su `level-name` en `server.properties`.
6. Iniciar con whitelist y verificar integridad.

Si un plugin falla:

1. Detener el servidor.
2. Retirar únicamente el JAR/configuración recién añadidos.
3. Restaurar su base de datos si realizó una migración.
4. Reiniciar y comparar el log.

Nunca usar un rollback destructivo sin tener una copia externa y saber exactamente qué carpetas se reemplazarán.

## 20. Criterio final de apertura

El servidor está listo cuando:

- el mundo correcto carga sin corrupción;
- el borde de 21.800 protege el límite exportado;
- las estructuras subterráneas esenciales están verificadas;
- Nether y End permanecen bloqueados para jugadores normales;
- Java y Bedrock juegan y reconectan correctamente;
- los packs visuales no impiden el ingreso;
- Towny, permisos, datapack y sistemas temáticos sobreviven un reinicio;
- la prueba de carga mantiene aproximadamente 20 TPS;
- existe un backup restaurable previo a la apertura;
- ambos administradores conocen el procedimiento de parada, backup, actualización y reversión.

Solo después de cumplir esta lista conviene retirar la whitelist y anunciar la apertura.

# Mundo inspirado en Mushoku Tensei

## 1. Objetivo general

Crear un mundo de Minecraft **vanilla compatible con Paper y Geyser/Floodgate**, inspirado visualmente en la geografía de *Mushoku Tensei*, pero adaptado para una experiencia survival multijugador.

El mundo debe priorizar:

- Exploración prolongada.
- Buen rendimiento con jugadores Java y Bedrock.
- Compatibilidad con plugins de Paper.
- Terreno reconocible y variado, sin depender de mods del lado del cliente.
- Espacio suficiente para facciones, ciudades, eventos y construcciones futuras.

## 2. Dimensiones principales

| Propiedad | Requerimiento |
| --- | --- |
| Tamaño total | 22.000 × 22.000 bloques |
| Centro previsto | X: 0, Z: 0 |
| Extensión teórica | X/Z desde -11.000 hasta +11.000 |
| Borde recomendado | 21.800 bloques de diámetro |
| Margen de seguridad | 100 bloques por lado |
| Altura | Techo y=511 confirmado sobre Paper 26.2 con datapack sonda (`min_y=-64`, `height=576`); Bedrock también llega a y=511 (Geyser extiende solo el overworld a -64..512 cuando el mundo Java supera y=320) |
| Capacidad prevista | 14 jugadores habituales; máximo inicial sugerido de 24 |

El borde recomendado de 21.800 bloques evita que los jugadores alcancen chunks incompletos o transiciones abruptas en el límite exportado por WorldPainter.

## 3. Diseño geográfico

El terreno debe inspirarse en el mapa del mundo de *Mushoku Tensei*, sin pretender una reproducción exacta a escala.

### Características deseadas

- Grandes masas continentales separadas por océanos navegables.
- Regiones climáticas claramente diferenciadas.
- Cordilleras que funcionen como límites naturales.
- Grandes llanuras aptas para ciudades y facciones.
- Bosques extensos, taigas, desiertos y zonas áridas.
- Islas secundarias utilizables para eventos o construcciones especiales.
- Ríos principales suficientemente anchos para ser reconocibles y navegables.
- Costas variadas, evitando bordes excesivamente rectos.
- Distribución equilibrada de recursos y biomas jugables.

### Criterios de jugabilidad

- Evitar que el punto de aparición quede rodeado por océano, montañas infranqueables o biomas hostiles.
- Reservar una región central razonablemente accesible para el spawn inicial.
- Mantener zonas remotas destinadas a futuras facciones, jefes, eventos y estructuras personalizadas.
- Evitar grandes extensiones completamente vacías o sin recursos.

## 4. Generación y estructuras

### Superficie

No deben generarse automáticamente estructuras vanilla visibles en la superficie, entre ellas:

- Aldeas.
- Mansiones del bosque.
- Puestos de saqueadores.
- Templos del desierto o de la jungla.
- Ruinas oceánicas y naufragios, si se decide mantener una superficie completamente limpia.
- Portales en ruinas.
- Monumentos u otras construcciones que interfieran con la ambientación.

Las estructuras de superficie importantes se colocarán posteriormente mediante schematics o construcción manual.

### Subsuelo

Estado verificado en el export real:

- **Minerales:** se generan solos. WorldPainter trata la capa `Resources` como capa mínima y el post-proceso `RESOURCES` corre siempre (~1,85 % del subsuelo: coal, iron, copper, gold, redstone, lapis, diamond, emerald y variantes `deepslate_*`).
- **Cuevas:** no aparecen por defecto. Solo se generan donde se pinta una capa de cuevas (`Caves`, `Caverns`, `Chasms`) y corre el post-proceso `CAVES`.
- **Geodas:** WorldPainter no tiene capa nativa (pendiente: evaluar `pockets/UndergroundPocketsLayer` con amatista).
- **Estructuras** (minas abandonadas, Trial Chambers, Ancient Cities/Warden, strongholds): WorldPainter no las genera. Hay que colocarlas a mano (schematics o construcción), sin depender de `/locate structure`.

> **Verificación obligatoria:** WorldPainter no escribe ninguna estructura moderna dentro de los chunks personalizados (el mundo importado queda con cero estructuras). Después de exportar **no** se debe esperar que `/locate structure` encuentre minas, Trial Chambers, Ancient Cities ni strongholds: si se quieren, hay que añadirlas mediante datapack, regiones vanilla controladas o schematics.

## 5. Dimensiones adicionales

### Nether

- Mantener generación vanilla.
- Bloquear el acceso durante al menos la primera semana del servidor.
- Preparar previamente la fortaleza del Nether destinada al evento de apertura.
- La fortaleza podrá contener enemigos colocados de antemano, spawners controlados, loot personalizado y medidas antirush.
- El desbloqueo debe realizarse mediante plugin o permiso, sin borrar ni regenerar la dimensión.

### End

- Mantener terreno, islas, dragón y progresión vanilla.
- Bloquear el acceso durante al menos la primera semana o hasta el evento correspondiente.
- Cambiar únicamente la ambientación visual hacia un cielo blanco o una neblina gris, inspirada en el Mundo Vacío.
- El cambio visual deberá implementarse mediante resource pack, shader compatible o configuración visual que no modifique las mecánicas.
- Verificar por separado el resultado en Java y Bedrock; Geyser puede requerir una versión Bedrock del paquete.

## 6. Estructuras personalizadas futuras

### Fortaleza aérea de Perugius

- No forma parte obligatoria del primer exportado de WorldPainter.
- Se incorporará posteriormente mediante schematic, preferentemente con WorldEdit o FAWE.
- Debe colocarse en una zona previamente reservada y alejada del spawn.
- Antes de insertarla se realizará una copia del mundo.
- Después de colocarla se podrá modificar manualmente para aproximarla a Chaos Breaker.
- La estructura deberá revisarse desde Java y Bedrock para detectar bloques invisibles, entidades incompatibles o problemas de iluminación.

### Otras estructuras

- Sedes de Orsted Corp y de los Apóstoles Humanos.
- Arenas de eventos y jefes.
- Monumentos para el ranking de las Siete Potencias Mundiales.
- Zonas de recompensa asociadas al Warden, Wither y Ender Dragon.

Estas construcciones deben añadirse sin regenerar el mundo principal.

## 7. World Border

Configuración inicial recomendada:

```mcfunction
/worldborder center 0 0
/worldborder set 21800
/worldborder damage buffer 5
/worldborder damage amount 1
/worldborder warning distance 20
/worldborder warning time 15
```

Si el centro real del mapa exportado no es `0,0`, el comando `worldborder center` deberá ajustarse antes de abrir el servidor.

El valor usado por `/worldborder set` representa el **diámetro total**, no el radio.

## 8. Requisitos técnicos de exportación

- Exportar para la versión exacta de Minecraft utilizada por Paper.
- Usar un nombre de carpeta simple, por ejemplo `world`.
- Evitar espacios, tildes y caracteres especiales en el nombre técnico.
- Conservar como mínimo:
  - `level.dat`
  - carpeta `region`
  - carpeta `entities`
  - carpeta `poi`
  - carpeta `data`
  - carpeta `datapacks`, si corresponde
- No ejecutar Chunky sobre todo el mapa si WorldPainter ya generó los chunks.
- Medir el tamaño descomprimido antes de subirlo (peso medido del mundo completo: 10,1–13,7 GB).
- Mantener espacio disponible para datos de jugadores, crecimiento del mundo, logs y copias de seguridad.
- No incluir archivos temporales, previews o copias duplicadas dentro de la carpeta del mundo.

## 9. Instalación en el hosting

1. Detener completamente el servidor.
2. Crear una copia de seguridad de la instalación y del mundo automático actual.
3. Comprimir el mundo exportado en `.zip`.
4. Subirlo preferentemente mediante SFTP si el archivo es grande.
5. Descomprimirlo en la raíz del servidor.
6. Configurar en `server.properties`:

```properties
level-name=world
```

7. Renombrar el mundo anterior como respaldo; no borrarlo hasta terminar las pruebas.
8. Iniciar el servidor y revisar la consola completa.
9. Confirmar que Paper abrió el `level.dat` correcto y no creó otro mundo vacío.
10. Aplicar el world border una vez confirmado el centro del mapa.

## 10. Compatibilidad

El mundo deberá funcionar con:

- Paper.
- Geyser.
- Floodgate.
- Jugadores Java y Bedrock.
- Plugins de facciones, permisos, NPC, dificultad y rankings.
- Resource pack exclusivamente visual, sin exigir mods del cliente.

No deberán utilizarse bloques, biomas, dimensiones o entidades que requieran Forge, Fabric o NeoForge en el cliente.

## 11. Pruebas antes de la apertura

### Terreno

- Confirmar que el spawn sea seguro y accesible.
- Teletransportarse a los cuatro extremos y esquinas del mapa.
- Verificar costas, océanos y transiciones entre biomas.
- Confirmar que no existan paredes verticales o chunks vacíos cerca del borde.
- Comprobar que el borde esté centrado correctamente.

### Estructuras y recursos

- Localizar al menos una mina abandonada.
- Localizar al menos una Trial Chamber.
- Localizar al menos una Ancient City.
- Localizar todos los strongholds necesarios o garantizar al menos uno accesible.
- Confirmar distribución de minerales, cuevas, geodas y mobs.
- Verificar que no aparezcan estructuras vanilla de superficie no deseadas.

### Rendimiento

- Probar con jugadores explorando regiones diferentes.
- Revisar TPS y MSPT con `spark`.
- Comprobar consumo de RAM y almacenamiento.
- Confirmar que viajar por chunks ya exportados no produzca generación pesada.
- Probar el cruce del mapa en Java y Bedrock.

### Dimensiones y bloqueos

- Confirmar que Nether y End estén inaccesibles para jugadores normales.
- Verificar que los administradores puedan ingresar para preparar eventos.
- Probar el procedimiento de desbloqueo antes del día de apertura.
- Confirmar que el resource pack del End funcione sin impedir el ingreso de Bedrock.

## 12. Estado de implementación

| Elemento | Estado |
| --- | --- |
| Hosting de 12 GB / 80 GB NVMe | Disponible |
| Paper, Geyser y Floodgate | Configuración en curso |
| Plugins principales | Mayormente configurados |
| Diseño base en WorldPainter | **Exportado (2026-10-17)**: mundo `world`, 1.936 regiones (11,49 GB) |
| Subida del mundo | Pendiente |
| World border | Pendiente |
| Verificación de estructuras subterráneas | **Actualizado 2026-10-22:** trial chambers secundarias **SC1–SC10 colocadas** (reportado por el usuario; no verificable desde este repo). **Falta la arena de Hitogami** en `(-2304, -16, -3008)` + portal al End; `.schem` de la arena **sin decidir** |
| Bloqueo inicial de Nether y End | Pendiente de prueba |
| Resource pack del End | Pendiente |
| Fortaleza aérea de Perugius | Fase futura; **el mundo base ya está exportado (2026-10-17) y el punto está reservado (X=−916, Z=6316, Y₀=150)** |
| Fortaleza/evento del Nether | Fase futura |

## 13. Criterio de aceptación

El mundo estará listo para abrir al público cuando:

- Paper lo cargue sin errores de conversión o corrupción.
- El tamaño total entre dentro del almacenamiento con margen suficiente.
- El borde coincida con el área útil del mapa.
- No existan estructuras de superficie no deseadas.
- Las estructuras subterráneas esenciales hayan sido verificadas.
- Nether y End permanezcan bloqueados para jugadores normales.
- Java y Bedrock puedan conectarse, desplazarse y visualizar el terreno correctamente.
- El servidor mantenga 20 TPS durante una prueba representativa.
- Exista una copia de seguridad restaurable previa a la apertura.

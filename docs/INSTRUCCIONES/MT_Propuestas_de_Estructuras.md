# Propuestas de estructuras para el servidor Mushoku Tensei

**Servidor objetivo:** Paper Java 26.2+ con Geyser/Floodgate  
**Mapa:** mundo de WorldPainter de aproximadamente 22.000 × 22.000 bloques  
**Método recomendado:** preparar todas las estructuras en una copia local, reexportarlas como Sponge `.schem` v3 y subir el mundo ya ensamblado.

## 1. Resumen de selección

| Estructura | Propuesta principal | Formato | Estado |
|---|---|---|---|
| Gran laberinto | GoFeDaKo Maze Generator | `.schem` v3 | Recomendado |
| Templo de Milis | St. Basine Cathedral | Schematic WorldEdit | Recomendado |
| Castillo de Kishirika | Rikarisu and Roxy Village | Mundo Java | Requiere permiso y extracción |
| Universidad de Ranoa | Campus modular: St. Sayers + Eterna + edificios auxiliares | Varios schematics | Selección definitiva |
| Ranoa fiel 1:1 | Mapa de qNiw | Mundo Java | Referencia visual; Patreon y sin interiores |
| Fortaleza de Perugius | Proyecto local actual / Celestara como alternativa | Mundo Java | Validar licencia |
| Mazmorras Trial | Dungeons and Taverns | Datapack | Complementario |

---

# 2. Gran laberinto

## Propuesta principal: generador GoFeDaKo

- **Enlace:** [Maze Generator to Schematic](https://gofedako.com/tool/maze/)
- **Ventaja:** permite diseñar un laberinto específicamente para nuestro terreno.
- **Formatos:** Sponge `.schem` v2/v3, `.litematic`, `.nbt`, `.mcstructure` y otros.
- **Capacidad:** hasta 512 × 512 celdas, varios pisos y distintas formas.

### Configuración recomendada

| Parámetro | Valor |
|---|---|
| Celdas | 128 × 128 |
| Huella aproximada | 500–550 bloques por lado |
| Pasillos | 3 bloques de ancho |
| Muros | 1 bloque de grosor |
| Altura libre | 5–6 bloques |
| Pisos iniciales | 1 |
| Formato | Sponge `.schem` v3 |

### Adaptación

- Paleta de toba, ladrillos de piedra y deepslate agrietada.
- Cuatro sectores temáticos.
- Entre 12 y 20 salas de combate.
- De 4 a 8 bóvedas.
- Arena central para jefe.
- Puntos de control y salida de emergencia administrativa.
- Trial Spawners separados para evitar activación simultánea.

### Alternativa ya construida

- [Underground Maze Structure](https://mc-schematics.com/s/underground-maze-structure-455e7bbd20)
- Java 26.2, formato `.litematic`, aproximadamente 129 × 32 × 137.
- Requiere reemplazar bambú y concreto por una paleta más vanilla.

---

# 3. Gran Iglesia o Templo de Milis

## Propuesta principal: St. Basine Cathedral

- **Enlace:** [St. Basine Cathedral](https://www.planetminecraft.com/project/st-basine-cathedral-schematic/)
- **Formato:** schematic para WorldEdit.
- **Versión original:** Java 1.19.
- **Interior:** completo.
- **Permiso:** se puede utilizar con atribución visible a la página y a los creadores.

### Adaptación a Milis

- Fachada de calcita, cuarzo y diorita.
- Cúpula central dorada o de cobre encerado.
- Cuatro patios estacionales: primavera, verano, otoño e invierno.
- Nave principal con símbolo de Milis y gran vitral.
- Oficinas del clero y habitaciones restringidas.
- Cripta para el Emblema Sagrado de Milis.
- Sala de pruebas para obtener la armadura del Caballero de Milis.

### Alternativa moderna

- [True Colors Cathedral](https://abfielder.com/Products/ProductDetails.php?id=13714)
- Java 1.21.10.
- Descargas `.schem` y `.litematic`.
- Dimensiones aproximadas: 51 × 109 × 105.
- Interior, torres y tejado accesibles.

---

# 4. Castillo de Kishirika en Rikarisu

## Propuesta principal: Rikarisu and Roxy Village

- **Enlace:** [Rikarisu and Roxy Village](https://www.planetminecraft.com/project/rikarisu-and-roxy-village/)
- **Contenido:** Rikarisu y el castillo demoníaco de *Mushoku Tensei*.
- **Formato:** mundo Java descargable.
- **Estado:** 95 % terminado.
- **Interior del castillo:** vacío, adecuado para convertirlo en dungeon.

### Condición de uso

La página permite descargar el mapa, pero no publica un permiso explícito para incorporarlo a un servidor público. Antes de utilizarlo deberá contactarse al creador **Roi_lije1er**, acreditar también a **sheluvjuli** y evitar redistribuir el mundo o el schematic derivado sin autorización.

### Flujo de extracción

1. Descargar y abrir el mundo en una copia local.
2. Medir castillo, cráter y accesos.
3. Seleccionar el conjunto con FAWE.
4. Copiarlo y guardarlo como `kishirika_castle.schem`.
5. Reexportarlo como Sponge `.schem` v3.
6. Colocarlo en el mundo de WorldPainter.

### Diseño interior propuesto

- Entrada turística aparentemente segura.
- Zona residencial demoníaca cerrada.
- Gran hueco o torre destruida.
- Sala de audiencias estrecha y trono de jade.
- Cámaras de teletransporte.
- Sala de los Ojos Demoníacos.
- Sector maldito con Trial Spawners.
- Arena final para evento de Kishirika.

### Alternativa no canónica

- [Astaroth — The Demonic Castle](https://www.planetminecraft.com/project/astaroth-the-demonic-castle/)
- Incluye schematic y mundo descargable.
- Es grande y demoníaco, pero antiguo; requiere conversión desde formato legacy y modernización de materiales.

---

# 5. Universidad Mágica de Ranoa

## Decisión adoptada: campus modular

En lugar de depender de una academia completa con una estética difícil de adaptar, Ranoa se construirá como un campus de aproximadamente **500 × 420 bloques**, compuesto por varios schematics. Las piezas se unirán con murallas, galerías, patios y un puente construidos específicamente para el servidor. El objetivo es acercarse a la silueta fortificada de la serie: piedra gris, tejados rojos, torres altas, patio central y acceso mediante un puente largo.

### Paquete de estructuras

| Cantidad | Construcción | Función en Ranoa |
|---:|---|---|
| 2 | St. Sayers College — East Wing | Alas académicas simétricas |
| 1 | Eterna — The Grand Library | Biblioteca y archivo mágico |
| 1 | Medieval Ranger's Guild V.1 | Rectorado y gran salón |
| 4 | Epic Wizard Tower | Torres elementales de investigación |
| 1 | Big Fantasy Greenhouse | Botánica, alquimia y cultivo mágico |
| 1 | Crimson Crown Arena | Duelos, exámenes y torneos |
| 3–4 | Medieval Tavern & Interior | Residencias estudiantiles |

## 5.1 Alas académicas — St. Sayers College

- **Enlace:** [East Wing of the St. Sayers College](https://www.planetminecraft.com/project/east-wing-of-the-st-sayers-college-fully-furnished/)
- **Formato:** mundo y `.schem` descargable.
- **Versión original:** Java 1.14.
- **Interior:** completo, con aulas, bibliotecas temáticas, salas comunes y alojamiento para profesores.
- **Uso propuesto:** colocar dos copias enfrentadas o espejadas a ambos lados del patio principal.
- **Condición:** el autor permite utilizar la construcción, pero prohíbe republicarla; antes de publicar un colegio construido alrededor de ella conviene contactar al creador y conservar el crédito.

Las dos alas compartirán la misma fachada exterior, pero sus interiores se dividirán entre magia de agua y curación, fuego y combate, tierra y defensa, viento, invocación, encantamiento y estudios generales.

## 5.2 Gran biblioteca — Eterna

- **Enlace:** [Eterna — The Grand Library](https://www.planetminecraft.com/project/eterna-the-grand-library/)
- **Formato:** mundo y schematic.
- **Uso propuesto:** ubicarla en el extremo norte como remate visual del eje central.
- **Contenido jugable:** archivo de grimorios, libros coleccionables, biblioteca restringida y cámara de investigación del Desastre de Maná.
- **Condición:** acreditar al creador y no volver a publicar la descarga sin permiso.

## 5.3 Rectorado y salón principal — Medieval Ranger's Guild

- **Enlace:** [Medieval Ranger's Guild V.1](https://www.planetminecraft.com/project/medieval-ranger-s-guild-v-1/)
- **Formato:** schematic.
- **Versión original:** Java 1.19.2.
- **Interior:** completo e incluye un gran salón.
- **Uso propuesto:** convertir el gran salón en recepción, comedor ceremonial y rectorado; las salas secundarias servirán para administración, enfermería y profesores.
- **Condición:** como la publicación no muestra una autorización clara para servidores públicos, se deberá acreditar al autor y solicitar permiso antes del lanzamiento.

## 5.4 Torres de investigación — Epic Wizard Tower

- **Enlace:** [Epic Wizard Tower — Full Interior](https://www.planetminecraft.com/project/epic-wizard-tower-full-interior/)
- **Formato:** schematic con interior.
- **Cantidad:** cuatro copias, una en cada esquina del recinto principal.
- **Uso propuesto:** asignar una torre a agua, fuego, tierra y viento; variar ligeramente la paleta, los laboratorios y la iluminación interior sin alterar su silueta común.
- **Condición:** la publicación no expone términos específicos para servidores; acreditar al creador y confirmar permiso antes de incorporarlas al paquete público.

## 5.5 Invernadero — Big Fantasy Greenhouse

- **Enlace:** [Big Fantasy Greenhouse](https://www.planetminecraft.com/project/big-fantasy-greenhouse-schematic/)
- **Formato:** schematic.
- **Ubicación:** detrás del ala oriental.
- **Uso propuesto:** botánica mágica, cultivo de ingredientes, alquimia y misiones de recolección.
- **Condición:** el creador permite usarlo en mundos y servidores con atribución.

## 5.6 Arena de duelos — Crimson Crown Arena

- **Enlace:** [Crimson Crown Arena](https://www.planetminecraft.com/project/crimson-crown-arena-download/)
- **Diseño:** arena simétrica con patio central de combate, zonas para espectadores y tres agujas.
- **Ubicación:** sector este o sudeste, separada de las aulas para que pueda cerrarse durante eventos.
- **Uso propuesto:** duelos protegidos, exámenes prácticos, torneos y demostraciones de magia.
- **Condición:** al no figurar una licencia pública precisa, acreditar y solicitar autorización para su uso en el servidor.

## 5.7 Residencias — Medieval Tavern & Interior

- **Enlace:** [Medieval Tavern & Interior](https://www.planetminecraft.com/project/medieval-tavern-amp-interior-free-schematic/)
- **Formato:** schematic gratuito y amueblado.
- **Cantidad:** tres o cuatro copias modificadas para que no resulten idénticas.
- **Ubicación:** sectores oeste y sur, formando calles internas alrededor del campus.
- **Uso propuesto:** dormitorios, comedor estudiantil, enfermería secundaria y casa de huéspedes.
- **Condición:** se permite utilizar y modificar la construcción en servidores públicos o privados; no se debe revender ni volver a subir el archivo.

## Distribución del campus

| Sector | Contenido |
|---|---|
| Norte | Eterna y el rectorado, formando el frente monumental del patio |
| Centro | Gran patio, fuente o estatua y las dos alas St. Sayers |
| Esquinas | Cuatro torres elementales conectadas por la muralla |
| Oeste y sur | Residencias, enfermería, comedor y servicios |
| Este | Invernadero, talleres y campo de prácticas |
| Sudeste | Arena de duelos con acceso independiente |
| Acceso sur | Puerta monumental, camino elevado y puente largo |

## Paleta unificada

- **Muros:** ladrillos de piedra, andesita pulida, toba y acentos de calcita.
- **Tejados:** ladrillos y terracota roja; ladrillos del Nether rojos solamente como detalle.
- **Madera:** abeto y roble oscuro.
- **Ventanas:** vidrio gris claro, blanco y azul pálido.
- **Elementos mágicos:** amatista, faroles y sea lanterns ocultas en interiores.
- **Caminos:** piedra, andesita, grava y parches de adoquín.
- **Vegetación:** hiedra contenida, jardines formales y árboles de clima frío.

## Conexiones que deberán construirse a medida

- Muralla exterior continua con torres integradas.
- Puerta sur monumental y puente de acceso largo.
- Galerías cubiertas de al menos siete bloques de ancho entre edificios.
- Patio académico central con líneas de visión hacia la biblioteca.
- Plazas menores para NPC, tablones de misiones y comercio.
- Transiciones de terreno, jardines y sótanos técnicos para ocultar redstone o comandos.

## Función dentro del servidor

Ranoa no será solamente decorativa. Concentrará tutoriales avanzados, profesores NPC, reputación académica, exámenes semanales, fabricación y reparación de artefactos, libros coleccionables, duelos protegidos, comercio mágico y la investigación narrativa del Desastre de Maná.

## Referencia de máxima fidelidad: mapa 1:1 de qNiw

- **Enlace:** [Ranoa University of Magic y ciudad de Sharia](https://www.patreon.com/qNiw/posts/mushoku-tensei-171837174)
- **Contenido:** ciudad de Sharia y Universidad Mágica de Ranoa a escala 1:1.
- **Versión publicada:** Java 1.21.11.
- **Interiores:** no incluidos.
- **Acceso:** publicación para miembros de Patreon.
- **Uso decidido:** referencia de proporciones, tejados, murallas y puente; no será el archivo base del campus modular.
- **Permiso:** aunque el creador permite modificar el mapa, para utilizar o redistribuir públicamente una versión derivada deberá pedirse autorización concreta.

## Preparación e importación

1. Reservar y nivelar un área aproximada de 500 × 420 bloques en una copia local del mundo.
2. Convertir las construcciones antiguas, especialmente St. Sayers 1.14, a Sponge `.schem` v3 desde una versión intermedia compatible.
3. Colocar primero el patio, las dos alas y la biblioteca para fijar escala y ejes.
4. Añadir rectorado, torres, residencias, invernadero y arena.
5. Construir las murallas, galerías, puerta y puente para ocultar diferencias de estilo.
6. Reemplazar bloques con máscaras de FAWE hasta unificar la paleta.
7. Revisar contenedores, entidades, command blocks, iluminación, rutas y colisiones.
8. Registrar autor, enlace y permiso de cada módulo antes de subir el mundo al hosting.

---

# 6. Fortaleza aérea de Perugius

## Estado recomendado

Mantener como principal el castillo aéreo que ya se está preparando localmente para el servidor, porque puede adaptarse sin depender de una estructura externa nueva.

## Alternativa visual: Celestara

- **Enlace:** [Celestara — Epic Floating Castle](https://www.planetminecraft.com/project/celestara-epic-floating-castle-mega-build/)
- Java 1.21.4 y versión convertida para Bedrock.
- Castillo blanco flotante masivo y cuatro castillos secundarios.
- No tiene interiores.
- El autor prohíbe uso comercial y redistribución.

Debido a que nuestro servidor acepta donaciones para cubrir capacidad, no debería incorporarse sin obtener permiso explícito del autor. Puede utilizarse como referencia visual, no como archivo definitivo.

## Alternativa sencilla con schematic

- [Floating Island Castle 1.19.2](https://www.planetminecraft.com/project/floating-island-castle-1-19-2/)
- Schematic gratuito para WorldEdit.
- Menor escala; útil como base secundaria o isla satélite.

### Elementos necesarios de Chaos Breaker

- Palacio central blanco.
- Varias torres conectadas.
- Jardines y patios elevados.
- Sala del trono.
- Biblioteca y cámaras de invocación.
- Muelles para transporte aéreo.
- Islas menores o fragmentos suspendidos.
- Barrera invisible para impedir accesos prematuros.

---

# 7. Trial Chambers y mazmorras secundarias

## Dungeons and Taverns

- **Enlace oficial:** [Dungeons and Taverns en Modrinth](https://modrinth.com/datapack/dungeons-and-taverns)
- Compatible con Java 26.2.
- Datapack del lado del servidor.
- Añade estructuras de estilo vanilla, incluyendo Trial Dungeons con Trial Spawners y Vaults.

### Uso propuesto

- No instalarlo en producción después de generar todo el mapa sin probar primero su interacción con WorldPainter.
- Extraer o adaptar estructuras concretas en una copia local.
- Utilizar las mazmorras como módulos del gran laberinto.
- Reemplazar loot y llaves por recompensas temáticas del servidor.
- Limitar la cantidad de spawners activos en una misma zona.

---

# 8. Orden de integración recomendado

1. Terminar la topografía y fijar el WorldBorder.
2. Colocar Milis y comprobar escala urbana.
3. Reservar unos 500 × 420 bloques para Ranoa y fijar su patio, ejes y accesos.
4. Colocar las dos alas St. Sayers, Eterna y el rectorado.
5. Añadir torres, residencias, invernadero y arena; después construir murallas, galerías y puente.
6. Extraer Rikarisu y Kishirika después de obtener permiso.
7. Colocar la fortaleza aérea de Perugius y definir su zona de exclusión.
8. Construir y enterrar el gran laberinto.
9. Añadir Trial Dungeons y cámaras secundarias.
10. Pregenerar los chunks y comprobar iluminación y spawns.
11. Crear regiones de WorldGuard.
12. Añadir NPC, loot y activadores de eventos.
13. Probar navegación desde Java y Bedrock.
14. Empaquetar y subir el mundo final al hosting.

# 9. Reglas para importar estructuras

- Trabajar siempre sobre una copia local.
- Guardar una copia del mundo antes de cada pegado grande.
- Convertir `.schematic` legacy a Sponge `.schem` v3.
- Pegar con aire cuando sea necesario para conservar habitaciones interiores.
- No ejecutar pegados masivos directamente en producción.
- Revisar entidades, command blocks, contenedores y spawners antes de exportar.
- Eliminar command blocks desconocidos.
- Registrar autor, enlace y condiciones de uso de cada estructura.
- No redistribuir archivos externos dentro del paquete público del servidor sin permiso.


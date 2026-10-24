# Servidor Mushoku — base Paper 26.2 + Geyser

Paquete preparado para acompañar el mapa MT 22.000 × 22.000. Está pensado
para Paper 26.2, Java 25 o posterior, Geyser-Spigot y Floodgate.

No incluye archivos JAR: el panel del hosting debe instalar siempre las últimas
versiones disponibles desde sus fuentes oficiales. Esto evita dejar congelada
una versión de Geyser que podría quedar incompatible con una actualización de
Minecraft Bedrock.

## Qué está preparado

- Plantilla inicial de `server.properties` para 24 jugadores.
- Nether y End bloqueados para el lanzamiento.
- Estructuras aleatorias desactivadas; las estructuras subterráneas serán
  colocadas de forma controlada en el mapa.
- World border de **21.800 × 21.800**, centrado en 0,0, dentro de un mapa de 22.000 × 22.000.
- Datapack administrativo con funciones de lanzamiento y apertura.
- Resource pack Java con cielo del End gris.
- Resource pack Bedrock con niebla gris, listo para la carpeta de Geyser.
- Parámetros recomendados para Geyser, Floodgate y LevelledMobs.
- Sistema inicial de las Siete Potencias Mundiales mediante Skript.
- Facciones de jugadores mediante Towny: cada pueblo funciona como facción.
- Dos alianzas fijas, Orsted Corp y Apóstoles del Dios Humano, administradas
  como naciones de Towny y grupos de LuckPerms.
- Enfermedades temáticas con efectos vanilla visibles en Java y Bedrock.
- Siete reliquias temáticas en recompensas de Trial Chambers, con modelos Java
  y mapeos Geyser v2 para Bedrock.
- Integración prevista con duelos Elo, títulos, TAB y hologramas.

## Decisiones de compatibilidad

- Altura extendida **`-64..511`**, fijada por el datapack que WorldPainter escribe dentro del mundo. Por Geyser 2.12.0 los jugadores **Bedrock también la ven** (extiende el overworld a `-64..512`); falta el test con un cliente Bedrock real.
- Ninguna mecánica depende de la mano secundaria, clic izquierdo/derecho,
  brillo de entidad, encantamientos personalizados ni recetas de yunque.
- Los menús esenciales son comandos y texto simple; funcionan con controles
  táctiles y mando.
- Los JAR no se incluyen. Las versiones cambian y deben descargarse desde sus
  proyectos oficiales el día de la instalación.

## Lo que se completa al contratar el hosting

1. Dirección y puerto Java.
2. Puerto UDP Bedrock asignado por el proveedor.
3. RAM y comando de inicio disponible.
4. URL pública del resource pack Java.
5. Exportación definitiva del mapa desde WorldPainter.
6. Construcción/colocación de la fortaleza personalizada y las estructuras
   subterráneas. **Actualizado 2026-10-22:** las trial chambers secundarias SC1–SC10 ya
   están **colocadas** (reportado por el usuario; no verificable desde este repo) y la
   fortaleza de Perugius está pegada. **Falta** la arena de Hitogami en `(-2304, -16, -3008)`
   y el portal al End detrás de ella.
7. Coordenadas del salón de las Potencias y de la arena de duelos.

`ESTADO_IMPLEMENTACION.md` separa lo que ya funciona al copiar los archivos de
lo que todavía requiere instalar y configurar un plugin en el hosting.

Seguir `INSTALACION_HOSTING.md` cuando el servidor esté contratado.


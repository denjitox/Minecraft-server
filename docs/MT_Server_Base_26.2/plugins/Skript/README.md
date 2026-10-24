# Instalación de sistemas Skript

1. Instalar Skript 2.16.2 o una versión estable posterior compatible con 26.2.
2. Iniciar y apagar el servidor para que genere `plugins/Skript/`.
3. Copiar `potencias.sk` y `enfermedades.sk` a `plugins/Skript/scripts/`.
4. Instalar LuckPerms y crear los grupos incluidos en este paquete.
5. Iniciar y ejecutar `sk reload potencias` y `sk reload enfermedades`.
6. Revisar la consola: no abrir el servidor si Skript informa errores.

El script es una configuración inicial. Antes del lanzamiento se probará contra
los builds exactos instalados y se conectarán Duels, los hologramas y las
ubicaciones definitivas de las mazmorras.

Pruebas mínimas: `/potencias`, `/mtestado`, `/visionapostol` con su grupo y
`/enfermedad JUGADOR dryne` desde una cuenta administrativa. La leche debe
eliminar los efectos porque son efectos de poción vanilla.


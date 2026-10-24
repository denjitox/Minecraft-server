# Sistema de las Siete Potencias Mundiales

## Plugins elegidos

### Núcleo

- **Skript 2.16.2:** lógica de jefes fundadores, puntos PvP válidos, comando
  `/potencias` y asignación administrativa.
- **LuckPerms:** grupos, permisos y prefijos de cada Potencia.

### Presentación y competición

- **Duels Optimised:** Elo y duelos oficiales del Dios de la Espada.
- **PlaceholderAPI:** puente entre estadísticas y elementos visuales.
- **TAB:** prefijos en la lista de jugadores y nametags.
- **DecentHolograms:** salón dentro de la fortaleza aérea.
- **ajLeaderboards:** rankings secundarios basados en placeholders numéricos.

## Asignación

| Puesto | Título | Regla inicial |
| ---: | --- | --- |
| 1 | Dios de la Técnica | Mayor contribución en la primera muerte de un Warden |
| 2 | Dios Dragón | Mayor contribución en la primera muerte del Ender Dragon |
| 3 | Dios de la Lucha | Mayor contribución en la primera muerte del Wither |
| 4 | Dios Demonio | Puntuación de mazmorras; asignación administrativa inicial |
| 5 | Dios de la Muerte | Mayor cantidad de bajas PvP válidas |
| 6 | Dios de la Espada | Primer puesto Elo de Duels Optimised |
| 7 | Dios del Norte | Exploración y logros; asignación administrativa inicial |

El script acumula daño de todos los participantes y entrega los puestos 1–3 al
jugador que más daño haya causado, no necesariamente al que dé el último golpe.
La asignación se persiste por UUID; el nombre guardado es solamente visual. Un
cambio de nick no crea dos propietarios ni deja permisos LuckPerms huérfanos.

Para el puesto 5, una misma víctima solamente otorga un punto al mismo atacante
cada doce horas. Este filtro reduce el intercambio artificial de muertes.

Los puestos 4, 6 y 7 quedan administrables hasta conectar las estadísticas
definitivas del mundo, las coordenadas de mazmorras y la arena.

## Datos y copias de seguridad

Skript guarda las variables persistentes dentro de su propia carpeta. Incluir
esa carpeta en los backups diarios junto con el mundo, LuckPerms y Duels.


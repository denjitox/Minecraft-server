# Desarrollo del 25/10/2026

## Alcance

Un título recién otorgado debía verse en el tablist y en `/potencias` sin relog. El cambio se
limitó a `MushokuCore`; `potencias.sk` no se tocó. No se pudo compilar: faltan el JDK 25 y
`servidor\libraries` en este árbol (ver "Error conservado").

## Diagnóstico

- `/potencias` y los placeholders leen `IPotenciasService` en vivo (`propietario()`,
  `posicionDelJugador()`), y `PotenciasService.asignarInterno` actualiza la caché `asientos`
  antes de escribir. No había caché que invalidar: el listado ya era inmediato.
- El nombre del dueño sí fallaba. `nombreDe()` devolvía `null` fuera del hilo principal, y la
  muerte de jefe y el recálculo del puesto 5 asignan desde un hilo async. Resultado: `/potencias`
  mostraba el UUID y ese `NULL` quedaba grabado en `world_powers.nombre_dueno`.
- TAB refresca `%luckperms-prefix%` cada 1000 ms y el grupo cada `permission-refresh-interval`
  (1000 ms por defecto), según `TabConstants` y `GroupManager`. Nada garantizaba el cambio en el
  tick de la escritura, y con ese intervalo apagado en la config del servidor el tablist solo
  cambiaba al reconectar.

## Cambios

- `integration/TabListRefresher.java` (nuevo): fuerza `PlayerPlaceholder.update(TabPlayer)` sobre
  `%luckperms-prefix%`, `%luckperms-prefixes%`, `%luckperms-suffix%`, `%luckperms-suffixes%` y
  `%luckperms-weight%`. Reflexión porque TAB es softdepend y su jar no está en el classpath.
  `solicitar()` acepta cualquier hilo y salta al principal; sin TAB no hace nada.
- `module/ranks/LuckPermsOutbox.java`: instancia el refrescador y lo llama tras cada operación LP
  exitosa, tanto en el camino inmediato como en los reintentos.
- `module/ranks/PotenciasService.java`: caché `nombres` alimentada en el hilo principal
  (`resolverMuerteDeJefe` para todos los contribuyentes, `registrarBajaValida` para el atacante).
  `nombreDe()` la lee desde async y cae a `getOfflinePlayer` en el principal, así el dueño se
  guarda con nombre y no con UUID.

## Verificación

- Compilación: **no ejecutada**, ver "Error conservado".
- Balance de llaves y paréntesis en los tres archivos, con comentarios y strings quitados:
  `TabListRefresher` 21/21 y 46/46; `LuckPermsOutbox` 53/53 y 146/146; `PotenciasService`
  130/130 y 485/485.
- Firmas de TAB verificadas contra el código fuente del proyecto (`api/`, `shared/`): `TabAPI` es
  clase abstracta con `getInstance()`, `getPlayer(UUID)`, `getPlaceholderManager()`;
  `PlaceholderManager.getPlaceholder(String)` devuelve `Placeholder`;
  `PlayerPlaceholder.update(TabPlayer)` existe. `%group%` quedó afuera a propósito: su update
  relee `TabPlayer.permissionGroup`, un campo que solo cambia por `setGroup`, así que forzarlo no
  aportaba nada. `TabAPI` no expone `getFeatureManager()`.
- LuckPerms invalida su caché de meta al cambiar los datos (`PermissionHolder` →
  `getCachedData().invalidate()`), así que el prefijo leído tras el cambio es el nuevo.
- Sin prueba runtime: no hay servidor, TAB ni LuckPerms en este árbol.

## Error conservado

```text
No se encuentra un jar del classpath:
C:\Dev\OPENCLAUDIO\minecraft_server\servidor\libraries\io\papermc\paper\paper-api\26.2.build.134-stable\paper-api-26.2.build.134-stable.jar
En build.ps1: 44 Carácter: 32
exit=1
```

`C:\Dev\OPENCLAUDIO\` no existe, `C:\Dev\Minecraft_server\Minecraft server\servidor\` tampoco,
no hay `javac` ni `java` en el PATH ni `JAVA_HOME`, y no hay ningún `paper-api*.jar` ni `javac.exe`
en `C:\Dev` ni en `C:\Users\Usuario` (búsquedas con `Get-ChildItem`, 25/10/2026). No se ajustó
ningún parámetro para forzar el build.

## Pendiente de runtime y de configuración

- El cambio en el tablist depende de la config del servidor: TAB tiene que usar los placeholders
  de LuckPerms o el formato por grupo, los grupos `potenciaN` necesitan prefijo y un peso mayor
  que `default` para que LuckPerms los tome como grupo primario. Sin eso el refresco corre pero
  no hay nada distinto que mostrar.
- El formato por grupo de TAB (`groups.yml`) sigue a `permission-refresh-interval`; adelantarlo
  requiere API interna de TAB.
- Falta la prueba con un jugador real: matar un jefe y correr `/potencias` sin reconectar.

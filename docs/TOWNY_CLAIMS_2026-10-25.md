# Diagnóstico de claims de Towny — 25/10/2026

## Causa verificada en producción

Towny tenía la economía activada sin un proveedor económico compatible:

```yaml
economy:
  using_economy: 'true'
```

El propio archivo de Towny indica que, en ese estado, no es posible crear pueblos ni ejecutar
operaciones que requieran dinero. El precio configurado para crear un pueblo era `250.0`.

También se verificó:

- 0 pueblos y 0 naciones en `plugins/Towny/data`;
- 17 residentes registrados, todos sin pueblo;
- `world` tenía `claimable=true` y `usingTowny=true`;
- `townyperms.yml` concedía `towny.command.town.new` a nómadas;
- MushokuCore tenía `modules.factions=false`, por lo que no limitaba claims;
- el log registró un comando Towny rechazado desde consola: los comandos territoriales deben
  ejecutarse como jugador.

## Corrección aplicada

Se cambió únicamente `using_economy` a `false` en el hosting. Antes de escribir se creó:

`/plugins/Towny/settings/config.yml.bak-20261025T180414Z`

El archivo remoto corregido se volvió a descargar y comparar en memoria:

- tamaño: 132.499 bytes;
- SHA256: `DB014808594590627163A1116D336C82389BF4057F8FA0897D65242872BEB2B2`.

## Activación y prueba

La corrección requiere reiniciar el servidor desde el panel. Después, un jugador debe ejecutar:

```text
/town new <nombre>
/town claim
```

`/town claim` debe ejecutarlo el alcalde o un asistente y el bloque nuevo debe ser contiguo al
territorio existente. La prueba posterior al reinicio todavía no está verificada.

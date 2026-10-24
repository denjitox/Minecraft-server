# Desarrollo del 24/10/2026

## Alcance

Se estabilizó el módulo `regional-difficulty` sin arrancar Paper. El servidor local no se inició para evitar consumo de CPU y memoria mientras había otros trabajos en paralelo.

## Cambios

- El módulo toma `plugin` y `logger` desde `PluginContext`; ya no habilita un listener con campos nulos.
- `regional-difficulty.zones` se lee como lista YAML y se ordena por radio.
- Solo se escalan entidades que implementan `Enemy`.
- `SPAWNER` conserva el escalado regional. El límite especial se aplica únicamente a `TRIAL_SPAWNER`.
- Se eliminó el conjunto permanente de UUID procesados. La idempotencia usa modificadores con `NamespacedKey`.
- La selección de zona ya no confunde un multiplicador válido `1.0` con una zona ausente.
- Un mob herido no se cura al agregar el modificador de vida.
- Se quitó `dog` de las exclusiones; Paper usa `wolf`.

## Verificación liviana

- Compilación aislada con `build.ps1`: correcta.
- JAR: `build/libs/MushokuCore-1.0.0.jar`, 211.745 bytes.
- SHA256: `2B98BFCD6CC7EB52C3FE85D4BB6435772A46E2D1B6145324F6E029705DF2180D`.
- Prueba temporal del parser con la configuración real y otra lista desordenada: `REGIONAL_CONFIG_CHECK_OK`.
- No se hizo prueba runtime de spawns, atributos ni Trial Spawners.

## Error conservado

La primera compilación falló por usar `Map<?, ?>.getOrDefault` con un valor `String`:

```text
PluginConfigImpl.java:154: error: incompatible types: String cannot be converted to CAP#1
String name = String.valueOf(mapa.getOrDefault("name", "zona_" + indice));
where CAP#1 is a fresh type-variable:
CAP#1 extends Object from capture of ?
1 error
javac fallo con exit code 1
```

Log completo: `%TEMP%\opencode\mushokucore_build_regional_20261024.log`.

Se corrigió leyendo `name` con `mapa.get("name")` y tratando `null` explícitamente.

## Pendiente de runtime

En una ventana de prueba controlada hay que verificar un enemigo natural, uno de spawner normal y uno de Trial Spawner en cada zona y dimensión. No instalar ni subir este JAR a producción antes de esa prueba.

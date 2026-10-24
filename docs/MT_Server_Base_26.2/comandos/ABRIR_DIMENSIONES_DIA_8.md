# Apertura — día 8

1. Avisar a los jugadores y ejecutar `save-all flush`.
2. Apagar el servidor normalmente.
3. En `server.properties`, cambiar `allow-nether=true`.
4. En `bukkit.yml`, cambiar `settings.allow-end: true`.
5. Confirmar que la fortaleza personalizada y la protección del portal estén
   instaladas antes de permitir el acceso.
6. Iniciar el servidor.
7. Ejecutar:

```text
function mt:stage_dimensions_open
save-all flush
```

No borrar ni regenerar `world_nether` o `world_the_end` después de que los
jugadores hayan comenzado a utilizarlos.


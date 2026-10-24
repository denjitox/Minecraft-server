# Perfil inicial para 24 jugadores

- Paper 26.2 y Java 25 o posterior.
- RAM inicial: 10–12 GB si el proveedor ofrece CPU rápida dedicada o con baja
  sobreventa. La velocidad de un núcleo importa más que asignar RAM excesiva.
- `view-distance=8` y `simulation-distance=6`.
- No volver a pregenerar el terreno exportado por WorldPainter.
- World border centrado en 0,0 y diámetro 21.800.
- Mantener `nerf-spawner-mobs: false` en `spigot.yml`; la fortaleza usa IA de
  criaturas generadas por Trial Spawners.
- Probar MSPT/TPS con 10, 20 y 24 bots o jugadores antes del lanzamiento (24 es el tope real de `max-players`).
- No instalar plugins de limpieza automática de entidades sin revisar primero
  si eliminan mobs persistentes, proyectiles o recompensas de Trial Chambers.
- Instalar `spark` para medir MSPT, CPU, memoria y chunks antes de ajustar.
- Usar Chunky solamente si queda terreno vanilla sin pregenerar. No ejecutarlo
  sobre los 22.000 × 22.000 ya exportados por WorldPainter.
- FastAsyncWorldEdit se usa para importar la fortaleza y luego queda limitado a
  administradores; no es un sustituto de una pregeneración bien hecha.


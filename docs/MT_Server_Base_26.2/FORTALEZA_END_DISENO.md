# Fortaleza personalizada del End

Este documento define la estructura que se colocará cuando el mundo haya sido
exportado. No debe generarse aleatoriamente.

## Recorrido

1. Acceso exterior sin edificios visibles en superficie: entrada por caverna.
2. Galería de guardianes: esqueletos, zombis y silverfish.
3. Biblioteca quebrada: evocadores limitados y vex.
4. Tres salas de sello con Trial Spawners.
5. Cámara ominosa: combate escalado por cantidad de jugadores.
6. Sala del portal: se desbloquea al completar los tres sellos.

## Reglas

- Proteger toda la región contra romper y colocar bloques hasta completar el
  encuentro. Permitir abrir cofres únicamente donde corresponda.
- No dejar mobs normales cargados antes de la llegada del grupo.
- Usar Trial Spawners o activadores por zona.
- Evitar más de 35 criaturas activas simultáneamente dentro de la fortaleza.
- El portal del End permanece físicamente inaccesible durante la primera semana.
- Warden excluido: pertenece a las ciudades antiguas, no a esta fortaleza.

## Pendiente al disponer del servidor

> **Actualizado 2026-10-22.** El servidor ya está disponible y las coordenadas **ya están
> elegidas**: arena de Hitogami (trial chamber principal) en `(-2304, -16, -3008)`, con la
> fortaleza/portal al End detrás de esa arena. Lo que falta es **construir/pegar** la
> estructura, no elegir dónde va. El `.schem` de la arena **sigue sin decidir** (decisión del
> usuario) y `compact_trial_chamber.ofs0.schem` **no existe en disco** (medido 2026-10-22).
> El portal **no existe como bloque** hasta que muera el jefe: eso es correcto y sigue vigente.
> Detalle: `INSTRUCCIONES\TRIAL_CHAMBERS_COORDENADAS.md` y
> `mundo\PLAN_TRIAL_CHAMBER_JEFE_PORTAL_END_2026-10-21.md`.

- ~~Elegir coordenadas definitivas bajo una cordillera del Continente Central.~~ **Hecho:**
  `X=-2304, Y=-16, Z=-3008` (ancla de paste, piso/centro de la arena).
- Construir o importar la estructura y verificar que ninguna sala quede expuesta.
- Configurar protección regional y estados de puertas.
- Ensayar el encuentro con grupos de 4, 10 y 20 jugadores.


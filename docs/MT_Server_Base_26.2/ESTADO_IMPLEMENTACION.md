# Estado real de implementación

## Listo para copiar y probar

- Datapack de frontera, etapas y bloqueo de lanzamiento.
- Cielo/niebla gris del End para Java y Bedrock.
- Mapeos Geyser v2 y modelos de siete reliquias.
- Recompensas adicionales de Trial Chambers mediante advancements del datapack.
- Skript de Siete Potencias con propietarios por UUID.
- Skript de enfermedades con efectos vanilla.
- Grupos y permisos LuckPerms.
- Reglas y procedimiento de Towny para facciones y alianzas fijas.

## Requiere el hosting o una decisión final

- Instalar los JAR enumerados en `DESCARGAS_OFICIALES.md`.
- Aplicar los fragmentos sobre las configuraciones que genere cada JAR.
- Crear Orsted Corp y Apóstoles como naciones desde dos pueblos capitales de
  administración; Towny no debe inicializarse sin su base de datos real.
- Publicar el pack Java y completar su URL.
- Exportar/subir el mundo final y colocar Celestara/Chaos Breaker.
- Elegir coordenadas de estructuras, arena y salón de Potencias.
  - **Actualizado 2026-10-22:** las coordenadas de las **10 trial chambers secundarias SC1–SC10** ya están elegidas y **colocadas** en el server oficial — dato **reportado por el usuario**, *no verificable desde este repo* (los `.log` de `servidor\logs\` no registran `//paste` ni los schematics; grep 2026-10-22: 0 coincidencias). Sigue pendiente la **arena de Hitogami** (trial chamber principal) en `(-2304, -16, -3008)` y el **portal al End** detrás de ella. El `.schem` de la arena **no está decidido**: `compact_trial_chamber.ofs0.schem` **no existe en disco** (medido 2026-10-22) y `large_vanilla_trial_chamber.schem` tiene **0 block entities** (vaults/spawners solo en la paleta). Ver `INSTRUCCIONES\TRIAL_CHAMBERS_COORDENADAS.md`.
- Adaptar LevelledMobs al esquema exacto del build instalado.

No se incluye `MushokuCore.jar`. La carpeta homónima contiene el contrato de
configuración para una futura migración desde Skript, no un plugin ejecutable.

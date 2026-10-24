# Contrato para MushokuCore

Esta carpeta no contiene un JAR. Define la configuración que deberá consumir
el plugin propio cuando se migre la lógica temporal de Skript.

Módulos previstos:

- `ranks`: Potencias por UUID, daño a jefes y rankings.
- `factions`: Towny + dos naciones fijas, cupos y bloqueos.
- `diseases`: contagio, inmunidad temporal y curas.
- `relics`: entrega segura y auditoría de reliquias.
- `bedrock_bridge`: comandos/forms opcionales sin mecánicas exclusivas.

Hasta entonces, `potencias.sk`, `enfermedades.sk`, Towny, LuckPerms y el
datapack implementan el lanzamiento sin prometer automatizaciones inexistentes.

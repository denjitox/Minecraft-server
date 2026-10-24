# Matriz de compatibilidad Java–Bedrock

| Sistema | Java | Bedrock por Geyser | Decisión |
| --- | --- | --- | --- |
| Towny, claims y chat | Sí | Sí, por comandos | Sin GUI obligatoria |
| Siete Potencias | Sí | Sí | Texto simple y UUID |
| Enfermedades | Sí | Sí | Solo efectos vanilla |
| Reliquias | Modelo del pack Java | Mapeo v2 + pack Bedrock | Dos packs separados |
| Cielo del End | `end_sky.png` | Fog/biome del pack Bedrock | Probar tras cada update |
| Mano secundaria | Sí | Limitada | Ninguna mecánica depende de ella |
| Glowing de entidades | Sí | No fiable | Solo decorativo, nunca objetivo |
| Encantamientos propios | Posibles | No fiables | No se utilizan |
| Yunque/smithing custom | Posibles | No equivalentes | No se utilizan |
| Chat clickeable | Sí | Irregular | Siempre existe comando alternativo |
| Banners complejos | Sí | Capas limitadas | Estandartes de facción: máximo 6 capas |

## Prueba mínima antes de abrir

1. Conectar un cliente Java y uno Bedrock actuales.
2. Reclamar/unclaim un bloque, invitar y expulsar un miembro de prueba.
3. Recibir cada reliquia y verificar textura, nombre, uso y reconexión.
4. Aplicar cada enfermedad y confirmar duración y curación con leche.
5. Comprobar TAB, prefijos y `/potencias` desde mando/táctil.
6. Viajar al End únicamente en el entorno de ensayo y comprobar la niebla.

Si una actualización de Bedrock rompe el pack, se desactivan temporalmente los
modelos personalizados; los objetos siguen siendo ítems vanilla con nombre y
lore, por lo que no se pierde el inventario ni la progresión.

El pack incluye `client_biomes/the_end.biome_client.json`, formato recomendado
desde Bedrock 1.21.40, y conserva `biomes_client.json` como fallback legado.

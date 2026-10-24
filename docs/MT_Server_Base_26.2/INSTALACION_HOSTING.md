# Instalación en el hosting

## 1. Software

- Elegir Paper 26.2 con Java 25 o posterior.
- Instalar el último Geyser-Spigot.
- Instalar el último Floodgate-Spigot.
- Instalar LevelledMobs compatible con Paper 26.2.
- Instalar Skript 2.16.2 o el último build estable compatible con 26.2.
- Instalar LuckPerms, PlaceholderAPI, TAB y DecentHolograms.
- Instalar Towny 0.103.2.x o posterior compatible con 26.2 y TownyChat. No
  instalar otro plugin de claims en paralelo.
- Instalar CoreProtect e InventoryRollbackPlus para moderación y recuperación.
- Instalar FastAsyncWorldEdit y WorldGuard para colocar/proteger la fortaleza;
  retirar o negar sus permisos a jugadores normales.
- Instalar Duels Optimised para el puesto Dios de la Espada.
- ajLeaderboards es recomendado para rankings secundarios y estadísticas.
- Instalar ViaVersion (obligatorio con el Geyser actual). Geyser emula un
  cliente Java 26.3 mientras el servidor corre Paper 26.2; sin ViaVersion el
  aviso de versión queda activo y las cuentas Bedrock pueden fallar al conectar.
  Con ViaVersion instalado el aviso desaparece. Si más adelante Geyser y Paper
  vuelven a coincidir, se puede retirar.

Enlaces oficiales y endpoints dinámicos: `DESCARGAS_OFICIALES.md`.

## 2. Primer inicio

Iniciar el servidor una vez para generar sus configuraciones y apagarlo de
forma normal. No subir todavía el mundo definitivo.

Copiar los valores de `configuracion/server.properties.inicial` al archivo
generado por el host. No reemplazar a ciegas propiedades nuevas de Paper.

En `bukkit.yml`, establecer:

```yaml
settings:
  allow-end: true
```

> **Actualizado 2026-10-22.** Antes este documento decía `allow-end: false`. La decisión del
> usuario (2026-10-17) es **`allow-end: true`**, y está así en
> `configuracion\bukkit.yml.fragmento-inicial` y `servidor\bukkit.yml` (medido en este turno;
> la copia de `ENTREGA_Loding_20261017\03_configuracion\` **ya no existe en disco**). Es seguro
> porque **no existe ningún bloque `end_portal`** hasta que muera el jefe de la trial chamber:
> nadie llega al End antes. Ese portal **sigue pendiente**, junto con la arena de Hitogami
> (ver `INSTRUCCIONES\TRIAL_CHAMBERS_COORDENADAS.md`).

## 3. Geyser y Floodgate

Aplicar manualmente los valores de
`plugins/Geyser-Spigot/configuracion-recomendada.yml` al `config.yml` generado
por la versión instalada. Sustituir `PUERTO_UDP_DEL_HOST`.

El hosting debe permitir tráfico UDP. El puerto Bedrock no puede compartirse
con voice chat, Query u otro servicio UDP.

Copiar `MT_Void_Bedrock.mcpack` a:

```text
plugins/Geyser-Spigot/packs/
```

Copiar `plugins/Geyser-Spigot/custom_mappings/mushoku_relics.json` a:

```text
plugins/Geyser-Spigot/custom_mappings/
```

En el archivo generado por Geyser confirmar `enable-custom-content: true`.
Reiniciar por completo; `/reload` no registra correctamente los ítems Bedrock.

## 4. Mundo y datapack

Subir el mundo exportado como `world`. Antes del primer ingreso, copiar
`datapacks/mt_core` dentro de `world/datapacks/`.

Iniciar y ejecutar en consola:

```text
function mt:setup
```

Comprobar con:

```text
datapack list
worldborder get
difficulty
```

## 5. Resource pack Java

Subir `MT_Void_Java.zip` a una URL HTTPS de descarga directa y completar
`resource-pack`. La plantilla ya contiene su UUID y SHA-1. Si se modifica o se
vuelve a comprimir el pack, hay que calcular un SHA-1 nuevo.
El pack queda opcional inicialmente para no bloquear el acceso por un error de
descarga.

## 6. Apertura de dimensiones

Al cumplirse la semana, apagar el servidor y seguir
`comandos/ABRIR_DIMENSIONES_DIA_8.md`.

## 7. Verificación

- Probar una cuenta Java y una Bedrock.
- Ejecutar `geyser connectiontest IP PUERTO_UDP` desde la consola.
- Comprobar cielo del End en ambas ediciones.
- Confirmar que el portal no sea accesible antes de abrir el End.
- Mantener una copia del mundo antes de colocar la fortaleza final.
- Confirmar que ViaVersion cargue sin errores y que el aviso de versión de
  Geyser ya no aparezca en el log.
- Ejecutar `sk reload potencias` y comprobar que no existan errores de sintaxis.
- Ejecutar `sk reload enfermedades` y comprobar que no existan errores.
- Ejecutar los comandos de `plugins/LuckPerms/CREAR_GRUPOS.txt` una sola vez.
- Seguir `plugins/Towny/CONFIGURACION.md` para crear las dos naciones fijas.
- Probar `/potencias` desde una cuenta Java y una Bedrock.
- Abrir una recompensa normal y una ominosa de Trial Chambers en el servidor de
  pruebas, y confirmar que las reliquias conservan nombre/modelo al reconectar.
- Probar `/mtestado` y los efectos de enfermedad desde Java y Bedrock.


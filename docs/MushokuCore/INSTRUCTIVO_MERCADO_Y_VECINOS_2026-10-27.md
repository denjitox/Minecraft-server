# Mercado y vecinos: compilar, desplegar y probar

Fecha: 2026-10-27
Repo de trabajo: `C:\Dev\Minecraft_server\Minecraft server`
Repo de la PC principal: `C:\Dev\Minecraft-server`

## Estado al 2026-10-28 00:00

Lo que pasó, en orden:

1. **Build oficial y despliegue inicial (2026-10-27 22:58).** `build.ps1` en la PC principal: exit 0, 148 clases, jar de **320.497 B**, SHA256 `130B2603…351C3B`. Subida con WinSCP por el patrón `.new`, verificada bajando cada archivo antes del `mv`:

| Archivo remoto | Bytes | Backup del anterior |
|---|---:|---|
| `/plugins/MushokuCore-1.0.0.jar` | 320.497 | `MushokuCore-1.0.0.jar.bak-20261027T2258Z` (248.440 B) |
| `/plugins/MushokuCore/config.yml` | 6.587 | `config.yml.bak-20261027T2258Z` (5.595 B) |
| `/plugins/MushokuCore/messages.yml` | 7.629 | `messages.yml.bak-20261027T2258Z` (5.142 B) |

El diff previo contra el live mostró solo agregados: ninguna línea que existiera en producción se perdió.

2. **Primer arranque con los módulos (2026-10-27 23:25), reinicio hecho por el usuario.** Backup `mushoku_20261028T022603Z_pre-migracion.db`, `V003` aplicada, log con `Modulo 'market' habilitado: 5 categoria(s), 30 item(s).` y `Modulo 'npcs' habilitado: 12 definicion(es), 0 ubicado(s).`, cero `SEVERE` de MushokuCore.

3. **Segundo build y despliegue (2026-10-27 23:55).** `NpcEntityFactory` pasó a la API tipada de `Mannequin` y se agregó `npcs-skins.yml` con las 12 skins resueltas por Mojang. Jar de **328.312 B**, SHA256 `5127B290…5357540`, desplegado y verificado por descarga. Backup del jar anterior: `MushokuCore-1.0.0.jar.bak-20261027T2355Z` (320.497 B).

**El servidor sigue corriendo el jar de 320.497 B.** Paper no reemplaza plugins en caliente: hasta el próximo reinicio no hay mannequins ni skins, solo aldeanos de respaldo.

Sigue sin hacerse:

- **Prueba in-game del mercado y de los vecinos.** Nadie compró, habló ni clickeó un NPC todavía.
- **Prueba de Bedrock.** El fuente de Geyser 2.12 dice que la skin se resuelve y se manda por la lista de jugadores, pero `setDescription` está en `// TODO`, así que en Bedrock se espera skin y nombre sin la línea de título. [A VERIFICAR con cliente real]
- **Outbox de LuckPerms en producción**: `agregarGrupo`/`quitarGrupo` devuelven `false` (`luckperms_outbox` con `attempts=50`, `done=0`). Causa sin determinar.

Lo que sí está verificado al 2026-10-27:

| Verificación | Resultado |
|---|---|
| **Compilación** | Los **113 fuentes** compilan con `javac 25.0.5` y `--release 25` sin errores ni warnings de código, contra `paper-api 1.21.9`, adventure 4.25.0, Towny 0.103.2.0 y PlaceholderAPI 2.11.6. Se empaquetaron las 148 clases y los recursos: jar de prueba de **320.483 B** |
| **Migraciones** | `V001`, `V002` y `V003` se aplican en orden sobre SQLite con el mismo corte de sentencias que usa `MigrationRunner`. Quedan las 16 tablas esperadas, incluida `idx_market_purchases_player` |
| **SQL de los repositorios** | Los 6 `INSERT`/`SELECT` de `MarketRepository` y `NpcRepository` corren contra esas tablas, incluidos los dos `ON CONFLICT ... DO UPDATE`. `reputacion` devuelve el valor pisado (9, no 5) y `ultimoRegalo` devuelve la fecha pisada |
| YAML | `market.yml`, `npcs.yml`, `config.yml`, `messages.yml` y `plugin.yml` parsean sin error (librería `yaml` de Node) |
| Materiales | Los 52 materiales distintos usados en el catálogo y en regalos/trueques existen en la API 1.21.9 |
| Slots del menú | 5 categorías en slots 10 a 14, sin repetidos |
| Catálogo | 5 categorías, 30 ofertas, `amount` y `price-diamonds` en rango y `amount` dentro del stack del material |
| Vecinos | 12 definiciones, todas con diálogo neutral, 44 referencias de material en regalos y trueques |
| Claves de mensaje | Las 41 claves `mercado.*` y `vecinos.*` usadas en el código existen en `messages.yml` (la única coincidencia suelta, `mercado.compra`, es la acción de auditoría, no una clave) |
| API de la skin | `Mannequin#setProfile(io.papermc.paper.datacomponent.item.ResolvableProfile)`, `ResolvableProfile.resolvableProfile()` estático, `ResolvableProfile$Builder#addProperty(com.destroystokyo.paper.profile.ProfileProperty)` y `ProfileProperty(String,String,String)`, todo confirmado con `javap` sobre el jar de la API, no solo con la javadoc |

La primera validación se hizo en la notebook con un classpath armado a mano, porque ahí no existe `servidor\libraries`. Eso valida el código y las firmas, **no** reemplaza al build oficial. El jar de producción lo generó `build.ps1` en la PC principal el 2026-10-27, compilando contra el `paper-api` que resuelve solo (148 clases, 328.312 B). Los pasos del método están en el anexo A.

## 1. Qué se agregó

| Pieza | Ruta | Qué hace |
|---|---|---|
| Módulo mercado | `MushokuCore/src/main/java/.../module/market/` | GUI de categorías y ofertas, compra con diamantes del inventario, `/mercado` |
| Módulo vecinos | `MushokuCore/src/main/java/.../module/npcs/` | Entidad con skin, diálogo, regalos con cooldown, reputación, trueques, `/vecino` |
| Repositorios | `database/repository/MarketRepository.java`, `NpcRepository.java` | Compras, reputación y regalos |
| Utilidades | `util/InventarioUtil.java`, `util/Componentes.java` | Contar y quitar items, pasar texto legacy a Adventure |
| Migración | `resources/migrations/V003__market_npcs.sql` | Tablas `market_purchases`, `npc_reputation`, `npc_gifts` |
| Catálogo | `resources/market.yml` | Precios en diamantes; solo vende, no compra |
| Vecinos | `resources/npcs.yml` | 12 NPC con diálogo, regalo y trueques |
| Skins | `docs/perugius/skins/resolver_skins_mojang.ps1` | Resuelve las 12 skins por Mojang y arma `npcs-skins.yml` (value + signature) |

Decisiones de diseño que conviene tener presentes:

- La moneda son **diamantes físicos** del inventario. No hay Vault, ni saldo virtual, ni venta al mercado.
- Los precios están en YAML, no en base de datos. `market_purchases` es solo auditoría.
- Las coordenadas de los NPC **no** están en `npcs.yml`. Se guardan en `plugins/MushokuCore/npcs-locations.yml` cuando corrés `/vecino colocar <id>`.
- Matrimonio, hijos y genética de MCA quedan fuera: dependen del mod del cliente y no funcionan en Bedrock.

## 2. Compilar en la PC principal

Requisitos: JDK 25 en `C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot` y la carpeta `libraries` de Paper. En la PC principal el repo `C:\Dev\Minecraft-server` no tiene `servidor\` al lado: `build.ps1` resuelve la tercera ruta de la lista, `C:\Dev\OPENCLAUDIO\minecraft_server\servidor\libraries`, y de ahí sale también `paper-api-26.2.build.134-stable.jar` (el mismo Paper que corre el servidor local).

`build.ps1` **tiene que correr en un proceso PowerShell hijo**. Si lo invocás en línea, el aviso de `javac` sobre API deprecada que va a stderr corta la corrida y no se arma el jar.

```powershell
cd C:\Dev\Minecraft-server
powershell -NoProfile -ExecutionPolicy Bypass -File .\MushokuCore\build.ps1 *> "$env:TEMP\build_market_npcs.log"
Get-Content "$env:TEMP\build_market_npcs.log" -Tail 25
Get-ChildItem .\MushokuCore\build\libs | Select-Object Name,Length,LastWriteTime
```

Señales de que salió bien:

- Exit code 0 y la tabla final con `Jar`, `Bytes` y `Sha256`.
- `build\libs\MushokuCore-1.0.0.jar` con fecha de hoy y más bytes que el jar anterior (248.440 B al 2026-10-27).

Si `javac` falla, el error real está en el log. Los puntos más probables, por ser código nuevo:

| Síntoma | Causa | Qué hacer |
|---|---|---|
| `cannot find symbol: class Mannequin` | Alguien reemplazó la reflexión por uso directo | Volver a la versión por `Class.forName` |
| `cannot find symbol: method trueque(...)` | Se esperaba `void` | `NpcsService.trueque` devuelve `boolean` desde esta versión |
| `incompatible types: ... NpcHolder` | Cambió el holder de la GUI | Revisar `NpcGui.manejarClick` |
| `package net.kyori.adventure.text.serializer.legacy does not exist` | Falta el artefacto `adventure-text-serializer-legacy` en el classpath | Ya resuelto: ver abajo |

**El primer build oficial falló por esta última razón** y el error no se veía: `Componentes.java` importa `LegacyComponentSerializer`, que vive en `net\kyori\adventure-text-serializer-legacy` y no en `adventure-api`. En la notebook compilaba porque ahí el classpath se armó a mano con ese jar; el `build.ps1` no lo pedía y javac moría con exit 1 mientras el script solo decía `javac fallo con exit code 1`, porque la salida quedaba detrás de `Write-Verbose`.

Dos cambios en `build.ps1` (2026-10-27):

1. Se agrega `adventure-text-serializer-legacy` a la lista de dependencias resueltas por patrón, igual que las demás. Paper lo trae en runtime, así que es solo de compilación.
2. `Invoke-Nativo` ahora devuelve código y salida, y `Mostrar-Fallo` mete las últimas 40 líneas en el `throw`. Un fallo de `javac` o `jar` se lee sin pasar `-Verbose`.


## 3. Copiar los YAML al data folder

Paper **no vuelve a extraer** un recurso del jar si el archivo ya existe en `plugins/MushokuCore/`. Con un plugin ya instalado, esto aplica a los cinco archivos.

| Archivo | ¿Obligatorio? | Motivo |
|---|---|---|
| `messages.yml` | **Sí** | Sin las secciones `mercado.*` y `vecinos.*`, los comandos muestran la clave cruda |
| `config.yml` | Recomendado | Agrega `modules.market`, `modules.npcs`, `market.max-purchases-per-minute` y `npcs.*`. Sin copiarlo, los defaults del código ya son sanos (módulos encendidos, 10 compras por minuto, rutina 20 s), pero no vas a ver las claves para ajustarlas |
| `market.yml` | No | Se extrae solo si no existe. Copialo si querés reemplazar precios ya editados |
| `npcs.yml` | No | Igual que `market.yml` |
| `npcs-skins.yml` | Opcional | Lo genera el script del paso 7 |
| `npcs-locations.yml` | No | Lo crea `/vecino colocar` |

**Hecho el 2026-10-27:** en el hosting se subieron `config.yml` y `messages.yml` por SFTP con el mismo patrón del paso 4. Antes se comparó el live contra el repo línea por línea: las únicas diferencias eran agregados, así que no se perdió ninguna personalización.

Si en algún momento trabajás contra una copia local del servidor, el equivalente es:

```powershell
$origen = "C:\Dev\Minecraft-server\MushokuCore\src\main\resources"
$destino = "<ruta del servidor>\plugins\MushokuCore"
Copy-Item "$origen\messages.yml" $destino -Force
Copy-Item "$origen\config.yml"   $destino -Force
```


## 4. Desplegar el jar

**Hecho el 2026-10-27** con WinSCP y sesión guardada, desde la PC principal. El patrón que funcionó, para repetirlo:

1. `put` de los tres archivos con extensión `.new` (jar, `config.yml`, `messages.yml`).
2. `get` de cada `.new` y comparación de SHA256 con el local. Si no coincide, no se sigue.
3. `mv <archivo> <archivo>.bak-<sello>` y después `mv <archivo>.new <archivo>`.
4. `get` de los tres archivos ya en su lugar y comparación de SHA256 otra vez.

Por qué así: `mv` falla si el destino existe, y `subir_sftp.py` reanuda por tamaño y puede dejar un archivo distinto con el mismo nombre parcial. El script que se usó es `reemplazar_sftp_verificado.py`, del árbol de la PC principal, con ese mismo orden.

El reinicio **lo hace el usuario** desde el panel: por SFTP no se puede y el jar viejo sigue cargado hasta que el proceso arranque de nuevo.


## 5. Verificar el arranque

En la consola, después del reinicio:

```
Modulos habilitados: [ranks, diseases, factions, regional-difficulty, market, npcs, commands]
Modulo 'market' habilitado: 5 categoria(s), 30 item(s).
Modulo 'npcs' habilitado: 12 definicion(es), 0 ubicado(s), rutina cada 20s.
```

Si `npcs` aparece con `0 definicion(es)`, `npcs.yml` no se leyó: revisá que exista en `plugins/MushokuCore/` y que el log no diga `npcs.yml no encontrado`.

Sobre la migración:

- La primera vez, el log de base de datos informa la aplicación de `V003`.
- Si `V003` falla, el plugin se deshabilita entero. Restaurá el backup de `mushoku.db` y revisá el error completo antes de tocar nada.
- Una segunda corrida debe decir `esquema al dia` y no volver a aplicarla.

Después, en juego o por consola:

```
/mushoku estado
```

Tiene que listar `market` y `npcs` entre los módulos activos.

## 6. Probar el mercado

| Paso | Resultado esperado |
|---|---|
| `/mercado` | Menú de 27 slots con 5 categorías y un botón de cerrar |
| Click en "Bloques" | Lista de ofertas con precio y cantidad; los slots 45 a 53 son relleno |
| Click en una oferta sin diamantes | Mensaje de falta de diamantes y la compra no se cobra |
| Click con diamantes suficientes | Se descuentan los diamantes, el item aparece en el inventario y el chat confirma |
| Click con el inventario lleno | Aviso de falta de espacio y no se cobra nada |
| 11 compras en menos de un minuto | A partir de la 11, aviso de espera |
| Shift click o arrastre dentro del menú | Se cancela; no se puede mover nada del cofre virtual |
| `/mercado comprar ladrillos-piedra` | Compra directa sin abrir la GUI (útil en Bedrock) |
| `/mercado recargar` | Relee `market.yml`; requiere `mushoku.admin.mercado` |

Comprobación en base: cada compra deja una fila en `market_purchases` con jugador, item, cantidad, precio y fecha.

## 7. Probar los vecinos

Todavía no hay coordenadas cargadas: los 12 están definidos pero sin ubicar. Colocalos vos, en el lugar real de la fortaleza.

| Paso | Resultado esperado |
|---|---|
| `/vecino lista` | 12 entradas, todas en "sin ubicar" |
| Ir al punto elegido y correr `/vecino colocar perugius` | Se crea la entidad, se escribe `npcs-locations.yml` y el chat confirma mundo y coordenadas |
| `/vecino info perugius` | Título, si tiene skin, cantidad de regalos y trueques, y la ubicación |
| Click derecho sobre el NPC | Línea de diálogo según reputación |
| Click derecho con un regalo en la mano | Consume el item, suma reputación y arranca el cooldown |
| Repetir el regalo antes del cooldown | Aviso de que ya lo recibió; el item no se consume |
| Shift click sobre el NPC | Abre la pantalla de trueques |
| Click en un trueque sin el costo | Aviso de lo que falta y la pantalla queda abierta |
| Click en un trueque con el costo | Se cobra, se entrega y la pantalla se refresca con los conteos nuevos |
| `/vecino hablar perugius` | Mismo diálogo que el click derecho |
| `/vecino trueques perugius` | Misma pantalla que el shift click |
| `/vecino reputacion perugius` | Valor y nivel (alta, neutral o baja) |
| Intentar pegarle al NPC | El daño se cancela |
| `/vecino quitar perugius` | Desaparece y se borra del YAML de ubicaciones |

Detalles que conviene saber:

- El bloque debajo de los pies del NPC también abre el diálogo. Sirve cuando el cliente no puede clickear la entidad.
- Si el NPC no tiene `skin.value`, se crea un **aldeano** con el nombre visible (sin la línea de título, que solo tiene el mannequin). Es el respaldo cuando falta la skin.
- Si `Mannequin` existe pero el perfil falla, el log deja un warning con el id y el motivo (`no se pudo crear el mannequin con skin (...)`). Se usa aldeano y el resto funciona igual.
- `/vecino recargar` relee `npcs.yml` y `npcs-locations.yml`, vuelve a spawnear y elimina la entidad de un NPC que ya no esté en el YAML.

Permisos:

| Permiso | Para qué |
|---|---|
| `mushoku.mercado.use` | Abrir el mercado y comprar |
| `mushoku.admin.mercado` | `/mercado recargar` |
| `mushoku.vecino.use` | Hablar, trueques y consultar reputación |
| `mushoku.admin.vecinos` | `colocar`, `quitar`, `recargar` |

## 8. Skins de los vecinos (resueltas y desplegadas el 2026-10-27 23:55)

El base64 de una skin **no contiene el PNG**: es un JSON con una URL que el cliente, o Geyser, descarga al ver al NPC. La idea original era publicar los 12 PNG o subirlos a Mineskin. No hizo falta: las 12 skins de `FUENTES.md` salieron de cuentas reales, y el sessionserver de Mojang devuelve la propiedad `textures` ya firmada.

Script: `docs/perugius/skins/resolver_skins_mojang.ps1`

1. Por cada id resuelve la cuenta, descarga la textura y compara el SHA256 con el documentado.
2. Si coincide, usa el `value` y la `signature` firmados por Mojang.
3. Si la cuenta cambió de skin, arma la propiedad sin firma apuntando a la textura original en `textures.minecraft.net`. Ojo con el detalle del CDN: la URL usa el SHA256 **sin ceros a la izquierda**, o sea `09df...` se sirve como `/texture/9df...`.
4. Si nada de eso responde, termina con código 1 sin escribir el YAML.

Resultado del 2026-10-27: **12 de 12**. Once firmadas por Mojang y `rudeus` sin firma, porque la cuenta `Rudeus` cambió de skin después del 2026-10-18 y la textura original sigue publicada en el CDN. Cada URL se verificó bajándola y comparando el hash del archivo.

El archivo quedó en `MushokuCore\src\main\resources\npcs-skins.yml` (viaja en el jar) y desplegado en `plugins/MushokuCore/npcs-skins.yml`, 14.038 B, SHA256 `3E3E94CE8C3BF92E...`.

```powershell
# regenerar (no requiere clave ni cuenta)
powershell -NoProfile -ExecutionPolicy Bypass -File .\docs\perugius\skins\resolver_skins_mojang.ps1

# resolver y verificar sin escribir
powershell -NoProfile -ExecutionPolicy Bypass -File .\docs\perugius\skins\resolver_skins_mojang.ps1 -SoloProbar
```

Para aplicarlo sin reiniciar: `/vecino recargar`. Para volver a aldeano: borrar `npcs-skins.yml` del data folder, o dejar los `value` vacíos, y recargar.

`generar_skins.ps1` (publicar PNG propios con `-PublicBaseUrl`) sigue existiendo como alternativa, pero ahora se niega a pisar un `npcs-skins.yml` que ya tiene valores, salvo que se pase `-Force`.

Sobre Bedrock, corregido después de leer el fuente de Geyser 2.12: `MannequinEntity.setProfile` llama a `AvatarEntity.setSkin`, que resuelve la propiedad estática y descarga la textura. `isListed()` en `false` solo cambia el paquete con el que viaja la skin. Lo que Geyser no implementa es `setDescription`, que está en `// TODO`: en Bedrock se ve la skin y el nombre, pero no la línea de título. Falta confirmarlo con un cliente real.

## 8.1 Mannequin o Citizens: decisión y refactor

Se evaluaron las dos opciones. Ganó **Mannequin vanilla**, por estas razones:

- En `paper-api 26.2` la API es tipada y estable (`org.bukkit.entity.Mannequin`, `ResolvableProfile`, `ProfileProperty`). La reflexión que tenía `NpcEntityFactory` era prevención nuestra, no una limitación de la API.
- Citizens no desbloquea las skins: `/npc skin <nombre>` solo sirve para skins de cuentas existentes, y para PNG propios exige `/npc skin --url <png público>` o un blob de Mineskin. Es el mismo requisito que ya resolvimos con Mojang.
- Mantener un segundo store (los saves de Citizens) además de SQLite, más reescribir creación, colocación, borrado y carga, no se justifica para 12 NPC que solo hablan y venden.
- Citizens seguiría siendo la opción si algún día los NPC tienen que caminar, porque su pathfinding está probado.

Cambios en el código:

- `NpcEntityFactory` ya no usa reflexión. Crea `Mannequin` con `setProfile(ResolvableProfile)`, `setImmovable(true)` y `setDescription(Component)`, y marca la entidad como persistente.
- El fallback a aldeano deja un warning con el id y el motivo, en vez de caer en silencio.
- Si el jar llegara a correr sin la entidad, el `LinkageError` se atrapa y queda el aldeano.

Build y despliegue del 2026-10-27 23:55: jar de **328.312 B**, SHA256 `5127B2909F4BAC63...`, 148 clases, con `npcs-skins.yml` adentro. Backup del jar anterior en `plugins/MushokuCore-1.0.0.jar.bak-20261027T2355Z` (320.497 B).

## 9. Rollback

El hosting tiene dos puntos de retorno:

| Backup | Bytes | A qué vuelve |
|---|---:|---|
| `/plugins/MushokuCore-1.0.0.jar.bak-20261027T2355Z` | 320.497 | Jar con mercado y vecinos, `NpcEntityFactory` por reflexión, sin `npcs-skins.yml` |
| `/plugins/MushokuCore-1.0.0.jar.bak-20261027T2258Z` | 248.440 | Jar anterior a los módulos nuevos |

1. Detené el servidor desde el panel.
2. Mové o renombrá el jar actual y renombrá el backup elegido a `MushokuCore-1.0.0.jar`.
3. Los YAML anteriores quedaron como `config.yml.bak-20261027T2258Z` y `messages.yml.bak-20261027T2258Z` en `/plugins/MushokuCore/`. `npcs-skins.yml` no existía antes: para anularlo alcanza con borrarlo.
4. Si querés apagar solo los módulos nuevos sin volver el jar, en `plugins/MushokuCore/config.yml`:

```yaml
modules:
  market: false
  npcs: false
```

5. Reiniciá.

La migración `V003` no se revierte sola. Las tres tablas nuevas quedan vacías y no afectan a los módulos viejos, así que no hace falta bajarlas.


## 10. Pendientes después de la prueba

- **Reiniciar el panel** para que cargue el jar de 328.312 B (el de mannequins tipados y skins).
- Colocar un NPC, mirarlo desde Java y desde Bedrock, y confirmar en el log que no cayó a aldeano.
- Confirmar si en Bedrock falta la línea de título, como dice el fuente de Geyser.
- Ajustar precios de `market.yml` con datos de economía real (cuántos diamantes circulan por hora).
- Definir si la economía suma más adelante una fuente de ingreso, porque hoy el mercado solo retira diamantes.
- Investigar el outbox de LuckPerms en producción (`attempts=50`, `done=0`).

## Anexo A. Cómo se compiló sin el servidor

Método reproducido en la notebook el 2026-10-27. Sirve para volver a verificar el código cuando no hay `servidor\libraries` a mano.

1. JDK 25 de Adoptium, descomprimido en `%TEMP%\opencode\jdk\jdk-25.0.5+8`. `javac -version` responde `javac 25.0.5`.
2. Dependencias descargadas a `%TEMP%\opencode\libs`:

| Jar | Origen |
|---|---|
| `paper-api.jar` | `repo.papermc.io`, `io.papermc.paper:paper-api:1.21.9-R0.1-20251007.115206-20` |
| `adventure-api`, `adventure-key`, `adventure-text-serializer-legacy`, `adventure-text-serializer-plain` | `repo.papermc.io`, snapshot 4.25.0 (`4.25.0-20251006.123956-6`) |
| `examination-api`, `examination-string` | Maven Central 1.3.0 |
| `guava` 33.3.1, `gson` 2.11.0, `snakeyaml` 2.2, `joml` 1.10.8, `fastutil` 8.5.15, `log4j-api` 2.24.1, `slf4j-api` 2.0.16, `jspecify` 1.0.0, `checker-qual` 3.49.2 | Maven Central |
| `bungeecord-chat` `1.21-R0.2-deprecated+build.21` | `repo.papermc.io`. Sin este jar, `PluginBootstrap` no compila: `cannot access BaseComponent` |
| `placeholderapi` 2.11.6 | `repo.extendedclip.com/releases` (no está en Maven Central) |
| `Towny-0.103.2.0.jar` | release `0.103.2.0` de GitHub, adentro de `Towny.Advanced.0.103.2.0.zip` |

3. Compilación y empaquetado, igual que `build.ps1` pero escribiendo fuera del repo:

```powershell
$cp = (Get-ChildItem $libs -Filter *.jar -Recurse | ForEach-Object FullName) -join ';'
$sources = Get-ChildItem .\MushokuCore\src\main\java -Recurse -Filter *.java | ForEach-Object FullName
& javac -encoding UTF-8 --release 25 -classpath $cp -d $out @sources
Copy-Item .\MushokuCore\src\main\resources\* $out -Recurse -Force
& jar --create --file $outJar -C $out .
```

Resultado: `javac` exit 0, 148 clases, jar de 320.483 B con `market.yml`, `npcs.yml` y `migrations/V003__market_npcs.sql` adentro. Ese jar es **solo evidencia de que compila** y no se desplegó. El que corre en producción hoy es el que generó `build.ps1` en la PC principal a las 23:55: **328.312 B**, SHA256 `5127B2909F4BAC63624B304CA8688E0B2014E0C29676C4EC45AF4BD153357540`, con `npcs-skins.yml` adentro. La diferencia de tamaño contra el build de 22:58 (320.497 B) es el refactor a `Mannequin` tipado más el archivo de skins, no timestamps.

La verificación de la notebook se hizo **antes** de ese refactor, así que el primer defecto de la tabla de abajo ya no aplica: `NpcEntityFactory` no usa reflexión.

### Defectos que aparecieron al verificar

Ninguno lo detecta el compilador, y los tres se corrigieron antes de cerrar:

| Defecto | Cómo se detectó | Corrección |
|---|---|---|
| `Method.invoke` sobre `ResolvableProfile.Builder` habría tirado `IllegalAccessException` en runtime | El fuente de Paper `ver/1.21.9` muestra que la implementación (`BuilderImpl`) es package-private; `armador.getClass().getMethod(...)` devuelve un método declarado en esa clase | Buscar `addProperty` y `build` en la interfaz pública `ResolvableProfile$Builder` |
| La compra podía cobrar y dejar el item en el piso | `puedeEntregar` contaba los slots de armadura y mano secundaria como libres, y `PlayerInventory#addItem` no los usa | `InventarioUtil.puedeEntregar` mira `getStorageContents()` |
| `new ItemStack(material, amount)` con `amount` mayor al stack del material | El catálogo tenía `MUSHROOM_STEW` x4 y el estofado no se apila | `MarketItem.valido()` exige `amount <= getMaxStackSize()`; `npcs.yml` valida lo mismo en `entrega-cantidad`; el estofado pasó a x1 |


# Mercado y vecinos: compilar, desplegar y probar

Fecha: 2026-10-27
Repo de trabajo: `C:\Dev\Minecraft_server\Minecraft server`
Repo de la PC principal: `C:\Dev\Minecraft-server`

## Lo que NO se hizo todavía

Este documento describe pasos que **no se ejecutaron**:

- El plugin **nunca corrió con estos módulos**. No hay servidor levantado con el jar nuevo, ningún jugador vio el mercado ni un vecino, y no hay prueba en Java ni en Bedrock.
- El despliegue al hosting y el reinicio del panel quedan pendientes, y los hace el usuario.
- Las skins siguen sin publicar: los 12 PNG no están en disco ni en una URL pública, así que hasta que se complete el paso 8 todos los NPC salen como aldeano sin skin.
- La migración `V003` no se aplicó a la base de producción. Sí se aplicó, junto con `V001` y `V002`, a una base SQLite vacía en la notebook (ver abajo).

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

La compilación se hizo en la notebook con un classpath armado a mano, porque acá no existe `servidor\libraries`. Eso valida el código y las firmas, **no** reemplaza al build oficial: el jar de producción lo tiene que generar `build.ps1` en la PC principal, que compila contra el `paper-api` de `servidor\libraries`. Los pasos del método están en el anexo A.

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
| Skins | `docs/perugius/skins/generar_skins.ps1` | Descarga, verifica SHA256 y arma `npcs-skins.yml` |

Decisiones de diseño que conviene tener presentes:

- La moneda son **diamantes físicos** del inventario. No hay Vault, ni saldo virtual, ni venta al mercado.
- Los precios están en YAML, no en base de datos. `market_purchases` es solo auditoría.
- Las coordenadas de los NPC **no** están en `npcs.yml`. Se guardan en `plugins/MushokuCore/npcs-locations.yml` cuando corrés `/vecino colocar <id>`.
- Matrimonio, hijos y genética de MCA quedan fuera: dependen del mod del cliente y no funcionan en Bedrock.

## 2. Compilar en la PC principal

Requisitos: JDK 25 en `C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot` y la carpeta `servidor\libraries` de Paper al lado del repo.

`build.ps1` **tiene que correr en un proceso PowerShell hijo**. Si lo invocás en línea, el aviso de `javac` sobre API deprecada que va a stderr corta la corrida y no se arma el jar.

```powershell
cd C:\Dev\Minecraft-server
powershell -NoProfile -ExecutionPolicy Bypass -File .\MushokuCore\build.ps1 *> "$env:TEMP\build_market_npcs.log"
Get-Content "$env:TEMP\build_market_npcs.log" -Tail 25
Get-ChildItem .\MushokuCore\build\libs | Select-Object Name,Length,LastWriteTime
```

Señales de que salió bien:

- El log termina con el tamaño del jar, no en `== javac ==`.
- `build\libs\MushokuCore-1.0.0.jar` tiene fecha de hoy y más bytes que el jar actual (200.688 B al 2026-10-22).

Si `javac` falla, el error real está en el log. Los puntos más probables, por ser código nuevo:

| Síntoma | Causa | Qué hacer |
|---|---|---|
| `cannot find symbol: class Mannequin` | Alguien reemplazó la reflexión por uso directo | Volver a la versión por `Class.forName` |
| `cannot find symbol: method trueque(...)` | Se esperaba `void` | `NpcsService.trueque` devuelve `boolean` desde esta versión |
| `incompatible types: ... NpcHolder` | Cambió el holder de la GUI | Revisar `NpcGui.manejarClick` |

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

```powershell
$origen = "C:\Dev\Minecraft-server\MushokuCore\src\main\resources"
$destino = "C:\Dev\Minecraft-server\servidor\plugins\MushokuCore"
Copy-Item "$origen\messages.yml" $destino -Force
Copy-Item "$origen\config.yml"   $destino -Force
```

Si el servidor del panel es otra copia, apuntá `$destino` a ese `plugins\MushokuCore`.

## 4. Desplegar el jar

1. Guardá el jar actual (`MushokuCore-1.0.0.jar`) como `MushokuCore-1.0.0.jar.antes-mercado` en una carpeta de backups, fuera de `plugins\`.
2. Subí el jar nuevo a `/plugins/`. Sirve `reemplazar_sftp_verificado.py`, que verifica bajando el archivo de vuelta. No uses `subir_sftp.py`: reanuda por tamaño y puede dejar un archivo distinto con el mismo nombre parcial.
3. Reiniciá el servidor desde el panel. **El reinicio lo hacés vos**; por SFTP no se puede.

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
- Si el NPC no tiene `skin.value`, se crea un **aldeano** con el nombre y el título. Es el modo por defecto hasta publicar las skins.
- Si `Mannequin` existe pero el perfil falla, el log deja `no se pudo aplicar la skin (...)`. Se usa aldeano y el resto funciona igual.
- `/vecino recargar` relee `npcs.yml` y `npcs-locations.yml`, vuelve a spawnear y elimina la entidad de un NPC que ya no esté en el YAML.

Permisos:

| Permiso | Para qué |
|---|---|
| `mushoku.mercado.use` | Abrir el mercado y comprar |
| `mushoku.admin.mercado` | `/mercado recargar` |
| `mushoku.vecino.use` | Hablar, trueques y consultar reputación |
| `mushoku.admin.vecinos` | `colocar`, `quitar`, `recargar` |

## 8. Publicar las skins

El base64 de una skin **no contiene el PNG**: es un JSON con una URL pública que el cliente descarga al ver el NPC. Sin hosting público no hay skin.

Pasos:

1. Publicá los 12 PNG en una URL estable que termina en `/`. La lista con nombres de archivo, URL de origen y SHA256 está en `docs/perugius/skins/FUENTES.md`.
2. Verificá las descargas sin escribir nada:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\docs\perugius\skins\generar_skins.ps1 -SoloVerificar
```

3. Generá el YAML apuntando al servidor:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\docs\perugius\skins\generar_skins.ps1 `
  -PublicBaseUrl "https://tu-host/skins/" `
  -Salida "C:\ruta\al\servidor\plugins\MushokuCore\npcs-skins.yml"
```

4. Copiá `npcs-skins.yml` a `plugins/MushokuCore/` y corré `/vecino recargar`. No hace falta recompilar.
5. Para volver a aldeano, dejá el `value` vacío o borrá el archivo y recargá.

Si un SHA256 no coincide, el script termina con código 1 y no escribe el YAML. Ese error no se saltea.

Sobre Bedrock: `Mannequin` es una entidad de Java. Que Geyser la traduzca y muestre la skin **no está probado**. Por eso existe el fallback de aldeano y los comandos `/vecino hablar` y `/vecino trueques`, que no dependen de clickear la entidad.

## 9. Rollback

1. Detené el servidor desde el panel.
2. Reponé el jar anterior (`MushokuCore-1.0.0.jar.antes-mercado`).
3. Si querés apagar solo los módulos nuevos sin volver el jar, en `plugins/MushokuCore/config.yml`:

```yaml
modules:
  market: false
  npcs: false
```

4. Reiniciá.

La migración `V003` no se revierte sola. Las tres tablas nuevas quedan vacías y no afectan a los módulos viejos, así que no hace falta bajarlas.

## 10. Pendientes después de la prueba

- Confirmar en el log que la skin se aplicó y no cayó a aldeano.
- Decidir el hosting definitivo de los PNG. `mskins.net` no declara licencia: para producción conviene re-subir las imágenes a un host propio y atribuir la fuente.
- Ajustar precios de `market.yml` con datos de economía real (cuántos diamantes circulan por hora).
- Definir si la economía suma más adelante una fuente de ingreso, porque hoy el mercado solo retira diamantes.

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

Resultado: `javac` exit 0, 148 clases, jar de 320.483 B con `market.yml`, `npcs.yml` y `migrations/V003__market_npcs.sql` adentro. Ese jar es **solo evidencia de que compila**; no se desplegó.

### Defectos que aparecieron al verificar

Ninguno lo detecta el compilador, y los tres se corrigieron antes de cerrar:

| Defecto | Cómo se detectó | Corrección |
|---|---|---|
| `Method.invoke` sobre `ResolvableProfile.Builder` habría tirado `IllegalAccessException` en runtime | El fuente de Paper `ver/1.21.9` muestra que la implementación (`BuilderImpl`) es package-private; `armador.getClass().getMethod(...)` devuelve un método declarado en esa clase | Buscar `addProperty` y `build` en la interfaz pública `ResolvableProfile$Builder` |
| La compra podía cobrar y dejar el item en el piso | `puedeEntregar` contaba los slots de armadura y mano secundaria como libres, y `PlayerInventory#addItem` no los usa | `InventarioUtil.puedeEntregar` mira `getStorageContents()` |
| `new ItemStack(material, amount)` con `amount` mayor al stack del material | El catálogo tenía `MUSHROOM_STEW` x4 y el estofado no se apila | `MarketItem.valido()` exige `amount <= getMaxStackSize()`; `npcs.yml` valida lo mismo en `entrega-cantidad`; el estofado pasó a x1 |


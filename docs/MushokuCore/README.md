# MushokuCore — esqueleto inicial (2026-10-17)

Plugin propio del servidor Mushoku Tensei (Paper 26.2, Java 25).
Este directorio está **fuera** de `MT_Server_Base_26.2\` (regla 6 del proyecto: tocar ese
paquete obliga a regenerar manifiestos) y **no** escribe nada dentro de `servidor\`.

## Estado

- [x] Toolchain de compilación verificado y reproducible (offline, sin Gradle).
- [x] Esqueleto que compila: `MushokuCore-1.0.0.jar` con la clase principal y `plugin.yml`.
- [ ] Smoke test en el servidor (NO hecho: el export de WorldPainter ocupaba la RAM;
      arrancar el servidor queda para una etapa posterior).
- [ ] Módulos del §7 en adelante (comandos completos, config, base de datos, integraciones).

## Java

- Compilado con **Temurin JDK 25.0.4.1+1** (`C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot`),
  que es el `java`/`javac` del PATH y el valor de `JAVA_HOME`. El servidor local usa su propio
  JDK (`servidor\jdk25\jdk-25.0.4.1+1\`) — no hace falta para compilar.
- `javac -encoding UTF-8 --release 25`.

## Classpath de compilación (todo local, dentro de `servidor\libraries\`, solo lectura)

La API de Paper **no** está en el paperclip de `servidor\paper.jar` ni en `servidor\versions\26.2\paper-26.2.jar`;
está desempaquetada como jar de Maven local:

1. `servidor\libraries\io\papermc\paper\paper-api\26.2.build.134-stable\paper-api-26.2.build.134-stable.jar` (2.889.195 B) — contiene `org/bukkit/plugin/java/JavaPlugin.class`
2. `servidor\libraries\net\kyori\adventure-api\5.2.0\adventure-api-5.2.0.jar` — `CommandSender` extiende `Audience`
3. `servidor\libraries\net\kyori\adventure-key\5.2.0\adventure-key-5.2.0.jar`
4. `servidor\libraries\net\kyori\option\1.1.0\option-1.1.0.jar`
5. `servidor\libraries\net\md-5\bungeecord-chat\1.21-R0.2-deprecated+build.21\bungeecord-chat-1.21-R0.2-deprecated+build.21.jar` — `CommandSender.sendMessage(String)` declara sobrecargas con `BaseComponent`

Sin los jars 2–5, `javac` falla con `cannot access Namespaced` / `Audience` / `BaseComponent`
(errores conservados en el historial de la sesión del 2026-10-17).

## Compilar (reproducible, offline)

```powershell
cd C:\Dev\OPENCLAUDIO\minecraft_server\MushokuCore
.\build.ps1
```

No descarga nada y no arranca el servidor. Salida: `build\libs\MushokuCore-1.0.0.jar`.

Último jar generado (2026-10-17, verificado con `Get-FileHash` y listado del zip):
2.323 bytes, SHA256 `8F1CA74E6CF668492C2320AEED1400B007D75DBF33BB4BC952FB2EB71F15C0F6`,
contiene `ar/com/maydana/mushokucore/MushokuCorePlugin.class` y `plugin.yml`.

## Gradle

`build.gradle.kts` / `settings.gradle.kts` incluidos como **plantilla según el §5**, pero
**NO probados**: esta máquina no tiene Gradle instalado (`gradle` fuera del PATH, sin `~/.gradle`,
sin wrapper). Requieren además red para bajar Gradle y el `paper-api` de `repo.papermc.io`
(el coordinate `io.papermc.paper:paper-api:26.2.build.134-stable` es [A VERIFICAR] contra el
repositorio). El §24 (`gradlew.bat clean test shadowJar`) queda pendiente de tener Gradle.
Mientras tanto, el camino oficial de compilación es `build.ps1`.

## Desviaciones del §5/§6 del Instructivo (a conciencia)

- `plugin.yml` lleva `version: '1.0.0'` **literal**, no `'${version}'` (el token exige el
  filtrado de recursos de Gradle, que el build.ps1 no usa).
- `plugin.yml` declara por ahora **solo** el comando `mushoku` (el §6 lista además
  `potencias`, `mtestado`, `faccion`, `alianza`, `enfermedad`, `reliquia` — se agregan cuando
  cada módulo exista). El comando responde con un mensaje de "en construcción"; el resto del
  §6 (permisos, comandos) está pendiente.
- Paquete principal `ar.com.maydana.mushokucore` (igual que el §6/§7 del Instructivo; el plan de
  trabajo inicial sugería `com.mushokucore` pero mandaría a desviarse del spec).

## Falta para el smoke test

1. Copiar `build\libs\MushokuCore-1.0.0.jar` a `servidor\plugins\` (con backup del previo si existe).
2. Arrancar el servidor y revisar la consola: debe aparecer
   `MushokuCore 1.0.0 habilitado (esqueleto).` y `MushokuCore deshabilitado.` al `/stop`.
3. Probar `/mushoku` y `/mt`. **Nota:** el plugin `depend`e de LuckPerms
   (`LuckPerms-Bukkit-5.5.71.jar` está en `servidor\plugins\`, verificado) — si no carga,
   verificar primero ese orden de carga. `api-version: '26.2'` está [A VERIFICAR] contra el
   arranque real de Paper 26.2.

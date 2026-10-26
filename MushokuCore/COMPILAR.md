# Compilar MushokuCore

`build.ps1` compila sin Gradle: `javac` + `jar` contra el `paper-api` del servidor
local. No descarga nada y no arranca Minecraft.

## Requisitos

| Requisito | Detalle |
|---|---|
| JDK 25 | Temurin 25, con `javac` y `jar` en el `PATH` o en `JAVA_HOME` |
| Arbol `servidor\` | Con `libraries\` y `plugins\` del servidor Paper |

El `servidor\` **no está en git**. `build.ps1` lo busca, en orden:

1. `..\servidor\libraries` respecto de `MushokuCore\`
2. `..\..\servidor\libraries`
3. `C:\Dev\OPENCLAUDIO\minecraft_server\servidor\libraries` (ubicación histórica)

Si no encuentra ninguno, falla con un mensaje que pide `-LibRoot`. En la PC
principal la tercera ruta existe, así que `.\build.ps1` debería funcionar sin
argumentos. Desde otra máquina:

```powershell
.\build.ps1 -LibRoot 'D:\otra_instalacion\servidor\libraries'
```

## Artefactos que resuelve

Se buscan por patrón dentro del árbol del servidor. Cada patrón debe dar
**exactamente un** archivo; si hay dos versiones, el build falla y nombra los
candidatos, en vez de elegir una en silencio. Los jars ubicados en directorios
`relocated` se excluyen porque son copias internas reubicadas de otros plugins.

| Artefacto | Patrón | Origen |
|---|---|---|
| Paper API | `libraries\io\papermc\paper\paper-api\**\paper-api-*.jar` | `libraries` |
| Adventure API | `libraries\net\kyori\adventure-api\**\adventure-api-*.jar` | `libraries` |
| Adventure Key | `libraries\net\kyori\adventure-key\**\adventure-key-*.jar` | `libraries` |
| Adventure Option | `libraries\net\kyori\option\**\option-*.jar` | `libraries` |
| BungeeCord Chat | `libraries\net\md-5\bungeecord-chat\**\bungeecord-chat-*.jar` | `libraries` |
| Guava | `libraries\com\google\guava\guava\**\guava-*.jar` | `libraries` |
| Anotaciones JetBrains | `plugins\ajLeaderboards\libs\**\annotations-*.jar` | `plugins` |
| PlaceholderAPI | `plugins\PlaceholderAPI-*.jar` | `plugins` |
| Towny | `plugins\Towny-*.jar` | `plugins` |

PlaceholderAPI y Towny son **compileOnly**: no se empaquetan en el jar. Sin las
anotaciones de JetBrains, `javac` no muestra un error legible: aborta con
`CompletionFailure` (exit 4).

## Ejecución

```powershell
cd MushokuCore
.\build.ps1            # compila y empaqueta
.\build.ps1 -WhatIf    # informa qué haría, sin tocar el disco
.\build.ps1 -Verbose   # muestra la salida de javac y de jar
```

Salida en éxito: un objeto con `Jar`, `Bytes` y `Sha256`. Artefacto en
`build\libs\MushokuCore-1.0.0.jar`.

Si el build falla, `build\` se borra para no dejar un parcial que parezca bueno.

## Estado verificado

| Dato | Valor |
|---|---|
| Sintaxis del script | OK (parser de PowerShell, 824 tokens, 2026-10-25) |
| `Invoke-ScriptAnalyzer` | Sin hallazgos, todas las severidades (2026-10-25) |
| `-WhatIf` sin escribir en disco | Verificado con árbol de librerías simulado |
| Ambigüedad de versiones detectada | Verificado (dos `paper-api` → error con ambos nombres) |
| Dependencia faltante nombrada | Verificado (`Towny` ausente → error explícito) |
| Trampa de stderr en PS 5.1 | **No reproduce** con invocación directa en 5.1.19041; sí al capturar `2>&1` con `EAP=Stop`. `Invoke-Nativo` esquiva ambos casos. |
| Compilación real | Correcta en la PC principal con JDK 25 y el árbol histórico de `servidor` (2026-10-25) |
| Prueba `FactionsPolicyTest` | `FACTIONS_POLICY_CHECK_OK` (2026-10-25) |

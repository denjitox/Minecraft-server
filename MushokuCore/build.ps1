<#
.SYNOPSIS
  Compila MushokuCore SIN Gradle: solo JDK 25 + el paper-api local.

.DESCRIPTION
  javac contra el jar de la API de Paper que ya esta en el servidor local,
  copia plugin.yml y empaqueta con la herramienta `jar`. 100% offline:
  no descarga nada y no arranca el servidor de Minecraft.

.EXAMPLE
  .\build.ps1
  .\build.ps1 -LibRoot 'R:\otra_instalacion\libraries'
#>
[CmdletBinding(SupportsShouldProcess)]
param(
    [string]$LibRoot = 'C:\Dev\OPENCLAUDIO\minecraft_server\servidor\libraries'
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot

# Classpath completo de compilación (verificado 2026-10-18 contra servidor\libraries):
#  - paper-api necesita las clases de Adventure en el classpath (CommandSender extends Audience).
#  - paper-api trae anotaciones @NotNull de org.jetbrains.annotations y referencia Guava. Si esas
#    clases faltan, javac NO da un error legible: crashea con
#    `CompletionFailure: class file for org.jetbrains.annotations.NotNull not found` (exit 4).
#    Medido 2026-10-18 por el agente D; verificado por el integrador el mismo dia.
#  - El jar de anotaciones solo existe dentro del plugin ajLeaderboards del servidor local.
#  - El jar de PlaceholderAPI (plugin del servidor local) es necesario para compilar
#    integration\MushokuExpansion.java (subclase de PlaceholderExpansion). Es compileOnly:
#    en runtime el jar lo aporta plugins\PlaceholderAPI-*.jar, NO se empaqueta en el nuestro.
$cpJars = @(
    (Join-Path $LibRoot 'io\papermc\paper\paper-api\26.2.build.134-stable\paper-api-26.2.build.134-stable.jar'),
    (Join-Path $LibRoot 'net\kyori\adventure-api\5.2.0\adventure-api-5.2.0.jar'),
    (Join-Path $LibRoot 'net\kyori\adventure-key\5.2.0\adventure-key-5.2.0.jar'),
    (Join-Path $LibRoot 'net\kyori\option\1.1.0\option-1.1.0.jar'),
    (Join-Path $LibRoot 'net\md-5\bungeecord-chat\1.21-R0.2-deprecated+build.21\bungeecord-chat-1.21-R0.2-deprecated+build.21.jar'),
    (Join-Path $LibRoot 'com\google\guava\guava\33.6.0-jre\guava-33.6.0-jre.jar'),
    (Join-Path (Split-Path $LibRoot -Parent) 'plugins\ajLeaderboards\libs\org\jetbrains\annotations\13.0\annotations-13.0.jar'),
    (Join-Path (Split-Path $LibRoot -Parent) 'plugins\PlaceholderAPI-2.12.3.jar'),
    (Join-Path (Split-Path $LibRoot -Parent) 'plugins\Towny-0.103.2.0.jar')
)
foreach ($j in $cpJars) {
    if (-not (Test-Path $j)) { throw "No se encuentra un jar del classpath: $j" }
}
$classpath = $cpJars -join ';'

# Usar javac del JAVA_HOME si esta definido; si no, el del PATH (verificado: ambos son Temurin 25).
$javac = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\javac.exe' } else { 'javac' }
$jar   = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\jar.exe' }   else { 'jar' }

$srcDir  = Join-Path $root 'src\main\java'
$resDir  = Join-Path $root 'src\main\resources'
$outDir  = Join-Path $root 'build\classes'
$libsDir = Join-Path $root 'build\libs'
$outJar  = Join-Path $libsDir 'MushokuCore-1.0.0.jar'

if (Test-Path (Join-Path $root 'build')) {
    if ($PSCmdlet.ShouldProcess("$root\build", 'Remove build directory')) {
        Remove-Item -Recurse -Force (Join-Path $root 'build')
    } else {
        Write-Verbose "WHATIF: se borraria $root\build"
    }
}
New-Item -ItemType Directory -Force -Path $outDir, $libsDir | Out-Null

try {
    $sources = Get-ChildItem $srcDir -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
    if (-not $sources) { throw "No hay archivos .java en $srcDir" }

    Write-Verbose "== javac =="
    & $javac -encoding UTF-8 --release 25 -classpath $classpath -d $outDir $sources
    if ($LASTEXITCODE -ne 0) { throw "javac fallo con exit code $LASTEXITCODE" }

    Write-Verbose "== recursos =="
    # Copia TODOS los resources (plugin.yml, config.yml, messages.yml, diseases.yml, relics.yml
    # y migrations/V001__initial.sql) preservando subdirectorios — Fase 0 2026-10-17.
    Copy-Item -Path (Join-Path $resDir '*') -Destination $outDir -Recurse -Force

    Write-Verbose "== jar =="
    & $jar --create --file $outJar -C $outDir .
    if ($LASTEXITCODE -ne 0) { throw "jar fallo con exit code $LASTEXITCODE" }

    $hash = (Get-FileHash $outJar -Algorithm SHA256).Hash
    $size = (Get-Item $outJar).Length
    Write-Host ""
    Write-Host "JAR generado: $outJar"
    Write-Host "Tamano:       $size bytes"
    Write-Host "SHA256:       $hash"
} finally {
    # Garantizar limpieza si falla a mitad (no deja build/ parcialmente escrito)
    if (-not (Test-Path $outJar)) {
        if (Test-Path (Join-Path $root 'build')) {
            if ($PSCmdlet.ShouldProcess("$root\build", 'Cleanup failed build')) {
                Remove-Item -Recurse -Force (Join-Path $root 'build') -ErrorAction SilentlyContinue
            }
        }
    }
}

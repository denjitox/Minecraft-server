<#
.SYNOPSIS
  Compila MushokuCore SIN Gradle: solo JDK 25 + el paper-api local.

.DESCRIPTION
  javac contra el jar de la API de Paper que ya esta en el servidor local, copia
  los recursos y empaqueta con la herramienta `jar`. 100% offline: no descarga
  nada y no arranca el servidor de Minecraft.

  Las dependencias se descubren por patron, no por version fija: si el servidor
  actualiza un plugin, el build sigue funcionando.

  -WhatIf no toca el disco; informa que se compilaria y termina.

.PARAMETER LibRoot
  Raiz `libraries` de Paper. Si se omite se busca `servidor\libraries` junto a
  este script, un nivel arriba, y por ultimo en la ubicacion historica.

.EXAMPLE
  .\build.ps1
  .\build.ps1 -LibRoot 'R:\otra_instalacion\libraries'
#>
[CmdletBinding(SupportsShouldProcess, ConfirmImpact = 'Medium')]
param(
    [string]$LibRoot
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot

function Resolve-LibRoot {
    param([string]$Explicit)

    if ($Explicit) {
        if (-not (Test-Path $Explicit)) { throw "El -LibRoot indicado no existe: $Explicit" }
        return (Resolve-Path $Explicit).Path
    }

    # El arbol del servidor (servidor\, libraries\, plugins\) no esta en git:
    # en cada PC vive al lado del repo o un nivel arriba.
    $candidatos = @(
        (Join-Path $root '..\servidor\libraries')
        (Join-Path $root '..\..\servidor\libraries')
        'C:\Dev\OPENCLAUDIO\minecraft_server\servidor\libraries'
    )
    foreach ($c in $candidatos) {
        if (Test-Path $c) { return (Resolve-Path $c).Path }
    }
    throw "No se encontro 'servidor\libraries'. Pasa -LibRoot con la ruta al 'libraries' de Paper."
}

function Resolve-Dependencia {
    param(
        [Parameter(Mandatory)][string]$Directorio,
        [Parameter(Mandatory)][string]$Patron,
        [Parameter(Mandatory)][string]$Nombre
    )

    if (-not (Test-Path $Directorio)) { throw "Falta el directorio de $Nombre`: $Directorio" }

    $encontrados = @(
        Get-ChildItem -LiteralPath $Directorio -Filter $Patron -File -Recurse -ErrorAction SilentlyContinue |
            Where-Object { $_.FullName -notmatch '[\\/]relocated[\\/]' }
    )
    if ($encontrados.Count -eq 0) {
        throw "Falta $Nombre ($Patron) en $Directorio"
    }
    if ($encontrados.Count -gt 1) {
        $nombres = ($encontrados | ForEach-Object { $_.Name }) -join ', '
        throw "Hay $($encontrados.Count) candidatos para $Nombre en ${Directorio}: $nombres"
    }
    return $encontrados[0].FullName
}

function Invoke-Nativo {
    param(
        [Parameter(Mandatory)][string]$Exe,
        [Parameter(Mandatory)][string[]]$Arguments
    )

    # Con $ErrorActionPreference = 'Stop', PS 5.1 convierte el stderr de un comando
    # nativo en NativeCommandError terminante, y javac escribe ahi un aviso inofensivo.
    $previa = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $salida = & $Exe @Arguments 2>&1 | ForEach-Object { "$_" }
        $codigo = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previa
    }

    foreach ($linea in $salida) { Write-Verbose $linea }
    return $codigo
}

$LibRoot = Resolve-LibRoot -Explicit $LibRoot
$serverRoot = Split-Path $LibRoot -Parent
$pluginsDir = Join-Path $serverRoot 'plugins'

$cpJars = @(
    (Resolve-Dependencia -Directorio (Join-Path $LibRoot 'io\papermc\paper\paper-api') -Patron 'paper-api-*.jar' -Nombre 'paper-api')
    (Resolve-Dependencia -Directorio (Join-Path $LibRoot 'net\kyori\adventure-api') -Patron 'adventure-api-*.jar' -Nombre 'adventure-api')
    (Resolve-Dependencia -Directorio (Join-Path $LibRoot 'net\kyori\adventure-key') -Patron 'adventure-key-*.jar' -Nombre 'adventure-key')
    (Resolve-Dependencia -Directorio (Join-Path $LibRoot 'net\kyori\option') -Patron 'option-*.jar' -Nombre 'adventure option')
    (Resolve-Dependencia -Directorio (Join-Path $LibRoot 'net\md-5\bungeecord-chat') -Patron 'bungeecord-chat-*.jar' -Nombre 'bungeecord-chat')
    (Resolve-Dependencia -Directorio (Join-Path $LibRoot 'com\google\guava\guava') -Patron 'guava-*.jar' -Nombre 'guava')
    # paper-api referencia @NotNull: sin esas clases javac no da un error legible,
    # crashea con CompletionFailure (exit 4). Solo existen dentro de ajLeaderboards.
    (Resolve-Dependencia -Directorio (Join-Path $pluginsDir 'ajLeaderboards\libs') -Patron 'annotations-*.jar' -Nombre 'annotations de JetBrains')
    # PlaceholderAPI y Towny son compileOnly: en runtime los aporta plugins\.
    (Resolve-Dependencia -Directorio $pluginsDir -Patron 'PlaceholderAPI-*.jar' -Nombre 'PlaceholderAPI')
    (Resolve-Dependencia -Directorio $pluginsDir -Patron 'Towny-*.jar' -Nombre 'Towny')
)
$classpath = $cpJars -join ';'

# Usar javac del JAVA_HOME si esta definido; si no, el del PATH (ambos Temurin 25).
$javac = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\javac.exe' } else { 'javac' }
$jar   = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\jar.exe' }   else { 'jar' }

$srcDir   = Join-Path $root 'src\main\java'
$resDir   = Join-Path $root 'src\main\resources'
$buildDir = Join-Path $root 'build'
$outDir   = Join-Path $buildDir 'classes'
$libsDir  = Join-Path $buildDir 'libs'
$outJar   = Join-Path $libsDir 'MushokuCore-1.0.0.jar'

if (-not $PSCmdlet.ShouldProcess($root, "Compilar MushokuCore y escribir $outJar")) {
    return
}

if (Test-Path $buildDir) { Remove-Item -Recurse -Force $buildDir }
New-Item -ItemType Directory -Force -Path $outDir, $libsDir | Out-Null

$ok = $false
try {
    $sources = Get-ChildItem $srcDir -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
    if (-not $sources) { throw "No hay archivos .java en $srcDir" }

    Write-Verbose "== javac =="
    $argsJavac = @('-encoding', 'UTF-8', '--release', '25', '-classpath', $classpath, '-d', $outDir) + $sources
    $codigo = Invoke-Nativo -Exe $javac -Arguments $argsJavac
    if ($codigo -ne 0) { throw "javac fallo con exit code $codigo" }

    Write-Verbose "== recursos =="
    # Copia plugin.yml, config.yml, messages.yml y migrations\ preservando subdirectorios.
    Copy-Item -Path (Join-Path $resDir '*') -Destination $outDir -Recurse -Force

    Write-Verbose "== jar =="
    $codigo = Invoke-Nativo -Exe $jar -Arguments @('--create', '--file', $outJar, '-C', $outDir, '.')
    if ($codigo -ne 0) { throw "jar fallo con exit code $codigo" }

    $ok = $true

    [pscustomobject]@{
        Jar    = $outJar
        Bytes  = (Get-Item $outJar).Length
        Sha256 = (Get-FileHash $outJar -Algorithm SHA256).Hash
    }
} finally {
    # Solo limpia si el build no llego a completarse: un jar viejo de una corrida
    # anterior no debe hacer pasar por bueno un build fallido.
    if (-not $ok -and (Test-Path $buildDir)) {
        Remove-Item -Recurse -Force $buildDir -ErrorAction SilentlyContinue
    }
}

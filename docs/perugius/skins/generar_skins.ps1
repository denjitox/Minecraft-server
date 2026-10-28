# generar_skins.ps1
#
# Descarga las 12 skins de docs/perugius/skins/FUENTES.md, verifica el SHA256 y genera
# npcs-skins.yml con el base64 de la propiedad "textures" que consume el modulo npcs de
# MushokuCore.
#
# Importante: el base64 NO contiene el PNG. Es un JSON con una URL publica, y el cliente
# (o Geyser) la descarga al ver al NPC. Por eso la skin solo aparece si el PNG esta
# publicado en una URL accesible. Publicar es un paso manual y explicito: cuando tengas
# las imagenes subidas, pasas la base con -PublicBaseUrl.
#
# Uso tipico:
#   powershell -NoProfile -ExecutionPolicy Bypass -File .\docs\perugius\skins\generar_skins.ps1
#       Descarga y verifica. Deja npcs-skins.yml con value vacio (los NPC usan aldeano).
#
#   powershell -NoProfile -ExecutionPolicy Bypass -File .\docs\perugius\skins\generar_skins.ps1 `
#     -PublicBaseUrl "https://tu-host/skins/" `
#     -Salida "C:\ruta\al\servidor\plugins\MushokuCore\npcs-skins.yml"
#       Genera el base64 con <base><archivo>.png. Requiere que esas URLs ya existan.
#
# Opciones:
#   -Pngs <dir>          Donde se guardan los PNG. Default: %TEMP%\mushoku_skins.
#   -Salida <archivo>    Donde se escribe el YAML. Default: MushokuCore\src\main\resources\npcs-skins.yml.
#   -PublicBaseUrl <url> Base publica que termina en "/". Sin esto no se genera skin.
#   -SoloVerificar       Descarga y verifica, pero no escribe el YAML.
#   -Force               Vuelve a descargar aunque el hash local ya coincida.
#
# El script no publica nada por su cuenta. Si un hash no coincide, conserva el error y
# termina con codigo 1 sin escribir el YAML.

[CmdletBinding()]
param(
    [string]$Pngs = (Join-Path $env:TEMP 'mushoku_skins'),
    [string]$Salida,
    [string]$PublicBaseUrl,
    [switch]$SoloVerificar,
    [switch]$Force
)

$ErrorActionPreference = 'Stop'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$raizRepo = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..')).Path
if (-not $Salida) {
    $Salida = Join-Path $raizRepo 'MushokuCore\src\main\resources\npcs-skins.yml'
}

# id en npcs.yml, archivo local, pagina de la skin y hash documentado.
$skins = @(
    @{ id = 'perugius';      archivo = 'perugius.png';                   url = 'https://mskins.net/en/skin/Perugius-orgJz';        sha = '09df5b31b4fb91f3dc5100dac639568f3be2064456e94df7c2c1ab6787633847' },
    @{ id = 'orsted';        archivo = 'perugius_alt_orsted.png';        url = 'https://mskins.net/en/skin/Orsted-g6kQG';          sha = '7d58fc299ee3872227481986c056a0414872e9ed0446164ccf68b45c0af96bc7' },
    @{ id = 'recepcion';     archivo = 'recepcion.png';                  url = 'https://mskins.net/en/skin/Receptionist-XYYrk';    sha = '9d8cd10842b76ac79ac3878bc4dcaaf9d6257392fa688ade67f39327bba407fa' },
    @{ id = 'dragonknight';  archivo = 'sirviente_1_dragonknight.png';  url = 'https://mskins.net/en/skin/DragonKnight-wnVxw';    sha = '78b35220bedd37d2057c201844330682571602e937bcf8be47df2a6c245cb5a4' },
    @{ id = 'spiritknight';  archivo = 'sirviente_2_spiritknight.png';  url = 'https://mskins.net/en/skin/Spiritknight-r4m0B';    sha = '8980ae30b3b9387efd33dd80ff04c094ee087efb85a9f852a9b8ca2a9fc0bb5e' },
    @{ id = 'knight-rojo';   archivo = 'sirviente_3_knight_rojo.png';   url = 'https://mskins.net/en/skin/Knight-N8PLD';          sha = '0a2e2dc109d22b610043a25567ab96c5179a6338461b40725f06874dcabb145b' },
    @{ id = 'sylphiette';    archivo = 'sirviente_4_sylphiette.png';    url = 'https://mskins.net/en/skin/Sylphiette-3gBLx';      sha = 'b96e864521694bd63109a14cf9dec0e2df8955b446e19a304cd1068fc2c12e6e' },
    @{ id = 'guerrero';      archivo = 'sirviente_5_guerrero.png';      url = 'https://mskins.net/en/skin/Ruijerd-GkQzQ';         sha = '0f2876df8b72cd28381683840fb7ab5400523f419c30b78f18ced097dda7d0ad' },
    @{ id = 'rudeus';        archivo = 'extra_rudeus.png';               url = 'https://mskins.net/en/skin/Rudeus-X9rVg';          sha = '1e37b0c2b2b2714466b2f1f0b446b970ca14fe99318c810bcccb604053bce274' },
    @{ id = 'roxy';          archivo = 'extra_roxy.png';                 url = 'https://mskins.net/en/skin/RoxyMigurdia-GJxRJ';    sha = '25711e420b85be102dd369e07b8a9b52d895cf1972e68bd72fd79632db5e9453' },
    @{ id = 'eris';          archivo = 'extra_eris.png';                 url = 'https://mskins.net/en/skin/ErisGreyrat77-NGxD6';   sha = 'cadbe64aee49b1ea89821484bea1a5bfd6fb100e70472441afb9d498fef49b2d' },
    @{ id = 'mago-oscuro';   archivo = 'extra_mago_oscuro.png';          url = 'https://mskins.net/en/skin/Mushoku_Tensei-XK3Yg';  sha = 'f1c42090f2aba76b8277f9bdcea8720244c40dcec4d5e6feaec49240b73d0b52' }
)

function Get-Sha256([string]$archivo) {
    (Get-FileHash -LiteralPath $archivo -Algorithm SHA256).Hash.ToLowerInvariant()
}

if (-not (Test-Path -LiteralPath $Pngs)) {
    New-Item -ItemType Directory -Path $Pngs -Force | Out-Null
}

$resultados = New-Object System.Collections.Generic.List[object]
$fallos = New-Object System.Collections.Generic.List[string]

foreach ($skin in $skins) {
    $destino = Join-Path $Pngs $skin.archivo
    $necesitaDescarga = $true

    if ((Test-Path -LiteralPath $destino) -and -not $Force) {
        $hashLocal = Get-Sha256 $destino
        if ($hashLocal -eq $skin.sha) {
            $necesitaDescarga = $false
            Write-Host ("OK       {0,-24} {1}" -f $skin.id, $hashLocal)
        } else {
            Write-Host ("HASH MAL {0,-24} local={1} esperado={2}; se vuelve a descargar" -f $skin.id, $hashLocal, $skin.sha)
        }
    }

    if ($necesitaDescarga) {
        $descarga = $skin.url + '/download'
        try {
            Invoke-WebRequest -Uri $descarga -OutFile $destino -UseBasicParsing -UserAgent 'Mozilla/5.0'
        } catch {
            $fallos.Add("$($skin.id): fallo la descarga de $descarga -> $($_.Exception.Message)")
            continue
        }
        $hashLocal = Get-Sha256 $destino
        if ($hashLocal -ne $skin.sha) {
            $fallos.Add("$($skin.id): SHA256 no coincide. local=$hashLocal esperado=$($skin.sha) archivo=$destino")
            continue
        }
        Write-Host ("BAJADA   {0,-24} {1}" -f $skin.id, $hashLocal)
    }

    $bytes = (Get-Item -LiteralPath $destino).Length
    $resultados.Add([pscustomobject]@{
        id      = $skin.id
        archivo = $skin.archivo
        bytes   = $bytes
        sha     = $skin.sha
    })
}

if ($fallos.Count -gt 0) {
    Write-Host ''
    Write-Host 'ERRORES (no se escribe el YAML):' -ForegroundColor Red
    foreach ($fallo in $fallos) {
        Write-Host "  - $fallo" -ForegroundColor Red
    }
    exit 1
}

Write-Host ''
Write-Host ("Verificadas {0}/{1} skins en {2}" -f $resultados.Count, $skins.Count, $Pngs)

if ($SoloVerificar) {
    Write-Host 'SoloVerificar: no se escribe npcs-skins.yml.'
    exit 0
}

if (-not $PublicBaseUrl) {
    Write-Host ''
    Write-Host 'Sin -PublicBaseUrl: npcs-skins.yml queda con value vacio.' -ForegroundColor Yellow
    Write-Host 'Los NPC usan el aldeano de respaldo hasta que publiques los PNG y vuelvas a correr el script.'
} elseif (-not $PublicBaseUrl.EndsWith('/')) {
    Write-Host 'ERROR: -PublicBaseUrl tiene que terminar en "/".' -ForegroundColor Red
    exit 1
}

$lineas = New-Object System.Collections.Generic.List[string]
$lineas.Add('# npcs-skins.yml - GENERADO por docs/perugius/skins/generar_skins.ps1')
$lineas.Add('# Fecha: ' + (Get-Date).ToString('yyyy-MM-dd HH:mm:ss'))
$lineas.Add('# No editar a mano: se pisa al volver a correr el script.')
$lineas.Add('# value vacio significa que el NPC usa el aldeano de respaldo, sin skin.')
if (-not $PublicBaseUrl) {
    $lineas.Add('# Falta publicar los PNG y pasar -PublicBaseUrl. Ver docs/perugius/skins/FUENTES.md.')
}

foreach ($resultado in $resultados) {
    $value = ''
    if ($PublicBaseUrl) {
        $urlSkin = $PublicBaseUrl + $resultado.archivo
        $json = @{ textures = @{ SKIN = @{ url = $urlSkin } } } | ConvertTo-Json -Compress -Depth 5
        $value = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($json))
    }
    $lineas.Add('')
    $lineas.Add(('{0}:' -f $resultado.id))
    $lineas.Add(('  value: "{0}"' -f $value))
    $lineas.Add('  signature: ""')
}

$directorioSalida = Split-Path -Parent $Salida
if ($directorioSalida -and -not (Test-Path -LiteralPath $directorioSalida)) {
    New-Item -ItemType Directory -Path $directorioSalida -Force | Out-Null
}
$lineas | Set-Content -LiteralPath $Salida -Encoding UTF8
Write-Host ''
Write-Host ("Escrito {0} ({1} skins)" -f $Salida, $resultados.Count)
if ($PublicBaseUrl) {
    Write-Host 'Recordatorio: las URLs tienen que responder 200 desde fuera de la red local.'
    Write-Host 'Si el servidor corre con el JAR, este archivo tambien tiene que estar en plugins/MushokuCore/.'
}
exit 0

# resolver_skins_mojang.ps1
#
# Resuelve las 12 skins del modulo npcs contra la API de Mojang y escribe
# npcs-skins.yml con la propiedad "textures" ya lista (value + signature).
#
# Por que este camino y no publicar PNG ni subir a Mineskin:
#   - Las 12 skins de FUENTES.md salieron de cuentas reales (mskins.net indexa por
#     apodo). El sessionserver de Mojang devuelve la propiedad "textures" firmada de
#     esa cuenta, sin cuenta propia, sin API key y sin hosting.
#   - Si la cuenta cambio de skin desde el 2026-10-18, el hash no coincide y el script
#     usa la textura original, que sigue publicada en textures.minecraft.net, armando
#     la propiedad sin firma. El cliente y Geyser aceptan propiedades sin firma.
#   - Nada de esto se apoya en un archivo local: la URL queda en el CDN de Mojang.
#
# Uso:
#   powershell -NoProfile -ExecutionPolicy Bypass -File .\docs\perugius\skins\resolver_skins_mojang.ps1
#
# Opciones:
#   -Salida <archivo>   Donde se escribe el YAML.
#                       Default: MushokuCore\src\main\resources\npcs-skins.yml
#   -SoloProbar         Resuelve y verifica, pero no escribe el YAML.
#
# Sale con codigo 1 si alguna skin no se pudo resolver ni verificar.

[CmdletBinding()]
param(
    [string]$Salida,
    [switch]$SoloProbar
)

$ErrorActionPreference = 'Stop'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$userAgent = 'MushokuCore-SkinSetup/1.0'

$raizRepo = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..')).Path
if (-not $Salida) {
    $Salida = Join-Path $raizRepo 'MushokuCore\src\main\resources\npcs-skins.yml'
}

# id en npcs.yml, cuenta de la que salio la skin, SHA256 del PNG documentado.
# El hash es a la vez el nombre de archivo de la textura en el CDN de Mojang.
$skins = @(
    @{ id = 'perugius';     jugador = 'Perugius';       sha = '09df5b31b4fb91f3dc5100dac639568f3be2064456e94df7c2c1ab6787633847' },
    @{ id = 'orsted';       jugador = 'Orsted';         sha = '7d58fc299ee3872227481986c056a0414872e9ed0446164ccf68b45c0af96bc7' },
    @{ id = 'recepcion';    jugador = 'Receptionist';   sha = '9d8cd10842b76ac79ac3878bc4dcaaf9d6257392fa688ade67f39327bba407fa' },
    @{ id = 'dragonknight'; jugador = 'DragonKnight';   sha = '78b35220bedd37d2057c201844330682571602e937bcf8be47df2a6c245cb5a4' },
    @{ id = 'spiritknight'; jugador = 'Spiritknight';   sha = '8980ae30b3b9387efd33dd80ff04c094ee087efb85a9f852a9b8ca2a9fc0bb5e' },
    @{ id = 'knight-rojo';  jugador = 'Knight';         sha = '0a2e2dc109d22b610043a25567ab96c5179a6338461b40725f06874dcabb145b' },
    @{ id = 'sylphiette';   jugador = 'Sylphiette';     sha = 'b96e864521694bd63109a14cf9dec0e2df8955b446e19a304cd1068fc2c12e6e' },
    @{ id = 'guerrero';     jugador = 'Ruijerd';        sha = '0f2876df8b72cd28381683840fb7ab5400523f419c30b78f18ced097dda7d0ad' },
    @{ id = 'rudeus';       jugador = 'Rudeus';         sha = '1e37b0c2b2b2714466b2f1f0b446b970ca14fe99318c810bcccb604053bce274' },
    @{ id = 'roxy';         jugador = 'RoxyMigurdia';   sha = '25711e420b85be102dd369e07b8a9b52d895cf1972e68bd72fd79632db5e9453' },
    @{ id = 'eris';         jugador = 'ErisGreyrat77';  sha = 'cadbe64aee49b1ea89821484bea1a5bfd6fb100e70472441afb9d498fef49b2d' },
    @{ id = 'mago-oscuro';  jugador = 'Mushoku_Tensei'; sha = 'f1c42090f2aba76b8277f9bdcea8720244c40dcec4d5e6feaec49240b73d0b52' }
)

function Get-PerfilMojang([string]$jugador) {
    $perfil = Invoke-RestMethod -Uri ("https://api.mojang.com/users/profiles/minecraft/" + [Uri]::EscapeDataString($jugador)) `
        -TimeoutSec 20 -UserAgent $userAgent
    if (-not $perfil -or -not $perfil.id) { return $null }
    return Invoke-RestMethod -Uri ("https://sessionserver.mojang.com/session/minecraft/profile/" + $perfil.id + '?unsigned=false') `
        -TimeoutSec 20 -UserAgent $userAgent
}

function Get-TexturaDePerfil($completo) {
    if (-not $completo -or -not $completo.properties) { return $null }
    $prop = $completo.properties | Where-Object { $_.name -eq 'textures' } | Select-Object -First 1
    if (-not $prop) { return $null }
    $json = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($prop.value)) | ConvertFrom-Json
    $url = $json.textures.SKIN.url
    if (-not $url) { return $null }
    [pscustomobject]@{
        url       = $url
        hash      = ($url -split '/')[-1]
        value     = $prop.value
        signature = $prop.signature
    }
}

function Test-TexturaCdn([string]$shaEsperado) {
    # Mojang arma la URL sin los ceros a la izquierda del SHA256: 09df... -> /texture/9df...
    # Por eso el nombre de archivo puede tener menos de 64 caracteres y el hash del PNG no.
    $url = 'https://textures.minecraft.net/texture/' + $shaEsperado.TrimStart('0')
    $tmp = Join-Path ([IO.Path]::GetTempPath()) ('mushoku_skin_' + $shaEsperado + '.png')
    try {
        Invoke-WebRequest -Uri $url -OutFile $tmp -TimeoutSec 25 -UserAgent $userAgent -UseBasicParsing
    } catch {
        return $null
    }
    $shaLocal = (Get-FileHash -LiteralPath $tmp -Algorithm SHA256).Hash.ToLowerInvariant()
    Remove-Item -LiteralPath $tmp -Force -ErrorAction SilentlyContinue
    if ($shaLocal -ne $shaEsperado) { return $null }
    return $url
}

$resultados = New-Object System.Collections.Generic.List[object]
$fallos = New-Object System.Collections.Generic.List[string]

foreach ($skin in $skins) {
    $fila = [ordered]@{
        id        = $skin.id
        jugador   = $skin.jugador
        origen    = ''
        url       = ''
        value     = ''
        signature = ''
    }

    $textura = $null
    try {
        $textura = Get-TexturaDePerfil (Get-PerfilMojang $skin.jugador)
    } catch {
        $textura = $null
    }

    if ($textura -and $textura.hash.TrimStart('0') -eq $skin.sha.TrimStart('0')) {
        $fila.origen = 'mojang-firmada'
        $fila.url = $textura.url
        $fila.value = $textura.value
        $fila.signature = if ($textura.signature) { $textura.signature } else { '' }
    } else {
        # La cuenta cambio de skin, o no existe: la textura original sigue en el CDN.
        $url = Test-TexturaCdn $skin.sha
        if (-not $url) {
            $motivo = if ($textura) { "la cuenta usa otra textura ($($textura.hash))" } else { 'no se pudo resolver la cuenta' }
            $fallos.Add("$($skin.id): $motivo y la textura documentada no responde en el CDN")
            continue
        }
        $json = @{ textures = @{ SKIN = @{ url = $url } } } | ConvertTo-Json -Compress -Depth 5
        $fila.origen = 'cdn-sin-firma'
        $fila.url = $url
        $fila.value = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($json))
        $fila.signature = ''
    }

    $resultados.Add([pscustomobject]$fila)
    Write-Host ("OK   {0,-14} {1,-16} {2}" -f $fila.id, $fila.origen, $fila.url)
}

Write-Host ''
if ($fallos.Count -gt 0) {
    Write-Host 'ERRORES (no se escribe el YAML):' -ForegroundColor Red
    foreach ($fallo in $fallos) { Write-Host "  - $fallo" -ForegroundColor Red }
    exit 1
}

$firmadas = ($resultados | Where-Object { $_.origen -eq 'mojang-firmada' }).Count
Write-Host ("Resueltas {0}/{1} skins: {2} firmadas por Mojang, {3} desde el CDN sin firma." -f `
        $resultados.Count, $skins.Count, $firmadas, ($resultados.Count - $firmadas))

if ($SoloProbar) {
    Write-Host 'SoloProbar: no se escribe npcs-skins.yml.'
    exit 0
}

$lineas = New-Object System.Collections.Generic.List[string]
$lineas.Add('# npcs-skins.yml - GENERADO por docs/perugius/skins/resolver_skins_mojang.ps1')
$lineas.Add('# Fecha: ' + (Get-Date).ToString('yyyy-MM-dd HH:mm:ss'))
$lineas.Add('# No editar a mano: se pisa al volver a correr el script.')
$lineas.Add('# value: propiedad "textures" en base64. signature: firma de Mojang cuando la skin')
$lineas.Add('# viene de la cuenta actual; vacia cuando se usa la textura original del CDN.')
$lineas.Add('# value vacio significa que ese NPC usa el aldeano de respaldo, sin skin.')
foreach ($r in $resultados) {
    $lineas.Add('')
    $lineas.Add(('{0}:' -f $r.id))
    $lineas.Add(('  value: "{0}"' -f $r.value))
    $lineas.Add(('  signature: "{0}"' -f $r.signature))
}

$directorioSalida = Split-Path -Parent $Salida
if ($directorioSalida -and -not (Test-Path -LiteralPath $directorioSalida)) {
    New-Item -ItemType Directory -Path $directorioSalida -Force | Out-Null
}
$lineas | Set-Content -LiteralPath $Salida -Encoding UTF8
Write-Host ''
Write-Host ("Escrito {0}" -f $Salida)
Write-Host 'Para aplicarlo sin reiniciar: copiar el archivo a plugins/MushokuCore/ y usar /vecino recargar.'
exit 0

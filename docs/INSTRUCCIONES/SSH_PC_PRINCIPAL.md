# Conexion SSH desde la notebook a la PC principal (Windows 10)

Objetivo: que el agente de la notebook lea y edite el proyecto y corra el build en
la PC principal, sin tocar nada fuera de `C:\Dev`.

Este archivo lo genera la notebook. La clave publica de abajo ya existe y su
privada no sale de `C:\Users\Usuario\.ssh\mushoku_main`.

## Paso 1: correr esto en la PC principal

Abrir **PowerShell como administrador** (boton derecho sobre Inicio → "Windows
PowerShell (Administrador)") y pegar el bloque completo.

```powershell
# --- 1. Instalar OpenSSH Server ---
$cap = Get-WindowsCapability -Online -Name 'OpenSSH.Server*'
if ($cap.State -ne 'Installed') {
    Write-Output "instalando OpenSSH Server..."
    Add-WindowsCapability -Online -Name $cap.Name | Out-Null
}

# --- 2. Servicio automatico ---
Set-Service -Name sshd -StartupType Automatic
Start-Service sshd
Get-Service sshd | Select-Object Name, Status, StartType | Format-Table -AutoSize

# --- 3. Firewall ---
if (-not (Get-NetFirewallRule -Name 'OpenSSH-Server-In-TCP' -ErrorAction SilentlyContinue)) {
    New-NetFirewallRule -Name 'OpenSSH-Server-In-TCP' -DisplayName 'OpenSSH Server (sshd)' `
        -Enabled True -Direction Inbound -Protocol TCP -Action Allow -LocalPort 22 | Out-Null
    Write-Output "regla de firewall creada"
}

# --- 4. Shell por defecto: PowerShell (no cmd) ---
New-Item -Path 'HKLM:\SOFTWARE\OpenSSH' -Force | Out-Null
New-ItemProperty -Path 'HKLM:\SOFTWARE\OpenSSH' -Name DefaultShell `
    -Value 'C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe' `
    -PropertyType String -Force | Out-Null

# --- 5. Clave publica de la notebook ---
$clave = 'ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAID3fvDJZD8hHctpIN4it533pgOkUUKHAdfJu6RAmUqAb claudio@notebook-mushoku'

# 5a. Cuenta normal
$dir = Join-Path $env:USERPROFILE '.ssh'
New-Item -ItemType Directory -Force -Path $dir | Out-Null
$ak = Join-Path $dir 'authorized_keys'
if (-not (Test-Path $ak) -or -not (Select-String -Path $ak -SimpleMatch $clave -Quiet)) {
    Add-Content -Path $ak -Value $clave -Encoding ascii
}
icacls.exe $ak /inheritance:r /grant:r "${env:USERNAME}:(F)" /grant:r "*S-1-5-18:(F)" | Out-Null

# 5b. Cuenta administradora: sshd lee OTRO archivo e IGNORA el del perfil.
# Se escribe siempre: si la cuenta no es admin, el archivo existe y nadie lo lee.
$admAk = Join-Path $env:ProgramData 'ssh\administrators_authorized_keys'
if (-not (Test-Path $admAk) -or -not (Select-String -Path $admAk -SimpleMatch $clave -Quiet)) {
    Add-Content -Path $admAk -Value $clave -Encoding ascii
}
icacls.exe $admAk /inheritance:r /grant:r "*S-1-5-32-544:(F)" /grant:r "*S-1-5-18:(F)" | Out-Null

# --- 6. Reiniciar sshd para tomar el shell por defecto ---
Restart-Service sshd

# --- 7. Datos para la notebook ---
# Solo adaptadores con puerta de enlace: descarta loopback, Teredo, VPN caidas
# y adaptadores virtuales que reportan 0.0.0.0.
$ip = Get-NetIPConfiguration |
    Where-Object { $_.IPv4DefaultGateway -and $_.NetAdapter.Status -eq 'Up' } |
    ForEach-Object { $_.IPv4Address.IPAddress } |
    Where-Object { $_ -match '^\d{1,3}(\.\d{1,3}){3}$' -and $_ -ne '0.0.0.0' }

Write-Output ""
Write-Output "LISTO. Pasale estos datos a la notebook:"
Write-Output ("  usuario: " + $env:USERNAME)
Write-Output ("  equipo:  " + $env:COMPUTERNAME)
Write-Output ("  sshd:    " + (Get-Service sshd).Status)
if ($ip) {
    Write-Output ("  IP LAN:  " + ($ip -join ', '))
} else {
    Write-Output "  IP LAN:  no detectada. Correr 'ipconfig' y buscar la IPv4 del"
    Write-Output "           adaptador cuya 'Puerta de enlace predeterminada' no este vacia."
}
```

## Paso 2: datos que necesito

El usuario y la IP salen impresos al final del bloque. Si algo no aparece
porque el bloque cortó antes, sacarlos a mano:

```powershell
whoami
Get-NetIPConfiguration | Where-Object { $_.IPv4DefaultGateway } |
    ForEach-Object { "$($_.InterfaceAlias): $($_.IPv4Address.IPAddress)" }
Get-Service sshd | Select-Object Status, StartType
```

`whoami` devuelve `equipo\usuario`: el usuario SSH es la parte de la derecha.
Con eso ajusto `~\.ssh\config` en la notebook.

## Paso 3: verificar

Desde la PC principal, para confirmar que el servicio escucha:

```powershell
Get-NetTCPConnection -LocalPort 22 -State Listen
```

## Seguridad: no desactivar la contrasena todavia

`PasswordAuthentication` sigue en `yes` por defecto. Recien cuando la clave
funcione conviene apagarlo, para no quedarse afuera si algo fallo. Cuando este
verificado, en `C:\ProgramData\ssh\sshd_config`:

```
PasswordAuthentication no
```

y despues `Restart-Service sshd`.

## Limites de la sesion SSH

- **Sin UAC.** Aunque la cuenta sea administradora, la sesion SSH entra sin
  elevar: no puedo escribir en `Program Files` ni correr comandos elevados. Para
  el proyecto en `C:\Dev` no hace falta.
- **Sin GUI.** No veo la pantalla ni puedo clickear. Moonlight no me sirve para
  eso. Si hace falta mirar el juego, un screenshot guardado como PNG lo puedo
  leer.
- **Consola del servidor.** No puedo escribir en la consola de un proceso ya
  corriendo. Leo `logs\latest.log`; para reiniciar el servidor necesito que me lo
  pidas.
- **Alcance.** Trabajo en `C:\Dev`. Cualquier escritura fuera de ahi la aviso
  antes.

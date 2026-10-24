# Lanzamiento — día 1

Con Nether y End desactivados en los archivos de configuración, iniciar Paper y
ejecutar:

```text
function mt:setup
function mt:stage_launch
whitelist on
save-all flush
```

Verificar que `allow-nether=false` y `settings.allow-end=false` se hayan leído
durante el arranque.

Luego verificar:

```text
towny version
sk reload potencias
sk reload enfermedades
geyser reload
```

No usar `/reload` de Bukkit/Paper. Si Geyser registra los siete mapeos y ambos
Skripts cargan sin errores, entrar con una cuenta Java y una Bedrock antes de
retirar la whitelist.


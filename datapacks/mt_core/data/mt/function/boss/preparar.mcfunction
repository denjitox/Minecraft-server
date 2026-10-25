scoreboard players set global mt_prep 1
scoreboard players set global mt_timer 200
function mt:puerta/vano_cerrar
bossbar set mt:jefe players @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64]
bossbar set mt:jefe visible true
bossbar set mt:jefe max 400
bossbar set mt:jefe value 400
title @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64] title {"text":"HITOGAMI","color":"dark_purple","bold":true}
title @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64] subtitle {"text":"El Dios Humano despierta.","color":"light_purple"}
playsound minecraft:entity.wither.spawn hostile @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64] ~ ~ ~ 1 0.6

function mt:puerta/vano_abrir
fill -2305 -42 -2971 -2303 -40 -2969 minecraft:air replace minecraft:bedrock
fill -2305 -42 -2971 -2303 -40 -2969 minecraft:end_portal
scoreboard players set global mt_end 1
title @a actionbar {"text":"La puerta al End ha sido desbloqueada.","color":"light_purple"}
playsound minecraft:block.portal.trigger master @a ~ ~ ~ 0.8 1.2
bossbar set mt:jefe visible false
bossbar set mt:jefe players
kill @e[tag=mt_add]

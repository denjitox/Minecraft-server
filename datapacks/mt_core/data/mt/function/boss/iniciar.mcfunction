scoreboard players set global mt_prep 0
scoreboard players set global mt_fight 1
kill @e[tag=mt_boss]
kill @e[tag=mt_add]
execute as @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64] run scoreboard players add #n mt_calc 1
bossbar set mt:jefe max 400
bossbar set mt:jefe value 400
bossbar set mt:jefe players @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64]
# attributes en minuscula con id/base; `Attributes`/`Name`/`Base` es formato de item y se ignora en silencio.
# [A VERIFICAR] LevelledMobs puede reescribir max_health al spawnear (contrato 10, sec. 2.1): excluir al jefe.
summon minecraft:ravager -2304 -15 -3008 {Tags:["mt_boss","mt_add_protegido"],CustomName:'{"text":"Hitogami, el Dios Humano","color":"dark_purple"}',CustomNameVisible:1b,PersistenceRequired:1b,DeathLootTable:"mt:boss/hitogami",attributes:[{id:"minecraft:max_health",base:400.0d},{id:"minecraft:attack_damage",base:14.0d},{id:"minecraft:armor",base:8.0d},{id:"minecraft:knockback_resistance",base:1.0d},{id:"minecraft:movement_speed",base:0.30d},{id:"minecraft:follow_range",base:48.0d}]}
data merge entity @e[tag=mt_boss,limit=1] {Health:400.0f}
scoreboard players set #n mt_calc 0

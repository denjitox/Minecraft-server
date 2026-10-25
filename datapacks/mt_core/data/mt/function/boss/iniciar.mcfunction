scoreboard players set global mt_prep 0
scoreboard players set global mt_fight 1
kill @e[tag=mt_boss]
kill @e[tag=mt_add]
execute as @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64] run scoreboard players add #n mt_calc 1
bossbar set mt:jefe max 400
bossbar set mt:jefe value 400
bossbar set mt:jefe players @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64]
summon minecraft:ravager -2304 -15 -3008 {Tags:["mt_boss","mt_add_protegido"],CustomName:'{"text":"Hitogami, el Dios Humano","color":"dark_purple"}',CustomNameVisible:1b,PersistenceRequired:1b,DeathLootTable:"mt:boss/hitogami",Attributes:[{Name:"minecraft:max_health",Base:400},{Name:"minecraft:attack_damage",Base:14},{Name:"minecraft:armor",Base:8},{Name:"minecraft:knockback_resistance",Base:1.0},{Name:"minecraft:movement_speed",Base:0.30},{Name:"minecraft:follow_range",Base:48}]}
data merge entity @e[tag=mt_boss,limit=1] {Health:400.0f}
scoreboard players set #n mt_calc 0

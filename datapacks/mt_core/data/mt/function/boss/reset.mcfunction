scoreboard players set global mt_fight 0
scoreboard players set global mt_prep 0
bossbar set mt:jefe visible false
bossbar set mt:jefe players
kill @e[tag=mt_boss]
kill @e[tag=mt_add]
fill -2312 -15 -2984 -2297 -11 -2984 minecraft:barrier replace minecraft:air
fill -2312 -15 -2984 -2297 -11 -2984 minecraft:barrier replace minecraft:end_portal

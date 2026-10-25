scoreboard objectives add mt_stage dummy
scoreboard objectives add mt_roll dummy
scoreboard objectives add mt_fight dummy
scoreboard objectives add mt_prep dummy
scoreboard objectives add mt_end dummy
scoreboard objectives add mt_seal dummy
scoreboard objectives add mt_timer dummy
scoreboard objectives add mt_calc dummy
scoreboard objectives add mt_boss_first_kill dummy
scoreboard objectives add mt_cd dummy

execute unless score global mt_fight matches 0.. run scoreboard players set global mt_fight 0
execute unless score global mt_prep matches 0.. run scoreboard players set global mt_prep 0
execute unless score global mt_end matches 0.. run scoreboard players set global mt_end 0
execute unless score global mt_seal matches 0.. run scoreboard players set global mt_seal 0
execute unless score global mt_boss_first_kill matches 0.. run scoreboard players set global mt_boss_first_kill 0

bossbar add mt:jefe {"text":"Hitogami, el Dios Humano","color":"dark_purple"}
bossbar set mt:jefe color purple
bossbar set mt:jefe style notched_10
bossbar set mt:jefe visible false

function mt:boss/reset
tellraw @a[tag=admin] {"text":"[mt_core] Hitogami cargado","color":"light_purple"}

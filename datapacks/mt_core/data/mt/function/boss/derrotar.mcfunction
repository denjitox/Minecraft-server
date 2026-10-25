execute if score global mt_end matches 0 run function mt:puerta/desbloquear
kill @e[tag=mt_add]
bossbar set mt:jefe visible false
bossbar set mt:jefe players
scoreboard players set global mt_fight 0
scoreboard players set global mt_prep 0
title @a title {"text":"VICTORIA","color":"gold","bold":true}
title @a subtitle {"text":"Hitogami ha caido. El portal al End se ha abierto.","color":"yellow"}
playsound minecraft:ui.toast.challenge_complete master @a ~ ~ ~ 1 1

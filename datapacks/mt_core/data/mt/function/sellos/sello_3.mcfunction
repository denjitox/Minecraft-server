scoreboard players add global mt_seal 1
tellraw @a {"text":"[Sellos] Sello 3/3 activado.","color":"green"}
playsound minecraft:block.amethyst_block.hit master @a ~ ~ ~ 1 1
execute if score global mt_seal matches 3 run function mt:sellos/abrir_arena

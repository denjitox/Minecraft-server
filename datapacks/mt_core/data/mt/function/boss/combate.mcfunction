function mt:boss/barra
execute unless entity @e[tag=mt_boss] if score global mt_fight matches 1 run scoreboard players set global mt_fight 2
execute unless entity @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64] run function mt:boss/limpiar
actionbar @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64] actionbar {"text":"Hitogami, el Dios Humano","color":"light_purple"}

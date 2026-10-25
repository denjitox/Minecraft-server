execute if score global mt_fight matches 0 unless score global mt_prep matches 1 unless score global mt_end matches 1 if entity @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64] run function mt:boss/preparar
execute if score global mt_prep matches 1 run function mt:boss/cuenta_atras
execute if score global mt_fight matches 1 run function mt:boss/combate
execute if score global mt_fight matches 2 run function mt:boss/derrotar

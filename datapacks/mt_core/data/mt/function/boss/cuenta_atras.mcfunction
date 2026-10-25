scoreboard players remove global mt_timer 1
execute if score global mt_timer matches 100 run title @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64] title {"text":"3","color":"red","bold":true}
execute if score global mt_timer matches 60 run title @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64] title {"text":"2","color":"red","bold":true}
execute if score global mt_timer matches 20 run title @a[x=-2330,y=-20,z=-3046,dx=52,dy=44,dz=64] title {"text":"1","color":"red","bold":true}
execute if score global mt_timer matches 0 run function mt:boss/iniciar

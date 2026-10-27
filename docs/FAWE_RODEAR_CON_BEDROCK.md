# Rodear una estructura con bedrock (FAWE)

Envolver una construcción en una caja de bedrock sin dañarla. Todo se apoya en
comandos estables de WorldEdit/FAWE: no hace falta ningún script ni plugin extra.

## Receta recomendada: dos comandos

Seleccioná la estructura con `//pos1` y `//pos2` ajustados al bloque exacto (sin
aire de más), y después:

```
//outset 1
//faces bedrock
```

`//outset 1` agranda la selección 1 bloque en las seis direcciones. `//faces`
rellena solo las seis caras del nuevo volumen, o sea la capa que quedó a un
bloque de la estructura. La construcción no se toca: queda aire entre ella y la
cáscara.

Si preferís una jaula pegada, sin cámara de aire:

```
//outset 1
//contract 1
//faces bedrock
```

`//contract 1` devuelve la selección a la medida original, así que las caras caen
sobre el borde de la estructura. Ojo: esto **sí reemplaza** los bloques del
perímetro.

## Jaula de más de un bloque de espesor

Cada pasada trabaja desde afuera hacia adentro:

```
//outset 3
//faces bedrock
//outset 2
//faces bedrock
//outset 1
//faces bedrock
```

Tres capas. Para dos, empezá en `//outset 2`.

## Sin FAWE: comando vanilla

Si no querés depender del plugin, el `/fill` de Minecraft tiene el modificador
`outline`, que rellena solo la cáscara:

```
/fill <x1> <y1> <z1> <x2> <y2> <z2> bedrock outline
```

Hay que pasarle las coordenadas ya agrandadas un bloque. Límite: 32.768 bloques
por comando; si el volumen es mayor, partilo en capas.

## Caso Hitogami: cámara del portal

La cámara está en `-2305 -42 -2971` a `-2303 -40 -2969` (3×3×3). El vano tiene que
quedar libre, así que la jaula se arma y después se abre el hueco:

```
//pos1 -2306 -43 -2972
//pos2 -2302 -39 -2968
//faces bedrock
//pos1 -2312 -15 -2984
//pos2 -2297 -11 -2984
//set air
```

El segundo bloque es el vano confirmado del diseño. Verificá que el `z` del vano
sea el correcto para tu pegado antes de correrlo.

## Antes de ejecutar

- **Selección ajustada.** Si `//pos1`/`//pos2` incluyen aire de más, la jaula queda
  flotando a esa distancia.
- **Volumen.** Una jaula de 100×100×100 son ~60.000 bloques por capa; FAWE lo
  hace, pero avisá a los jugadores por el tirón de TPS.
- **`//undo`** revierte la última operación completa. Con varias pasadas seguidas,
  cada una es un paso de undo.
- **Guardá un schematic antes** si la estructura es irrecuperable:
  `//copy` + `//schem save <nombre>`.

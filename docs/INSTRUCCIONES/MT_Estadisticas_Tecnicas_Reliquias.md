# Estadísticas técnicas de las reliquias

> Especificación de balance e implementación para las siete reliquias iniciales de las Trial Chambers del servidor *Mushoku Tensei*. Este documento define valores concretos para `MushokuCore`; el nombre, lore y resource pack son únicamente presentación.

**Versión del diseño:** 1.0  
**Objetivo:** Paper Java 26.2+ con Geyser/Floodgate  
**Jugadores previstos:** 25–30 simultáneos  
**Fuente de obtención:** bóvedas normales y ominosas de Trial Chambers

---

## 1. Catálogo resumido

| ID interno | Reliquia | Tipo | Base vanilla | Normal | Ominosa | Máximo activo global |
| --- | --- | --- | --- | ---: | ---: | ---: |
| `suppression_ring` | Anillo de Supresión Mágica | Menor/defensiva | Amethyst Shard | 3 % | 5 % | 12 |
| `sara_earring` | Pendiente de Sara | Menor/distancia | Echo Shard | 3 % | 5 % | 12 |
| `migurd_pendant` | Colgante Migurd | Menor/acuática | Heart of the Sea | 3 % | 5 % | 10 |
| `paul_sword` | Espada de Paul | Mayor/cuerpo a cuerpo | Iron Sword | 2 % | 5 % | 6 |
| `superd_spear` | Lanza Superd | Mayor/PvE | Trident | 1 % | 4 % | 3 |
| `aqua_heartia` | Aqua Heartia | Mayor/mágica | Diamond Sword modificada | — | 3 % | 2 |
| `magic_armor_mk1` | Armadura Mágica MK-I | Mayor/armadura | Netherite Chestplate | — | 3 % | 2 |

Las probabilidades son tiradas exclusivas: se elige como máximo una reliquia por bóveda válida. El total continúa siendo **12 % en una bóveda normal** y **30 % en una ominosa**.

Si una reliquia alcanza el máximo global, su peso se retira antes de la tirada. No se selecciona para luego reemplazarla silenciosamente. Como compensación, una tirada que hubiera quedado sin candidatos puede entregar materiales de reparación, pero nunca otra reliquia fuera de su tabla.

---

## 2. Reglas globales

### 2.1 Ranuras virtuales

Para no depender de accesorios de mods ni de la mano secundaria:

- cada jugador dispone de **una ranura de reliquia mayor**;
- cada jugador dispone de **dos ranuras de reliquia menor**;
- las ranuras se administran con `/reliquia equipar`, `/reliquia quitar` y un formulario opcional de Bedrock;
- la reliquia debe permanecer en el inventario del jugador;
- las armas mayores solo aplican su habilidad de ataque cuando están en la mano principal;
- la Armadura Mágica solo funciona cuando está colocada en el torso;
- si el objeto sale del inventario, su ranura virtual se desactiva inmediatamente;
- el servidor valida las ranuras al entrar, morir, cambiar de mundo y modificar el inventario.

Un formulario de Geyser mejora la interfaz, pero los comandos son siempre la vía principal y universal.

### 2.2 Categorías

| Categoría | Regla |
| --- | --- |
| Menor | Puede combinarse con otra menor y una mayor |
| Mayor | Solo una activa por jugador |
| Arma | Debe estar en la mano principal para atacar o activar |
| Armadura | Debe estar equipada en su ranura vanilla |
| Pasiva | Se aplica automáticamente mientras esté correctamente equipada |
| Activa | Requiere uso del objeto o comando alternativo y respeta enfriamiento |

### 2.3 Fórmula de daño

Los porcentajes se aplican al daño final que habría provocado el ataque después de las reglas vanilla, pero antes de sumar el bono de la reliquia:

```text
bono = daño_base_final × porcentaje
daño_total = daño_base_final + bono + daño_plano
```

Límites globales:

- un bono porcentual de reliquia no puede superar **4 puntos de daño** por impacto;
- el daño plano y porcentual combinado no puede superar **6 puntos adicionales en PvE**;
- en PvP no puede superar **3 puntos adicionales**;
- dos efectos de la misma reliquia no se acumulan;
- los multiplicadores de reliquias se suman entre sí antes de aplicarse, nunca se multiplican de forma encadenada;
- el daño causado por reflejo, espinas, mascotas o explosiones indirectas no activa bonos de arma.

Un punto de daño equivale a medio corazón.

### 2.4 Enfriamientos

- se guardan como instante absoluto UTC en SQLite;
- continúan mientras el jugador está desconectado;
- no se reinician al morir, cambiar de mundo o reiniciar el servidor;
- se muestran en segundos enteros mediante action bar o `/reliquia info`;
- el modo creativo no elimina enfriamientos;
- administradores pueden reiniciarlos solo con permiso y registro de auditoría.

### 2.5 Durabilidad y reparación

- ninguna reliquia admite `Mending`;
- `Unbreaking` funciona únicamente si se permite expresamente para esa reliquia;
- el costo de una habilidad se descuenta aunque el objetivo muera por el golpe;
- una habilidad no puede activarse si el objeto quedaría con menos de 1 punto de durabilidad;
- las reliquias nunca se destruyen: al llegar a 1 quedan en estado `DORMANT`;
- un objeto `DORMANT` conserva identidad y apariencia, pero no aplica habilidades;
- no se reparan en yunque ni combinando dos copias;
- se reparan mediante `/reliquia reparar` usando Fragmentos de Cámara.

| Material | Reparación sugerida |
| --- | ---: |
| Fragmento de Cámara normal | 8 % de la durabilidad máxima |
| Fragmento de Cámara ominoso | 20 % de la durabilidad máxima |
| Núcleo de reparación de jefe | 50 % de la durabilidad máxima |

### 2.6 PvP, regiones y eventos

Todas las activas se bloquean:

- en regiones con `mushoku-relics: deny`;
- mientras el jugador está en creativo o espectador;
- durante los primeros 10 segundos después de entrar al servidor;
- si el jugador está muerto, congelado por una cinemática o pendiente de teleportación;
- cuando la reliquia no coincide con su registro `ACTIVE` en la base.

El servidor distingue `PVE`, `PVP`, `DUEL` y `EVENT`. Los duelos pueden imponer valores propios sin modificar la configuración global.

---

## 3. Anillo de Supresión Mágica

```text
ID: suppression_ring
Clase: reliquia menor defensiva
Base: AMETHYST_SHARD
Durabilidad virtual: 160 cargas
Copias activas: 12
```

### Estadísticas

| Propiedad | PvE | PvP |
| --- | ---: | ---: |
| Reducción de daño mágico | 12 % | 7 % |
| Reducción de duración de efectos negativos | 15 % | 10 % |
| Duración de Barrera de Supresión | 6 s | 4 s |
| Reducción mágica durante la activa | 35 % | 20 % |
| Enfriamiento | 75 s | 90 s |
| Costo | 4 cargas | 4 cargas |

### Pasiva — Supresión

Reduce el daño clasificado por el servidor como mágico o de efecto indirecto. La lista inicial incluye:

- magia e indirect magic;
- daño de pociones instantáneas;
- aliento del dragón;
- proyectiles mágicos de jefes registrados;
- daño especial de enfermedades, si su configuración lo permite.

No reduce daño cuerpo a cuerpo, proyectiles normales, caída, fuego, lava, vacío, hambre ni explosiones ordinarias.

La reducción de duración se calcula una sola vez al aplicar Veneno, Debilidad, Lentitud, Náusea u Oscuridad. No afecta Wither aplicado por jefes, Mal Augurio ni efectos narrativos marcados como `unsuppressable`.

### Activa — Barrera de Supresión

Se activa usando el anillo desde la mano principal o mediante `/reliquia activar menor1|menor2`. Durante la ventana indicada aumenta temporalmente la reducción mágica.

Reglas:

- no otorga inmunidad completa;
- no elimina efectos ya activos;
- no se acumula con otro anillo;
- la mejor reducción disponible reemplaza a la menor;
- consumir leche no reinicia su enfriamiento.

### Rol de balance

Es una defensa especializada. Debe ser poco útil contra enemigos normales y valiosa contra brujas, guardianes, dragón, enfermedades o jefes mágicos.

---

## 4. Pendiente de Sara

```text
ID: sara_earring
Clase: reliquia menor de proyectiles
Base: ECHO_SHARD
Durabilidad virtual: 180 cargas
Copias activas: 12
```

### Estadísticas

| Propiedad | PvE | PvP |
| --- | ---: | ---: |
| Bono pasivo de proyectil | 8 % | 4 % |
| Distancia mínima para el bono | 8 bloques | 10 bloques |
| Bono de Disparo Certero | +3 daño | +1,5 daño |
| Velocidad del proyectil en la activa | +20 % | +10 % |
| Ventana para disparar | 8 s | 6 s |
| Enfriamiento | 45 s | 60 s |
| Costo | 3 cargas y 2 puntos de comida | Igual |

### Pasiva — Ojo de Arquera

El bono se aplica a flechas, flechas espectrales y proyectiles de ballesta disparados directamente por el portador. No afecta tridentes, fuegos artificiales, pociones, bolas de nieve ni proyectiles de otra entidad.

La distancia se mide entre la posición de lanzamiento y el impacto. Esto evita usar el bono en combate cuerpo a cuerpo con arco.

### Activa — Disparo Certero

Al activarla, el primer proyectil válido disparado dentro de la ventana recibe velocidad y daño adicional. La activa se consume al disparar, no al impactar.

Reglas:

- Multishot solo potencia el proyectil central;
- Piercing solo aplica el daño adicional al primer objetivo;
- el disparo conserva gravedad y dispersión vanilla;
- no atraviesa escudos ni ignora armadura;
- si vence la ventana sin disparar, se consume el 50 % del enfriamiento y no se gastan cargas;
- cambiar de mundo cancela la ventana.

### Rol de balance

Premia distancia y puntería sin superar a un arco Power V bien utilizado ni crear un disparo letal automático.

---

## 5. Colgante Migurd

```text
ID: migurd_pendant
Clase: reliquia menor acuática y de soporte
Base: HEART_OF_THE_SEA
Durabilidad virtual: 140 cargas
Copias activas: 10
```

### Estadísticas

| Propiedad | Valor |
| --- | ---: |
| Reducción del consumo de aire | 50 % |
| Velocidad de nado adicional | 10 % |
| Reducción del cooldown de Aqua Heartia | 15 % |
| Absorción de Manto Acuático | 4 puntos/2 corazones |
| Duración del manto | 8 s PvE / 6 s PvP |
| Enfriamiento | 90 s |
| Costo | 5 cargas y 2 niveles de experiencia |

### Pasiva — Afinidad Migurd

Mientras el jugador está en agua:

- consume aire a la mitad de la frecuencia normal;
- recibe una mejora moderada de nado;
- reduce en 15 % el enfriamiento base de Aqua Heartia.

El cooldown de Aqua Heartia pasa de 25 a **21,25 segundos**, pero se muestra redondeado hacia arriba como 22. El valor real se conserva en milisegundos.

No se acumula con otro Colgante Migurd. Conduit Power y Water Breathing siguen funcionando normalmente, pero no duplican la reducción de aire.

### Activa — Manto Acuático

Solo se puede activar en agua, bajo lluvia con cielo visible o dentro de una región marcada como acuática. Concede absorción temporal.

Reglas:

- no cura vida real;
- la absorción desaparece al terminar la duración;
- no se renueva antes de finalizar;
- salir del agua no cancela un manto ya creado;
- en Nether queda deshabilitada aunque exista agua colocada por otro plugin.

### Rol de balance

Es la pareja natural de Aqua Heartia, pero ocupa una de las dos ranuras menores y no vuelve obligatoria esa combinación fuera de contenido acuático.

---

## 6. Espada de Paul

```text
ID: paul_sword
Clase: reliquia mayor de combate versátil
Base: IRON_SWORD
Durabilidad máxima: 750
Copias activas: 6
```

### Atributos base

| Atributo | Valor final |
| --- | ---: |
| Daño de ataque mostrado | 6,5 |
| Velocidad de ataque | 1,6 |
| Bono contra monstruos con espada/hacha | 10 % |
| Bono contra jugadores | 0 % |
| Encantamiento máximo permitido | Sharpness III |
| Unbreaking | Hasta III |

El valor final se define con modificadores propios del objeto. No se suma accidentalmente encima de los atributos de una espada vanilla duplicados por el plugin.

### Pasiva — Veterano de tres escuelas

Inflige 10 % más daño contra monstruos que porten un arma cuerpo a cuerpo. Se considera arma una espada, hacha, tridente o arma de jefe registrada.

No afecta jugadores, animales, aldeanos ni gólems amistosos.

### Activa — Barrido del Aventurero

Un uso prepara el siguiente golpe durante 5 segundos. Si impacta a una criatura válida:

- el objetivo principal recibe el golpe normal;
- hasta tres monstruos hostiles situados en un arco de 120° y radio de 3 bloques reciben 2,5 puntos de daño;
- en PvP no hay daño de área: el objetivo principal recibe solamente +1 punto;
- el barrido no activa Looting, Fire Aspect, robo de vida ni otros efectos por cada objetivo secundario.

| Parámetro | PvE | PvP |
| --- | ---: | ---: |
| Enfriamiento | 22 s | 35 s |
| Durabilidad consumida | 4 | 4 |
| Comida consumida | 2 puntos | 3 puntos |

### Restricciones

- no admite Sweeping Edge por encima de III;
- el barrido no daña mascotas, aldeanos o miembros de la misma facción;
- no rompe escudos automáticamente;
- no puede aplicar daño de área a través de paredes;
- el portador debe tener línea visual con cada objetivo secundario.

### Rol de balance

Es la reliquia mayor introductoria: fiable contra grupos, reparable y más fácil de obtener, pero inferior a netherita encantada en daño contra un único objetivo.

---

## 7. Lanza Superd

```text
ID: superd_spear
Clase: reliquia mayor de caza PvE
Base: TRIDENT
Durabilidad máxima: 1.100
Copias activas: 3
```

### Atributos base

| Atributo | Valor |
| --- | ---: |
| Daño cuerpo a cuerpo | 9 |
| Velocidad de ataque | 1,1 |
| Daño arrojado | Vanilla del tridente |
| Bono contra monstruos | 18 % |
| Bono contra jugadores | 0 % pasivo |
| Encantamientos incorporados | Loyalty III, Unbreaking III |

### Pasiva — Cazador Superd

El bono de 18 % se aplica solamente a criaturas hostiles. No afecta jugadores, mascotas, aldeanos o gólems que no hayan sido configurados como enemigos del evento.

Cada eliminación válida sin recibir daño incrementa una racha. Al llegar a cinco:

- concede Speed I durante 10 segundos;
- reinicia la racha a cero;
- muestra un mensaje breve y un sonido;
- no gasta durabilidad adicional.

La racha caduca después de 45 segundos sin una baja válida y se reinicia al recibir daño, morir, soltar la lanza, cambiar de portador o cambiar de mundo.

No cuentan criaturas de spawner personalizado marcado como granja, mascotas, criaturas invocadas por jugadores ni mobs sin recompensa.

### Activa — Embestida Superd

Prepara durante 5 segundos el siguiente golpe cuerpo a cuerpo.

| Parámetro | PvE | PvP |
| --- | ---: | ---: |
| Daño plano adicional | +4 | +2 |
| Multiplicador de retroceso | 1,20 | 1,10 |
| Enfriamiento | 35 s | 50 s |
| Durabilidad consumida | 8 | 8 |
| Comida consumida | 3 puntos | 4 puntos |

No impulsa al portador ni permite atravesar bloques. La habilidad requiere que el ataque tenga al menos 90 % de carga para impedir spam.

### Restricciones

- no admite Channeling ni Riptide;
- Loyalty es obligatorio y no se puede retirar;
- la activa no se aplica al lanzamiento;
- si el tridente arrojado no vuelve por inventario lleno, se deposita en el buzón de reliquias;
- mientras está convertido en entidad arrojada, continúa asociado a su `relic_uuid`.

### Rol de balance

Es una de las mejores armas PvE del catálogo inicial, pero no obtiene el bono de monstruos ni la racha contra jugadores.

---

## 8. Aqua Heartia

```text
ID: aqua_heartia
Clase: reliquia mayor mágica
Base inicial: DIAMOND_SWORD
Representación: báculo mediante item_model/resource pack
Durabilidad máxima: 1.250
Copias activas: 2
```

### Decisión sobre el material base

El paquete de Trial Chambers ya la define como `DIAMOND_SWORD`, mientras la propuesta narrativa posterior recomienda `BLAZE_ROD`. La versión 1.0 conserva `DIAMOND_SWORD` para no romper modelos, loot ni mapeos de Geyser existentes.

Para impedir que sea una espada de diamante disfrazada:

- daño cuerpo a cuerpo final: **1 punto**;
- velocidad de ataque: **1,0**;
- no admite Sharpness, Smite, Bane of Arthropods, Fire Aspect ni Sweeping Edge;
- el plugin elimina encantamientos incompatibles y registra el intento.

Una migración futura a `BLAZE_ROD` debe convertir las instancias por `relic_uuid`, nunca por nombre.

### Pasiva — Afinidad acuática

Los proyectiles ordinarios disparados por el portador mientras está bajo lluvia con cielo visible o sumergido reciben:

| Entorno | PvE | PvP |
| --- | ---: | ---: |
| Lluvia | +10 % | +5 % |
| Sumergido | +15 % | +8 % |

No se acumulan lluvia y agua: se utiliza el mayor valor. La bonificación no afecta el propio Cañón Acuático ni proyectiles mágicos de otros plugins.

### Activa — Cañón Acuático

Dispara un trazado acuático desde la mirada del jugador.

| Parámetro | PvE | PvP |
| --- | ---: | ---: |
| Alcance | 18 bloques | 14 bloques |
| Radio de impacto | 1,25 bloques | 1 bloque |
| Daño al primer objetivo | 6 | 4 |
| Daño al segundo objetivo | 4,5 | 3 |
| Daño al tercer objetivo | 3 | 2 |
| Objetivos máximos | 3 | 3 |
| Retroceso | 1,20 | 0,80 |
| Enfriamiento | 25 s | 35 s |
| Durabilidad consumida | 20 | 20 |
| Comida consumida | 4 puntos | 5 puntos |

Con Colgante Migurd equipado, el enfriamiento PvE baja a 21,25 segundos y el PvP a 29,75 segundos.

### Reglas del trazado

- requiere línea visual;
- se detiene ante bloques sólidos;
- no rompe bloques, cultivos, cuadros ni soportes de armadura;
- no apaga portales ni altera líquidos;
- ignora mascotas y aliados de facción;
- cada objetivo solo puede recibir un impacto por activación;
- no funciona en el Nether;
- en zonas seguras muestra la animación local, pero no consume recursos ni causa daño;
- el daño se registra como `mushoku:aqua_heartia`, no como golpe de espada.

### Rol de balance

Es una herramienta de control y daño moderado con alcance. No debe superar el daño sostenido de un arco Power V ni reemplazar armas cuerpo a cuerpo.

---

## 9. Armadura Mágica MK-I

```text
ID: magic_armor_mk1
Clase: reliquia mayor de armadura
Base: NETHERITE_CHESTPLATE
Durabilidad máxima: 740
Copias activas: 2
```

Esta pieza es un prototipo recuperado de las Trial Chambers. No es todavía el conjunto MK-II completo del Gran Laberinto.

### Atributos base

Conserva los atributos vanilla de una pechera de netherita:

| Atributo | Valor |
| --- | ---: |
| Armadura | 8 |
| Dureza de armadura | 3 |
| Resistencia al retroceso | 0,10 |
| Penalización de movimiento | −5 % |

La penalización se aplica como modificador estable y desaparece al quitar la pechera.

### Pasiva — Estabilización

- reduce un 15 % adicional el retroceso no explosivo;
- reduce 10 % del daño de caída, con máximo de 4 puntos evitados;
- no reduce daño de vacío, hambre, `/kill` o límites del mundo;
- impide equipar Elytra mientras está activa.

### Automática — Protocolo de emergencia

Se activa cuando un impacto válido deja al portador en **6 corazones o menos**.

| Propiedad | PvE | PvP |
| --- | ---: | ---: |
| Resistance | Nivel I, 4 s | Nivel I, 3 s |
| Absorción | 4 puntos | 2 puntos |
| Enfriamiento | 90 s | 120 s |
| Durabilidad consumida | 12 | 16 |

La evaluación ocurre después del daño. No evita un golpe que ya habría matado al jugador y no funciona como tótem.

Reglas:

- el daño ambiental también puede activarla, excepto vacío y hambre;
- no vuelve a activarse mientras persista la absorción anterior;
- quitar y volver a colocar la armadura no reinicia el cooldown;
- si queda con 1 de durabilidad durante el protocolo, pasa a `DORMANT` al terminar;
- no se acumula con la Armadura del Dios de la Lucha;
- Resistance existente de nivel igual o superior no se eleva, pero la absorción sí puede aplicarse.

### Encantamientos

Permitidos:

- Protection hasta III;
- Projectile Protection, Blast Protection o Fire Protection hasta IV;
- Unbreaking hasta III;
- Thorns hasta II.

Prohibidos:

- Mending;
- Curse of Binding;
- combinaciones que excedan los máximos vanilla;
- cualquier encantamiento personalizado no autorizado.

### Rol de balance

Es una defensa de emergencia, no una segunda vida. Su penalización de velocidad y la incompatibilidad con Elytra compensan la supervivencia adicional.

---

## 10. Interacciones entre reliquias

| Combinación | Resultado |
| --- | --- |
| Colgante Migurd + Aqua Heartia | Reduce 15 % el cooldown de Aqua Heartia |
| Anillo + Armadura Mágica | Ambas defensas funcionan, pero cada una conserva su categoría de daño |
| Pendiente + Aqua Heartia | Pendiente mejora flechas; no mejora Cañón Acuático |
| Espada de Paul + cualquier arma mayor | Imposible: solo una mayor equipada |
| Lanza arrojada + cambio de mayor | El cambio se bloquea hasta que la lanza regrese o sea recuperada |
| Dos copias de una menor | Solo una puede ocupar una ranura; no se acumulan |

El Núcleo de Maná Inestable y otros artefactos futuros no forman parte del catálogo inicial. Cuando se implementen, deberán respetar el límite global de reducción de cooldown de **25 %**.

---

## 11. Identidad PDC

Cada objeto debe contener:

| Clave | Tipo | Ejemplo |
| --- | --- | --- |
| `mushokucore:relic_id` | String | `aqua_heartia` |
| `mushokucore:relic_uuid` | String UUID | `550e8400-e29b-41d4-a716-446655440000` |
| `mushokucore:schema` | Integer | `1` |
| `mushokucore:issued_at` | Long epoch | fecha de creación |
| `mushokucore:source` | String | `trial_ominous` |
| `mushokucore:virtual_charges` | Integer | `140` |

La base SQLite conserva la autoridad:

```text
relic_uuid
relic_id
original_owner_uuid
current_owner_uuid
created_at
source
status
last_seen_at
last_seen_world
last_seen_holder_uuid
```

Estados permitidos:

```text
PENDING -> ACTIVE -> DORMANT -> ACTIVE
                  -> LOST
                  -> RETIRED
```

- `PENDING`: registrado, todavía no entregado;
- `ACTIVE`: instancia válida y utilizable;
- `DORMANT`: sin energía/durabilidad suficiente;
- `LOST`: instancia no localizada y bloqueada preventivamente;
- `RETIRED`: retirada administrativamente; nunca debe reactivarse automáticamente.

Si aparecen dos objetos con el mismo `relic_uuid`, ambos se bloquean, se registra `DUPLICATE_DETECTED` y un administrador decide cuál conservar.

---

## 12. Activación compatible con Bedrock

### Método primario

| Objeto | Activación |
| --- | --- |
| Anillo | Usar desde mano principal o comando |
| Pendiente | Usar para preparar el siguiente disparo |
| Colgante | Usar o comando estando en entorno válido |
| Espada de Paul | Usar para preparar el siguiente golpe |
| Lanza Superd | Usar agachado; el uso normal conserva lanzamiento |
| Aqua Heartia | Usar desde mano principal |
| Armadura Mágica | Automática al cruzar el umbral de vida |

### Comandos alternativos

```text
/reliquia info
/reliquia equipar <mayor|menor1|menor2>
/reliquia quitar <mayor|menor1|menor2>
/reliquia activar [mayor|menor1|menor2]
/reliquia cooldown
/reliquia reparar
```

No se requiere:

- mano secundaria;
- gesto exclusivo de Java;
- click sobre texto del chat;
- modelo 3D para saber qué hace el objeto;
- brillo de entidad para localizar objetivos;
- una receta de yunque personalizada.

---

## 13. Configuración sugerida

```yaml
relics:
  global:
    enabled: true
    schema: 1
    major-slots: 1
    minor-slots: 2
    prevent-destruction: true
    allow-mending: false
    persist-cooldowns: true
    pvp-extra-damage-cap: 3.0
    pve-extra-damage-cap: 6.0
    max-cooldown-reduction: 0.25

  suppression_ring:
    category: MINOR
    material: AMETHYST_SHARD
    max-active-copies: 12
    charges: 160
    magic-reduction-pve: 0.12
    magic-reduction-pvp: 0.07
    negative-duration-reduction: 0.15
    active-cooldown-seconds: 75

  sara_earring:
    category: MINOR
    material: ECHO_SHARD
    max-active-copies: 12
    projectile-bonus-pve: 0.08
    projectile-bonus-pvp: 0.04
    minimum-distance-pve: 8.0
    minimum-distance-pvp: 10.0
    active-cooldown-seconds: 45

  migurd_pendant:
    category: MINOR
    material: HEART_OF_THE_SEA
    max-active-copies: 10
    air-consumption-reduction: 0.50
    swim-speed-bonus: 0.10
    aqua-heartia-cooldown-reduction: 0.15
    active-cooldown-seconds: 90

  paul_sword:
    category: MAJOR
    material: IRON_SWORD
    max-active-copies: 6
    max-durability: 750
    attack-damage: 6.5
    attack-speed: 1.6
    armed-monster-bonus: 0.10
    active-cooldown-seconds: 22

  superd_spear:
    category: MAJOR
    material: TRIDENT
    max-active-copies: 3
    max-durability: 1100
    hostile-mob-bonus: 0.18
    streak-required: 5
    streak-expiration-seconds: 45
    active-cooldown-seconds: 35

  aqua_heartia:
    category: MAJOR
    material: DIAMOND_SWORD
    max-active-copies: 2
    max-durability: 1250
    melee-damage: 1.0
    range-pve: 18.0
    range-pvp: 14.0
    damage-pve: 6.0
    damage-pvp: 4.0
    max-targets: 3
    active-cooldown-seconds: 25

  magic_armor_mk1:
    category: MAJOR
    material: NETHERITE_CHESTPLATE
    max-active-copies: 2
    max-durability: 740
    movement-penalty: 0.05
    trigger-health: 12.0
    active-cooldown-seconds: 90
```

Los valores específicos de PvP deben permanecer en configuración aunque inicialmente coincidan con PvE. Eso permite ajustar balance sin recompilar.

---

## 14. Eventos y servicios de implementación

### Eventos principales

| Situación | Responsabilidad del módulo |
| --- | --- |
| Interacción con objeto | Validar PDC, dueño, ranura, mundo, región y cooldown |
| Daño causado | Aplicar bonos y límites una sola vez |
| Daño recibido | Anillo, racha Superd y protocolo de armadura |
| Lanzamiento/impacto | Pendiente y trazabilidad de la lanza |
| Cambio de efecto | Reducción de duración del anillo |
| Cambio de aire | Colgante Migurd |
| Cambio de inventario | Revalidar ranuras y duplicados |
| Muerte o desconexión | Persistir estado y cancelar ventanas activas |
| Cambio de mundo | Reiniciar rachas; conservar cooldowns |
| Daño del objeto | Impedir destrucción y marcar `DORMANT` |

### Servicios sugeridos

```text
RelicRegistry
RelicInstanceRepository
RelicEquipmentService
RelicActivationService
RelicCooldownService
RelicDamageService
RelicRepairService
RelicAuditService
RelicRecoveryService
```

Los listeners deben delegar las reglas a servicios. No guardar toda la lógica en un único `RelicListener`.

---

## 15. Auditoría

Registrar como mínimo:

- generación y tipo de bóveda;
- tirada y tabla utilizada;
- entrega, propietario y coordenadas;
- cambio de dueño;
- equipamiento y desequipamiento;
- reparación;
- intento de modificación prohibida;
- duplicado detectado;
- recuperación administrativa;
- cambio de estado;
- eliminación o retiro.

Las activaciones ordinarias no necesitan una fila permanente individual, salvo las de un objeto bajo investigación. Pueden mantenerse métricas agregadas para balance.

---

## 16. Métricas de balance

Durante las primeras dos semanas recopilar:

| Métrica | Objetivo de revisión |
| --- | --- |
| Daño adicional por reliquia | Detectar si supera 15 % del daño total del portador |
| Activaciones por hora | Detectar cooldown inútil o demasiado corto |
| Muertes PvP con reliquia | Comparar contra jugadores sin reliquia |
| Daño evitado por Anillo/Armadura | Evitar inmunidad práctica |
| Cañones que golpean 2–3 objetivos | Comprobar valor real del área |
| Rachas Superd completadas | Detectar granjas o requisito imposible |
| Reliquias `DORMANT` | Ajustar costos de reparación |
| Copias activas | Ver si los topes bloquean demasiado pronto el loot |

Una reliquia debe aportar identidad y una ventaja situacional. Si se vuelve obligatoria para competir, debe reducirse antes de aumentar el poder de las demás.

---

## 17. Pruebas obligatorias

- [ ] La copia conserva UUID y estadísticas al reiniciar.
- [ ] El nombre o lore falsificado no activa habilidades.
- [ ] Dos objetos con el mismo UUID quedan bloqueados.
- [ ] Los cooldowns sobreviven a muerte, desconexión y reinicio.
- [ ] Ninguna habilidad funciona sin la ranura correcta.
- [ ] El límite de una mayor y dos menores se respeta.
- [ ] Las bonificaciones no se disparan recursivamente.
- [ ] Mascotas, aliados y aldeanos quedan protegidos.
- [ ] WorldGuard puede bloquear activas por región.
- [ ] Aqua Heartia no atraviesa paredes.
- [ ] La Lanza Superd no se pierde con inventario lleno.
- [ ] La Armadura Mágica no funciona como tótem.
- [ ] Mending y encantamientos prohibidos son retirados de forma segura.
- [ ] La reliquia no se destruye al llegar a 1 de durabilidad.
- [ ] Cada función puede operarse desde Java y Bedrock.
- [ ] El resource pack ausente no afecta ninguna mecánica.
- [ ] Veinte jugadores activando reliquias no degradan el TPS.

---

## 18. Decisiones cerradas por esta versión

- Las siete reliquias de la tabla inicial tienen estadísticas propias.
- Solo se obtiene una por bóveda válida.
- Aqua Heartia mantiene `DIAMOND_SWORD` en la versión 1.0 por compatibilidad.
- Las reliquias no usan la mano secundaria como requisito.
- El equipamiento se controla mediante ranuras virtuales.
- Los cooldowns se guardan en SQLite.
- Las habilidades tienen valores separados para PvE y PvP.
- Los topes globales se comprueban antes de la tirada.
- Las texturas no determinan identidad ni funcionamiento.
- Las reliquias agotadas quedan `DORMANT` y no se destruyen.


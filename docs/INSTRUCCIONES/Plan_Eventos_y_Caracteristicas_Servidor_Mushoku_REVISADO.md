# Plan de eventos y características — Servidor Mushoku

> Documento de diseño jugable y hoja de ruta. El servidor será survival vanilla con plugins, compatible con Java y Bedrock mediante Geyser/Floodgate, con un mundo de 22.000 × 22.000 inspirado en *Mushoku Tensei*.

## 1. Objetivo general

Crear una temporada survival donde los jugadores:

- exploren un mundo amplio y reconocible;
- atraviesen arcos narrativos inspirados en el lore sin reemplazar a los protagonistas de la obra;
- funden pueblos y facciones;
- elijan entre dos grandes alianzas;
- descubran enfermedades, reliquias y laberintos;
- compitan por las Siete Potencias Mundiales;
- desbloqueen Nether y End mediante eventos comunitarios;
- participen en una historia administrada sin depender de mods del cliente.

La progresión debe durar varias semanas. Ningún jugador debería poder completar Nether, End y todos los rankings durante los primeros días.

## 2. Pilares de diseño

### Exploración

- Mundo principal de 22.000 × 22.000 bloques.
- Borde jugable recomendado de 21.800 bloques de diámetro.
- Continentes y biomas inspirados en la geografía de *Mushoku Tensei*.
- Superficie sin estructuras vanilla aleatorias dominantes.
- Estructuras importantes colocadas deliberadamente.
- Minas, Ancient Cities, strongholds y Trial Chambers controladas.

### Progresión comunitaria

- Nether y End comienzan sellados.
- Los desbloqueos son eventos del servidor, no simples fechas automáticas.
- Las facciones tienen motivos para comerciar, explorar y competir.
- Los laberintos requieren grupos y preparación.

### Compatibilidad

- Java y Bedrock participan en los mismos eventos.
- Todo menú esencial dispone de comando textual.
- No se requiere Forge, Fabric o NeoForge del lado del jugador.
- Las recompensas siempre conservan un ítem vanilla subyacente.
- Los modelos y texturas son visuales; una falla del pack no destruye objetos.

### Competencia justa

- Las donaciones solo financian infraestructura.
- No se venden Potencias, reliquias, ventajas de combate ni claims.
- Los eventos importantes se anuncian con antelación.
- Las reglas contra boosting, multicuentas y filtraciones se aplican por igual.

## 3. Características permanentes

### 3.1 Mundo principal

| Característica | Configuración |
| --- | --- |
| Tamaño exportado | 22.000 × 22.000 |
| Borde jugable | 21.800 de diámetro |
| Centro | X 0, Z 0 |
| Dificultad | Difícil |
| Jugadores | Máximo inicial recomendado: 24 |
| Superficie | Sin estructuras vanilla aleatorias |
| Subsuelo | Cuevas, minerales y estructuras seleccionadas |
| Altura | Rango confirmado durante la exportación final |

El spawn debe quedar en una región segura, accesible y relativamente central, pero sin convertirse en la única zona comercial del mapa.

### 3.2 Crossplay

- Paper como servidor.
- Geyser-Spigot para traducción Java–Bedrock.
- Floodgate para cuentas Bedrock.
- Resource pack Java y pack Bedrock separados.
- Formularios Bedrock opcionales; comandos siempre disponibles.
- Prueba obligatoria desde Java, Bedrock móvil y Bedrock con mando.

### 3.3 Facciones

- Un pueblo Towny funciona como facción.
- Mínimo inicial: 3 miembros.
- Máximo inicial: 8 miembros.
- Claims: 8 base + 5 por miembro.
- Máximo: 48 claims.
- Sin saqueo ni destrucción offline durante el lanzamiento.
- Las guerras se realizan únicamente mediante eventos anunciados.

### 3.4 Alianzas fijas

Solo existen dos grandes naciones administradas por el servidor:

#### Orsted Corp

- Menor probabilidad de contraer enfermedades.
- Misiones de investigación y caza.
- Ventajas defensivas moderadas durante eventos autorizados.
- Estética de disciplina, conocimiento y preparación.

#### Apóstoles del Dios Humano

- Acceso temporal a `/visionapostol`.
- Pistas adicionales durante ciertos eventos.
- Misiones de información, espionaje y descubrimiento.
- Estética de profecía, manipulación y secretos.

Reglas de alianza:

- elección bloqueada durante 14 días;
- al desertar, 7 días sin beneficios;
- ningún beneficio otorga inmunidad total ni daño permanente;
- cambiar de alianza exige confirmación y queda registrado.

### 3.5 Dificultad regional

| Zona | Vida adicional de mobs | Daño adicional |
| --- | ---: | ---: |
| Radio 0–2.000 | 10 % | 10 % |
| Radio 2.001–6.000 | 20 % | 15 % |
| Radio 6.001–10.900 | 35 % | 25 % |
| Nether | 40 % | 30 % |
| End | 50 % | 35 % |

Exclusiones:

- Warden sin escalado automático;
- Ender Dragon configurado como encuentro independiente;
- Trial Spawners con aumento de vida limitado;
- animales, aldeanos y mascotas sin escalado;
- jefes de eventos administrados por configuración propia.

### 3.6 Enfermedades

| Enfermedad | Fuente sugerida | Efectos |
| --- | --- | --- |
| Fiebre de Dryne | Husks y zonas áridas | Debilidad y fatiga minera |
| Miasma de Laberinto | Arañas de cueva y silverfish | Lentitud y debilidad |
| Fiebre de Laplace | Endermen o anomalías | Oscuridad y náusea |
| Mal de Desplazamiento | Teleportes/eventos | Náusea y lentitud breve |

Características:

- efectos vanilla compatibles con Bedrock;
- duración limitada;
- leche como cura básica;
- inmunidad temporal tras curación;
- reducción de contagio para Orsted Corp;
- eventos especiales pueden elevar temporalmente la probabilidad;
- ninguna enfermedad debe impedir jugar durante horas.

### 3.7 Reliquias

Las Trial Chambers y laberintos pueden entregar:

| Reliquia | Base vanilla | Rareza inicial |
| --- | --- | --- |
| Anillo de Supresión Mágica | Amethyst Shard | Poco común |
| Pendiente de Sara | Echo Shard | Poco común |
| Colgante Migurd | Heart of the Sea | Poco común |
| Espada de Paul | Iron Sword | Rara |
| Lanza Superd | Trident | Muy rara |
| Aqua Heartia | Diamond Sword | Ominosa |
| Armadura Mágica | Netherite Chestplate | Ominosa |

Reglas:

- identificación mediante PDC/UUID, no solo por nombre;
- base vanilla conservada;
- una reliquia no desaparece si falla el resource pack;
- registro de creación y propietario original;
- premios únicos o muy raros requieren anuncio y auditoría;
- prohibido duplicarlas mediante rollback intencional, bugs o multicuentas.

### 3.8 Laberintos

Se utilizarán dos escalas principales.

#### Laberinto menor

- Fuente visual sugerida: Trial Dungeons de Dungeons & Taverns.
- Una sala central.
- Entre 1 y 8 salas de combate.
- Trial Spawners y uno o dos vaults.
- Duración estimada: 20–40 minutos.
- Grupo recomendado: 2–4 jugadores.
- Reutilizable con cooldown y variantes de mobs.

#### Laberinto mayor

- Fuente visual sugerida: SH Buried Halls.
- Red extensa de pasillos y salas.
- Secciones cerradas hasta derrotar oleadas.
- Variantes vegetales, inundadas y en ruinas.
- Duración estimada: 60–120 minutos.
- Grupo recomendado: 4–8 jugadores.
- Puede contener una sala de jefe o una reliquia de temporada.

Ambos se extraerán y adaptarán como `.schem`; los datapacks fuente no quedarán generando libremente en producción.

### 3.9 Fortaleza aérea de Perugius

- Estructura monumental colocada mediante FAWE.
- Región protegida con WorldGuard.
- Salón de las Siete Potencias.
- NPC de recepción y personajes de lore.
- Biblioteca, sala de audiencia y sectores cerrados.
- Plataforma de llegada controlada.
- Centro de eventos de temporada, no vivienda de jugadores.

### 3.10 Nether y End

#### Nether

- Cerrado durante la primera etapa.
- Apertura mediante asalto comunitario.
- Fortaleza personalizada con enemigos preparados.
- Medidas antirush.
- Loot y progresión controlados.

#### End

- Mecánicas vanilla conservadas.
- Dragón como evento fundacional.
- Cielo blanco o niebla gris mediante packs Java/Bedrock.
- Apertura posterior al Nether para extender la temporada.

## 4. Siete Potencias Mundiales

| Puesto | Título | Condición inicial |
| ---: | --- | --- |
| 1 | Dios de la Técnica | Mayor contribución en la primera muerte válida de un Warden |
| 2 | Dios Dragón | Mayor contribución en la primera muerte válida del Ender Dragon |
| 3 | Dios de la Lucha | Mayor contribución en la primera muerte válida del Wither |
| 4 | Dios Demonio | Mayor puntuación en laberintos mayores |
| 5 | Dios de la Muerte | Mayor cantidad de bajas PvP válidas |
| 6 | Dios de la Espada | Primer puesto del ranking de duelos |
| 7 | Dios del Norte | Exploración, descubrimientos y logros |

### Reglas generales

- Los puestos se guardan por UUID.
- En jefes se mide contribución, no solo golpe final.
- Una misma víctima PvP solo puntúa una vez cada 12 horas para el mismo atacante.
- No cuentan muertes pactadas, cuentas alternativas ni granjas de puntos.
- Los puestos 4–7 pueden cambiar periódicamente.
- Los puestos fundacionales 1–3 pueden conservar un registro histórico aunque cambie el campeón activo.
- Toda reasignación administrativa lleva motivo y fecha.

## 5. Calendario recomendado de lanzamiento

El calendario puede desplazarse si una estructura o plugin no supera las pruebas. Es preferible posponer un evento antes que abrirlo incompleto.

### Pretemporada — 7 días antes

Objetivos:

- probar Java y Bedrock;
- confirmar spawn y world border;
- formar una whitelist de prueba;
- verificar claims, enfermedades y comandos;
- ensayar Nether, End y laberintos en una copia;
- publicar reglamento y horarios.

Actividades:

- visita guiada del spawn;
- encuesta de horarios;
- creación de grupos de prueba;
- prueba de carga con 10–14 jugadores;
- reinicio y restauración de backup.

No se conservan progresos de la pretemporada salvo decisión expresa.

### Día 1 — El comienzo del viaje

Evento de apertura:

1. Presentación del mundo y reglas.
2. Confirmación de Nether y End sellados.
3. Entrega de kit puramente básico.
4. Expedición desde el spawn hacia regiones cercanas.
5. Habilitación de creación de pueblos.
6. Aparición de los primeros NPC o tablones de misiones.

Recompensas:

- comida inicial;
- brújula o mapa básico;
- cosmético o título de fundador;
- ningún objeto de combate avanzado.

### Días 2–3 — Fundación de facciones

- Registro de pueblos Towny.
- Selección de alcalde y asistentes.
- Explicación de claims.
- Primera feria de intercambio.
- Presentación pública de Orsted Corp y Apóstoles.
- No exigir elección inmediata de alianza.

### Días 4–5 — Señales del laberinto

- Aparición de pistas en libros, mapas o NPC.
- Pequeños brotes de Miasma.
- Misiones de recolección de leche, miel, pociones y comida.
- Descubrimiento de la entrada del primer laberinto menor.

### Día 6 — Primer laberinto menor

Formato:

- equipos de 2–4;
- salidas escalonadas para evitar saturación;
- 20–40 minutos;
- Trial Spawners vanilla;
- uno o dos vaults;
- sin reliquias irrepetibles obligatorias.

Objetivo técnico:

- medir MSPT con varios equipos;
- comprobar funcionamiento de spawners y vaults;
- verificar loot en Java y Bedrock;
- observar si la dificultad es razonable.

### Día 7 — Consejo de las alianzas

- Presentación formal de beneficios.
- Preguntas públicas.
- Apertura de solicitudes de alianza.
- Confirmación del bloqueo de 14 días.
- Anuncio del evento del Nether.

### Día 8 — Ruptura del sello del Nether

Evento comunitario:

1. Reunión en el portal principal.
2. Defensa contra varias oleadas.
3. Activación narrativa del portal.
4. Ingreso de un grupo de avanzada.
5. Asalto a la fortaleza preparada.
6. Apertura pública al terminar el evento.

Controles:

- backup previo;
- administradores dentro del Nether;
- punto seguro de retorno;
- límite temporal de entidades;
- loot único registrado;
- prohibición de ingresar antes del anuncio final.

### Días 9–13 — Era de exploración

- Recolección de recursos del Nether.
- Encargos entre facciones.
- Misiones de escolta y caravanas.
- Brotes de Fiebre de Dryne en regiones áridas.
- Primeras pistas sobre el Wither.
- Segundo laberinto menor con variante de mobs.

### Día 14 — Primer Wither oficial

Objetivo:

- determinar el puesto 3, Dios de la Lucha.

Reglas:

- arena oficial protegida;
- Wither invocado por administración;
- daño contabilizado por MushokuCore/Skript;
- participantes registrados antes del inicio;
- no se permite invocar otro Wither para adelantar el logro;
- el ganador es quien aporte mayor daño válido.

Después del evento puede permitirse la invocación normal de Withers según las reglas generales.

### Días 15–16 — La Esfera Roja

- Aparición del presagio sobre una región próxima al spawn.
- Investigación de anomalías por ambas alianzas.
- Recolección de mapas, amatistas y testimonios.
- Aviso fuera del juego con al menos 72 horas de anticipación.
- Preparación de refugios en las zonas de llegada.

### Día 17 — Desastre de Maná

Evento mundial:

1. Reunión o aviso de emergencia.
2. Cuenta regresiva visible para Java y Bedrock.
3. Teletransporte de los jugadores conectados a puntos seguros pregenerados.
4. Bloqueo temporal de homes y teleports.
5. Activación de refugios, pistas y canales de rescate.
6. Inicio de la campaña de búsqueda de desplazados.

No se destruyen construcciones reales ni se pierden inventarios. La región devastada de Fittoa se prepara de antemano como escenario.

### Días 18–24 — Búsqueda de los desplazados

- Misiones de rescate y escolta.
- Campamentos de refugiados y suministros.
- Evento de reputación de Dead End.
- Estación de lluvias y rescate de la Bestia Sagrada en Doldia.
- Infiltración en la prisión de Shirone.
- Primeras pistas sobre el Laberinto de Teletransporte.

### Semana 5 — Milis y Ranoa

- Juicio de Milis sobre la magia de teletransporte.
- Exámenes de la Universidad de Ranoa.
- Desafío del Rey Demonio Badigadi.
- Apertura de investigaciones sobre círculos antiguos.
- Torneo de duelos para inaugurar el puesto 6.

### Semanas 5–6 — Laberinto de Teletransporte

- Exploraciones parciales de los primeros niveles.
- Formación de un equipo de incursión y otro de apoyo.
- Mapeo comunitario de portales.
- Raid final contra el jefe del séptimo nivel.
- Competencia inicial para el puesto 4, Dios Demonio.

### Semana 7 — Cacería del Warden

Objetivo:

- determinar el puesto 1, Dios de la Técnica.

Reglas:

- Ancient City seleccionada y revisada;
- prohibido preparar trampas automáticas antes del evento;
- contribución medida por daño válido;
- muerte por entorno sin contribución suficiente no entrega el puesto;
- salida de emergencia para jugadores Bedrock con problemas de conexión.

La dificultad no debe aumentarse artificialmente al Warden mediante LevelledMobs.

### Semana 7 — Preparación para el End

- Búsqueda de strongholds verificados.
- Eventos de Eye of Ender.
- Carrera de suministros entre facciones.
- Reparación o apertura narrativa del portal.
- Torneo de duelos para inaugurar el puesto 6.
- Anuncio de condiciones del Dragon.
- Fragmentos de información sobre Perugius y Laplace.

### Semana 8 — Apertura del End

Evento:

1. Reunión frente al portal.
2. Confirmación del pack visual.
3. Ingreso coordinado.
4. Combate contra el Ender Dragon.
5. Asignación del puesto 2, Dios Dragón.
6. Apertura posterior de exploración de islas exteriores.

No se permite ingresar anticipadamente mediante homes, teleports o exploits.

## 6. Eventos recurrentes

### Expedición al laberinto menor

- Frecuencia: una o dos veces por semana.
- Duración: 20–40 minutos.
- Cupo: 2–4 por equipo.
- Recompensa: materiales, llaves y posibilidad baja de reliquia.
- Puede tener variación de mobs y enfermedad.

### Expedición al laberinto mayor

- Frecuencia: cada dos o tres semanas.
- Duración: 60–120 minutos.
- Cupo: 4–8.
- Recompensa: puntuación para Dios Demonio, ominous vaults y premios de temporada.
- Reinicio parcial o variante nueva para impedir memorización total.

### Caravana continental

- Una facción transporta un objeto entre dos regiones.
- Otras facciones pueden escoltar o interceptar dentro de reglas específicas.
- No se destruyen bases.
- Se otorgan puntos por llegada, defensa y recuperación.

### Brote de enfermedad

- Una región recibe probabilidad elevada de contagio.
- Los jugadores recolectan ingredientes para una cura comunitaria.
- Orsted obtiene misiones de investigación.
- Apóstoles reciben pistas sobre focos de infección.
- Recompensa comunitaria si se controla dentro del plazo.

### Feria de pueblos

- Mercado temporal en zona neutral.
- Puestos por facción.
- Concurso de decoración o estandartes.
- Intercambio de recursos sin PvP.
- Votación de la comunidad sin premio de poder.

### Torneo de duelos

- Categorías 1v1 y 2v2.
- Equipamiento normalizado o categorías por equipamiento.
- Elo para el puesto 6.
- Registro de resultados.
- Arena compatible con Java y Bedrock.

### Cacería regional

- Objetivos PvE en una región lejana.
- Mobs con dificultad regional.
- Recompensas por objetivos, no solo por golpe final.
- Ideal para introducir zonas remotas del mapa.

### Guerra limitada de facciones

- Solo en horario anunciado.
- Objetivos por captura, no destrucción total.
- Sin saqueo offline.
- Regiones y bloques protegidos.
- CoreProtect activo.
- Duración máxima predefinida.

## 7. Fortaleza de Perugius como arco narrativo

### Etapa 1 — Avistamiento

- Aparición visual de la fortaleza.
- No hay acceso directo.
- Rumores y libros distribuidos por el mundo.

### Etapa 2 — Invitación

- Un NPC entrega una misión comunitaria.
- Las facciones reúnen objetos o completan laberintos.
- Se habilita una plataforma de llegada.

### Etapa 3 — Primera audiencia

- Visita guiada.
- Presentación del Salón de las Potencias.
- Exposición de propietarios actuales.
- Apertura de biblioteca y sectores públicos.

### Etapa 4 — Crisis de la fortaleza

- Enemigos o anomalías aparecen en sectores cerrados.
- Combate por equipos.
- Recompensas cosméticas, lore y acceso futuro.
- No destruir la construcción principal.

### Etapa 5 — Centro de temporada

- Audiencias de campeones.
- Torneos.
- Reasignaciones de Potencias.
- Exposiciones de reliquias.
- Eventos de cierre de temporada.

## 8. Horarios recomendados

Como el grupo incluye jugadores de Latinoamérica y España, el horario principal sugerido para eventos importantes es:

```text
Sábados o domingos — 17:00 de Argentina
```

Equivale aproximadamente a:

- 21:00 en España durante horario de invierno europeo;
- 22:00 en España durante horario de verano europeo.

Los eventos críticos deben anunciarse con al menos 72 horas de anticipación. Cuando sea posible, realizar una segunda ventana o repetir eventos menores para quienes no puedan asistir.

## 9. Plantilla para diseñar cada evento

Antes de anunciarlo, completar:

```text
Nombre:
Fecha y hora:
Duración estimada:
Ubicación:
Cupo:
Ediciones admitidas: Java / Bedrock
Objetivo principal:
Requisitos de entrada:
Inventario permitido:
Respawn:
PvP: sí / no / limitado
Premios:
Premios únicos:
Reglas especiales:
Administradores responsables:
Backup realizado:
Plan de cancelación:
Plan de recuperación:
```

## 10. Reglas de recompensas

- Publicar las recompensas posibles antes del evento, salvo secretos narrativos.
- Los cosméticos pueden ser exclusivos.
- Los objetos de poder deben tener alternativas obtenibles.
- No entregar netherite, elytra o reliquias fuertes durante los primeros días.
- Registrar todo objeto único.
- Si un evento falla técnicamente, restaurar o repetir; no improvisar premios desproporcionados.
- Los administradores participantes no compiten por premios únicos.
- Los premios no se venden por dinero real.

## 11. Prevención de exploits

- Whitelist durante pruebas.
- Control de UUID y nombres.
- Cooldown de bajas repetidas.
- Zonas de evento protegidas.
- Bloqueo de homes/RTP en dimensiones cerradas.
- Trial Spawners y vaults auditados tras pegarlos.
- Eliminación de command blocks importados.
- Loot tables revisadas.
- Reliquias con UUID propio.
- CoreProtect como fuente de verdad para bloques y contenedores.
- Backup antes de cada evento fundacional.
- Revisión por los dos administradores para sanciones o restauraciones importantes.

## 12. Características obligatorias para abrir

- [ ] Paper estable.
- [ ] Geyser y Floodgate probados.
- [ ] Mundo cargado y world border configurado.
- [ ] Spawn protegido.
- [ ] Whitelist y permisos.
- [ ] Nether y End bloqueados.
- [ ] Towny y LuckPerms funcionando.
- [ ] CoreProtect instalado.
- [ ] Un laberinto menor probado.
- [ ] Enfermedades básicas probadas.
- [ ] `/potencias` funcionando.
- [ ] Backups restaurables.
- [ ] Reglamento publicado.
- [ ] Prueba de carga con Java y Bedrock.

## 13. Características que pueden esperar

- Fortaleza de Perugius terminada.
- Laberinto mayor definitivo.
- NPC con skins avanzadas.
- Hologramas completos.
- Puestos 4, 6 y 7 totalmente automatizados.
- Guerra avanzada de facciones.
- Jefes personalizados complejos.
- Modelos visuales de todas las reliquias.
- Apertura del End.
- Migración completa de Skript a MushokuCore.

Estas funciones no deben retrasar el lanzamiento si el núcleo estable ya está listo.

## 14. Métricas de éxito

### Técnicas

- TPS cercano a 20.
- MSPT inferior a 50 ms.
- Sin errores repetitivos en consola.
- Sin duplicación de reliquias.
- Reconexión Bedrock sin pérdida de inventario.
- Backups restaurables.

### Jugables

- Participación de varias facciones.
- Eventos completables sin depender de una sola persona.
- Ninguna alianza domina por beneficios automáticos.
- Laberintos con duración dentro del rango previsto.
- Recompensas valiosas sin romper la economía.
- Jugadores de España y Latinoamérica pueden asistir a eventos principales.

### Comunitarias

- Reglas comprendidas.
- Disputas registradas y resueltas por evidencia.
- Calendario anunciado con anticipación.
- Donaciones separadas de las ventajas jugables.
- Encuestas después de los eventos fundacionales.

## 15. Plan operativo para los administradores

### Una semana antes de cada evento grande

- confirmar estructura y coordenadas;
- probar mobs y loot;
- revisar compatibilidad Bedrock;
- publicar fecha y requisitos;
- designar administrador principal y suplente.

### Un día antes

- crear backup;
- revisar regiones y permisos;
- comprobar teleports y respawn;
- limpiar entidades innecesarias;
- ensayar inicio y finalización.

### Una hora antes

- reinicio limpio;
- informe spark de referencia;
- verificar Geyser;
- confirmar participantes;
- cerrar temporalmente el área.

### Durante el evento

- un administrador dirige;
- el otro supervisa consola, rendimiento y exploits;
- registrar premios y decisiones;
- evitar cambios improvisados de plugins.

### Después

- `save-all flush`;
- backup posterior;
- revisar logs;
- publicar ganadores;
- actualizar Potencias;
- registrar errores y mejoras;
- decidir si la estructura se reinicia, permanece o se cierra.

## 16. Hoja de ruta

| Fase | Contenido | Prioridad |
| --- | --- | --- |
| 0 | Mundo, crossplay, permisos y backups | Crítica |
| 1 | Lanzamiento, pueblos y laberinto menor | Crítica |
| 2 | Alianzas, enfermedades y Nether | Alta |
| 3 | Wither, laberinto mayor y puesto 4 | Alta |
| 4 | Warden, duelos y exploración | Alta |
| 5 | Ender Dragon y apertura del End | Alta |
| 6 | Fortaleza de Perugius y NPC | Media |
| 7 | MushokuCore completo y eventos avanzados | Media |
| 8 | Cierre de temporada y nueva campaña | Futura |

## 17. Cierre de temporada

La temporada puede cerrar después de ocho a doce semanas con:

- torneo final;
- asalto al laberinto mayor definitivo;
- audiencia en la fortaleza de Perugius;
- reconocimiento de las Siete Potencias;
- museo de reliquias y facciones;
- estadísticas generales;
- anuncio de cambios para la siguiente temporada.

## 18. Auditoría narrativa del plan

El plan actual es sólido en progresión, rendimiento y administración, pero su primera versión depende demasiado de hitos propios de Minecraft —Nether, Wither, Warden y Ender Dragon—. Esos hitos deben conservarse porque funcionan bien como objetivos survival, aunque conviene envolverlos dentro de una historia que recuerde claramente a *Mushoku Tensei*.

### Ajustes recomendados

- Convertir el **Desastre de Maná** en el gran punto de quiebre de la temporada, después de que los jugadores hayan tenido tiempo de formar pueblos.
- Utilizar la **búsqueda de desplazados de Fittoa** como justificación para explorar todos los continentes.
- Reservar el **Laberinto de Teletransporte** como el mayor contenido PvE anterior al cierre de temporada.
- Darle a Ranoa actividades propias: exámenes, investigación, duelos y encargos de profesores.
- Hacer que Orsted Corp y los Apóstoles reciban objetivos diferentes dentro del mismo evento, sin revelar información capaz de decidir por sí sola el resultado.
- Presentar a Laplace y la Armadura del Dios de la Lucha mediante presagios y fragmentos; no introducirlos como jefes comunes.
- Inspirarse en sucesos del lore, pero permitir que las decisiones y ganadores pertenezcan a los jugadores del servidor.

## 19. Eventos inspirados en el lore

### 19.1 La Esfera Roja — presagio del Desastre de Maná

**Tipo:** evento ambiental y de investigación.  
**Duración:** 48–72 horas.  
**Momento recomendado:** final de la segunda semana.

Una esfera roja aparece sobre una región próxima al spawn y crece lentamente. Los NPC dejan de conocer ciertas rutas, aparecen partículas en lugares concretos y las brújulas entregadas por la administración apuntan a anomalías.

Objetivos:

- localizar entre cinco y siete puntos de inestabilidad;
- recopilar fragmentos de amatista o mapas alterados;
- entregar informes en Ranoa u Orsted Corp;
- decidir si se intenta contener, estudiar o explotar la anomalía.

La decisión modifica pequeños detalles del evento siguiente, pero no permite evitar el Desastre de Maná.

### 19.2 El Desastre de Maná — evento fundacional

**Tipo:** evento mundial único.  
**Duración:** 60–90 minutos, seguido por una campaña de varios días.  
**Momento recomendado:** entre los días 14 y 18.

El cielo cambia, suena una cuenta regresiva y los jugadores conectados son teleportados a destinos aleatorios dentro de un conjunto de regiones seguras pregeneradas. Los grupos pueden quedar separados para recrear la dispersión de Fittoa.

Implementación segura:

- preparar entre 20 y 30 puntos de llegada, nunca coordenadas completamente aleatorias;
- excluir océanos, lava, cuevas cerradas, territorios reclamados y zonas de jefes;
- conservar inventario y experiencia;
- aplicar Resistencia, Caída lenta y Regeneración durante unos segundos;
- guardar la ubicación anterior para recuperación administrativa;
- impedir homes y teleports durante la primera etapa;
- mantener un refugio o NPC de orientación a menos de 500 bloques de cada punto;
- ejecutar un backup inmediatamente antes del evento.

La destrucción de Fittoa debe ser escenográfica y preparada en una copia del mundo. No conviene borrar construcciones reales de jugadores.

### 19.3 Campaña de búsqueda de los desplazados

**Tipo:** campaña cooperativa mundial.  
**Duración:** cinco a siete días.

Después del Desastre, aparecen NPC, diarios y campamentos en distintos continentes. Cada rescate entrega información sobre otros desaparecidos y contribuye a una barra comunitaria.

Variantes:

- escoltar supervivientes hasta un campamento;
- llevar medicinas o alimentos;
- reconstruir un puente o refugio;
- encontrar placas con nombres de desaparecidos;
- liberar cautivos de bandidos;
- recuperar pertenencias sin convertirlas en objetos de poder.

Orsted Corp recibe coordenadas aproximadas y tareas logísticas. Los Apóstoles reciben visiones ambiguas y pueden descubrir atajos, pero algunas pistas deberán ser deliberadamente incompletas.

### 19.4 La reputación de Dead End

**Tipo:** escolta y reputación.  
**Duración:** 45–60 minutos.  
**Frecuencia:** repetible con variantes.

Un NPC Superd debe atravesar pueblos donde su presencia provoca miedo. Los jugadores ganan puntos evitando bajas civiles, completando encargos y derrotando monstruos que amenazan la ruta.

Resultados posibles:

- reputación positiva: comerciantes y rutas nuevas;
- reputación neutral: recompensa material básica;
- fracaso: precios temporalmente peores y nueva misión de reparación de imagen.

No se debe utilizar la apariencia del NPC como única señal importante, porque algunos jugadores Bedrock podrían no cargar la skin.

### 19.5 El Ojo Demoníaco de Kishirika

**Tipo:** búsqueda competitiva sin PvP obligatorio.  
**Duración:** 60 minutos.

Kishirika aparece disfrazada en una ciudad y pide comida u objetos absurdamente comunes. Quien complete la cadena obtiene acceso a una prueba de percepción con pasillos falsos, cofres señuelo, invisibilidad y pistas contradictorias.

Recompensas recomendadas:

- título cosmético **Portador del Ojo**;
- uso limitado de una habilidad de visión con cooldown;
- brújula especial para un evento posterior;
- variante visual de Echo Shard o Amethyst Shard.

El premio no debe otorgar visión permanente de jugadores ni atravesar claims.

### 19.6 La estación de lluvias de Doldia

**Tipo:** defensa y rescate regional.  
**Duración:** 60–90 minutos.

Una zona preparada del Gran Bosque se inunda y solo puede recorrerse mediante puentes altos, botes y copas de árboles. Durante la tormenta desaparece la Bestia Sagrada y criaturas hostiles atacan el poblado.

Fases:

1. evacuar aldeanos y animales;
2. reparar tres defensas del pueblo;
3. seguir rastros de la Bestia Sagrada;
4. asaltar el campamento de traficantes;
5. defender el árbol sagrado durante una oleada final.

Es un buen evento para premiar coordinación, construcción rápida y movilidad, no solamente daño.

### 19.7 La prisión de Shirone

**Tipo:** infiltración y rescate PvE.  
**Duración:** 60–75 minutos.

Los jugadores deben entrar en una fortaleza, liberar prisioneros y escapar antes de que se cierren las puertas. Las llaves se distribuyen entre capitanes, salas de redstone y sectores de parkour sencillo.

Se pueden ofrecer dos rutas:

- **asalto directo:** más combate y menos acertijos;
- **infiltración:** disfraces narrativos, pasadizos y NPC sobornables.

La misión termina con una defensa en el patio, inspirada en la rebelión interna de Shirone. Los prisioneros deben representarse con NPC o aldeanos invulnerables para evitar fallos de escolta.

### 19.8 El juicio de Milis

**Tipo:** investigación, debate y defensa de reliquia.  
**Duración:** 60–90 minutos.

Una reliquia o investigación sobre círculos de teletransporte es acusada de violar los tabúes de Milis. Los jugadores reúnen testimonios, exploran la cripta y deciden qué evidencia presentar ante un tribunal administrado.

Desenlaces:

- absolución y acceso regulado a un círculo;
- confiscación de la reliquia y misión de recuperación legal;
- descubrimiento de corrupción interna;
- ataque externo durante el juicio, obligando a rivales políticos a cooperar.

No debe convertirse en una votación puramente subjetiva: las pruebas encontradas tienen que determinar qué desenlaces están disponibles.

### 19.9 Exámenes de la Universidad de Ranoa

**Tipo:** minieventos académicos.  
**Frecuencia:** semanal o quincenal.

Pruebas posibles:

- **Agua:** apagar objetivos y transportar pociones sin romper recipientes.
- **Fuego:** combate cronometrado sin incendiar zonas protegidas.
- **Tierra:** construir una defensa con bloques limitados.
- **Viento:** carrera con Elytra temporal, levitación o parkour asistido.
- **Invocación:** identificar y derrotar la criatura correcta entre señuelos.
- **Alquimia:** fabricar una secuencia de pociones a partir de pistas.
- **Encantamiento:** optimizar equipo dentro de un presupuesto de experiencia.

La puntuación puede otorgar reputación académica, acceso a aulas restringidas y cosméticos. No debe conceder ventajas permanentes exclusivas.

### 19.10 El desafío del Rey Demonio Badigadi

**Tipo:** exhibición y jefe de arena.  
**Duración:** 30–45 minutos.

Un jefe inmortal llega a Ranoa, derrota a varios retadores NPC y desafía a los jugadores. La meta no es matarlo, sino superar umbrales de daño, sobrevivir a rondas y cumplir condiciones anunciadas.

Fases sugeridas:

1. prueba individual de golpe máximo;
2. supervivencia por parejas;
3. combate comunitario contra seis patrones de ataque;
4. celebración final sin muerte canónica del personaje.

El NPC o jefe puede regenerarse entre fases. La recompensa principal debe ser un título, estatua o acceso a una misión; no la Armadura del Dios de la Lucha.

### 19.11 El Laberinto de Teletransporte

**Tipo:** raid PvE principal.  
**Duración:** 90–150 minutos.  
**Momento recomendado:** semanas 5–7.

El laberinto tendrá siete niveles conectados por círculos de teletransporte. Los primeros pisos imitan túneles orgánicos y los últimos ruinas antiguas. Algunos portales retroceden, separan al equipo o conducen a salas de recompensa.

Reglas recomendadas:

- grupo de incursión de 4–8 jugadores;
- grupo de apoyo exterior encargado de suministros y rescates;
- puntos de control después de cada dos niveles;
- mapa incompleto que se amplía en cada intento;
- jefe final tipo hidra con varias cabezas o fases;
- rescate de un NPC o cristal humano como objetivo, no solo loot;
- reliquia de temporada registrada como recompensa comunitaria.

Para Bedrock, los círculos deben activarse mediante presión, interacción o comandos simples; no depender de clics precisos en hologramas.

### 19.12 La sucesión de Asura

**Tipo:** evento político con escolta, objetivos y traición controlada.  
**Duración:** dos sesiones de 60–90 minutos.

Una candidata debe llegar a la capital, conseguir apoyos y sobrevivir a intentos de asesinato. Las facciones pueden proteger una caravana, asegurar distritos o investigar a un posible traidor.

La traición no debe asignarse libremente a un jugador con capacidad de arruinar el evento. Conviene utilizar un NPC, un administrador narrativo o un objetivo secreto limitado y reversible.

Resultados posibles:

- coronación con todos los apoyos;
- victoria militar con menor legitimidad;
- tratado entre facciones;
- retirada y segundo intento en otra fecha.

### 19.13 La audiencia de Perugius

**Tipo:** prueba narrativa y de equipo.  
**Duración:** 60–90 minutos.

La fortaleza aérea abre sus puertas solamente a quienes superen tres pruebas:

1. conocimiento del mundo y sus monumentos;
2. combate contra invocaciones en salas cerradas;
3. decisión moral sobre una anomalía vinculada con Laplace.

El premio es acceso permanente a sectores públicos de la fortaleza, al Salón de las Potencias y a una red limitada de transporte. Los sectores privados permanecen cerrados para arcos posteriores.

### 19.14 Fracturas del sello de Laplace

**Tipo:** campaña mundial de fin de temporada.  
**Duración:** una semana.

Aparecen siete fracturas de maná en regiones distintas. Cada una altera mobs, clima, enfermedades o rutas. Las facciones deciden en qué orden cerrarlas, mientras los Apóstoles reciben visiones y Orsted Corp prepara los sellos.

Cada fractura requiere una especialidad diferente:

- combate;
- alquimia;
- exploración;
- construcción defensiva;
- laberinto;
- escolta;
- sacrificio de recursos comunitarios.

El evento permite reutilizar casi todas las mecánicas desarrolladas durante la temporada y prepara el cierre sin necesitar representar todavía la resurrección completa de Laplace.

### 19.15 La Armadura del Dios de la Lucha

**Tipo:** raid final opcional.  
**Duración:** 90–120 minutos.

Un portador controlado por la armadura actúa como jefe de varias fases. El objetivo consiste en romper anclajes, sobrevivir y sellar la armadura, no obtenerla como equipamiento común.

Reglas esenciales:

- la armadura completa nunca queda en el inventario de un jugador;
- sus fragmentos son objetos de misión o cosméticos;
- el jefe cambia de patrón según los anclajes destruidos;
- debe existir un checkpoint para reconexiones Bedrock;
- si el servidor baja de rendimiento, las oleadas auxiliares se reducen automáticamente;
- la victoria actualiza el Salón de las Potencias y cierra el arco de temporada.

## 20. Orden narrativo recomendado

| Etapa | Semana | Arco principal | Eventos secundarios |
| --- | ---: | --- | --- |
| Fundación | 1 | Pueblos, alianzas y primer laberinto | Feria, cacerías y primeras enfermedades |
| Presagio | 2 | Esfera Roja, Nether y primer Wither | Exámenes de Ranoa y Ojo Demoníaco |
| Dispersión | 3 | Desastre de Maná y búsqueda de desplazados | Dead End y caravanas de suministros |
| Viaje | 4 | Doldia, Shirone y Milis | Lluvias, rescates y juicio religioso |
| Profundidades | 5–6 | Laberinto de Teletransporte | Brotes, mapas y grupos de apoyo |
| Poder político | 7 | Sucesión de Asura | Torneos, espionaje y audiencia de Perugius |
| Ruptura | 8 | Fracturas del sello de Laplace | Reasignación de Potencias |
| Final | 9–10 | Armadura del Dios de la Lucha o gran raid | Museo, ceremonia y estadísticas |

### Prioridad de implementación

**Temporada 1 — recomendados:**

- Esfera Roja;
- Desastre de Maná;
- búsqueda de desplazados;
- estación de lluvias de Doldia;
- exámenes de Ranoa;
- desafío de Badigadi;
- Laberinto de Teletransporte;
- audiencia de Perugius.

**Cuando MushokuCore esté más maduro:**

- reputación dinámica de Dead End;
- juicio ramificado de Milis;
- sucesión de Asura con objetivos secretos;
- fracturas regionales de Laplace;
- Armadura del Dios de la Lucha con múltiples fases.

## 21. Fuentes de inspiración del lore

- [Teleport Incident — Mushoku Tensei Wiki](https://mushokutensei.fandom.com/wiki/Teleport_Incident): dispersión de Fittoa y campaña de búsqueda.
- [Mushoku Tensei Light Novel, volumen 12 — Seven Seas](https://sevenseasentertainment.com/books/mushoku-tensei-jobless-reincarnation-light-novel-vol-12/): descenso y rescate en el Laberinto de Teletransporte.
- [Mushoku Tensei Light Novel, volumen 17 — Seven Seas](https://sevenseasentertainment.com/books/mushoku-tensei-jobless-reincarnation-light-novel-vol-17/): conflicto sucesorio de Asura y amenaza de traición.
- [Mushoku Tensei Light Novel, volumen 18 — Seven Seas](https://sevenseasentertainment.com/books/mushoku-tensei-jobless-reincarnation-light-novel-vol-18/): desaparición de la Bestia Sagrada de Doldia.
- [Mushoku Tensei Light Novel, volumen 20 — Seven Seas](https://sevenseasentertainment.com/books/mushoku-tensei-jobless-reincarnation-light-novel-vol-20/): intriga política y religiosa en Millis.
- [Mushoku Tensei Light Novel, volumen 23 — Seven Seas](https://sevenseasentertainment.com/books/mushoku-tensei-jobless-reincarnation-light-novel-vol-23/): prueba divina y convocatoria a la fortaleza de Perugius.
- [Seven Great Powers — Mushoku Tensei Wiki](https://mushokutensei.fandom.com/wiki/Seven_Great_Powers): origen de los rangos, monumentos y sucesión de títulos.

Las fuentes sirven como inspiración temática. La campaña del servidor debe evitar copiar diálogos completos o redistribuir contenido de novelas y mapas protegidos.

## 22. Decisión recomendada

No es necesario reiniciar el mundo entre arcos. Puede mantenerse como mundo persistente y abrir regiones, estructuras y capítulos nuevos de forma progresiva.

Para la primera temporada, el recorrido recomendado es:

1. apertura, pueblos y presentación de alianzas;
2. primer laberinto y ruptura del sello del Nether;
3. Esfera Roja y primer Wither;
4. Desastre de Maná;
5. búsqueda de desplazados, Dead End y Doldia;
6. prisión de Shirone y juicio de Milis;
7. exámenes de Ranoa y desafío de Badigadi;
8. Laberinto de Teletransporte;
9. Warden, torneo de duelos y apertura del End;
10. audiencia de Perugius y fracturas iniciales de Laplace.

La Armadura del Dios de la Lucha debe quedar como cierre opcional solamente si el jefe, los checkpoints y la reducción automática de carga superan las pruebas de Java y Bedrock. De lo contrario, las fracturas de Laplace funcionan como final de temporada sin arriesgar la estabilidad.

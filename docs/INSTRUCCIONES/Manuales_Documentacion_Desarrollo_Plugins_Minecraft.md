# Manuales y documentación para desarrollar plugins de Minecraft

> Índice técnico para crear `MushokuCore` y otros plugins del servidor inspirado en *Mushoku Tensei*. El objetivo no es aprender a configurar plugins de terceros, sino desarrollar plugins propios para Paper que funcionen con jugadores Java y Bedrock mediante Geyser/Floodgate.

**Actualizado:** 19 de octubre de 2026  
**Servidor objetivo:** Paper Java 26.2 inicialmente; validar antes de migrar  
**Lenguaje recomendado:** Java 25 o superior, según lo exigido por la build exacta de Paper  
**Proyecto complementario:** `Instructivo_Diseno_MushokuCore.md`

---

## 1. Qué documentación usar primero

La documentación debe consultarse en este orden:

1. documentación de desarrollo de Paper;
2. JavaDocs de la versión exacta de Paper contra la cual se compila;
3. documentación oficial de la API del plugin integrado;
4. código fuente oficial, si la documentación no cubre un caso;
5. issues o discusiones del repositorio para errores conocidos;
6. tutoriales comunitarios únicamente como apoyo.

No copiar ejemplos antiguos de Spigot sin revisar si usan APIs obsoletas, NMS, reflexión, códigos de color heredados o tareas pesadas en el hilo principal.

### Portal principal

- [Desarrollo con Paper](https://docs.papermc.io/paper/dev/)
- [Primeros pasos](https://docs.papermc.io/paper/dev/getting-started/)
- [Preparación del proyecto](https://docs.papermc.io/paper/dev/project-setup/)
- [Cómo funciona un plugin](https://docs.papermc.io/paper/dev/how-do-plugins-work/)
- [Referencia de la API de Paper](https://docs.papermc.io/paper/dev/api/)
- [JavaDocs de Paper](https://jd.papermc.io/paper/)
- [Depuración desde el IDE](https://docs.papermc.io/paper/dev/debugging/)

### Advertencia de versión

La documentación vigente de Paper muestra ejemplos para 26.3, pero el servidor del proyecto fue preparado para 26.2. En el plugin deben coincidir deliberadamente:

- la versión de `paper-api` del archivo Gradle;
- el JDK requerido por esa build;
- `api-version` de `plugin.yml`;
- la versión del servidor de pruebas;
- la versión del servidor de producción.

Un servidor más antiguo rechaza un plugin cuyo `api-version` sea posterior. No actualizar solo uno de esos cinco elementos.

---

## 2. Ruta de aprendizaje recomendada

| Etapa | Tema | Resultado práctico |
| --- | --- | --- |
| 1 | Java: clases, interfaces, colecciones, excepciones y `CompletableFuture` | Código entendible y asincronía segura |
| 2 | Gradle y dependencias `compileOnly` | JAR reproducible sin duplicar Paper ni otras API |
| 3 | Ciclo de vida de Paper | Inicio y cierre limpios |
| 4 | Eventos y comandos | Primera mecánica jugable |
| 5 | Configuración y mensajes | Sistema administrable sin recompilar |
| 6 | PDC y SQLite | Datos persistentes y objetos identificables |
| 7 | APIs de Towny, LuckPerms y PlaceholderAPI | Integración con el servidor existente |
| 8 | Geyser/Floodgate | Experiencia equivalente para Bedrock |
| 9 | Pruebas y perfilado | Release segura para producción |

Para `MushokuCore`, conviene implementar primero un comando de lectura como `/potencias`, luego persistencia, y recién después recompensas, enfermedades y eventos globales.

---

## 3. Estructura mínima de un plugin

```text
mushoku-core/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── gradle/wrapper/
└── src/
    ├── main/
    │   ├── java/ar/mushoku/core/
    │   │   ├── MushokuCorePlugin.java
    │   │   ├── command/
    │   │   ├── listener/
    │   │   ├── service/
    │   │   ├── repository/
    │   │   └── integration/
    │   └── resources/
    │       ├── plugin.yml
    │       ├── config.yml
    │       └── messages.yml
    └── test/java/
```

Separar la lógica de Paper de la lógica de negocio permite probar rankings, enfermedades y recompensas sin levantar un servidor completo.

### Responsabilidad de cada capa

| Capa | Responsabilidad |
| --- | --- |
| `command` | Validar argumentos y permisos; llamar servicios |
| `listener` | Traducir eventos de Paper a acciones del sistema |
| `service` | Reglas de facciones, potencias, enfermedades y eventos |
| `repository` | SQLite, migraciones y consultas |
| `integration` | Adaptadores de Towny, LuckPerms, Geyser y PlaceholderAPI |
| clase principal | Crear servicios, registrar listeners y cerrar recursos |

La clase principal no debe convertirse en un archivo con toda la lógica.

---

## 4. Configuración del proyecto

Paper recomienda Gradle, aunque Maven también es válido. La plantilla exacta debe copiarse de la página de configuración del proyecto y fijarse a la versión probada.

### Reglas para Gradle

- incluir siempre Gradle Wrapper;
- usar `compileOnly` para Paper y APIs presentes en el servidor;
- usar `implementation` solo para bibliotecas que deban viajar dentro del JAR;
- sombrear y reubicar bibliotecas incluidas cuando puedan colisionar;
- no usar versiones `latest`, `+` ni rangos dinámicos;
- no compilar usando el JAR completo del servidor;
- ejecutar pruebas con cada build.

Ejemplo conceptual, sin números inventados:

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.opencollab.dev/main/")
    maven("https://repo.helpch.at/releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:PAPER_VERSION-R0.1-SNAPSHOT")
    compileOnly("org.geysermc.geyser:api:GEYSER_API_VERSION")
    compileOnly("org.geysermc.floodgate:api:FLOODGATE_API_VERSION")
    compileOnly("net.luckperms:api:5.5")
    compileOnly("me.clip:placeholderapi:PLACEHOLDERAPI_VERSION")
}
```

Tomar las versiones actuales de los repositorios oficiales en el momento de crear la rama de actualización. Las versiones de Geyser publicadas como `SNAPSHOT` cambian y deben quedar bloqueadas en el archivo de versiones del proyecto.

---

## 5. `plugin.yml` y ciclo de vida

- [Manual de `plugin.yml`](https://docs.papermc.io/paper/dev/plugin-yml/)
- [Clase `JavaPlugin`](https://jd.papermc.io/paper/org/bukkit/plugin/java/JavaPlugin.html)

`plugin.yml` declara como mínimo nombre, versión, clase principal y versión de API. También puede declarar comandos, permisos y dependencias.

```yaml
name: MushokuCore
version: '${version}'
main: ar.mushoku.core.MushokuCorePlugin
# Para el servidor actualmente planificado; cambiar solo junto con Paper.
api-version: '26.2'
authors: [AdministracionMushoku]

depend:
  - LuckPerms

softdepend:
  - Towny
  - PlaceholderAPI
  - Geyser-Spigot
  - floodgate

commands:
  potencias:
    description: Muestra las Siete Potencias
    permission: mushoku.potencias.ver

permissions:
  mushoku.potencias.ver:
    default: true
  mushoku.admin:
    default: op
```

No ejecutar lógica en el constructor de la clase principal.

| Método | Uso recomendado |
| --- | --- |
| `onLoad()` | Preparación que deba ocurrir antes de habilitar plugins; registro temprano de flags si una API lo exige |
| `onEnable()` | Cargar configuración, abrir almacenamiento, validar dependencias y registrar comandos/listeners |
| `onDisable()` | Cancelar tareas, guardar colas pendientes y cerrar conexiones |

Los *Paper plugins* con `paper-plugin.yml` siguen marcados como experimentales. Para `MushokuCore` conviene comenzar con el formato clásico `plugin.yml` salvo que una API exclusiva justifique la migración.

---

## 6. Manuales fundamentales de Paper

### Eventos

- [Event API](https://docs.papermc.io/paper/dev/event-listeners/)

Usar eventos en lugar de revisar constantemente el estado de todos los jugadores. Registrar listeners al habilitar el plugin y mantener cada handler corto.

Buenas prácticas:

- comprobar pronto si el evento realmente interesa;
- respetar eventos cancelados cuando corresponda;
- elegir deliberadamente la prioridad;
- no hacer consultas SQLite lentas ni peticiones de red dentro del evento;
- no usar `PlayerMoveEvent` para lógica pesada en cada movimiento.

### Comandos

- [Introducción a Command API](https://docs.papermc.io/paper/dev/command-api/basics/introduction/)
- [Comandos básicos en `plugin.yml`](https://docs.papermc.io/paper/dev/plugin-yml/#commands)

Cada acción administrativa debe tener permiso propio, validación de argumentos, respuesta clara y registro de auditoría. Los comandos que entregan reliquias o cambian rankings deben ser idempotentes cuando sea posible.

### Configuración

- [Configuración de plugins](https://docs.papermc.io/paper/dev/plugin-configurations/)

Separar:

- `config.yml`: reglas y valores numéricos;
- `messages.yml`: texto visible y MiniMessage;
- archivos de contenido: enfermedades, reliquias y eventos;
- SQLite: progreso y estado de jugadores.

No guardar bases de datos, listas grandes de UUID ni historiales en YAML.

### Scheduler y asincronía

- [Scheduler de Paper](https://docs.papermc.io/paper/dev/scheduler/)
- [Soporte conjunto Paper/Folia](https://docs.papermc.io/paper/dev/folia-support/)

El servidor intenta procesar 20 ticks por segundo: un tick dispone de unos 50 ms. Nunca usar `Thread.sleep()` en el hilo principal.

| Trabajo | Dónde hacerlo |
| --- | --- |
| Cambiar mundo, entidad o inventario | Hilo del servidor / scheduler correcto |
| Consulta SQL, archivo grande o HTTP | Tarea asíncrona |
| Aplicar el resultado de una consulta a un jugador | Volver al scheduler del servidor |

No acceder a objetos Bukkit desde un hilo arbitrario salvo que la documentación diga expresamente que es seguro.

### Mensajes y componentes

- [Adventure](https://docs.advntr.dev/)
- [Formato MiniMessage](https://docs.advntr.dev/minimessage/format)

Usar componentes Adventure y MiniMessage. Evitar concatenar JSON manual, códigos `§` y mensajes donde el color sea la única forma de transmitir información.

### Persistent Data Container

- [PDC en Paper](https://docs.papermc.io/paper/dev/pdc/)

PDC permite identificar de forma estable ítems, entidades, mundos y otras estructuras compatibles sin depender del nombre o del lore.

Usos para el servidor:

- `mushoku:relic_id` para reliquias;
- `mushoku:disease_id` para marcadores técnicos;
- `mushoku:boss_id` para jefes especiales;
- `mushoku:reward_instance` para impedir duplicaciones.

No usar lore como fuente de verdad: el jugador puede verlo, traducirlo o modificarlo mediante otros sistemas.

---

## 7. Persistencia y seguridad de datos

Para 25–30 jugadores, SQLite es suficiente si las escrituras están agrupadas y no bloquean el hilo del servidor. Usar migraciones con número de esquema.

Tablas iniciales sugeridas:

| Tabla | Contenido |
| --- | --- |
| `players` | UUID, último nombre y fechas |
| `power_rankings` | puesto, titular, causa y fecha |
| `boss_contributions` | daño/contribución por encuentro |
| `faction_membership` | alianza fija, ingreso y bloqueo |
| `diseases` | enfermedad, etapa y vencimientos |
| `relic_instances` | instancia, dueño, origen y estado |
| `audit_log` | actor, acción, objetivo y detalle |
| `schema_version` | migración aplicada |

Requisitos:

- claves únicas para premios irrepetibles;
- transacciones al entregar un objeto y registrar su propiedad;
- consultas preparadas;
- copia antes de cada migración;
- apagado limpio;
- nunca guardar contraseñas o claves en Git.

---

## 8. Compatibilidad Java y Bedrock

Geyser traduce el protocolo, pero no vuelve automáticamente compatible una interfaz Java personalizada. Toda mecánica esencial debe tener una vía vanilla o un comando.

### Documentación de desarrollo

- [Geyser API](https://geysermc.org/wiki/geyser/api/)
- [Incluir Geyser/Floodgate en el proyecto](https://geysermc.org/wiki/geyser/getting-started-with-the-api/)
- [Eventos de Geyser](https://geysermc.org/wiki/geyser/events/)
- [Floodgate API](https://geysermc.org/wiki/floodgate/api/)
- [Formularios Cumulus](https://geysermc.org/wiki/geyser/forms/)

### Qué API usar

| Necesidad | API |
| --- | --- |
| Saber si el jugador entra desde Bedrock | `GeyserApi#isBedrockPlayer(UUID)` o Floodgate |
| Obtener la conexión Bedrock | `GeyserApi#connectionByUuid(UUID)` |
| Mostrar formulario nativo Bedrock | Cumulus mediante Geyser/Floodgate |
| Consultar datos Floodgate o vinculación | `FloodgateApi` |
| Cargar pack Bedrock por sesión | eventos de resource packs de Geyser |

Si el plugin usa Geyser al iniciar, declarar `depend: [Geyser-Spigot]`. Si puede funcionar sin él, declarar `softdepend` y comprobar que la instancia exista antes de utilizarla.

### Contrato de compatibilidad

Cada función debe cumplir:

- comando equivalente aunque exista GUI;
- ítems basados en materiales vanilla;
- inventarios simples y sin gestos exclusivos de Java;
- títulos, sonidos, partículas y efectos disponibles en ambas ediciones;
- texto que no dependa únicamente de hover o click;
- pruebas reales con un cliente Bedrock, no solo una suposición basada en Java.

Los formularios Bedrock son una mejora visual, no la única puerta de acceso.

---

## 9. APIs de los plugins instalados

### LuckPerms

- [Developer API](https://luckperms.net/wiki/Developer-API)
- [JavaDocs](https://javadoc.io/doc/net.luckperms/api/latest/index.html)
- [Artefacto en Maven Central](https://central.sonatype.com/artifact/net.luckperms/api)

Usarlo para permisos, grupos, prefijos y contexto. La API actual es 5.5 y se obtiene normalmente mediante `ServicesManager`. Las operaciones de carga/modificación pueden ser asíncronas: no bloquear el hilo principal esperando un `CompletableFuture`.

No editar directamente los archivos o tablas de LuckPerms.

### Towny

- [Repositorio oficial](https://github.com/TownyAdvanced/Towny)
- [Towny API](https://github.com/TownyAdvanced/Towny/wiki/TownyAPI)
- [JavaDocs](https://townyadvanced.github.io/Towny/javadoc/release/)

Usarlo como fuente de verdad para pueblos, residentes, naciones, claims y permisos territoriales. `MushokuCore` debe guardar solo la relación temática adicional, por ejemplo si una nación pertenece a Orsted Corp o a los Apóstoles.

Escuchar eventos públicos de Towny; no inferir cambios leyendo archivos internos.

### PlaceholderAPI

- [Usar PlaceholderAPI desde un plugin](https://wiki.placeholderapi.com/developers/using-placeholderapi/)
- [Crear una expansión](https://wiki.placeholderapi.com/developers/creating-a-placeholderexpansion/)

Crear una expansión interna con identificador `mushoku` para exponer, por ejemplo:

```text
%mushoku_power_rank%
%mushoku_power_title%
%mushoku_alliance%
%mushoku_disease%
%mushoku_kills%
```

La expansión debe registrarse en `onEnable()` solo si PlaceholderAPI está habilitado. Si la expansión es interna, implementar su persistencia según exige la documentación para que sobreviva a recargas de PlaceholderAPI.

### WorldGuard y WorldEdit/FAWE

- [API de regiones de WorldGuard](https://worldguard.enginehub.org/en/latest/developer/regions/)
- [Regiones protegidas](https://worldguard.enginehub.org/en/latest/developer/regions/protected-region/)
- [API de WorldEdit](https://worldedit.enginehub.org/en/latest/api/)

Usos posibles:

- proteger la fortaleza de Perugius;
- consultar si un evento ocurre dentro de una región;
- registrar flags personalizadas;
- pegar o gestionar estructuras mediante una operación controlada.

El registro de flags personalizadas debe hacerse en la fase temprana que indica WorldGuard. Las operaciones grandes de edición deben probarse en una copia y no ejecutarse durante horas pico.

### Otras integraciones

| Plugin | Estrategia de integración |
| --- | --- |
| TAB | Consumir placeholders de `MushokuCore`; no editar TAB desde el plugin |
| DecentHolograms | Consumir placeholders o comandos documentados |
| ajLeaderboards | Mostrar estadísticas; `MushokuCore` conserva la autoridad sobre los puestos |
| LevelledMobs | Usar su API/eventos públicos si están documentados; evitar tocar sus archivos en ejecución |
| CoreProtect | Integración opcional para consultas administrativas, nunca como base de datos propia |
| Citizens | API solo para NPC únicos; ofrecer diálogo alternativo por comando para Bedrock |
| SkinsRestorer | Cosmético; no convertirlo en dependencia de mecánicas |

---

## 10. Diseño de `MushokuCore`

### Módulos sugeridos

```text
MushokuCore
├── Core: configuración, mensajes, base y auditoría
├── Powers: Siete Potencias y jefes fundadores
├── Alliances: Orsted Corp y Apóstoles
├── Diseases: contagio, etapas y curas
├── Relics: objetos de Trial Chambers
├── Events: Desastre de Maná y aperturas dimensionales
├── Integrations: Towny, LuckPerms, PAPI, Geyser
└── Admin: diagnóstico, migración y recuperación
```

### API interna

Los módulos no deben llamarse entre sí mediante comandos de consola. Deben exponer interfaces Java, por ejemplo:

```java
public interface PowerService {
    Optional<PowerRank> findRank(UUID playerId);
    AssignmentResult assignFounderRank(int position, UUID playerId, BossProof proof);
}
```

Esto facilita pruebas, evita analizar texto y permite reemplazar SQLite en el futuro.

### Dependencias obligatorias y opcionales

| Integración | Tipo propuesto | Comportamiento si falta |
| --- | --- | --- |
| LuckPerms | obligatoria inicialmente | Desactivar plugin con error claro |
| Towny | opcional por módulo | Desactivar alianzas territoriales |
| PlaceholderAPI | opcional | Desactivar placeholders |
| Geyser/Floodgate | opcional | Mantener Java; desactivar formularios Bedrock |
| WorldGuard | opcional | Usar coordenadas/configuración alternativa |

---

## 11. Pruebas

### Pruebas unitarias

- [MockBukkit](https://docs.mockbukkit.org/)
- [Primeros pasos con MockBukkit](https://docs.mockbukkit.org/docs/en/user_guide/introduction/getting_started)
- [JUnit 5](https://junit.org/junit5/docs/current/user-guide/)

MockBukkit ayuda con lógica basada en Bukkit, pero no reemplaza una prueba real de Paper, Geyser o plugins externos.

Probar como mínimo:

- asignación irrepetible de los puestos 1, 2 y 3;
- empate de contribuciones a un jefe;
- reinicio durante una recompensa;
- UUID Bedrock y Java;
- jugador desconectado;
- Towny o PlaceholderAPI ausente;
- configuración incompleta;
- migración desde una versión anterior;
- reliquia duplicada o alterada;
- enfriamientos y horarios alrededor de un reinicio.

### Servidor de integración

Mantener un servidor local separado con copias de las mismas versiones de producción. Paper recomienda el plugin Gradle `run-task` para descargar y ejecutar un servidor de pruebas desde el proyecto.

Matriz mínima antes de publicar:

| Cliente | Pruebas |
| --- | --- |
| Java | comandos, inventarios, muertes, reconexión |
| Bedrock Windows/móvil | formularios, comandos, inventarios, resource pack |
| Dos jugadores | PvP, Towny, rankings y concurrencia |
| Reinicio | persistencia, tareas y premios pendientes |

---

## 12. Rendimiento y perfilado

- [spark](https://spark.lucko.me/docs/)
- [Guía de profiler](https://spark.lucko.me/docs/guides/Using-the-profiler)

Reglas:

- no recorrer todos los chunks o entidades cada tick;
- indexar consultas frecuentes;
- agrupar escrituras;
- almacenar en caché solo datos con una política clara de invalidación;
- usar eventos en vez de sondeos;
- medir con spark antes de optimizar;
- establecer presupuestos: ningún listener frecuente debería dominar el tick.

Una prueba con 2 jugadores no descubre todos los problemas. Simular eventos, muchos mobs y varias operaciones simultáneas en el servidor de staging.

---

## 13. Seguridad

- cada comando administrativo requiere un permiso específico;
- no confiar en texto enviado por el cliente;
- limitar longitudes y valores numéricos;
- preparar consultas SQL;
- nunca construir comandos con texto de jugadores sin validarlo;
- usar UUID, no nombre, como identidad;
- registrar entrega, retiro y recuperación de reliquias;
- ocultar rutas internas y trazas al jugador común;
- no descargar código o dependencias durante el juego fuera del mecanismo controlado de build;
- revisar licencias antes de copiar código.

Los comandos destructivos deben admitir previsualización o confirmación y producir una entrada de auditoría.

---

## 14. Flujo de publicación

1. Crear una rama y fijar versiones.
2. Ejecutar pruebas unitarias.
3. Compilar con Gradle Wrapper.
4. Revisar el contenido del JAR y sus dependencias.
5. Iniciar servidor local limpio.
6. Probar actualización desde la base anterior.
7. Probar con Java y Bedrock.
8. Perfilar el evento más pesado con spark.
9. Hacer copia de seguridad del servidor.
10. Instalar en staging y revisar logs.
11. Publicar en producción durante una ventana de mantenimiento.
12. Conservar el JAR y esquema anteriores para rollback.

No usar `/reload` para actualizar el plugin. Reiniciar el servidor limpiamente.

### Versionado

Aplicar SemVer al plugin:

- `MAJOR`: migración o cambio incompatible;
- `MINOR`: nueva función compatible;
- `PATCH`: corrección compatible.

Cada release debe incluir JAR, checksum, changelog, versión de Paper probada, versiones de dependencias y pasos de migración.

---

## 15. Plan de estudio aplicado

### Semana 1: esqueleto

- proyecto Gradle;
- `plugin.yml`;
- `/mushoku version`;
- configuración y mensajes;
- primer listener;
- pruebas básicas.

### Semana 2: datos y rankings

- SQLite y migraciones;
- repositorios;
- `/potencias`;
- contribución a jefes;
- auditoría e idempotencia.

### Semana 3: integraciones

- LuckPerms;
- Towny;
- PlaceholderAPI;
- TAB y hologramas mediante placeholders.

### Semana 4: crossplay y release

- detección Bedrock;
- formularios opcionales;
- pruebas Java/Bedrock;
- perfilado;
- staging y primera release.

---

## 16. Lista de control para una función nueva

Antes de considerar terminada cualquier función:

- [ ] Tiene requisito escrito y casos límite.
- [ ] Funciona tras reiniciar.
- [ ] Usa UUID.
- [ ] No bloquea el hilo principal.
- [ ] Tiene permisos separados para uso y administración.
- [ ] Registra acciones sensibles.
- [ ] Tiene comando alternativo a la GUI.
- [ ] Fue probada desde Java.
- [ ] Fue probada desde Bedrock.
- [ ] Tolera la ausencia de dependencias opcionales.
- [ ] No duplica premios al repetirse un evento.
- [ ] Tiene prueba automática o procedimiento reproducible.
- [ ] Está documentada en el changelog.

---

## 17. Relación con los otros manuales

| Archivo | Uso |
| --- | --- |
| `Manual_Documentacion_Plugins_Mushoku.md` | Operar y configurar plugins instalados |
| `Instructivo_Diseno_MushokuCore.md` | Especificación detallada del plugin propio |
| `Manuales_Documentacion_Desarrollo_Plugins_Minecraft.md` | Aprender las API y aplicar un flujo de desarrollo correcto |
| `MT_Server_Base_26.2/COMPATIBILIDAD_BEDROCK.md` | Restricciones concretas de crossplay |
| `MT_Server_Base_26.2/SIETE_POTENCIAS.md` | Reglas funcionales del ranking |
| `MT_Server_Base_26.2/RELIQUIAS_TRIAL_CHAMBERS.md` | Diseño funcional de objetos especiales |

Estos documentos separan tres problemas distintos: administrar el servidor, definir las reglas de negocio y escribir el código.


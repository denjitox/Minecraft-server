# Instructivo de diseño y desarrollo de MushokuCore

> Especificación técnica del plugin personalizado para el servidor survival inspirado en *Mushoku Tensei*. El plugin se ejecutará en Paper, conservará compatibilidad con jugadores Java y Bedrock mediante Geyser/Floodgate y coordinará sistemas existentes como Towny y LuckPerms.

## 1. Objetivo

`MushokuCore` será el núcleo de las mecánicas exclusivas del servidor. No debe intentar reemplazar todos los plugins instalados. Su función es implementar las reglas que Towny, LuckPerms, el datapack y otros plugins genéricos no pueden expresar por sí solos.

Debe administrar:

- las Siete Potencias Mundiales;
- los logros fundadores contra Warden, Ender Dragon y Wither;
- estadísticas y medidas contra la explotación de rankings;
- las dos alianzas fijas: Orsted Corp y Apóstoles del Dios Humano;
- límites, bloqueos y beneficios de facciones;
- enfermedades temáticas;
- identificación y entrega segura de reliquias;
- apertura programada o administrativa de Nether y End;
- menús equivalentes para Java y Bedrock;
- auditoría administrativa y persistencia de datos.

El resultado final será:

```text
MushokuCore.jar
```

que se instalará en:

```text
plugins/MushokuCore.jar
```

## 2. Principios de diseño

### 2.1 Compatibilidad antes que espectacularidad

Toda función esencial debe poder utilizarse desde:

- Minecraft Java;
- Minecraft Bedrock por Geyser/Floodgate;
- teclado y mouse;
- mando;
- pantalla táctil.

Por eso:

- todos los menús importantes tendrán un comando alternativo;
- no se dependerá de clics de chat para confirmar acciones;
- ninguna mecánica esencial exigirá mano secundaria;
- no se dependerá del brillo de entidades;
- las enfermedades utilizarán efectos vanilla;
- las reliquias tendrán un ítem vanilla como base;
- los modelos visuales serán decorativos y nunca la única forma de reconocer un objeto.

### 2.2 Integración, no duplicación

| Responsabilidad | Fuente de verdad |
| --- | --- |
| Pueblos, claims y naciones | Towny |
| Grupos, prefijos y permisos | LuckPerms |
| Historial de bloques y contenedores | CoreProtect |
| Traducción Java–Bedrock | Geyser/Floodgate |
| Escalado general de mobs | LevelledMobs |
| Reglas temáticas propias | MushokuCore |
| Presentación en TAB | TAB + PlaceholderAPI |
| Hologramas | DecentHolograms |

MushokuCore consulta o modifica estos sistemas mediante sus API públicas. No debe editar directamente sus bases de datos.

### 2.3 Sin NMS salvo necesidad demostrada

Usar la API de Paper siempre que sea posible. Evitar clases internas de Minecraft, reflexión y paquetes versionados. Esto reduce las roturas al actualizar Paper.

### 2.4 Identidad por UUID

Los jugadores se guardan por UUID. El nombre se conserva únicamente para mostrarlo y se actualiza cuando el jugador vuelve a entrar.

### 2.5 Fallo seguro

Si Towny, LuckPerms, Floodgate o la base de datos no están disponibles:

- no conceder premios ni cambiar alianzas;
- registrar claramente el motivo;
- mantener el servidor encendido si no hay riesgo de corrupción;
- desactivar solo el módulo afectado;
- impedir acciones que podrían duplicar objetos o permisos.

## 3. Alcance por versiones

### Versión 1.0 — MVP

Debe incluir:

- almacenamiento SQLite;
- configuración y migraciones;
- Siete Potencias, puestos 1, 2, 3 y 5;
- seguimiento de contribución contra jefes;
- protección contra farmeo repetido de bajas;
- comandos `/potencias`, `/mtestado` y administración básica;
- enfermedades y curas;
- detección Java/Bedrock;
- placeholders para TAB/hologramas;
- logs de auditoría;
- importación de datos existentes de Skript.

### Versión 1.1 — Facciones

- integración completa con Towny;
- Orsted Corp y Apóstoles como naciones fijas;
- bloqueo de cambio de alianza durante 14 días;
- penalización de desertor durante 7 días;
- cálculo de claims: 8 base + 5 por miembro, máximo 48;
- mínimo 3 y máximo 8 miembros por facción durante la etapa inicial;
- beneficios configurables por alianza.

### Versión 1.2 — Reliquias y Trial Chambers

- identificación de reliquias mediante PDC;
- entrega idempotente y registro de propiedad;
- tablas de probabilidad configurables;
- recompensas normales y ominosas;
- integración con modelos Java y mapeos Geyser v2;
- herramientas administrativas de recuperación.

### Versión 1.3 — Eventos y contenido

- apertura controlada de dimensiones;
- eventos de la fortaleza de Perugius;
- API para NPC y jefes futuros;
- puestos 4, 6 y 7 conectados a mazmorras, duelos y exploración;
- formularios Bedrock opcionales.

No intentar desarrollar todas las versiones simultáneamente.

## 4. Entorno de desarrollo

### Requisitos

- JDK requerido por la versión exacta de Paper instalada; para el proyecto actual se prevé Java 25.
- Gradle Wrapper incluido en el repositorio.
- IntelliJ IDEA Community o equivalente.
- Git.
- Servidor Paper local de pruebas.
- Clientes de prueba Java y Bedrock.

No compilar contra el JAR descargado del servidor. Usar las dependencias/API oficiales de Paper.

### Repositorio sugerido

```text
mushoku-core/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── gradle/wrapper/
├── README.md
├── CHANGELOG.md
├── LICENSE
└── src/
    ├── main/
    │   ├── java/ar/com/maydana/mushokucore/
    │   └── resources/
    │       ├── plugin.yml
    │       ├── config.yml
    │       ├── messages.yml
    │       ├── diseases.yml
    │       ├── relics.yml
    │       └── migrations/
    └── test/java/
```

## 5. Proyecto Gradle

Ejemplo orientativo de `build.gradle.kts`:

```kotlin
plugins {
    java
    id("com.gradleup.shadow") version "VERSION_ESTABLE"
}

group = "ar.com.maydana"
version = "1.0.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.lucko.me/")
    maven("https://repo.glaremasters.me/repository/towny/")
    maven("https://repo.opencollab.dev/main/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:PAPER_VERSION-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:LUCKPERMS_VERSION")
    compileOnly("com.palmergames.bukkit.towny:towny:TOWNY_VERSION")
    compileOnly("org.geysermc.floodgate:api:FLOODGATE_VERSION")
    compileOnly("me.clip:placeholderapi:PLACEHOLDERAPI_VERSION")

    implementation("org.xerial:sqlite-jdbc:SQLITE_VERSION")

    testImplementation(platform("org.junit:junit-bom:JUNIT_VERSION"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("org.sqlite", "ar.com.maydana.mushokucore.lib.sqlite")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
```

Las versiones deben fijarse en un catálogo o propiedades de Gradle y actualizarse deliberadamente. No usar rangos dinámicos ni `latest` en una compilación destinada a producción.

## 6. `plugin.yml`

```yaml
name: MushokuCore
version: '${version}'
main: ar.com.maydana.mushokucore.MushokuCorePlugin
api-version: '26.2'
authors: [AdministracionMushoku]
description: Sistemas tematicos y progresion del servidor Mushoku

depend:
  - LuckPerms

softdepend:
  - Towny
  - floodgate
  - PlaceholderAPI
  - TAB
  - DecentHolograms

commands:
  mushoku:
    description: Comando principal de MushokuCore
    usage: /mushoku ayuda
    aliases: [mt]
  potencias:
    description: Muestra las Siete Potencias Mundiales
  mtestado:
    description: Muestra el estado tematico del jugador
  faccion:
    description: Gestion complementaria de facciones
  alianza:
    description: Consulta o solicita una alianza
  enfermedad:
    description: Administracion de enfermedades
  reliquia:
    description: Administracion de reliquias

permissions:
  mushoku.use:
    default: true
  mushoku.potencias.view:
    default: true
  mushoku.factions.use:
    default: true
  mushoku.admin:
    default: op
  mushoku.admin.reload:
    default: op
  mushoku.admin.potencias:
    default: op
  mushoku.admin.enfermedades:
    default: op
  mushoku.admin.reliquias:
    default: op
  mushoku.admin.dimensiones:
    default: op
```

Usar `depend` solo cuando el plugin no pueda funcionar de ninguna manera sin la dependencia. Las integraciones opcionales deben ser `softdepend` y comprobarse en tiempo de ejecución.

## 7. Arquitectura interna

Separar cada responsabilidad:

```text
ar.com.maydana.mushokucore
├── MushokuCorePlugin.java
├── bootstrap/
├── command/
├── config/
├── database/
│   ├── DatabaseManager.java
│   ├── MigrationRunner.java
│   └── repository/
├── integration/
│   ├── FloodgateHook.java
│   ├── LuckPermsHook.java
│   ├── PlaceholderHook.java
│   └── TownyHook.java
├── module/
│   ├── dimensions/
│   ├── diseases/
│   ├── factions/
│   ├── ranks/
│   └── relics/
├── service/
├── listener/
├── model/
├── ui/
├── audit/
└── util/
```

Cada módulo debe tener:

- configuración propia;
- listeners propios;
- servicio de dominio;
- repositorio de datos;
- comandos;
- permisos;
- pruebas;
- método de inicio y cierre.

No colocar toda la lógica en la clase principal ni dentro de listeners.

## 8. Ciclo de inicio y apagado

Orden recomendado de `onEnable()`:

1. guardar recursos predeterminados si no existen;
2. validar configuración;
3. abrir SQLite;
4. ejecutar migraciones;
5. detectar dependencias;
6. iniciar servicios;
7. registrar listeners;
8. registrar comandos;
9. registrar placeholders;
10. iniciar tareas periódicas;
11. informar módulos activos e inactivos.

En `onDisable()`:

1. impedir nuevas operaciones;
2. cancelar tareas;
3. vaciar colas de escritura;
4. cerrar la base de datos;
5. registrar un cierre limpio.

Clase principal mínima:

```java
public final class MushokuCorePlugin extends JavaPlugin {
    private PluginBootstrap bootstrap;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            bootstrap = new PluginBootstrap(this);
            bootstrap.enable();
            getLogger().info("MushokuCore iniciado correctamente.");
        } catch (Exception exception) {
            getLogger().log(Level.SEVERE, "No se pudo iniciar MushokuCore", exception);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.disable();
        }
    }
}
```

## 9. Configuración principal

Actualizar el contrato existente a este criterio:

```yaml
schema: 2
enabled: true

storage:
  type: sqlite
  file: mushoku.db
  busy-timeout-ms: 5000

modules:
  ranks: true
  factions: true
  diseases: true
  relics: true
  dimensions: true
  bedrock-bridge: true

world:
  name: world
  border-diameter: 21800
  center-x: 0
  center-z: 0
  min-y: -64
  max-y: 320

dimensions:
  nether-locked-at-launch: true
  end-locked-at-launch: true
  minimum-unlock-days: 7
  allow-admin-bypass: true

factions:
  min-members: 3
  max-members: 8
  claims-base: 8
  claims-per-member: 5
  claims-max: 48
  allegiance-lock-days: 14
  deserter-cooldown-days: 7
  fixed-nations:
    orsted: Orsted_Corp
    apostles: Apostoles_Humanos

ranks:
  pvp-same-victim-cooldown-hours: 12
  boss-contribution-expiry-minutes: 60
  minimum-contribution-percent: 1.0
  announce-global: true

bedrock:
  enable-forms: true
  always-provide-command-fallback: true
```

El rango vertical debe coincidir con el mundo finalmente exportado. No activar Y 511 en producción hasta verificar mapa, Paper, WorldEdit, protecciones y Bedrock.

## 10. Base de datos

Usar SQLite para el servidor inicial. Todas las consultas de disco deben realizarse fuera del hilo principal; las modificaciones de entidades, inventarios, mundos y API Bukkit/Paper vuelven al hilo del servidor.

Tablas mínimas:

```sql
CREATE TABLE schema_version (
    version INTEGER PRIMARY KEY,
    applied_at TEXT NOT NULL
);

CREATE TABLE players (
    uuid TEXT PRIMARY KEY,
    last_name TEXT NOT NULL,
    first_join_at TEXT NOT NULL,
    last_seen_at TEXT NOT NULL,
    bedrock INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE world_powers (
    position INTEGER PRIMARY KEY CHECK (position BETWEEN 1 AND 7),
    title TEXT NOT NULL,
    owner_uuid TEXT,
    owner_name TEXT,
    acquired_at TEXT,
    reason TEXT,
    revision INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE boss_damage (
    boss_uuid TEXT NOT NULL,
    boss_type TEXT NOT NULL,
    player_uuid TEXT NOT NULL,
    damage REAL NOT NULL,
    last_hit_at TEXT NOT NULL,
    PRIMARY KEY (boss_uuid, player_uuid)
);

CREATE TABLE pvp_score (
    player_uuid TEXT PRIMARY KEY,
    valid_kills INTEGER NOT NULL DEFAULT 0,
    updated_at TEXT NOT NULL
);

CREATE TABLE pvp_pair_cooldown (
    attacker_uuid TEXT NOT NULL,
    victim_uuid TEXT NOT NULL,
    last_valid_kill_at TEXT NOT NULL,
    PRIMARY KEY (attacker_uuid, victim_uuid)
);

CREATE TABLE allegiances (
    player_uuid TEXT PRIMARY KEY,
    allegiance TEXT NOT NULL,
    joined_at TEXT NOT NULL,
    locked_until TEXT,
    deserter_until TEXT
);

CREATE TABLE diseases (
    player_uuid TEXT NOT NULL,
    disease_id TEXT NOT NULL,
    contracted_at TEXT NOT NULL,
    expires_at TEXT,
    source TEXT,
    PRIMARY KEY (player_uuid, disease_id)
);

CREATE TABLE relic_instances (
    relic_uuid TEXT PRIMARY KEY,
    relic_id TEXT NOT NULL,
    created_at TEXT NOT NULL,
    original_owner_uuid TEXT,
    source TEXT NOT NULL,
    status TEXT NOT NULL
);

CREATE TABLE audit_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    occurred_at TEXT NOT NULL,
    actor_uuid TEXT,
    action TEXT NOT NULL,
    target TEXT,
    details TEXT
);
```

Nunca borrar migraciones ya publicadas. Crear `V001__initial.sql`, `V002__...sql`, etcétera.

## 11. Módulo de Siete Potencias

### Puestos iniciales

| Puesto | Título | Regla |
| ---: | --- | --- |
| 1 | Dios de la Técnica | Mayor contribución en la primera muerte válida de un Warden |
| 2 | Dios Dragón | Mayor contribución en la primera muerte válida del Ender Dragon |
| 3 | Dios de la Lucha | Mayor contribución en la primera muerte válida del Wither |
| 4 | Dios Demonio | Reservado para mazmorras/eventos |
| 5 | Dios de la Muerte | Mayor cantidad de bajas PvP válidas |
| 6 | Dios de la Espada | Ranking de duelos oficial |
| 7 | Dios del Norte | Exploración y logros |

### Contribución contra jefes

Escuchar daño a Warden, Wither y Ender Dragon. Registrar:

- UUID de la entidad;
- tipo de jefe;
- UUID del atacante real;
- daño efectivo, sin contar exceso sobre la vida restante;
- instante del último golpe.

Resolver proyectiles, mascotas y fuentes indirectas hasta el jugador responsable cuando la API lo permita.

Al morir el jefe:

1. comprobar que el puesto todavía está libre;
2. verificar que el jefe pertenezca al mundo/evento permitido;
3. sumar contribución válida;
4. descartar participantes por debajo del mínimo configurable;
5. elegir mayor contribución;
6. resolver empates por primer instante en alcanzar el daño final empatado;
7. guardar la asignación en una transacción;
8. actualizar LuckPerms;
9. publicar el evento y registrar auditoría.

La asignación debe ser idempotente: el mismo evento no puede entregar dos veces un puesto.

### Bajas PvP válidas

Una baja no suma si:

- atacante y víctima son la misma persona;
- comparten una identidad vinculada conocida;
- la misma pareja atacante/víctima ya puntuó en las últimas 12 horas;
- ocurrió en un mundo de prueba, arena no válida o evento excluido;
- la víctima permaneció conectada demasiado poco o existe otra señal clara de boosting;
- un administrador la anuló.

El puesto 5 se recalcula después de cada baja válida y mediante una tarea de reconciliación periódica.

### Cambio de propietario

Primero guardar la nueva asignación. Después retirar el grupo anterior y conceder el nuevo mediante LuckPerms. Si LuckPerms falla, dejar una operación pendiente para reintento; nunca perder la información de quién ganó.

## 12. Módulo de facciones y alianzas

### Modelo

- Pueblo Towny = facción de jugadores.
- Nación Towny = alianza macro.
- Solo existen `Orsted_Corp` y `Apostoles_Humanos` como naciones públicas.
- Los jugadores pueden crear pueblos, pero no naciones.

### Flujo de elección

1. El jugador ejecuta `/alianza elegir orsted` o `/alianza elegir apostoles`.
2. El plugin comprueba que pertenece a una facción válida.
3. Muestra beneficios, duración del bloqueo y confirmación.
4. Java confirma mediante comando; Bedrock puede usar formulario o el mismo comando.
5. Se registra la elección y `locked_until`.
6. Se solicita o ejecuta la integración Towny correspondiente.
7. Se aplica el grupo LuckPerms.
8. Se registra la operación.

Si una integración falla, revertir lo aplicado en la misma operación o marcarla como pendiente. Evitar estados en los que Towny diga Orsted y LuckPerms diga Apóstoles.

### Beneficios sugeridos

**Orsted Corp**:

- 50 % menos probabilidad de contraer enfermedades;
- bonificación moderada de resistencia durante eventos defensivos autorizados;
- acceso a misiones de investigación y caza.

**Apóstoles del Dios Humano**:

- `/visionapostol` con visión nocturna temporal y 30 minutos de enfriamiento;
- pistas adicionales en determinados eventos;
- acceso a misiones de información.

Los beneficios deben ser configurables. No otorgar daño permanente, inmunidad total, reliquias pagas ni ventajas obtenibles mediante donaciones.

### Límites

- mínimo 3 miembros;
- máximo inicial 8;
- 8 claims base;
- 5 claims adicionales por miembro;
- máximo 48;
- alianza bloqueada 14 días;
- desertor sin beneficios durante 7 días.

MushokuCore debe observar eventos de Towny y validar límites mediante su API, nunca editando archivos de Towny.

## 13. Módulo de enfermedades

Definir cada enfermedad en `diseases.yml`:

```yaml
dryne:
  display-name: "Fiebre de Dryne"
  sources:
    - HUSK_DAMAGE
  base-chance-percent: 4.0
  duration-seconds: 180
  effects:
    WEAKNESS: 0
    MINING_FATIGUE: 0
  cures:
    - MILK
  immunity-after-cure-seconds: 300

miasma:
  display-name: "Miasma de laberinto"
  sources:
    - CAVE_SPIDER_DAMAGE
    - SILVERFISH_DAMAGE
  base-chance-percent: 4.0
  duration-seconds: 120
  effects:
    SLOWNESS: 0
    WEAKNESS: 0

laplace:
  display-name: "Fiebre de Laplace"
  sources:
    - ENDERMAN_DAMAGE
  base-chance-percent: 4.0
  duration-seconds: 45
  effects:
    DARKNESS: 0
    NAUSEA: 0
```

Reglas:

- el amplificador `0` corresponde al nivel I;
- aplicar reducción Orsted antes de la tirada;
- no acumular infinitamente duración o nivel;
- guardar la enfermedad para sobrevivir a una reconexión;
- permitir curación con leche si así se configura;
- informar en texto simple;
- no ejecutar una tarea por jugador cada tick;
- procesar expiraciones con una cola o tarea periódica liviana.

## 14. Módulo de reliquias

### Identidad segura

No identificar una reliquia solamente por nombre o lore. Usar `PersistentDataContainer` con claves como:

```text
mushokucore:relic_id
mushokucore:relic_uuid
mushokucore:schema
```

El nombre y lore son presentación; el PDC es la identidad lógica.

### Catálogo inicial

| Reliquia | Base Java | Normal | Ominosa |
| --- | --- | ---: | ---: |
| Anillo de Supresión Mágica | Amethyst Shard | 3 % | 5 % |
| Pendiente de Sara | Echo Shard | 3 % | 5 % |
| Colgante Migurd | Heart of the Sea | 3 % | 5 % |
| Espada de Paul | Iron Sword | 2 % | 5 % |
| Lanza Superd | Trident | 1 % | 4 % |
| Aqua Heartia | Diamond Sword | 0 % | 3 % |
| Armadura Mágica | Netherite Chestplate | 0 % | 3 % |

### Entrega

1. Detectar la primera generación válida de la recompensa.
2. Realizar una sola tirada por evento/contenedor.
3. Crear UUID único para la reliquia.
4. Guardar primero el registro como `PENDING`.
5. Entregar el objeto.
6. Confirmar como `ACTIVE`.
7. Si el inventario está lleno, soltar de manera controlada o enviar a un buzón; nunca perderlo silenciosamente.
8. Registrar jugador, coordenadas, mundo, tipo de cámara y probabilidad.

### Bedrock

Cada reliquia mantiene base vanilla, nombre y lore. Para la apariencia:

- Java usa el componente `minecraft:item_model` y el pack Java;
- Bedrock usa mapeos personalizados Geyser v2;
- el pack Bedrock se entrega desde Geyser;
- `enable-custom-content` debe estar activo;
- una falla visual no puede convertir o borrar el ítem.

## 15. Módulo de dimensiones

El bloqueo no debe consistir únicamente en cancelar portales. Debe cubrir:

- portales vanilla;
- comandos de teleportación propios;
- homes/RTP de plugins conocidos;
- teleportación a jugadores dentro de dimensiones bloqueadas;
- comandos administrativos con bypass explícito;
- reaparición o retorno accidental.

Estados:

```text
LOCKED -> PREPARED -> OPEN
```

- `LOCKED`: jugadores normales no ingresan.
- `PREPARED`: administradores autorizados preparan el evento.
- `OPEN`: acceso público habilitado.

El cambio debe requerir:

1. permiso administrativo;
2. confirmación textual;
3. backup recomendado;
4. registro de fecha, administrador y estado anterior;
5. persistencia antes de anunciarlo.

No abrir una dimensión solamente porque hayan transcurrido siete días. El plazo es mínimo; la administración confirma la apertura cuando la fortaleza y el evento estén listos.

## 16. Puente Bedrock

Floodgate permite detectar si un UUID corresponde a un jugador Bedrock y enviar formularios. La capa debe ser opcional:

```java
public interface PlayerUi {
    void showPowers(Player player);
    void showStatus(Player player);
    void confirmAllegiance(Player player, Allegiance allegiance);
}
```

Implementaciones:

```text
JavaChatUi
BedrockFormUi
```

Si Floodgate no está cargado o el formulario falla, usar automáticamente texto y comandos. La lógica de negocio nunca debe vivir dentro del formulario.

## 17. Comandos

### Jugadores

```text
/mushoku ayuda
/potencias
/mtestado
/faccion info
/alianza info
/alianza elegir <orsted|apostoles>
/alianza confirmar
/reliquia info
```

### Administración

```text
/mushoku estado
/mushoku reload
/mushoku diagnostico
/mushoku backupdb
/potencias admin asignar <1-7> <jugador> <motivo>
/potencias admin liberar <1-7> <motivo>
/enfermedad aplicar <jugador> <id>
/enfermedad curar <jugador> [id|todas]
/alianza admin establecer <jugador> <alianza>
/reliquia dar <jugador> <id>
/reliquia inspeccionar
/dimension estado
/dimension preparar <nether|end>
/dimension abrir <nether|end> confirmar
/dimension cerrar <nether|end> confirmar
```

Todos los comandos que cambien datos deben:

- validar permisos;
- funcionar desde consola cuando corresponda;
- usar autocompletado;
- producir un mensaje inequívoco;
- registrar auditoría;
- no revelar stacktraces al jugador.

`/mushoku reload` solo recarga archivos propios seguros. No debe simular `/reload` de Bukkit ni reconstruir dependencias críticas en caliente.

## 18. PlaceholderAPI

Placeholders sugeridos:

```text
%mushoku_power_1_name%
%mushoku_power_1_title%
%mushoku_player_power_position%
%mushoku_player_power_title%
%mushoku_player_allegiance%
%mushoku_player_disease%
%mushoku_player_valid_kills%
%mushoku_dimensions_nether_state%
%mushoku_dimensions_end_state%
```

Los placeholders deben leer una caché y no consultar SQLite en cada actualización de TAB u holograma.

## 19. Rendimiento y concurrencia

- No ejecutar consultas SQL, HTTP ni archivos pesados en el hilo principal.
- No llamar API Bukkit/Paper desde tareas asíncronas salvo que la documentación lo permita.
- No recorrer todos los jugadores o entidades cada tick.
- No guardar a disco por cada punto de daño; acumular en memoria y realizar escrituras agrupadas seguras.
- Vaciar datos críticos inmediatamente al morir un jefe o cambiar un puesto.
- Usar cachés con invalidación explícita.
- Limitar tamaño y retención de `audit_log` mediante archivado, no borrado silencioso.
- Medir tareas del plugin con spark durante pruebas.

Presupuesto orientativo del plugin bajo carga normal:

- sin tareas repetidas cada tick;
- tareas periódicas generales cada 20–100 ticks según necesidad;
- ninguna operación normal superior a unos pocos milisegundos en el hilo principal;
- cero consultas por placeholder renderizado.

## 20. Seguridad contra duplicación y abuso

- UUID único para cada reliquia.
- Transacciones para premios y puestos.
- Restricción por mundo y región para eventos fundadores.
- Lista de mundos de prueba excluidos.
- Cooldown por pareja en PvP.
- Registro de asignaciones administrativas.
- Confirmación doble para abrir dimensiones o borrar datos.
- Comandos sensibles negados por defecto.
- Nunca confiar en nombres visibles de objetos.
- No ejecutar comandos configurables arbitrarios sin una lista permitida.
- Escapar o parametrizar todas las consultas SQL.
- Copiar la base de datos antes de una migración.

## 21. Migración desde Skript y datapack

No activar simultáneamente dos implementaciones que entreguen el mismo premio.

### Orden

1. Detener el servidor y hacer backup.
2. Exportar variables de `potencias.sk` y `enfermedades.sk`.
3. Instalar MushokuCore con módulos de escritura desactivados.
4. Ejecutar importación en modo simulación.
5. Comparar UUID, nombres, puestos y puntuaciones.
6. Ejecutar importación real.
7. Desactivar únicamente el script del módulo migrado.
8. Activar el módulo Java.
9. Reiniciar y verificar.
10. Mantener el backup de Skript hasta completar varios días de operación.

Para reliquias, elegir una sola fuente de entrega:

- mientras el datapack entregue loot, `modules.relics=false`; o
- al activar el módulo Java, retirar/desactivar los advancements de entrega del datapack.

El datapack puede seguir administrando otras funciones no migradas.

## 22. Pruebas automatizadas

### Unitarias

- fórmula de claims;
- reducción de contagio;
- expiración de cooldowns;
- desempate de contribución;
- cálculo de ranking PvP;
- transición de estados dimensionales;
- probabilidades expresadas como rangos válidos;
- serialización y migración de configuración.

### Integración

- SQLite y migraciones;
- reinicio con datos persistidos;
- cambio de propietario de una Potencia;
- error simulado de LuckPerms;
- Towny ausente/presente;
- Floodgate ausente/presente;
- jugador que cambia de nombre;
- inventario lleno durante entrega de reliquia.

### Prueba manual Java–Bedrock

- `/potencias` y `/mtestado`;
- elección y confirmación de alianza;
- formularios Bedrock y alternativa por comandos;
- curación con leche;
- texturas y nombres de reliquias;
- reconexión con inventario intacto;
- TAB, prefijos y hologramas;
- portales bloqueados;
- mensajes legibles desde móvil y mando.

## 23. Servidor de ensayo

Crear una copia separada del servidor con:

```text
server-test/
├── paper.jar
├── plugins/
├── world_test/
└── start-test.*
```

Nunca probar compilaciones nuevas por primera vez en producción. La base de datos, mundo y puertos del entorno de ensayo deben ser distintos.

Prueba de lanzamiento:

1. iniciar sin MushokuCore;
2. iniciar con MushokuCore y módulos apagados;
3. activar un módulo por vez;
4. forzar reinicios en puntos críticos;
5. simular una dependencia ausente;
6. probar con Java y Bedrock;
7. ejecutar una sesión con 10–14 jugadores;
8. revisar `latest.log` y spark;
9. restaurar un backup de ensayo.

## 24. Compilación e instalación

En Windows:

```powershell
.\gradlew.bat clean test shadowJar
```

En Linux:

```bash
./gradlew clean test shadowJar
```

El resultado estará en:

```text
build/libs/MushokuCore-1.0.0.jar
```

Instalación:

1. detener el servidor;
2. crear backup;
3. copiar el JAR a `plugins/`;
4. iniciar una vez;
5. revisar la configuración generada;
6. detener;
7. ajustar archivos;
8. iniciar con whitelist;
9. ejecutar `/mushoku diagnostico`;
10. comprobar consola y pruebas mínimas.

## 25. Actualizaciones y reversión

Cada versión debe incluir:

- número semántico;
- changelog;
- versión mínima de Paper;
- versiones verificadas de Towny, LuckPerms, Floodgate y PlaceholderAPI;
- migraciones nuevas;
- procedimiento de reversión;
- hash SHA-256 del JAR publicado.

Antes de actualizar:

1. detener;
2. copiar JAR anterior, configuración y base de datos;
3. probar la nueva versión en ensayo;
4. instalar en producción con whitelist;
5. revisar migraciones y log;
6. conservar el backup hasta validar.

No volver a un JAR anterior si la base de datos ya fue migrada a un esquema incompatible. Restaurar juntos el JAR y la copia de base correspondiente.

## 26. Criterios de aceptación

MushokuCore 1.0 estará listo cuando:

- inicia y se apaga sin errores;
- conserva datos tras reinicios y cierres inesperados simulados;
- no bloquea el hilo principal con SQLite;
- asigna correctamente los puestos 1, 2, 3 y 5;
- no duplica una asignación ni una reliquia;
- sincroniza grupos de LuckPerms;
- funciona aunque Floodgate no esté disponible;
- ofrece comandos equivalentes a los formularios Bedrock;
- mantiene UUID e inventario de jugadores Bedrock;
- no duplica las recompensas del datapack;
- pasa las pruebas Java y Bedrock;
- mantiene aproximadamente 20 TPS durante la prueba representativa;
- dispone de backup y reversión documentados.

## 27. Entregables que debe producir el desarrollador

```text
MushokuCore-1.0.0.jar
MushokuCore-1.0.0-sources.jar
README.md
CHANGELOG.md
MIGRATION.md
config.yml
messages.yml
diseases.yml
relics.yml
SHA256SUMS.txt
```

También debe entregar:

- repositorio Git completo;
- instrucciones de compilación reproducible;
- licencia del código;
- lista de dependencias y licencias;
- datos de prueba o comandos para reproducir cada sistema;
- procedimiento de importación desde Skript;
- comprobación documentada en Java y Bedrock.

## 28. Referencias oficiales

- [Desarrollo de plugins Paper](https://docs.papermc.io/paper/dev/)
- [Configuración de plugin.yml](https://docs.papermc.io/paper/dev/plugin-yml/)
- [Persistent Data Container de Paper](https://docs.papermc.io/paper/dev/pdc/)
- [Scheduler de Paper](https://docs.papermc.io/paper/dev/scheduler/)
- [API de Floodgate](https://geysermc.org/wiki/floodgate/api/)
- [Forms/Cumulus para Bedrock](https://geysermc.org/wiki/geyser/forms/)
- [Ítems personalizados de Geyser](https://geysermc.org/wiki/geyser/custom-items/)
- [Repositorio oficial de Towny](https://github.com/TownyAdvanced/Towny)

## 29. Decisión recomendada

Desarrollar primero `MushokuCore 1.0` con Potencias, enfermedades, almacenamiento, auditoría y compatibilidad Bedrock. Mantener Towny como dueño de facciones, LuckPerms como dueño de permisos y el datapack como respaldo temporal. Después de una semana estable de pruebas, migrar facciones y finalmente reliquias.

Este orden reduce el riesgo de corrupción, duplicación de recompensas y diferencias entre Java y Bedrock.

// Plantilla Gradle según §5 del Instructivo de Diseño de MushokuCore.
// NO VERIFICADA: esta máquina no tiene Gradle ni wrapper (verificado 2026-10-17:
// `gradle` no está en el PATH y no existe ~/.gradle). Usar mientras tanto .\build.ps1.
plugins {
    java
}

group = "ar.com.maydana"
version = "1.0.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Versión = la del paper-api instalado en el servidor local
    // (servidor\libraries\io\papermc\paper\paper-api\26.2.build.134-stable).
    // [A VERIFICAR] confirmar el coordinate exacto en repo.papermc.io antes de usar en producción.
    compileOnly("io.papermc.paper:paper-api:26.2.build.134-stable")

    // DECISION D18 (2026-10-18): alinear con el servidor. NO se inventan versiones:
    //  - paper-api 26.2.build.134-stable  = servidor\libraries\io\papermc\paper\paper-api\26.2.build.134-stable.
    //  - sqlite-jdbc 3.49.1.0             = servidor\libraries\org\xerial\sqlite-jdbc\3.49.1.0.
    // sqlite-jdbc NO se compila ni se empaqueta: Paper ya lo carga en runtime. Se deja la versión
    // documentada acá para que build.gradle.kts y el servidor coincidan cuando se migre del build.ps1.
    // (build.ps1 no usa Gradle: compila con javac de JAVA_HOME 25 contra esos mismos jars.)
}

tasks.jar {
    archiveFileName.set("MushokuCore-1.0.0.jar")
    // plugin.yml lleva la versión literal '1.0.0' (§6 muestra '${version}',
    // que requiere filtrado de recursos de Gradle; el build.ps1 no lo soporta,
    // por eso se deja el literal). Si algún día se usa el token '${version}',
    // activar processResources { filesMatching("plugin.yml") { expand(...) } }.
}

import nu.studer.gradle.jooq.JooqGenerate
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    /*
        If either of the below versions change, ensure that the hardcoded jOOQ version in the buildscript block below
        is synced with the version managed by Spring Boot.
     */
    id("org.springframework.boot") version "2.7.3"
    id("io.spring.dependency-management") version "1.0.12.RELEASE"

    id("com.google.cloud.tools.jib") version "3.3.0"
    id("nu.studer.jooq") version "6.0.1"

    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "com.mspigl.jooq-codegen-testcontainers-skaffold"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_11

allprojects {
    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// need to be declared after repositories are declared
val jooqVersion = dependencyManagement.importedProperties["jooq.version"]
val postgresVersion = dependencyManagement.importedProperties["postgresql.version"]
val flywayVersion = dependencyManagement.importedProperties["flyway.version"]
val kotlinVersion = dependencyManagement.importedProperties["kotlin.version"]
val slf4jVersion = dependencyManagement.importedProperties["slf4j.version"]
val mockitoVersion = dependencyManagement.importedProperties["mockito.version"]
val testcontainersVersion = "1.17.3"

val databaseSchema = "sample_schema"

sourceSets {
    val database by creating {
        compileClasspath += sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
    }

    main {
        output.dir(database.output)
    }
}

buildscript {
    // see https://github.com/etiennestuder/gradle-jooq-plugin#enforcing-the-jooq-configuration-xml-schema-version
    configurations["classpath"].resolutionStrategy.eachDependency {
        if (requested.group == "org.jooq") {
            /*
                The JOOQ version managed by Spring Boot cannot be determined from this context, so it has to be
                hardcoded. If the version of the org.springframework.boot or io.spring.dependency-management Gradle
                plugins change, make sure that the below version is updated to match the version managed by Spring Boot.
             */
            useVersion("3.14.16")
        }
    }
}

dependencies {
    implementation(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))

    implementation("org.springframework.boot:spring-boot-starter-log4j2") {
        modules {
            module("org.springframework.boot:spring-boot-starter-logging") {
                replacedBy("org.springframework.boot:spring-boot-starter-log4j2", "Use Log4j2 instead of Logback")
            }
        }
    }

    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // dependencies required for executing jOOQ codegen against a Testcontainers database
    "databaseImplementation"(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
    "databaseImplementation"("org.testcontainers:postgresql")
    "databaseImplementation"("org.jooq:jooq:$jooqVersion")
    "databaseImplementation"("org.jooq:jooq-meta:$jooqVersion")
    // including this so that Testcontainers/Flyway logging works properly
    "databaseImplementation"("org.slf4j:slf4j-simple:$slf4jVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:localstack")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")

    runtimeOnly("org.postgresql:postgresql")

    // provide the database source set's output to the jOOQ generator so that the classes are accessible by the generator
    jooqGenerator(sourceSets["database"]!!.output)
    jooqGenerator(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
    jooqGenerator("org.testcontainers:postgresql")
    jooqGenerator("org.jooq:jooq-meta:$jooqVersion")
    jooqGenerator("org.jooq:jooq:$jooqVersion")
    jooqGenerator("org.flywaydb:flyway-core:$flywayVersion")
    jooqGenerator("org.postgresql:postgresql:$postgresVersion")
    jooqGenerator("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    jooqGenerator("org.slf4j:slf4j-simple:$slf4jVersion")
}

jooq {
    version.set(jooqVersion)

    configurations {
        create("main") {
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN

                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"

                    database.apply {
                        name = "com.mspigl.jooqcodegentestcontainersskaffold.db.LocalMigrationDatabase"
                        inputSchema = databaseSchema
                        excludes = "flyway_schema_history"
                    }

                    target.apply {
                        packageName = "com.mspigl.jooqcodegentestcontainersskaffold"
                    }
                }
            }
        }
    }
}

jib {
    to {
        image = "mspigl/jooq-codegen-testcontainers-skaffold:latest"
    }

    extraDirectories {
        paths {
            path {
                setFrom("$buildDir/classes/kotlin/database/com/mspigl/jooqcodegentestcontainersskaffold/db/migrations")
                into = "/app/classes/com/mspigl/jooqcodegentestcontainersskaffold/db/migrations"
            }
        }
    }
}

// cache jOOQ code generation so that it will only run after a clean or a source set change
tasks.named<JooqGenerate>("generateJooq") {
    allInputsDeclared.set(true)
    outputs.cacheIf { true }
}

plugins {
    kotlin("jvm").version("1.8.10")
    id("com.google.cloud.tools.jib").version("3.3.1")
    id("io.gitlab.arturbosch.detekt").version("1.22.0")
    id("io.qameta.allure").version("2.11.2")
    id("net.saliman.properties").version("1.5.2")
}

java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenLocal()
    mavenCentral()
}

ext {
    set("logback.version", "1.2.3")
    set("detekt.version", "1.22.0")
    set("mockk.version", "1.13.4")
    set("kotest.version", "5.5.5")
    set("jackson.version", "2.12.4")
    set("structurizr.version", "1.23.2")
    set("structurizr-dsl.version", "1.29.1")
    set("structurizr-annotations.version", "1.3.5")
    set("structurizr-analysis.version", "1.3.5")
    set("structurizr-kotlin.version", "1.3.0")
    set("freemarker.version", "2.3.32")
    set("allure.version", "2.21.0")
}

dependencies {
    implementation("com.structurizr:structurizr-core:${property("structurizr.version")}")
    implementation("com.structurizr:structurizr-client:${property("structurizr.version")}")
    implementation("com.structurizr:structurizr-dsl:${property("structurizr-dsl.version")}")
    implementation("commons-logging:commons-logging:1.2")

    runtimeOnly("org.apache.httpcomponents.client5:httpclient5:5.1.2")
    runtimeOnly("javax.xml.bind:jaxb-api:2.3.1")

    implementation("ch.qos.logback:logback-classic:${property("logback.version")}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${property("jackson.version")}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${property("jackson.version")}")
    implementation("org.freemarker:freemarker:${property("freemarker.version")}")

    testImplementation("io.mockk:mockk:${property("mockk.version")}")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${property("kotest.version")}")
    testImplementation("io.kotest:kotest-assertions-core-jvm:${property("kotest.version")}")
    testImplementation("io.kotest:kotest-property-jvm:${property("kotest.version")}")
    testImplementation("io.qameta.allure:allure-junit5:${property("allure.version")}")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${property("detekt.version")}")
}

detekt {
    toolVersion = "${property("detekt.version")}"
    source = files("$projectDir/src/main/kotlin", "$projectDir/src/test/kotlin")
    config = files("$projectDir/detekt.yml")
    buildUponDefaultConfig = true
    ignoreFailures = true

    val cliAutoCorrect = System.getProperty("detektFormat")
    autoCorrect = cliAutoCorrect == "true"
}

allure {
    adapter.aspectjWeaver.set(true)
    adapter.autoconfigure.set(true)
    //resultsDir = file("$buildDir/reports/test/allure-results")
    //reportDir = file("$buildDir/reports/test/allure")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all-compatibility")
            jvmTarget = "11"
        }
    }
    withType<io.gitlab.arturbosch.detekt.Detekt> {
        jvmTarget = "11"
    }

    withType<Jar> {
        // Otherwise you'll get a "No main manifest attribute" error
        manifest {
            attributes(
                "Main-Class" to "edu.obya.c4.appl.SynchronizeAppKt"
            )
        }
        // To avoid the duplicate handling strategy error
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        // To add all of the dependencies
        from(sourceSets.main.get().output)

        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }
}

jib {
    from {
        image = "gcr.io/distroless/java:11-debug"
    }
    to {
        image = "ghcr.io/edu.obya/arch-c4-sync"
        tags = properties["version"]?.let { mutableSetOf(it as String, "latest") } ?: mutableSetOf("latest")
        auth {
            username = properties["githubUsername"]?.toString()
            password = properties["githubToken"]?.toString()
        }
    }
    container {
        entrypoint = mutableListOf("")
        args = mutableListOf("/usr/local/bin/c4-sync")
    }
    extraDirectories {
        paths {
            path {
                setFrom("./scripts")
                into = "/usr/local/bin"
            }
        }
        this.permissions.set(mapOf("/usr/local/bin/*" to "755"))
    }
}

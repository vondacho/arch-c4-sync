plugins {
    kotlin("jvm").version("1.5.21")
    id("com.google.cloud.tools.jib").version("3.0.0")
    id("io.gitlab.arturbosch.detekt").version("1.17.1")
    id("io.qameta.allure").version("2.9")
}

version = properties["version"] ?: "unspecified"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenLocal()
    mavenCentral()
}

ext {
    set("logback.version", "1.2.3")
    set("detekt.version", "1.17.1")
    set("mockk.version", "1.12.0")
    set("kotest.version", "4.6.1")
    set("jackson.version", "2.12.4")
    set("structurizr.version", "1.10.0")
    set("structurizr-dsl.version", "1.17.0")
    set("structurizr-annotations.version", "1.3.5")
    set("structurizr-analysis.version", "1.3.5")
    set("structurizr-kotlin.version", "1.3.0")
    set("freemarker.version", "2.3.31")
    set("allure.version", "2.14.0")
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-reflect:1.8.10")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

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

jib {
    from {
        image = "gcr.io/distroless/java:11-debug"
    }
    to {
        tags = properties["version"]?.let { mutableSetOf(it as String, "latest") } ?: mutableSetOf("latest")
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
        permissions = mapOf("/usr/local/bin/*" to "755")
    }
}

detekt {
    toolVersion = "${property("detekt.version")}"
    input = files("$projectDir/src/main/kotlin", "$projectDir/src/test/kotlin")
    config = files("$projectDir/detekt.yml")
    buildUponDefaultConfig = true
    ignoreFailures = true

    val cliAutoCorrect = System.getProperty("detektFormat")
    autoCorrect = cliAutoCorrect == "true"

    reports {
        xml {
            enabled = true
            destination = file("$buildDir/reports/detekt/detekt.xml")
        }
        html {
            enabled = true
            destination = file("$buildDir/reports/detekt/detekt.html")
        }
    }
}

allure {
    version = "${property("allure.version")}"
    aspectjweaver = true
    autoconfigure = true
    resultsDir = file("$buildDir/reports/test/allure-results")
    reportDir = file("$buildDir/reports/test/allure")
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
}

task<Jar>("fatJar") {
    archiveBaseName.set(rootProject.name)
    archiveClassifier.set("")
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    manifest {
        attributes(
            "Main-Class" to "com.edgelab.c4.appl.SynchronizeAppKt"
        )
    }
}

plugins {
    kotlin("jvm") version "2.1.10"
    application
}

group = "io.github.leo-gan"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit5"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("failed", "skipped")
        showStandardStreams = false
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

application {
    mainClass.set("ion.codegen.CliKt")
    applicationName = "gld-iongen-kotlin"
}

sourceSets {
    test {
        kotlin.srcDir("benches")
        kotlin.srcDir("examples")
    }
}

tasks.register<JavaExec>("microbench") {
    group = "verification"
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("ion.bench.MicrobenchKt")
    jvmArgs("-Xms256m", "-Xmx1g")
}

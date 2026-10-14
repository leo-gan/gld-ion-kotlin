import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
    kotlin("jvm") version "2.1.10"
    application
    // 0.34.0 is the last release that supports Kotlin 2.1 and Gradle 8.12.
    id("com.vanniktech.maven.publish") version "0.34.0"
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

mavenPublishing {
    configure(
        KotlinJvm(
            javadocJar = JavadocJar.Empty(),
            sourcesJar = true,
        ),
    )
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates("io.github.leo-gan", "gld-ion-kotlin", version.toString())
    pom {
        name.set("gld-ion-kotlin")
        description.set("From-scratch Amazon Ion 1.0 and 1.1 library for Kotlin.")
        inceptionYear.set("2026")
        url.set("https://github.com/leo-gan/gld-ion-kotlin")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("leo-gan")
                name.set("Leonid Ganeline")
                email.set("leo.gan.57@gmail.com")
                url.set("https://github.com/leo-gan")
            }
        }
        scm {
            url.set("https://github.com/leo-gan/gld-ion-kotlin")
            connection.set("scm:git:https://github.com/leo-gan/gld-ion-kotlin.git")
            developerConnection.set("scm:git:ssh://git@github.com/leo-gan/gld-ion-kotlin.git")
        }
    }
}

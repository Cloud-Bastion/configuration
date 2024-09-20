import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

plugins {
    id("com.strumenta.antlr-kotlin") version "1.0.0"
    id("maven-publish")
    kotlin("jvm") version "1.9.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.google.inject:guice:7.0.0")
    implementation("com.strumenta:antlr-kotlin-runtime:1.0.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.+")
    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:5.13.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

publishing {

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Cloud-Bastion/configuration")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

kotlin {
    sourceSets {
        main {
            dependencies {
                implementation("com.strumenta:antlr-kotlin-runtime:1.0.0")
            }
        }
    }
}

val generateKotlinGrammarSource = tasks.register<AntlrKotlinTask>("generateKotlinGrammarSource") {
    dependsOn("cleanGenerateKotlinGrammarSource")

    // ANTLR .g4 files are under {example-project}/antlr
    // Only include *.g4 files. This allows tools (e.g., IDE plugins)
    // to generate temporary files inside the base path
    source = fileTree(layout.projectDirectory.dir("src/main/antlr4")) {
        include("**/*.g4")
    }

    val pkgName = "cloud.bastion.configuration.parsers.generated"
    packageName = pkgName
    arguments = listOf("-visitor")

    // Generated files are outputted inside build/generatedAntlr/{package-name}
    val outDir = "generatedAntlr/${pkgName.replace(".", "/")}"
    outputDirectory = layout.buildDirectory.dir(outDir).get().asFile
}

tasks.withType<KotlinCompile<*>> {
    dependsOn(generateKotlinGrammarSource)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    sourceSets {
        main {
            kotlin {
                srcDir(layout.buildDirectory.dir("generatedAntlr"))
            }
        }
    }
}



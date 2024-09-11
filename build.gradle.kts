import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

plugins {
    id("com.strumenta.antlr-kotlin") version "1.0.0"
    kotlin("jvm") version "1.9.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.google.inject", "guice", "7.0.0")
    implementation("com.strumenta:antlr-kotlin-runtime:1.0.0")
    implementation("com.google.code.gson:gson:2.11.0")
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

kotlin {
    sourceSets {
        main {
            kotlin {
                srcDir(layout.buildDirectory.dir("generatedAntlr"))
            }
        }
    }
}



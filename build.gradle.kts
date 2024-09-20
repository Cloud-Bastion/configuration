import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

plugins {
    id("com.strumenta.antlr-kotlin") version "1.0.0"
    id("maven-publish")
    id("java-library")
    kotlin("jvm") version "1.9.21"
}

group = "cloud.bastion"
version = "0.0.3"

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

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            /*artifactId = "configuration"
            pom {
                name = "configuration"
                description = "description"
                url = "https://github.com/Cloud-Bastion/configuration"
                licenses {
                    licenses {}
                }
                developers {
                    developer {}
                }

            }
*/
            from(components["java"])
        }
    }

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

val generateKotlinGrammarSource = tasks.register<AntlrKotlinTask>("generateKotlinGrammarSource") {
    dependsOn("cleanGenerateKotlinGrammarSource")

    // ANTLR .g4 files are under {example-project}/antlr
    // Only include *.g4 files. This allows tools (e.g., IDE plugins)
    // to generate temporary files inside the base path
    source = fileTree(layout.projectDirectory.dir("src/main/antlr4")) {
        include("**/*.g4")
    }

    val pkgName = "cloud.bastion.configuration.parser.generated"
    packageName = pkgName
    arguments = listOf("-visitor")

    // Generated files are outputted inside build/generatedAntlr/{package-name}
    val outDir = "generatedAntlr/${pkgName.replace(".", "/")}"
    outputDirectory = layout.buildDirectory.dir(outDir).get().asFile
}

tasks.withType<KotlinCompile<*>> {
    dependsOn(generateKotlinGrammarSource)
}

tasks.withType<Jar> {
    dependsOn(generateKotlinGrammarSource)
}

tasks.register<Jar>("sourcesJar2") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
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
            dependencies {
                implementation("com.strumenta:antlr-kotlin-runtime:1.0.0")
            }
        }
    }
}



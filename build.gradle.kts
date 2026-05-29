import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
    id("org.jetbrains.grammarkit") version "2023.3.0.3"
}

group = "org.mc-stan.stan"
version = "1.0.0"

repositories {
    mavenCentral()
}

intellij {
    version.set("2024.1")
    type.set("IC")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

grammarKit {
    // Newest IntelliJ build whose Maven artifacts are fully publicly resolvable AND
    // whose platform JARs let Grammar-Kit run headlessly (no EarlyAccessRegistryManager crash).
    // Boundary: 2021.3.3 (213.7172.25) introduced private ai.grazie/infra deps; 2021.3.1
    // (213.6461.73) had unpublished Maven modules. 2021.3.2 is the sweet spot.
    // This is build-time only — generated sources compile against intellij.version = "2024.1".
    intellijRelease.set("213.6777.52")   // IntelliJ IDEA 2021.3.2
}

sourceSets {
    main {
        java {
            srcDirs("src/main/gen")
        }
    }
}

tasks {
    named<GenerateParserTask>("generateParser") {
        sourceFile.set(file("src/main/grammars/Stan.bnf"))
        targetRootOutputDir.set(file("src/main/gen"))
        pathToParser.set("org/intellij/stan/parser/StanParser.java")
        pathToPsiRoot.set("org/intellij/stan/psi")
        purgeOldFiles.set(true)
    }

    named<GenerateLexerTask>("generateLexer") {
        sourceFile.set(file("src/main/grammars/Stan.flex"))
        targetOutputDir.set(file("src/main/gen/org/intellij/stan/lexer"))
        purgeOldFiles.set(true)
    }

    compileJava {
        dependsOn("generateParser", "generateLexer")
    }

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("251.*")
    }

    buildSearchableOptions {
        enabled = false
    }

    signPlugin {
        certificateChain.set(providers.environmentVariable("CERTIFICATE_CHAIN"))
        privateKey.set(providers.environmentVariable("PRIVATE_KEY"))
        password.set(providers.environmentVariable("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(providers.environmentVariable("PUBLISH_TOKEN"))
    }
}

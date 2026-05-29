plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "org.mc-stan.stan"
version = "1.0.0"

repositories {
    mavenCentral()
}

intellij {
    // Targets IntelliJ IDEA Community, making the plugin compatible with
    // all JetBrains IDEs including PyCharm.
    version.set("2024.1")
    type.set("IC")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("")
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

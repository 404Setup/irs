plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.11"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    paperweight.foliaDevBundle("1.20.1-R0.1-SNAPSHOT")
}
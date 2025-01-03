plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.11"
}

java {
    sourceCompatibility = JavaVersion.toVersion(17)
    targetCompatibility = JavaVersion.toVersion(17)
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    compileOnly(project(":Impl"))
    paperweight.foliaDevBundle("1.20.1-R0.1-SNAPSHOT")
}
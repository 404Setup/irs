plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.11"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    compileOnly(project(":Impl"))
    compileOnly(project(":NMS_1206"))

    paperweight.foliaDevBundle("1.21.4-R0.1-SNAPSHOT")
}
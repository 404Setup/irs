plugins {
    `java-library`
    `maven-publish`
    idea
    signing
}

group = "one.tranic"
version = "1.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
}

val targetJavaVersion = 17

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = Charsets.UTF_8.name()
    options.release = targetJavaVersion
}

tasks.withType<Javadoc> {
    options.encoding = Charsets.UTF_8.name()
}

tasks.withType<ProcessResources> {
    filteringCharset = Charsets.UTF_8.name()
}

val apiAndDocs: Configuration by configurations.creating {
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}

configurations.api {
    extendsFrom(apiAndDocs)
}

publishing {
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("repo"))
        }

        /*maven {
            name = "central"
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            /*credentials(HttpHeaderCredentials::class.java) {
                name = rootProject.extensions.extraProperties.properties["centralAuthHeaderName"] as String
                value = rootProject.extensions.extraProperties.properties["centralAuthHeaderValue"] as String
            }
            authentication {
                val header by registering(HttpHeaderAuthentication::class)
            }*/
        }*/
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = "irs"
            version = project.version.toString()

            pom {
                name = "IRScheduler"
                description = "Provide a unified and fast scheduler tool for Spigot and Folia"
                url = "https://github.com/404Setup/irs"
                inceptionYear.set("2024")
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "404"
                        name = "404Setup"
                        email = "153366651+404Setup@users.noreply.github.com"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/404Setup/irs.git"
                    developerConnection = "scm:git:ssh://github.com/404Setup/irs.git"
                    url = "https://github.com/404Setup/irs"
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
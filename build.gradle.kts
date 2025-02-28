import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.vanniktech.maven.publish.SonatypeHost
import groovy.util.Node
import groovy.util.NodeList

plugins {
    `java-library`
    idea
    signing

    id("com.gradleup.shadow") version "8.3.5"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.11"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "one.tranic"
version = "1.3.3.2"

allprojects {
    apply(plugin = "java")
    apply(plugin = "com.gradleup.shadow")

    version = rootProject.version

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    shadow(project(":Impl", configuration = "shadow"))
    shadow(project(":NMS_1201", configuration = "shadow"))
    shadow(project(":NMS_1206", configuration = "shadow"))
    shadow(project(":NMS_1214", configuration = "shadow"))

    paperweight.foliaDevBundle("1.20.1-R0.1-SNAPSHOT")
}

val targetJavaVersion = 17

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    //withJavadocJar()
    withSourcesJar()
}

tasks.withType<ShadowJar> {
    archiveBaseName.set(project.name)
    archiveClassifier.set("")
}

tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}

tasks.withType<JavaCompile> {
    options.encoding = Charsets.UTF_8.name()
    options.release = targetJavaVersion
}

tasks.withType<Javadoc> {
    enabled = false
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

mavenPublishing {
    coordinates(group as String, "irs", version as String)

    pom {
        name.set("IRScheduler")
        description.set("Provide a unified and fast scheduler tool for Spigot and Folia")
        inceptionYear.set("2024")
        url.set("https://github.com/404Setup/irs")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("404")
                name.set("404Setup")
                url.set("https://github.com/404Setup")
            }
        }
        scm {
            url.set("https://github.com/404Setup/irs")
            connection.set("scm:git:git://github.com/404Setup/irs.git")
            developerConnection.set("scm:git:ssh://git@github.com/404Setup/irs.git")
        }

        withXml {
            val root = asNode()
            val nodes = root["dependencies"] as NodeList
            if (nodes.isNotEmpty()) {
                root.remove(nodes.first() as Node)
            }
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()
}

/*
publishing {
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("repo"))
        }

        maven {
            name = "central"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            /*credentials(HttpHeaderCredentials::class.java) {
                name = rootProject.extensions.extraProperties.properties["centralAuthHeaderName"] as String
                value = rootProject.extensions.extraProperties.properties["centralAuthHeaderValue"] as String
            }
            authentication {
                val header by registering(HttpHeaderAuthentication::class)
            }*/
            credentials {
                username = rootProject.extensions.extraProperties.properties["centralAuthUserName"] as String
                password = rootProject.extensions.extraProperties.properties["centralAuthPasswd"] as String
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
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
}*/

/*signing {
    sign(publishing.publications["mavenJava"])
}*/
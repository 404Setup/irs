# IRScheduler

![Maven Central Version](https://img.shields.io/maven-central/v/one.tranic/irs)

Provide a unified and fast scheduler tool for Spigot and Folia.

## About the Scheduler
Irs is compatible with Folia, but this is not a reason to abuse the scheduler.

There are three schedulers in Folia: `GlobalRegionScheduler`, `AsyncScheduler`, and `EntityScheduler`.

Both `Bukkit.getScheduler()` and `new BukkitRunnable().run()` that were available in Spigot and 
Paper are no longer available.

To learn more, see the Folia [project description](https://github.com/PaperMC/Folia#thread-contexts-for-api).

## Install
Irs are published to a central repository and can be imported without adding additional repositories.

`maven`

```xml
<dependency>
    <groupId>one.tranic</groupId>
    <artifactId>irs</artifactId>
    <version>1.0.0</version>
</dependency>
```

`Gradle (Groovy)`
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'one.tranic:irs:1.0.0'
}
```

`Gradle (Kotlin DSL)`
```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("one.tranic:irs:1.0.0")
}
```

## Shadow Jar Relocation Guide

When using the Irs library in a plugin, it is essential to relocate its dependencies 
to prevent conflicts with other plugins that might also include the same library.
Here is a guide to set up relocation.

### Gradle Shadow Plugin

#### Step 1: Add Shadow Plugin
Add the Shadow plugin to your `build.gradle` file.

`Gradle (Groovy)`:
```groovy
plugins {
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}
```

`Gradle (Kotlin DSL)`:
```kotlin
plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}
```

#### Step 2: Configure Relocation

Configure the Shadow plugin to relocate the `one.tranic.irs` package.
This ensures that the Irs library’s classes do not conflict with other plugins.

`Gradle (Groovy)`:
```groovy
shadowJar {
    relocate 'one.tranic.irs', 'your.plugin.shadow.irs'
}
```

`Gradle (Kotlin DSL)`:
```kotlin
tasks.shadowJar {
    relocate("one.tranic.irs", "your.plugin.shadow.irs")
}
```

#### Step 3: Build the Relocated Jar

Run the Shadow task to build your plugin with the relocated dependencies:
```bash
gradle shadowJar
```

The resulting jar file in the `build/libs` directory will include the relocated Irs library, 
avoiding classloader conflicts.

#### Step 4: Verify Relocation

To ensure that relocation works as intended, decompile the built jar and verify that the 
`one.tranic.irs` package has been renamed to `your.plugin.shadow.irs` or your specified relocation path.

### Maven Shade Plugin

For Maven users, you can use the Maven Shade Plugin to achieve relocation.

#### Step 1: Add Maven Shade Plugin
Include the Maven Shade Plugin in your `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.4.1</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <relocations>
                            <relocation>
                                <pattern>one.tranic.irs</pattern>
                                <shadedPattern>your.plugin.shadow.irs</shadedPattern>
                            </relocation>
                        </relocations>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

#### Step 2: Build the Relocated Jar

Run the Maven package command:
```bash
mvn package
```

The resulting jar file in the `target` directory will include the relocated Irs library.

#### Step 3: Verify Relocation

As with Gradle, decompile the jar and confirm that the `one.tranic.irs` package has been 
relocated to `your.plugin.shadow.irs` or the specified path.

---

By following this guide, you can safely include the Irs library in your project without 
worrying about conflicts with other plugins.
# IRScheduler

[![Maven Central Version](https://img.shields.io/maven-central/v/one.tranic/irs)](https://central.sonatype.com/artifact/one.tranic/irs) 
[![javadoc](https://javadoc.io/badge2/one.tranic/irs/javadoc.svg)](https://javadoc.io/doc/one.tranic/irs)

Use Fluent Interface Design Pattern to provide uniform and fast scheduling tools for Spigot/Folia.

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
    <version>1.3.2</version>
</dependency>
```

`Gradle (Groovy)`
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'one.tranic:irs:1.3.2'
}
```

`Gradle (Kotlin DSL)`
```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("one.tranic:irs:1.3.2")
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
mvn clean install
```

The resulting jar file in the `target` directory will include the relocated Irs library.

#### Step 3: Verify Relocation

As with Gradle, decompile the jar and confirm that the `one.tranic.irs` package has been 
relocated to `your.plugin.shadow.irs` or the specified path.

---

By following this guide, you can safely include the Irs library in your project without 
worrying about conflicts with other plugins.

## Usage
IRS tries to allow developers to use a set of codes to adapt to spigot and folia,
but in fact, developers also need to dispatch tasks to the correct scheduling instead of blind selection.
IRS is not so intelligent.

### Select the scheduler
I need to modify the data near an entity: `RegionScheduler` or `EntityScheduler`

I need to modify the weather: `GlobalRegionScheduler`

I need to get updates for my plugin, or other tasks that do not operate in the world: `AsyncScheduler` or CustomThread

### GlobalRegion Scheduler
```java
PluginSchedulerBuilder.builder(this)
    .sync() // Starting at 1.3, Sync is the default behavior.
    .task(task)
    .run();

// In Spigot/Paper
Bukkit.getScheduler().runTask(this, task);

// In Folia
Bukkit.getGlobalRegionScheduler().run(this, (e)-> task.run());
```

### Entity Scheduler
```Java
PluginSchedulerBuilder.builder(this)
    .sync(entity)
    .sync(player) // Yes, players can also use.
    .task(task)
    .run();

// In Spigot/Paper
Bukkit.getScheduler().runTask(this, task);

// In Folia
entity.getScheduler().run(this, (e) -> task.run(), null);
```

### Region Schduler
```java
PluginSchedulerBuilder.builder(this)
    .sync(entity.getLocation())
    .sync(location) // or
    .task(task)
    .run();

// In Spigot/Paper
Bukkit.getScheduler().runTask(this, task);

// In Folia
Bukkit.getRegionScheduler().run(this, location, (e) -> task.run());
```

### Async Scheduler
```java
PluginSchedulerBuilder.builder(this)
    .async()
    .task(task)
    .run();

// In Spigot/Paper
Bukkit.getScheduler().runTaskAsynchronously(this, task);

// In Folia
Bukkit.getAsyncScheduler().runNow(this, (e) -> task.run());
```
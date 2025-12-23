### setting up a super basic kotlin program

this is insane... jetbrains 2025 docs are super unhelpful here.

what I did:

1. install sdkman

2. `sdk install java`
2. `sdk install kotlin`
2. `sdk install gradle`

3. `gradle init`
3. git commit all the things
3. Manually copy in the following minimal build.gradle.kts:

```
plugins {
  kotlin("jvm") version "2.3.0"
}

group = "org.example" // A company name, for example, `org.jetbrains`
version = "1.0-SNAPSHOT" // Version to assign to the built artifact

repositories { // Sources of dependencies. See 
    mavenCentral() // Maven Central Repository. See 
}

dependencies { // All the libraries you want to use. See 
    // Copy dependencies' names after you find them in a repository
    testImplementation(kotlin("test")) // The Kotlin test library
}

tasks.test { // See 
    useJUnitPlatform() // JUnitPlatform for tests. See 
}
```
3. `./gradlew`
3. `./gradlew build`



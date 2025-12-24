### setting up a super basic kotlin program

this is insane... jetbrains 2025 docs are super unhelpful here.

Slightly more helpful (but took a bit of searching): https://github.com/Kotlin/kmp-native-wizard

---

what I did, starting from scratch:

1. install sdkman

2. `sdk install java`
2. `sdk install kotlin`
2. `sdk install gradle`

3. `gradle init`
3. git commit all the things
3. Manually copy in the following minimal build.gradle.kts:

```
plugins {
  // needed for kotlin files
  kotlin("jvm") version "2.3.0"

  // needed to be able to run the thing
  application
}

repositories {
    // needed for gradle to find its own deps
    mavenCentral()
}

application {
    // needed to tell java where our main is
    mainClass.set("MainKt")
}
```

3. `./gradlew`
3. `./gradlew build`

4. Create the file src/main/kotlin/Main.kt
4. Put your hello world into it
4. run


## to add a dep manually

1. add it to gradle/libs.versions.toml under versions
1. add it to gradle/libs.versions.toml under libraries, referencing the version
1. add it to sourceSets in build.gradle.kts
1. if it fails because its not in maven, cri

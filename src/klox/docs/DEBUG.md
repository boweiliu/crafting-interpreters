# how to setup native kotlin with gdb debugging

ugh also super annoying. the trick is to go to https://github.com/Kotlin/kmp-native-wizard and fetch out:

1. build.gradle.kts
2. gradle/libs.versions.toml
3. src/nativeMain/kotlin/Main.kt
4. settings.gradle.kts // not sure if i needed to update this but seemed relevant
5. change from `gradlew run` to `gradlew runDebugExecutableNative`

and also disable all the jvm-targeting lines in your build.gradle.kts including `application`

### gdb

find the exe and target it. mine was in 
```
gdb ./build/bin/native/debugExecutable/KotlinNativeTemplate.kexe
```

then do stuff like

```
(gdb) info functions main
(gdb) break Main.kt:2
(gdb) r
```


### tests

very tricky... 

trying  `./gradlew linkDebugTestNative` and then manually running?? the other approach using `gradlew nativeTest` doesnt have line numbers or debug.



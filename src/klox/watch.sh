#!/bin/bash


# ./gradlew linkDebugExecutableNative  && ./build/bin/native/debugExecutable/KotlinNativeTemplate.kexe hello.lox ; echo $?
# ./gradlew linkDebugTestNative && ./build/bin/native/debugTest/test.kexe

find src -name '*.kt' -type f | entr -d -s './gradlew linkDebugTestNative && ./build/bin/native/debugTest/test.kexe ; echo $?'

#!/bin/bash


# ./gradlew linkDebugExecutableNative  && ./build/bin/native/debugExecutable/KotlinNativeTemplate.kexe hello.lox ; echo $?
# ./gradlew linkDebugTestNative && ./build/bin/native/debugTest/test.kexe

# echo ./build/bin/native/debugTest/test.kexe ; echo $?
# exit 0

find src -name '*.kt' -type f | entr -d -s './gradlew linkDebugTestNative && ./build/bin/native/debugTest/test.kexe --ktest_regex_filter='\'".*$1.*"\'' ; echo $?'
#find src -name '*.kt' -type f | entr -d -s './gradlew linkDebugTestNative && ./build/bin/native/debugTest/test.kexe  ; echo $?'

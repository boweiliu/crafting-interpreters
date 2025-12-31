#!/bin/bash


# ./gradlew linkDebugExecutableNative  && ./build/bin/native/debugExecutable/KotlinNativeTemplate.kexe hello.lox ; echo $?
# ./gradlew linkDebugTestNative && ./build/bin/native/debugTest/test.kexe

# echo ./build/bin/native/debugTest/test.kexe ; echo $?
# exit 0

set -o pipefail

find src -name '*.kt' -type f | entr -d -s './gradlew linkDebugTestNative && ./build/bin/native/debugTest/test.kexe --ktest_regex_filter='\'".*$1.*"\''  2>&1 | tee /tmp/test.out ; echo $? ; cat /tmp/test.out | grep "RUN" -A8  ; rm -f /tmp/test.out '
#find src -name '*.kt' -type f | entr -d -s './gradlew linkDebugTestNative && ./build/bin/native/debugTest/test.kexe  ; echo $?'

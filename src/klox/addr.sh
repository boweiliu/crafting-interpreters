#!/bin/bash

echo -en "$@ " ; addr2line -C -e ./build/bin/native/debugTest/test.kexe "$@"

# echo '0x27df4c,0x281998,0x2824df,0x3bce88,0x2d51ef' | tr ',' '\n' | xargs -I{} bash addr.sh {}

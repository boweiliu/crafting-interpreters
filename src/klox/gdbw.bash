#!/bin/bash

# usage: ./gdbw [binary]

BINARY="$1"

# first, find which breakpoint we want to add

FNAME='kfun:#main'
ADDR="$(gdb -batch -ex "info address ${FNAME}(){}" "$1" | grep 'at address' | sed 's/.*at address//g' | awk '{ print $1 }' | sed 's/\..*$//g')"


cat - <<EOF > ./.gdb_setup.txt
# break main
define qquit
  set confirm off
  quit
end
document qquit
quit now
end
break *$ADDR
run
EOF

gdb -x ./.gdb_setup.txt "$1"



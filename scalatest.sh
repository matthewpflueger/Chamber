#!/bin/bash

ARGS="$*"

if [[ "$1" == "" ]]; then
    ARGS="-oNDXEHLO"
fi

#See http://www.scalatest.org/user_guide/using_the_runner for command line options 
#Add -DdisplayCmd=true to see command used...
exec mvn scala:run -Dlauncher=scalatest -DaddArgs="$ARGS"





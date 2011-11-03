#!/bin/bash

CONTEXT=src/main/webapp/WEB-INF/jetty.xml
TARGET=target/chamber-0.1-SNAPSHOT-allinone.jar
MAIN=com.echoed.chamber.Main


PACKAGE="mvn -DskipTests -Pallinone package"
CLASSPATH=".:`ls -1 ${TARGET}`"
OVERRIDES="src/overrides/resources"
ARGS="-server -Xms1024m -Xmx2048m -XX:PermSize=256m -Djava.net.preferIPv4Stack=true"

if [[ "$1" == "-o" ]]; then
    CLASSPATH="${OVERRIDES}:${CLASSPATH}"
    shift
fi

if [[ "$1" == "-d" ]]; then
    ARGS="$ARGS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000"
    shift
fi

if [ ! -e ${TARGET} ]; then
    echo "Missing ${TARGET}, running ${PACKAGE}"
    $PACKAGE
    result=$?
    if [[ $result > 0 ]]; then
        exit 1;
    fi
fi

if [ ! ${1} ]; then
    echo "Using default Spring context configuration ${CONTEXT}"
else
    CONTEXT=${1}
fi
 
# We set the classpath and specify the main class to allow for override.properties
# We also use exec here to replace the running shell with our java process
#exec java -Djgroups.tcp.address=`hostname` -cp .:src/overrides/resources:`ls -1 ${TARGET}` ${MAIN} ${CONTEXT}

# We do this to capture the pid of the process
sh -c "java ${ARGS} -cp ${CLASSPATH} ${MAIN} ${CONTEXT} >./std.out 2>&1 & APID=\"\$!\"; echo \$APID > chamber.pid"





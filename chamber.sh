#!/bin/bash

NAME=Chamber
DESC=Chamber

case "$1" in
    start)
        echo "Starting $DESC"
        shift

        CONTEXT=src/main/resources/jetty.xml
        TARGET=target/chamber-0.1-SNAPSHOT-allinone.jar
        MAIN=com.echoed.chamber.Main

        PACKAGE="mvn -DskipTests -Pallinone package"
        CLASSPATH=".:${TARGET}"
        OVERRIDES="src/overrides/resources"
        ARGS_INTERESTING="-XX:+CMSPermGenSweepingEnabled -XX:+CMSClassUnloadingEnabled"
        ARGS="-server -Xms1024m -Xmx2048m -XX:PermSize=256m  -Djava.net.preferIPv4Stack=true -Dsun.net.client.defaultConnectTimeout=5000 -Dsun.net.client.defaultReadTimeout=5000"


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
 
        # We do this to capture the pid of the process
        sh -c "java ${ARGS} -cp ${CLASSPATH} ${MAIN} ${CONTEXT} >./std.out 2>&1 & APID=\"\$!\"; echo \$APID > chamber.pid"

        tail -f std.out
        ;;

    stop)
        shift
        echo "Stopping $DESC"
        kill $1 `cat chamber.pid`
        ;;

    status)
        ps aux | grep `cat chamber.pid`
        ;;

    verify)
        shift
        echo "Running integration tests for $DESC"
        displaycmd=""
        if [[ "$1" == "-d" ]]; then
            displyacmd="-DdisplayCmd=true"
            shift
        fi

        mvn $displaycmd -Pitest verify
        ;;

    scalatest)
        shift
        display=""
        if [[ "$1" == "" ]]; then
            display="-eNDXEHLO"
        fi

        #See http://www.scalatest.org/user_guide/using_the_runner for command line options 
        #Add -DdisplayCmd=true to see command used...
        mvn scala:run -Dlauncher=scalatest -DaddArgs="$display"
        ;;

    console)
        rlwrap mvn scala:console
        ;;

    *)
        echo "Usage: $NAME {start|stop|status|verify|scalatest|console}" >&2
        exit 1
        ;;
esac

exit 0







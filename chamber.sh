#!/bin/bash

NAME=chamber
DESC=Chamber


if [[ ! -e "$JAVA_HOME/bin/java" ]]; then
    JAVA_HOME=/usr/local/lib/java
    echo "Setting JAVA_HOME=${JAVA_HOME}"
fi

function service_cmd() {

local service_args=($*)

case $service_args in
    clean)
        CLEAN="mvn -DskipTests clean"
        echo "Running ${CLEAN}"
        $CLEAN
        result=$?
        if [[ $result > 0 ]]; then
            exit 1;
        fi
        ;;

    package)
        PACKAGE="mvn -DskipTests -Pallinone package"
        echo "Running ${PACKAGE}"
        $PACKAGE
        result=$?
        if [[ $result > 0 ]]; then
            exit 1;
        fi
        ;;

    start)
        echo "Starting $DESC"
        shift

        ENV_TYPE=dev
        CONTEXT=src/main/resources/jetty.xml
        TARGET=target/chamber-0.1-SNAPSHOT-allinone.jar
        MAIN=com.echoed.chamber.Main

        NEWRELIC=/usr/local/lib/newrelic/newrelic.jar

        CLASSPATH=".:${TARGET}"
        OVERRIDES="src/overrides/resources"
        ARGS_INTERESTING="-XX:+CMSPermGenSweepingEnabled -XX:+CMSClassUnloadingEnabled -XX:+UseCompressedOops"
        ARGS="-server -Xms1024m -Xmx2048m -XX:PermSize=256m  -Djava.net.preferIPv4Stack=true -Dsun.net.client.defaultConnectTimeout=5000 -Dsun.net.client.defaultReadTimeout=5000"


        if [[ "$1" == "-o" ]]; then
            CLASSPATH="${OVERRIDES}:${CLASSPATH}"
            shift
        fi

        if [[ "$1" == "-d" ]]; then
            ARGS="$ARGS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000"
            shift
        fi

        if [ -e ${NEWRELIC} ]; then
            ARGS="-javaagent:${NEWRELIC} $ARGS"
        fi

        if [ ! -e ${TARGET} ]; then
            echo "Missing ${TARGET}"
            service_cmd "package"
        fi

        if [ ! ${1} ]; then
            echo "Using default Spring context configuration ${CONTEXT}"
        else
            CONTEXT=${1}
        fi

        [ -r /etc/default/$NAME ] && . /etc/default/$NAME
        #if [ -e /etc/default/$NAME ]; then 
        #    source /etc/default/$NAME
        #    echo "ENV_TYPE=${ENV_TYPE}"
        #fi

        echo "java ${ARGS} -DENV_TYPE=${ENV_TYPE} -cp ${CLASSPATH} ${MAIN} ${CONTEXT}"


        # We do this to capture the pid of the process
        sh -c "java ${ARGS} -DENV_TYPE=${ENV_TYPE} -cp ${CLASSPATH} ${MAIN} ${CONTEXT} >./std.out 2>&1 & APID=\"\$!\"; echo \$APID > chamber.pid"
        ;;

    startt)
        service_cmd "start"
        tail -f std.out
        ;;

    stop)
        shift
        echo "Stopping $DESC"
        kill -9 `cat chamber.pid`
        ;;

    restart)
        service_cmd "stop"
        service_cmd "start"
        ;;

    reload)
        service_cmd "dump"
        service_cmd "clean"
        service_cmd "package"
        service_cmd "restart"
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

    targz)
        rm chamber.tar.gz
        rm target/chamber-0.1-SNAPSHOT-allinone.jar
        mvn -DskipTests -Pallinone clean package
        tar -cvzf chamber.tar.gz --exclude chamber.pid --exclude chamber.iml --exclude std.out --exclude out --exclude-vcs --exclude-backups *
        ;;

    install)
        sudo ln -s /usr/local/lib/chamber/src/main/webapp current
	    cd /usr/local/lib/chamber

	    sudo apt-get install libssl0.9.8 
	    sudo apt-get install lynx
	    sudo apt-get install curl
	    sudo apt-get install git

        sudo apt-get install nginx
        sudo rm -f /etc/nginx/sites-enabled/default
        sudo rm -f /etc/nginx/nginx.conf
        sudo cp src/main/ops/etc/nginx/sites-enabled/default /etc/nginx/sites-enabled/default
        sudo cp src/main/ops/etc/nginx/nginx.conf /etc/nginx/nginx.conf
        sudo service nginx restart

        sudo apt-get install mysql-server
        sudo rm -f /etc/mysql/my.cnf
        sudo cp src/main/ops/etc/mysql/my.cnf /etc/mysql/my.cnf
        sudo service mysql restart

        sudo apt-get install python-pip
        sudo pip install pagerduty
        #sudo cp src/main/ops/usr/local/bin/pagerduty* /usr/local/bin/.

        sudo apt-get install monit
        sudo rm -f /etc/default/monit
        sudo rm -f /etc/monit/monitrc
        sudo cp src/main/ops/etc/default/monit /etc/default/monit
        sudo cp src/main/ops/etc/monit/monitrc /etc/monit/monitrc
        sudo cp src/main/ops/etc/monit/conf.d/* /etc/monit/conf.d/.
        sudo service monit restart

        sudo cp src/main/ops/etc/cloudkick.conf /etc/cloudkick.conf
	    sudo rm -f /etc/apt/sources.list.d/cloudkick.list
	    sudo cp src/main/ops/etc/apt/sources.list.d/cloudkick.list /etc/apt/sources.list.d/cloudkick.list
        sudo curl http://packages.cloudkick.com/cloudkick.packages.key >> cloudkick.packages.key
        sudo apt-key add cloudkick.packages.key
        sudo apt-get update
        sudo apt-get install cloudkick-agent
        
        sudo mkdir -p /usr/local/lib/newrelic/logs
        sudo chmod -R ugo+rwx /usr/local/lib/newrelic
        #sudo cp src/main/ops/opt/newrelic/newrelic* /opt/newrelic/.
        ;;

    status)
        ps uh -p `cat chamber.pid`
        ;;

    compass)
        compass watch -c src/main/compass/${ENV_TYPE}.rb
        ;;

    compass_compile)
        compass compile -f -c src/main/compass/${ENV_TYPE}.rb
        ;;

    migrate_status)
        PACKAGE="mvn migration:status -Dmigration.path=src/main/database"
        echo "Running ${PACKAGE}"
        $PACKAGE
        result=$?
        if [[ $result > 0 ]]; then
            exit 1;
        fi
        ;;

    migrate_up)
        PACKAGE="mvn migration:up -Dmigration.path=src/main/database"
        echo "Running ${PACKAGE}"
        $PACKAGE
        result=$?
        if [[ $result > 0 ]]; then
            exit 1;
        fi
        ;;

    migrate_down)
        PACKAGE="mvn migration:down -Dmigration.path=src/main/database"
        echo "Running ${PACKAGE}"
        $PACKAGE
        result=$?
        if [[ $result > 0 ]]; then
            exit 1;
        fi
        ;;

    dump)
        DUMP="mysqldump -u root -ppassword --database Echoed"
        echo "Running $DUMP"
        $DUMP > dump-`date +%Y%m%d%H%M%S`.sql
        result=$?
        if [[ $result > 0 ]]; then
            exit 1;
        fi
        ;;

    *)
        echo "Usage: $NAME {start|stop|restart|reload|status|verify|scalatest|console|targz|package|clean|compass|compass_compile|migrate_status|migrate_up|migrate_down|dump}" >&2
        exit 1
        ;;
esac
}

CMD=$*
service_cmd ${CMD[*]}

exit $?








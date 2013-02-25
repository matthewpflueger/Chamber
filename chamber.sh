#!/bin/bash

NAME=chamber
DESC=Chamber

SCRIPT=`readlink -f $0`
BASEDIR=`dirname $SCRIPT`

if [[ ! -e "$JAVA_HOME/bin/java" ]]; then
    JAVA_HOME=/usr/local/lib/java
    echo "Setting JAVA_HOME=${JAVA_HOME}"
fi

if [[ "${ENV_TYPE}" == "" ]]; then
    ENV_TYPE=dev
fi

CONTEXT=src/main/resources/jetty.xml
TARGET=target/chamber-0.1-SNAPSHOT-allinone.jar
MAIN=com.echoed.chamber.Main

NEWRELIC=${BASEDIR}/src/main/ops/opt/newrelic/newrelic.jar
MVN="mvn -Dmaven.test.skip=true -Dsun.net.client.defaultConnectTimeout=1000 -Dsun.net.client.defaultReadTimeout=1000"
MIN_MEM="1024m"
MAX_MEM="2564m"
PERM_SIZE="192m"
CONNECT_TIMEOUT="10000"
READ_TIMEOUT="10000"
CLASSPATH=".:${TARGET}"
OVERRIDES="src/overrides/resources"
ARGS_INTERESTING="-XX:+CMSPermGenSweepingEnabled -XX:+CMSClassUnloadingEnabled -XX:+UseCompressedOops"

DATABASE=Echoed
DATABASE_USER=root
DATABASE_PASSWORD=password

# Load the system environment variable overrides
[ -r /etc/default/$NAME ] && . /etc/default/$NAME
[ -r $BASEDIR/$NAME ] && . $BASEDIR/$NAME


function run_cmd() {
    local run_cmd_args=($*)
    echo "Running ${run_cmd_args[*]}"
    ${run_cmd_args[*]}
    result=$?
    if [[ $result > 0 ]]; then
        exit 1;
    fi
}

function service_cmd() {

    local service_args=($*)

    case $service_args in
        clean)
            rm -Rf out
            run_cmd "${MVN} clean"
            ;;

        compile)
            run_cmd "${MVN} compile"
            ;;

        package)
            run_cmd "${MVN} -Pallinone package"
            ;;

        start)
            echo "Starting $DESC"
            shift


            ARGS="-server -Xms${MIN_MEM} -Xmx${MAX_MEM} -XX:PermSize=${PERM_SIZE} -XX:OnError=\"./chamber.sh restart\" -XX:OnOutOfMemoryError=\"./chamber.sh restart\" -XX:+HeapDumpOnOutOfMemoryError -Djava.net.preferIPv4Stack=true -Dsun.net.client.defaultConnectTimeout=${CONNECT_TIMEOUT} -Dsun.net.client.defaultReadTimeout=${READ_TIMEOUT}"


            if [[ "$1" == "-o" ]]; then
                CLASSPATH="${OVERRIDES}:${CLASSPATH}"
                shift
            fi

            if [[ "$1" == "-d" ]]; then
                ARGS="$ARGS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8085"
                shift
            fi

            if [ -e ${NEWRELIC} ]; then
                ARGS="-javaagent:${NEWRELIC} -Dnewrelic.environment=${ENV_TYPE} $ARGS"
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
            #service_cmd "git_rev"
            #service_cmd "compass_compile"
            #service_cmd "requirejs"
            #service_cmd "migrate_up"
            #service_cmd "compile"
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
            ${MVN} -Pallinone clean package
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

        git_rev)
            run_cmd "${MVN} pl.project13.maven:git-commit-id-plugin:revision"
            ;;

        compass)
            compass watch -c src/main/compass/${ENV_TYPE}.rb
            ;;

        compass_compile)
            compass compile --force -c src/main/compass/${ENV_TYPE}.rb
            ;;

        requirejs)
            cd src/main/webapp/scripts && r.js -o app.build.js && cd ../../../
            ;;     

        migrate_status)
            run_cmd "${MVN} migration:status -Dmigration.env=${ENV_TYPE} -Dmigration.path=src/main/database"
            ;;

        migrate_up)
            run_cmd "${MVN} migration:up -Dmigration.env=${ENV_TYPE} -Dmigration.path=src/main/database"
            ;;

        migrate_down)
            run_cmd "${MVN} migration:down -Dmigration.env=${ENV_TYPE} -Dmigration.path=src/main/database"
            ;;

        migrate_new)
            run_cmd "${MVN} migration:new -Dmigration.env=${ENV_TYPE} -Dmigration.path=src/main/database -Dmigration.description=${service_args[*]:1}"
            ;;

        dump)
            DATE=`date +%Y%m%d%H%M%S`
            HOST=`hostname`
            DUMP="mysqldump -u ${DATABASE_USER} -p${DATABASE_PASSWORD} --database ${DATABASE}"
            echo "Running $DUMP | gzip > dump-${HOST}-${DATABASE}-${DATE}.sql.gz"
            $DUMP | gzip > dump-${HOST}-${DATABASE}-${DATE}.sql.gz
             
            result=$?
            if [[ $result > 0 ]]; then
                exit 1;
            fi
            ;;

        cloud_clean)
            echo
            echo "Enter the following on the command line:"
            echo
            #echo "mvn -Dclean.dryRun=true -Dclean.deleteVersion=xxxxxx -Dclean.verbose=true com.echoed:cloud-maven-plugin:clean"
            echo "sh -c \"mvn -e -X -Dclean.dryRun=false -Dclean.verbose=true com.echoed:cloud-maven-plugin:clean >./clean.log 2>&1 & APID=\\\"\\\$!\\\"; echo \\\$APID > clean.pid\""
            echo
            ;;

        *)
            echo "Usage: $NAME {start|stop|restart|reload|status|verify|scalatest|console|targz|package|clean|git_rev|compass|compass_compile|requirejs|migrate_status|migrate_up|migrate_down|dump|cloud_clean}" >&2
            exit 1
            ;;
    esac
}

CMD=$*
service_cmd ${CMD[*]}

exit $?








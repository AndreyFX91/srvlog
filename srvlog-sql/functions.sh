#!/bin/bash

export SRVLOG_DB_USERNAME=${SRVLOG_DB_USERNAME:-srvlog}
export SRVLOG_DB_PASSWORD=${SRVLOG_DB_PASSWORD:-123srvlog123}
export SRVLOG_DB_DATABASE=${SRVLOG_DB_DATABASE:-srvlog}

export SRVLOG_DB_ROOT_PASSWORD=${SRVLOG_DB_ROOT_PASSWORD:-11111}

export SRVLOG_DB_HOST=${SRVLOG_DB_HOST:-localhost}
export SRVLOG_DB_PORT=${SRVLOG_DB_PORT:-3306}

export SRVLOG_DB_SOCK=${SRVLOG_DB_SOCK:-/var/run/mysqld/mysqld.sock}

export PATH_TO_MYSQL=${PATH_TO_MYSQL:-mysql}


if [[ "$OSTYPE" == "linux-gnu" ]]; then
        OS_NAME="linux" 
elif [[ "$OSTYPE" == "darwin"* ]]; then
        OS_NAME="macosx"
elif [[ "$OSTYPE" == "cygwin" ]]; then
       # POSIX compatibility layer and Linux environment emulation for Windows
        OS_NAME="Windows"
elif [[ "$OSTYPE" == "msys" ]]; then
        # Lightweight shell and GNU utilities compiled for Windows (part of MinGW)
        OS_NAME="Windows"
else
        # Unknown.
        OS_NAME="Windows" 
fi


#SYSTEM=`uname`


#case "$SYSTEM" in
#  CYGWIN*) OS_NAME="Cygwin" ;;
#  Darwin*) OS_NAME="macosx"  ;;
#  Linux*)  OS_NAME="linux"   ;;
#esac


fn_exists() {
    declare -f -F $1 > /dev/null
    return $?
}

logInfo() {
    # or tts -s

    #test -t 0 && tput setaf 2 # green
    echo $1
    #test -t 0 && tput sgr0
    [ "$OS_NAME" = "Windows" ] && echo "$USER INFO: $1" || logger "$USER INFO: $1"
}

logError() {
    #test -t 0 && tput setaf 1 # red
    echo $1
    #test -t 0 && tput sgr0
    [ "$OS_NAME" = "Windows" ] && echo "$USER ERROR: $1" || logger "$USER ERROR: $1"
}

logWarn() {
    #test -t 0 && tput setaf 3 # yellow
    echo $1
    #test -t 0 && tput sgr0
    [ "$OS_NAME" = "Windows" ] && echo "$USER WARN: $1" || logger "$USER WARN: $1"
}

die() {
    errorCode=$?
    errorMessage=$1

    if [ $errorCode != 0 ]
    then
        logError ".ERROR.: $errorCode - $errorMessage"
        test -t 0 || exit 1
    fi

    return $errorCode
}


runScript() {

#    die 'Oooops...' # to react on previous fail (like DDL script before DML script)

    aScript=$1

    mkdir -p target
    logInfo "Installing to $SRVLOG_DB_USERNAME@$SRVLOG_DB_HOST:$SRVLOG_DB_PORT/$SRVLOG_DB_DATABASE $aScript ..."
    cat $aScript | sed s/srvlog/$SRVLOG_DB_DATABASE/g | \
        $PATH_TO_MYSQL --default-character-set=utf8 --protocol=TCP -h $SRVLOG_DB_HOST --port $SRVLOG_DB_PORT -b -vv -u $SRVLOG_DB_USERNAME -p$SRVLOG_DB_PASSWORD $SRVLOG_DB_DATABASE --show-warnings --comments > target/$aScript.log

    die "can not process $aScript"

    return $?

}

runScriptRoot() {

    aScript=$1

    mkdir -p target
    logWarn "Installing to root@$SRVLOG_DB_HOST:$SRVLOG_DB_PORT $aScript ..."
    cat $aScript | sed s/srvlog/$SRVLOG_DB_DATABASE/g | \
        $PATH_TO_MYSQL --default-character-set=utf8 --protocol=TCP -h $SRVLOG_DB_HOST --port $SRVLOG_DB_PORT -b -vv -uroot -p$SRVLOG_DB_ROOT_PASSWORD --show-warnings --comments > target/$aScript.log

    die "can not process $aScript"

    return $?

}



#!/bin/bash

PRJ="$(cd `dirname $0` && pwd)"

nc=${JAVA_HOME:=/opt/jdk21}
export JAVA_HOME
export PATH=$JAVA_HOME/bin:$PATH

nc=${MAVEN_HOME:=/opt/apache-maven-3.9.9}
export MAVEN_HOME

if [ -t 1 ]; then 
    BatchMode=""; 
else
    BatchMode="--batch-mode "
fi

nc=${MVN_OPTIONS:=${BatchMode}--errors --update-snapshots --fail-at-end}
nc=${MVN_GOAL:=clean install}

nc=${MAVEN_OPTS:=-Xmx512m -Xms512m}
export MAVEN_OPTS

MvnCmd="${MAVEN_HOME}/bin/mvn"

if [ ! -z "${WORKSPACE}" ]; then
    MvnRepo="${PRJ}/.repository"
    mkdir -p "${MvnRepo}"
    MvnCmd="$MvnCmd -Dmaven.repo.local=${MvnRepo}"
else
    cd ${PRJ}
    export WORKSPACE=${PRJ}
fi

echo "(export MVN_OPTIONS=\"${MVN_OPTIONS}\"; export MVN_GOAL=\"${MVN_GOAL}\"; ${PRJ}/build.sh)"

set -x
${MvnCmd} ${MVN_OPTIONS} ${MVN_GOAL} $@
${MvnCmd} ${MVN_OPTIONS} -f gui javafx:jlink

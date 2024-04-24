#!/bin/bash

PRJ="$(cd `dirname $0` && pwd)"

cd $PRJ

# mvn -X -Dorg.slf4j.simpleLogger.showLogName=true compile exec:exec@runApp
mvn exec:exec@runApp $@

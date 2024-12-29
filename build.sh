#!/bin/bash

PRJ="$(cd `dirname $0` && pwd)"

cd $PRJ

mvn clean install $@

mvn -f gui javafx:jlink

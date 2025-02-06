#!/bin/bash

PRJ="$(cd `dirname $0` && pwd)"

cd $PRJ

# https://stackoverflow.com/questions/53215038/how-to-log-request-response-using-java-net-http-httpclient/53231046#53231046
#    -Djdk.httpclient.HttpClient.log=requests,errors,headers,frames[:control:data:window:all],content,ssl,trace,channel,all \
# https://docs.oracle.com/en/java/javase/22/docs/api/java.net.http/module-summary.html

props="-Djdk.httpclient.HttpClient.log=requests"
props=""

export FORPAN_HOME=~/.forpan-dev

set -x

gui/target/forpan/bin/java ${props} \
    -m org.springdot.forpan.gui/org.springdot.forpan.gui.App \
    "$@"

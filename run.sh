#!/bin/bash

PRJ="$(cd `dirname $0` && pwd)"

cd $PRJ

# gui/target/forpan/bin/forpan $@

# https://stackoverflow.com/questions/53215038/how-to-log-request-response-using-java-net-http-httpclient/53231046#53231046
#    -Djdk.httpclient.HttpClient.log=requests,errors,headers,frames[:control:data:window:all],content,ssl,trace,channel,all \

set -x

gui/target/forpan/bin/java \
    -Djdk.httpclient.HttpClient.log=requests \
    -m org.springdot.forpan.gui/org.springdot.forpan.gui.App \
    "$@"

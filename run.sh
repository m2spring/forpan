#!/bin/bash

PRJ="$(cd `dirname $0` && pwd)"

cd $PRJ

gui/target/forpan/bin/forpan $@

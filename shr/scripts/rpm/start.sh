#!/bin/sh
nohup java -Dserver.port=$BDSHR_PORT -DSHR_LOG_LEVEL=$SHR_LOG_LEVEL -jar /opt/bdshr/lib/shr.war > /var/log/bdshr/bdshr.log &
echo $! > /var/run/bdshr/bdshr.pid

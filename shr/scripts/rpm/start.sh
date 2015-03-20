#!/bin/sh
nohup java -Dserver.port=$BDSHR_PORT -DSHR_LOG_LEVEL=$SHR_LOG_LEVEL -jar /opt/bdshr/lib/shr.war > /dev/null 2>&1  &
echo $! > /var/run/bdshr/bdshr.pid

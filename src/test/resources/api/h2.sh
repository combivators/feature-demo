#!/bin/sh
if [ $# -ne 1 ]; then
  echo "Usage: h2.sh [start|stop|status]"
  exit 1
fi
ver=1.4.200
h2lib=/opt/demo/lib/h2-$ver.jar
H2_START_OPTS="-web -webAllowOthers -tcp -tcpPort 9092 -tcpAllowOthers -tcpPassword password -ifNotExists -baseDir /opt/demo/data"
H2_STOP_OPTS="-tcpShutdown tcp://localhost:9092 -tcpPassword password"
JAVA8_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1"
JAVA11_OPTS="-XX:+UnlockExperimentalVMOptions"
JAVA_OPTS=$JAVA11_OPTS
stat=`ss -tln | grep ":9092" | wc -l`
if [ $1 = "start" ]
then
  if [ $stat = "0" ]; then
    java $JAVA_OPTS -cp $h2lib org.h2.tools.Server $H2_START_OPTS &
    exit 0
  else
    echo "H2 database is running"
    exit 1
  fi
fi
if [ $1 = "stop" ]
then
  if [ $stat != "0" ]; then
    java $JAVA_OPTS -cp $h2lib org.h2.tools.Server $H2_STOP_OPTS
    exit 0
  else
    echo "H2 database was stoped"
    exit 1
  fi
fi
if [ $1 = "status" ]
then
  if [ $stat = "0" ]; then
    echo "H2 database stoped"
    exit 0
  else
    echo "H2 database is running"
    exit 0
  fi
fi
echo "Unknow option '$1'"
echo "Usage: h2.sh [start|stop|status]"
exit 1
#!/bin/sh
if [ $# -ne 1 ]; then
  echo "Usage: demo.sh [start|stop|status]"
  exit 1
fi
JAVA8_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1"
JAVA11_OPTS="-XX:+UnlockExperimentalVMOptions"
JAVA_OPTS=$JAVA8_OPTS
lib=/opt/demo/lib
for i in $lib/*;
do
  jar=$i
  if [[ -z "${CLASSPATH}" ]]; then
    CLASSPATH=$jar
  else
    CLASSPATH=$CLASSPATH:$jar
  fi
done
stat=`ss -tln | grep ":8080" | wc -l`
if [ $1 = "start" ]
then
  if [ $stat = "0" ]; then
    java $JAVA_OPTS -cp $CLASSPATH net.tiny.boot.Main -v -p demo &
    exit 0
  else
    echo "Demo server is running"
    exit 1
  fi
fi
if [ $1 = "stop" ]
then
  if [ $stat != "0" ]; then
    curl -u paas:password http://localhost:8080/sys/stop
    exit 0
  else
    echo "Demo server was stoped"
    exit 1
  fi
fi
if [ $1 = "status" ]
then
  if [ $stat = "0" ]; then
    echo "Demo server stoped"
    exit 0
  else
    echo "Demo server is running"
    exit 0
  fi
fi
echo "Unknow option '$1'"
echo "Usage: demo.sh [start|stop|status]"
exit 1

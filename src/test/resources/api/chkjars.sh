#!/bin/sh
lib=/opt/demo/lib
for i in $lib/*;
do
  echo $i
  jar tvf $i | grep PersistenceUtil
done

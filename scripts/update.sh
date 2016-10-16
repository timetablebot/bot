#!/bin/bash
if [ $1 == "r" ] ; then
  systemctl stop timetablebot
fi 

mv TimetableBot-1.0-SNAPSHOT.jar TimetableBot.jar
chown timetablebot:timetablebot TimetableBot.jar
chown -R timetablebot:timetablebot ./libraries

if [ $1 == "r" ] ; then
  systemctl start timetablebot
fi

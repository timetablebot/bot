# https://docs.docker.com/engine/reference/builder/

# https://hub.docker.com/r/adoptopenjdk/openjdk11-openj9
FROM adoptopenjdk/openjdk11-openj9:alpine-jre

COPY target/dependency /timetablebot/libraries
COPY target/TimetableBot-1.0-SNAPSHOT.jar /timetablebot/bot.jar

VOLUME /timetablebot/config

WORKDIR /timetablebot/

ENTRYPOINT ["java", "-jar", "bot.jar"]

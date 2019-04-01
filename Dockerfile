FROM openjdk:latest

# https://stackify.com/guide-docker-java/
# https://docs.docker.com/engine/reference/builder/#from

# WORKDIR /timetablebot/

COPY target/dependency /timetablebot/libraries
COPY target/TimetableBot-1.0-SNAPSHOT.jar /timetablebot/bot.jar

# RUN mkdir /timetablebot/config
VOLUME /timetablebot/config

WORKDIR /timetablebot/

ENTRYPOINT ["/usr/bin/java", "-jar", "bot.jar"]

# CMD ["/usr/bin/java", "-jar", "bot.jar"]
# ENTRYPOINT ["/bin/bash"]

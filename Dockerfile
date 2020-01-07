# https://docs.docker.com/engine/reference/builder/
# https://docs.docker.com/develop/develop-images/multistage-build/

# https://hub.docker.com/_/maven
# Using a base image, so we don't have to download the maven libraries multiple times
FROM maven:3-jdk-13-alpine as mavenBase
WORKDIR /build/timetablebot/

COPY pom.xml .
# Downloading all maven plugins and dependencies
RUN mvn dependency:go-offline
# Building the application to download all maven core libraries
RUN mvn package

# https://hub.docker.com/_/maven
FROM maven:3-jdk-13-alpine as maven
WORKDIR /build/timetablebot/

COPY --from=mavenBase /root/.m2 /root/.m2
COPY src ./src/
COPY pom.xml .

RUN mvn package
RUN mvn dependency:copy-dependencies -DincludeScope=runtime -DexcludeGroupIds=org.glassfish

# https://hub.docker.com/r/adoptopenjdk/openjdk11-openj9
FROM adoptopenjdk/openjdk11-openj9:alpine-jre

COPY --from=maven /build/timetablebot/target/dependency /timetablebot/libraries
COPY --from=maven /build/timetablebot/target/TimetableBot*.jar /timetablebot/bot.jar

VOLUME /timetablebot/config

WORKDIR /timetablebot/

ENTRYPOINT ["java", "-jar", "bot.jar"]

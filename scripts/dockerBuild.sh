#!/bin/bash

cd $(dirname $BASH_SOURCE)
cd ..

if [ -x "$(command -v mvn)" ]; then
    # Creating the Jar
    mvn package
    # Copying the dependencies
    mvn dependency:copy-dependencies -DincludeScope=runtime -DexcludeGroupIds=org.glassfish
else
    echo "No mvn command found. Skipping maven build..."
fi

# Building the image
docker build -t timetablebot:jdk11 .
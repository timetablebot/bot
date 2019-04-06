#!/bin/bash

cd $(dirname $BASH_SOURCE)
cd ..

# Building the image
docker build -t timetablebot:jdk11 .
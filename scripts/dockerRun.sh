#!/bin/bash
cd $(dirname $BASH_SOURCE)
cd ..

# Run after dockerBuild.sh

CONFIG_DIR=$(pwd)/dockercfg/

# Running the built image
docker run \
	-d -i --name timetablebot \
	--mount "type=bind,src=${CONFIG_DIR},dst=/timetablebot/config" \
	timetablebot:jdk11
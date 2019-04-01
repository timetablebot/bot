#!/bin/bash
cd $(dirname $BASH_SOURCE)
cd ..

# Run after dockerBuild.sh

docker-compose down
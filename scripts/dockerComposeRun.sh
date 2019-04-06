#!/bin/bash
cd $(dirname $BASH_SOURCE)
cd ..

docker-compose up -d
docker-compose logs -f
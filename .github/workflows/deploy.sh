#!/usr/bin/env bash 
set -e
set -o pipefail

echo "building and pushing app image ..."

export ROOT_DIR=$(cd $(dirname $0) && pwd)

cd $ROOT_DIR
cd ../..
pwd

export APP_NAME=points-calculator-sink
export IMAGE_NAME=docker.io/dturanski/${APP_NAME}
#docker rmi $(docker images -a -q)
mvn clean package spring-boot:build-image

echo "pushing ${IMAGE_NAME}"
docker push $IMAGE_NAME

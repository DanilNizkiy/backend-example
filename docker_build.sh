#!/bin/bash
set x
#mvn clean install
REV=`date +"%m-%d-%H-%M"`
cp ./target/backend-example-0.0.1-SNAPSHOT.jar ./docker_context
cp ./src/main/resources/application.properties ./docker_context/config/application.properties
docker build -f docker_context/Dockerfile -t danilnizkiy/backend-example:"$REV" .
docker tag danilnizkiy/backend-example:"$REV" danilnizkiy/backend-example:latest
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker push danilnizkiy/backend-example:"$REV"
docker push danilnizkiy/backend-example:latest

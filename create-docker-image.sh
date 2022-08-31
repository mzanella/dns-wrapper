#!/bin/bash

mvn clean package -DskipTests -Pfatjar
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

cat > Dockerfile <<- EOM
FROM openjdk:17
COPY ./target/dns-wrapper-${VERSION}-jar-with-dependencies.jar /tmp/simulator.jar
WORKDIR /tmp
ENTRYPOINT ["java","-jar", "/tmp/simulator.jar"]
EOM

docker build . -t mzanella5/dnswrapper-simulator

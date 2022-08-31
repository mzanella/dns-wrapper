FROM openjdk:17
COPY ./target/dns-wrapper-1.0-SNAPSHOT-jar-with-dependencies.jar /tmp/simulator.jar
WORKDIR /tmp
ENTRYPOINT ["java","-jar", "/tmp/simulator.jar"]

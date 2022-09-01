# Dns-wrapper
Some utilities to query different DNS servers with Java

## Prerequisites
 - Java 11
 - Maven

## Compilation
From terminal:
```sh
dns-wrapper$ mvn package
```

## Tests
From terminal:
```sh
dns-wrapper$ mvn test
```
Most of the tests make use of [Testcontianers](https://www.testcontainers.org) library, combined
with the docker image created by [this Dockerfile](./Dockerfile). It is also available on
[Dockerhub](https://hub.docker.com/r/mzanella5/dnswrapper-simulator).

### DNS wrapper simulator
The goal of the image is only to start the Java main available in the
[MockDNSServer](./src/main/java/io/github/mzanella/dns/simulator/MockDNSServer.java) class. The
class only proxies request to other DNS servers keeping count of the requests and, eventually, 
adding a delay before answering. The main accepts as arguments:
 - at **position 0** the port on which serves the requests;
 - at **position 1** the delay;
 - starting from **position 2** the list of DNS servers to be used.
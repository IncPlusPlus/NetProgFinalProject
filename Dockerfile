# Alpine Linux with OpenJDK JRE
FROM arm64v8/openjdk:11-jdk

# copy source, POM, mvnw, and script
COPY pom.xml /pom.xml
COPY mvnw /mvnw
COPY src/ /src/
COPY .mvn/ /.mvn/
COPY entrypoint.sh /entrypoint.sh
COPY persistentSlave.sh /persistentSlave.sh

# runs application
CMD ["/entrypoint.sh", "persistentSlave.sh", "192.168.191.0", "1234"]
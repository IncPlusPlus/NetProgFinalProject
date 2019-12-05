# Alpine Linux with OpenJDK JRE
FROM arm32v7/ubuntu
RUN apt-get update && \
    apt-get install -y software-properties-common
RUN add-apt-repository ppa:openjdk-r/ppa -y \
    && apt-get update -q \
    && apt install -y openjdk-11-jdk -y
RUN apt install bash sudo curl -y

# copy source, POM, mvnw, and script
ADD pom.xml /pom.xml
ADD mvnw /mvnw
ADD src/ /src/
ADD .mvn/ /.mvn/
ADD entrypoint.sh /entrypoint.sh
ADD persistentSlave.sh /persistentSlave.sh

# runs application
CMD ["/entrypoint.sh", "persistentSlave.sh", "192.168.191.0", "1234"]
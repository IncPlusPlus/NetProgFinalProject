# Alpine Linux with OpenJDK JRE
FROM adoptopenjdk/openjdk11:armv7l-debian-jdk-11.0.5_10-slim
RUN apt update && \
    apt install -y software-properties-common
RUN add-apt-repository ppa:openjdk-r/ppa -y \
    && apt update -q \
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
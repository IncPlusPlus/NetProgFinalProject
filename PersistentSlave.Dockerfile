# Alpine Linux with OpenJDK JRE
FROM raspbian/jessie
RUN apt update
RUN apt update -q && \
    yes | apt install software-properties-common --no-install-recommends
RUN add-apt-repository ppa:openjdk-r/ppa -y
RUN apt update -q
RUN apt install openjdk-11-jdk -y
RUN apt install apt-utils bash sudo curl -y

# copy source, POM, mvnw, and script
ADD pom.xml /pom.xml
ADD mvnw /mvnw
ADD src/ /src/
ADD .mvn/ /.mvn/
ADD entrypoint.sh /entrypoint.sh
ADD persistentSlave.sh /persistentSlave.sh

# runs application
CMD ["/entrypoint.sh", "persistentSlave.sh", "192.168.191.0", "1234"]
# Alpine Linux with OpenJDK JRE
FROM bellsoft/liberica-openjdk-alpine:latest
RUN apk update
#RUN add-apt-repository ppa:openjdk-r/ppa -y \
#    && apt update -q \
#    && apt install openjdk-11-jdk -y
RUN apk add bash sudo curl

# copy source, POM, mvnw, and script
ADD pom.xml /pom.xml
ADD mvnw /mvnw
ADD src/ /src/
ADD .mvn/ /.mvn/
ADD entrypoint.sh /entrypoint.sh
ADD persistentSlave.sh /persistentSlave.sh

# runs application
CMD ["sh"]
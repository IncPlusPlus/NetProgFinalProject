# Alpine Linux with OpenJDK JRE
FROM bellsoft/liberica-openjdk-centos:latest
RUN yum update -y
#RUN add-apt-repository ppa:openjdk-r/ppa -y \
#    && apt update -q \
#    && apt install openjdk-11-jdk -y
RUN yum install bash sudo curl -y

# copy source, POM, mvnw, and script
ADD pom.xml /pom.xml
ADD mvnw /mvnw
ADD src/ /src/
ADD .mvn/ /.mvn/
ADD entrypoint.sh /entrypoint.sh
ADD persistentSlave.sh /persistentSlave.sh

# runs application
CMD ["sh"]
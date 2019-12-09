# Alpine Linux with OpenJDK JRE
FROM raspbian/jessie
RUN apt update
RUN wget -q -O - https://download.bell-sw.com/pki/GPG-KEY-bellsoft | apt-key add -
RUN echo "deb [arch=amd64] https://apt.bell-sw.com/ stable main" | tee /etc/apt/sources.list.d/bellsoft.list
RUN apt-get update
RUN apt-get install bellsoft-java13

#RUN add-apt-repository ppa:openjdk-r/ppa -y
#RUN apt update -q
#RUN apt install openjdk-11-jdk -y
#RUN apt install apt-utils bash sudo curl -y

# copy source, POM, mvnw, and script
ADD pom.xml /pom.xml
ADD mvnw /mvnw
ADD src/ /src/
ADD .mvn/ /.mvn/
ADD entrypoint.sh /entrypoint.sh
ADD persistentSlave.sh /persistentSlave.sh

# runs application
CMD ["/entrypoint.sh", "persistentSlave.sh", "192.168.191.0", "1234"]
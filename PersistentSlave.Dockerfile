# Alpine Linux with OpenJDK JRE
FROM raspbian/jessie
RUN apt update && apt install -y apt-transport-https ca-certificates
RUN wget -q -O - https://download.bell-sw.com/pki/GPG-KEY-bellsoft | apt-key add -
RUN echo "deb [arch=armhf] https://apt.bell-sw.com/ stable main" | tee /etc/apt/sources.list.d/bellsoft.list
RUN apt-get update
RUN apt-get install bellsoft-java13 -y
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
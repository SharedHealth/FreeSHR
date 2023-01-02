FROM centos

RUN cd /etc/yum.repos.d/
RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-*
RUN sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-*

RUN  yum install java-1.8.0-openjdk -y

COPY shr/build/distributions/shr-*.noarch.rpm /tmp/shr.rpm
RUN yum install -y /tmp/shr.rpm && rm -f /tmp/shr.rpm && yum clean all
COPY env/docker_shr /etc/default/bdshr
ENTRYPOINT . /etc/default/bdshr && java -jar /opt/bdshr/lib/shr-schema-*.jar && java -Dserver.port=$BDSHR_PORT -DSHR_LOG_LEVEL=$SHR_LOG_LEVEL -jar /opt/bdshr/lib/shr.war


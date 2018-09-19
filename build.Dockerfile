FROM debian:latest

WORKDIR /

RUN apt-get update && apt-get install -y gnupg2 pwgen

RUN echo "deb http://ppa.launchpad.net/linuxuprising/java/ubuntu bionic main" | tee /etc/apt/sources.list.d/linuxuprising-java.list && \
    echo oracle-java10-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 73C3DB2A && \
    apt-get update && \
    apt-get install -y oracle-java10-installer

RUN addgroup application && \
	useradd --home-dir /home/application --create-home --password `pwgen 20` --shell /bin/bash -g application application && \
	chown -R application:application /home/application && \
	chmod -R a-rwx /home/application && \
	chmod -R g+r /home/application && \
	chmod -R u+rx /home/application

USER application

ENTRYPOINT ["bin/bash"]
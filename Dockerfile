# Based on https://github.com/moss-it/docker-xelatex 
FROM debian:latest
MAINTAINER sjoblomj88@gmail.com
LABEL version="0.0.1"
ENV DEBIAN_FRONTEND noninteractive
EXPOSE 58404

# UPDATE PACKAGE INFORMATION
RUN apt-get update


# INSTALL TEX PACKAGES
RUN apt-get install --yes --no-install-recommends \
  ca-certificates \
  lmodern \
  texlive-full


# INSTALL PGFORNAMENT
RUN apt-get install --yes unzip curl
RUN mkdir -p ~/texmf/tex/latex/pgfornament
RUN mkdir -p ~/texmf/tex/generic/pgfornament

RUN curl -L -X GET http://mirrors.ctan.org/macros/latex/contrib/tkz/pgfornament.zip -o /tmp/pgfornament.zip
RUN unzip /tmp/pgfornament.zip -d /tmp
WORKDIR /tmp/pgfornament
RUN cp latex/* ~/texmf/tex/latex/pgfornament/
RUN cp -r generic/* ~/texmf/tex/generic/pgfornament/


# INSTALL JAVA
RUN mkdir -p /usr/share/man/man1 /usr/share/man/man2
RUN apt-get install -y --no-install-recommends openjdk-11-jre


# CLEANUP
RUN apt-get autoclean && apt-get --purge --yes autoremove
RUN rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*


# COPY AND RUN JAVA SERVER APPLICATION
COPY latex-compile-server /data
WORKDIR /data
RUN ./gradlew build
CMD ["./gradlew", "run"]

FROM alpine

WORKDIR /work

RUN apk update && apk add openjdk8 && apk add apache-ant

ENV ANT_HOME /usr/share/java/apache-ant
ENV PATH $PATH:$ANT_HOME/bin

ENTRYPOINT ant transform4pdf
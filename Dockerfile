#FROM alpine:latest AS builder
#
# In case you need more complex build steps (for example compile some libs or some resources)
# this should be done in a builder layer.
#
FROM azul/zulu-openjdk-alpine:17-jre

RUN apk --no-cache add bash \
    && apk --no-cache upgrade

ENTRYPOINT [""]
HEALTHCHECK NONE

COPY ./build/libs/arch-c4-sync-*.jar /usr/local/bin/arch-c4-sync.jar
COPY ./scripts/* /usr/local/bin

FROM azul/zulu-openjdk-alpine:17-jre

RUN apk --no-cache add bash \
    && apk --no-cache upgrade

ENTRYPOINT [""]
HEALTHCHECK NONE

COPY ./build/libs/arch-c4-sync-*.jar /usr/local/bin/arch-c4-sync.jar
COPY ./scripts/* /usr/local/bin

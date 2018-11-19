FROM navikt/java:11

COPY build/libs/sykepengesoknadfilter-all.jar "/app/app.jar"

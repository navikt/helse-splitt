FROM navikt/java:10

COPY build/libs/sykepengesoknadfilter-all.jar "/app/app.jar"

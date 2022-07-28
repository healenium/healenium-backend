FROM openjdk:8-jre-alpine
COPY /build/libs/hlm-backend-*.jar /hlm-backend.jar
CMD java -jar /hlm-backend.jar

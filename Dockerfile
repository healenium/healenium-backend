FROM openjdk:8
COPY /build/libs/hlm-backend-*.jar /hlm-backend.jar
CMD java -jar /hlm-backend.jar

FROM openjdk:8
COPY /build/libs/hlm-backend-*.jar /hlm-backend.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/hlm-backend.jar"]

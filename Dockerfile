FROM openjdk:8
COPY /build/libs/hl-backend-*.jar /hl-backend.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/hl-backend.jar"]

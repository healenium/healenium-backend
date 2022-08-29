FROM openjdk:8-jre-alpine
COPY /build/libs/healenium-backend-*.jar /healenium-backend.jar
CMD java -jar /healenium-backend.jar

FROM eclipse-temurin:17-jdk-alpine
COPY /build/libs/healenium-backend-*.jar /healenium-backend.jar
CMD java -jar /healenium-backend.jar

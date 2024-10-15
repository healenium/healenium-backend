FROM eclipse-temurin:22-alpine
COPY /build/libs/healenium-backend-*.jar /healenium-backend.jar
CMD java -jar /healenium-backend.jar

FROM eclipse-temurin:21-jre-alpine
RUN apk upgrade --no-cache
COPY /build/libs/healenium-backend-*.jar /healenium-backend.jar
CMD java -jar /healenium-backend.jar

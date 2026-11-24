FROM eclipse-temurin:23
RUN apt-get update && apt-get upgrade -y && rm -rf /var/lib/apt/lists/*
COPY /build/libs/healenium-backend-*.jar /healenium-backend.jar
CMD java -jar /healenium-backend.jar

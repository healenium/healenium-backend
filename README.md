# healenium-client
Client for Healenium
Back-end of 2 docker containers: healenium-backend and healenium-db. Process healing results storage and obtaining 
https://hub.docker.com/repository/docker/healenium/hlm-backend

Download Example of compose descriptor to test project 
```
$ curl https://raw.githubusercontent.com/healenium/healenium-client/master/docker-compose.yml -o docker-compose.yaml
```

Add init.sql into ./db/sql/init.sql folder in your project
```
CREATE SCHEMA healenium AUTHORIZATION healenium_user;
GRANT USAGE ON SCHEMA healenium TO healenium_user;
```

To start hlm-backend and simply run docker-compose 
```
docker-compose up -d
```

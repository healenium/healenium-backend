# healenium-client
Client for Healenium
Back-end of 2 docker containers: healenium-backend and healenium-db. Process healing results storage and obtaining 
https://hub.docker.com/repository/docker/healenium/hlm-backend

Download [Example of compose descriptor](https://github.com/healenium/healenium-client/blob/master/example/docker-compose.yaml) into your test project 
```
$ curl  https://raw.githubusercontent.com/healenium/healenium-client/master/example/docker-compose.yaml  -o docker-compose.yaml
```

Add [init.sql file](https://github.com/healenium/healenium-client/blob/master/example/init.sql)  into ./db/sql/init.sql folder in your project
```
$ curl https://raw.githubusercontent.com/healenium/healenium-client/master/example/init.sql -o init.sql
```

To start hlm-backend and simply run docker-compose 
```
docker-compose up -d
```
### BACKEND AECHITECTURE DIAGRAM
![](https://i.imgur.com/AEiPXoq.png)

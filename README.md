# healenium-client
Client for Healenium
Back-end of 2 docker containers: healenium-backend and healenium-db. Process healing results storage and obtaining 
https://hub.docker.com/repository/docker/healenium/hlm-backend

Download [Example of compose descriptor](https://github.com/healenium/healenium-client/blob/master/example/docker-compose.yaml) into your test project 
```
$ curl  https://raw.githubusercontent.com/healenium/healenium-client/master/example/docker-compose.yaml  -o docker-compose.yaml
```

Create /db/sql folder on the same level in your project. Add [init.sql file](https://github.com/healenium/healenium-client/blob/master/example/init.sql) into ./db/sql/init.sql folder in your project
```
$ curl https://raw.githubusercontent.com/healenium/healenium-client/master/example/init.sql -o init.sql
```
For example, you should have the project structure as below
![img.png](img.png)

To start hlm-backend and simply run docker-compose 
```
docker-compose up -d
```
Verify that hlm-backend:latest and postgres docker containers are up and running:
```
docker ps
```
![img_1.png](img_1.png)
### BACKEND AECHITECTURE DIAGRAM
![](https://i.imgur.com/AEiPXoq.png)

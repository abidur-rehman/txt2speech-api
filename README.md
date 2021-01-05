# Build Instructions

### `txt2speech-api` Repo

Git clone the project to local
```
# SSH Option
git clone git@
git checkout develop

# HTTPS Option
git clone https://
```


### Prerequisites

This project depends on Postgres database.

### Build

```
mvn clean compile
```

### Run in dev environment

```
mvn spring-boot:run -Dspring-boot.run.profiles=dev

java -jar target/txt2speech-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```


### Creating docker image for dev environment
Make sure to regenerate target folder before creating docker image
 
```
mvn package -Dmaven.test.skip=true

docker build -t txt2speech-api -f Dockerfile .

docker run -d -p 8080:8080 txt2speech-api:latest 
``````

### Pushing docker image to docker hub
 
```
mvn package -Dmaven.test.skip=true

docker build -t abidurrehman/txt2speech-api -f Dockerfile .

docker push abidurrehman/txt2speech-api:latest
``````

### `Deploy new image on Kubernetes`

Command `kubectl rollout restart deployment ts-api` will update the new deployment with new image

### Healthcheck link
```
http://localhost:8080/health
```

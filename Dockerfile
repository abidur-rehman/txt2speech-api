FROM balenalib/raspberrypi3-openjdk:11-jdk
VOLUME /tmp
COPY src/main/resources/application.properties /application.properties
COPY src/main/resources/logback.xml /logback.xml
ADD target/txt2speech-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=prod"]
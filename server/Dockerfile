FROM amazoncorretto:11
COPY target/*.jar server.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/server.jar"]
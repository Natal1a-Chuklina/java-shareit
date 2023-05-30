FROM amazoncorretto:11
COPY target/*.jar gateway.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/gateway.jar"]
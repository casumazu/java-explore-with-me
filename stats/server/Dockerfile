FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar ewm-stat-service-server-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/ewm-stat-service-server-0.0.1-SNAPSHOT.jar"]
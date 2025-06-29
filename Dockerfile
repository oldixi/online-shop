FROM openjdk:21-jdk-slim
LABEL authors="Oldixi"
COPY target/*.jar /online-shop.jar
ENTRYPOINT ["java", "jar", "online-shop.jar"]
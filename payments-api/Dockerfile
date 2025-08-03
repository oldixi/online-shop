FROM openjdk:21-jdk-slim
LABEL authors="Oldixi"
COPY build/libs/*.jar /payments-api.jar
ENTRYPOINT ["java", "-jar", "/payments-api.jar"]
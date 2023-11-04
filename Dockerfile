FROM maven:3.8.6-openjdk-17-slim as builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn  package -DskipTests

CMD ["java", "-jar", "/app/target/l2yh-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]
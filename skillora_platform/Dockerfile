FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
RUN addgroup -S skillora && adduser -S skillora -G skillora
COPY --from=build /app/target/*.jar app.jar
USER skillora

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

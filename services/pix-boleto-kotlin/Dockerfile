FROM gradle:8.8-jdk17 AS build
WORKDIR /app
COPY settings.gradle.kts build.gradle.kts ./
COPY src ./src
RUN gradle -q bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/pix-boleto-kotlin-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

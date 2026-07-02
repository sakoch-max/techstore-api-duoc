FROM eclipse-temurin:17-jre-alpine
WORKDIR /app


COPY target/*.jar app.jar


COPY src/main/resources/wallet /app/wallet

USER nobody

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

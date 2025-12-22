FROM openjdk:26-ea-slim
WORKDIR /app
COPY build/libs/auth-kotlin-demo-*.jar app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:default}", "-jar", "/app/app.jar"]
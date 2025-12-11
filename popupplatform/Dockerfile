FROM eclipse-temurin:17-jdk

WORKDIR /app

# Gradle 빌드 결과 복사
COPY app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
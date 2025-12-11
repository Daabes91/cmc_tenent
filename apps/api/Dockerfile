# Use a Gradle image to avoid downloading the wrapper distribution (GitHub 503s)
FROM gradle:8.6-jdk21-jammy AS builder
WORKDIR /workspace
COPY build.gradle.kts settings.gradle.kts gradlew ./ 
COPY gradle ./gradle
COPY src ./src
# gradle image already includes the binary; run without wrapper to skip external distribution fetch
RUN gradle --no-daemon bootJar

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
# Marketing/admin base URLs for PayPal return/cancel links
ARG APP_MARKETING_URL
ARG APP_MARKETING_BASE_PATH
ARG APP_ADMIN_URL
ENV APP_MARKETING_URL=${APP_MARKETING_URL}
ENV APP_MARKETING_BASE_PATH=${APP_MARKETING_BASE_PATH}
ENV APP_ADMIN_URL=${APP_ADMIN_URL}
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

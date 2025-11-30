import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    java
}

group = "com.clinic"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.flywaydb:flyway-core")
    implementation("org.postgresql:postgresql")
    implementation("com.nimbusds:nimbus-jose-jwt:9.37.3")
    implementation("commons-codec:commons-codec:1.16.0")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    
    // Google OAuth dependencies
    implementation("com.google.api-client:google-api-client:2.2.0")
    implementation("com.google.oauth-client:google-oauth-client:1.34.1")
    implementation("com.google.http-client:google-http-client-jackson2:1.43.3")

    // Lombok for reducing boilerplate code
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // SendGrid for email notifications
    implementation("com.sendgrid:sendgrid-java:4.10.2")

    // Spring Retry for webhook processing
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework:spring-aspects")

    // Caffeine cache for billing status caching
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    
    // Property-based testing with jqwik (JUnit 5 compatible)
    testImplementation("net.jqwik:jqwik:1.8.2")
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

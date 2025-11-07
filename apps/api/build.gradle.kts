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

    // SendGrid for email notifications
    implementation("com.sendgrid:sendgrid-java:4.10.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

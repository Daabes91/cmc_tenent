#!/bin/bash

# Script to generate a SAAS Manager with proper password hash

EMAIL="admin@saas.com"
PASSWORD="Admin123!"
FULL_NAME="SAAS Administrator"

echo "Generating SAAS Manager..."
echo "Email: $EMAIL"
echo "Password: $PASSWORD"
echo ""

# Use the Spring Boot application to generate the hash
cd "$(dirname "$0")"

# Create a temporary Java file to generate the hash
cat > src/main/java/com/clinic/util/GenerateSaasManager.java << 'JAVA_EOF'
package com.clinic.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class GenerateSaasManager {
    
    public static void main(String[] args) {
        SpringApplication.run(GenerateSaasManager.class, args);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public CommandLineRunner run(PasswordEncoder passwordEncoder) {
        return args -> {
            String password = args.length > 0 ? args[0] : "Admin123!";
            String hash = passwordEncoder.encode(password);
            System.out.println("Password: " + password);
            System.out.println("BCrypt Hash: " + hash);
            System.out.println("");
            System.out.println("SQL to insert SAAS Manager:");
            System.out.println("DELETE FROM saas_managers WHERE email = 'admin@saas.com';");
            System.out.println("INSERT INTO saas_managers (email, full_name, password_hash, status, created_at, updated_at)");
            System.out.println("VALUES ('admin@saas.com', 'SAAS Administrator', '" + hash + "', 'ACTIVE', NOW(), NOW());");
        };
    }
}
JAVA_EOF

echo "Compiling and running hash generator..."
./gradlew -q execute -PmainClass=com.clinic.util.GenerateSaasManager -PappArgs="['$PASSWORD']"

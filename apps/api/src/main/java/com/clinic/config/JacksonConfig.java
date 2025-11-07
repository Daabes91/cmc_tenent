package com.clinic.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Include null values in JSON serialization
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        // Register JSR310 module for Java 8 time support
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
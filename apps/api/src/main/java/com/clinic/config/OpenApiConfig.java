package com.clinic.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OpenApiConfig {

    @Bean
    @Primary
    public OpenAPI clinicOpenAPI(SecurityProperties securityProperties) {
        return new OpenAPI()
                .info(new Info()
                        .title("Clinic Platform Admin API")
                        .description("Endpoints serving the clinic administration applications.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Clinic Ops Team")
                                .email("ops@clinic.com"))
                        .license(new License().name("Proprietary").url("https://clinic.example.com"))
                )
                .externalDocs(new ExternalDocumentation()
                        .description("Postman Collection")
                        .url("https://postman.com/collections/clinic-admin"));
    }
}

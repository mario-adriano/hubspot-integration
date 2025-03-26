package com.techcase.hubspot_integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@OpenAPIDefinition(
    info = @Info(title = "HubSpot Integration API", version = "v1"),
    security = @SecurityRequirement(name = "bearerAuth")
)
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.techcase.hubspot_integration.repository")
@EntityScan(basePackages = "com.techcase.hubspot_integration.model")
public class HubspotIntegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(HubspotIntegrationApplication.class, args);
	}

}

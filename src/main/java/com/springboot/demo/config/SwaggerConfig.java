package com.springboot.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8088").description("Local Development Server"),
                        new Server().url("https://api.neomind.com").description("Production Server")
                ))
                .info(new Info()
                        .title("NeoMind AI Grow API")
                        .description("""
                                API documentation for NeoMind AI Grow Backend
                                
                                ## Authentication
                                This API uses JWT tokens from AWS Cognito for authentication.
                                
                                ### How to get JWT Token:
                                1. Use AWS Cognito User Pool to authenticate
                                2. Get JWT token from Cognito response
                                3. Use the token in Authorization header: `Bearer YOUR_JWT_TOKEN`
                                
                                ### Testing Flow:
                                1. First call `/api/session` to create a session
                                2. Then use other APIs with the same JWT token
                                
                                ## API Structure:
                                - **Workspaces**: Manage workspaces within companies
                                - **Workspace Members**: Manage members and permissions
                                - **Projects**: Manage projects within workspaces  
                                - **Sprints**: Manage sprints within projects
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NeoMind Team")
                                .email("contact@neomind.com")
                                .url("https://neomind.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token from AWS Cognito")));
    }
}
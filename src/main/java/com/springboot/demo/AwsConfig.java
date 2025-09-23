// src/main/java/com/springboot/demo/AwsConfig.java
package com.springboot.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
public class AwsConfig {

    @Bean
    public CognitoIdentityProviderClient cognitoIdentityProviderClient() {
        return CognitoIdentityProviderClient.builder()
                .region(Region.of(System.getenv().getOrDefault("AWS_REGION", "us-east-1"))) // chỉnh region
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}

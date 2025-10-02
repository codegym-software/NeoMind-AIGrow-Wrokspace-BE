package com.springboot.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.springboot.demo.config.SecurityConfig;

@SpringBootApplication(exclude = {
    SecurityAutoConfiguration.class,
    OAuth2ResourceServerAutoConfiguration.class,
    OAuth2ClientAutoConfiguration.class
})
@ComponentScan(
    basePackages = "com.springboot.demo",
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = SecurityConfig.class
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.springboot\\.demo\\.config\\..*"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.springboot\\.demo\\.security\\..*"
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.springboot\\.demo\\.manager\\..*"
        )
    }
)
public class SimpleNeoMindApp {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "minimal");
        SpringApplication.run(SimpleNeoMindApp.class, args);
    }
}

package com.springboot.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.springboot.demo.service.impl.AccountServiceImpl;
import com.springboot.demo.controller.UserController;

@SpringBootApplication(exclude = {
    SecurityAutoConfiguration.class,
    OAuth2ResourceServerAutoConfiguration.class
})
@ComponentScan(
    basePackages = {
        "com.springboot.demo.model",
        "com.springboot.demo.repository", 
        "com.springboot.demo.service",
        "com.springboot.demo.controller",
        "com.springboot.demo.dto"
    },
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {AccountServiceImpl.class, UserController.class}
        )
    }
)
public class TestNeoMindApp {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "minimal");
        SpringApplication.run(TestNeoMindApp.class, args);
    }
}

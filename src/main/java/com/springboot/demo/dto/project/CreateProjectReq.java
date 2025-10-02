package com.springboot.demo.dto.project;

public record CreateProjectReq(
        String name,
        String description,
        String color
) {}

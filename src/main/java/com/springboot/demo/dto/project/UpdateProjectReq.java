package com.springboot.demo.dto.project;

public record UpdateProjectReq(
        String name,
        String description,
        String color
) {}

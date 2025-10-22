package com.springboot.demo.dto.project;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record UpdateProjectReq(

        @NotEmpty(message = "Name is required!")
        String name,

        @NotEmpty(message = "Description is required!")
        String description,

        @Pattern(
            regexp = "^#(?:[0-9a-fA-F]{3}){1,2}$",
            message = "Color must be a valid HEX code, e.g. #3b82f6"
        )
        String color
) {}

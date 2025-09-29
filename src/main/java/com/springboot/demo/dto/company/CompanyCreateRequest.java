package com.springboot.demo.dto.company;

import jakarta.validation.constraints.NotBlank;

public record CompanyCreateRequest(@NotBlank String name, String logoUrl) {
}


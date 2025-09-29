package com.springboot.demo.dto.company;

import jakarta.validation.constraints.NotBlank;

public record CompanyUpdateRequest(@NotBlank String name, String logoUrl) {
}


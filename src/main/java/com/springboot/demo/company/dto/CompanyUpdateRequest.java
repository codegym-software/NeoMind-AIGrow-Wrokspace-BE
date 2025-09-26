package com.springboot.demo.company.dto;

import jakarta.validation.constraints.NotBlank;

public record CompanyUpdateRequest(@NotBlank String name, String logoUrl) {}

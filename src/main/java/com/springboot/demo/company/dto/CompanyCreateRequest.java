package com.springboot.demo.company.dto;
import jakarta.validation.constraints.NotBlank;

public record CompanyCreateRequest(@NotBlank String name, String logoUrl) {}

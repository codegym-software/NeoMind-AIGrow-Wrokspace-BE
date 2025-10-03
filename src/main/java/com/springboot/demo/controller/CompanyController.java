package com.springboot.demo.controller;

import com.springboot.demo.dto.company.CompanyCreateRequest;
import com.springboot.demo.dto.company.CompanyUpdateRequest;
import com.springboot.demo.model.Company;
import com.springboot.demo.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/companies")
@Tag(name = "Company Management", description = "APIs for managing companies")
@SecurityRequirement(name = "Bearer Authentication")
public class CompanyController {
    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new company", description = "Create a new company with name and optional logo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Company created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Company create(@Valid @RequestBody CompanyCreateRequest req) {
        return service.create(req);
    }

    @PreAuthorize("@gate.allows('company:read', authentication, #id)")
    @GetMapping("/{id}")
    public Company get(@PathVariable String id) {
        return service.getById(id);
    }

    @PreAuthorize("@gate.allows('company:update', authentication, #id)")
    @PutMapping("/{id}")
    public Company update(@PathVariable String id,
                          @Valid @RequestBody CompanyUpdateRequest req) {
        return service.update(id, req);
    }

    @PreAuthorize("@gate.allows('company:update', authentication, #id)")
    @PatchMapping("/{id}")
    public Company patch(@PathVariable String id, @RequestBody Map<String, Object> p) {
        Company c = service.getById(id);
        if (p.containsKey("name")) {
            c.setName(Objects.toString(p.get("name"), "").trim());
        }
        if (p.containsKey("logoUrl")) {
            c.setLogoUrl((String) p.get("logoUrl"));
        }
        return service.save(c);
    }

    @PreAuthorize("@gate.allows('company:delete', authentication, #id)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}


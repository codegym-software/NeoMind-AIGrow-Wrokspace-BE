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

    @GetMapping("/{id}")
    public Company get(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Company update(@PathVariable String id,
                          @Valid @RequestBody CompanyUpdateRequest req) {
        return service.update(id, req);
    }

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

    @GetMapping
    @Operation(summary = "Get companies for current user", description = "Retrieve companies where the current user is a member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Companies retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Page<Company> list(@Parameter(description = "Search query") @RequestParam(required = false) String q,
                              @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                              @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        return service.searchByCurrentUser(q, page, size);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @GetMapping("/admin/all")
    @Operation(summary = "Get all companies (Admin only)", description = "Retrieve all companies in the system - Admin access required")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All companies retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    public Page<Company> listAll(@Parameter(description = "Search query") @RequestParam(required = false) String q,
                                 @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                                 @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        return service.search(q, page, size);
    }
}


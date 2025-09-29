package com.springboot.demo.service;

import com.springboot.demo.dto.company.CompanyCreateRequest;
import com.springboot.demo.dto.company.CompanyUpdateRequest;
import com.springboot.demo.model.Company;
import org.springframework.data.domain.Page;

public interface CompanyService {
    Company create(CompanyCreateRequest req);

    Company getById(String id);

    Company update(String id, CompanyUpdateRequest req);

    Company save(Company c);

    Page<Company> search(String q, int page, int size);

    Page<Company> searchByCurrentUser(String q, int page, int size);

    void delete(String id);
}


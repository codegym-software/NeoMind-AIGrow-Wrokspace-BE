package com.springboot.demo.service;

import com.springboot.demo.dto.company.CompanyCreateRequest;
import com.springboot.demo.dto.company.CompanyUpdateRequest;
import com.springboot.demo.model.Company;
import org.springframework.data.domain.Page;

public interface CompanyService {
    Company create(CompanyCreateRequest req, String actorId);

    Company getById(String companyId);

    Company update(String companyId, CompanyUpdateRequest req);

    Company save(Company c);

    Page<Company> search(String q, int page, int size);

    Page<Company> searchByCurrentUser(String q, int page, int size, String actorId);

    void delete(String companyId);

}


package com.springboot.demo.company;

import com.springboot.demo.company.dto.CompanyCreateRequest;
import com.springboot.demo.company.dto.CompanyUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
//import java.time.LocalDateTime;

@Service
public class CompanyService {
    private final CompanyRepository repo;
    public CompanyService(CompanyRepository repo) { this.repo = repo; }

    @Transactional
    public Company create(CompanyCreateRequest req){
        Company c = new Company();
        c.setName(req.name().trim());
        c.setLogoUrl(req.logoUrl());
        return repo.save(c);
    }

    @Transactional(readOnly = true)
    public Company getById(String id){
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Company not found: " + id));
    }

    // Task 2: update công ty
    @Transactional
    public Company update(String id, CompanyUpdateRequest req){
        Company c = getById(id);
        c.setName(req.name().trim());
        c.setLogoUrl(req.logoUrl());
        return repo.save(c);
    }

    @Transactional
    public Company save(Company c){ return repo.save(c); }

    @Transactional(readOnly = true)
    public Page<Company> search(String q, int page, int size){
        var pageable = PageRequest.of(page, size);
        if (q == null || q.isBlank()) return repo.findAll(pageable);
        return repo.findByNameContainingIgnoreCase(q.trim(), pageable);
    }

    @Transactional
    public void delete(String id){
        if (!repo.existsById(id)) {
            throw new NoSuchElementException("Company not found: " + id);
        }
        repo.deleteById(id);
    }
}

package com.springboot.demo.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, String> {
    Page<Company> findByNameContainingIgnoreCase(String name, Pageable pageable);
}

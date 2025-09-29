package com.springboot.demo.repository;

import com.springboot.demo.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, String> {
    Page<Company> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Find companies by user membership
    @Query("SELECT c FROM Company c WHERE c.id IN :companyIds")
    Page<Company> findByIdIn(@Param("companyIds") List<String> companyIds, Pageable pageable);

    // Find companies by user membership with search
    @Query("SELECT c FROM Company c WHERE c.id IN :companyIds AND LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Company> findByIdInAndNameContainingIgnoreCase(@Param("companyIds") List<String> companyIds,
                                                        @Param("searchTerm") String searchTerm,
                                                        Pageable pageable);
}

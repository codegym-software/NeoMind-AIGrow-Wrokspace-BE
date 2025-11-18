package com.springboot.demo.service;

import com.springboot.demo.model.Company;
import com.springboot.demo.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CompanyServiceDeleteTest {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRepository companyRepository;

    private String testCompanyId;

    @BeforeEach
    void setUp() {
        // Create a test company
        Company company = new Company();
        company.setName("Test Company for Deletion");
        company = companyRepository.save(company);
        testCompanyId = company.getId();
    }

    @Test
    void testDeleteCompany_ShouldDeleteCompanyAndAllRelatedEntities() {
        // Given: A company exists
        assertTrue(companyRepository.existsById(testCompanyId));

        // When: Delete the company
        companyService.delete(testCompanyId);

        // Then: Company should be deleted
        assertFalse(companyRepository.existsById(testCompanyId));
    }

    @Test
    void testDeleteCompany_WhenCompanyDoesNotExist_ShouldThrowException() {
        // Given: A non-existent company ID
        String nonExistentId = "non-existent-id";

        // When/Then: Should throw NoSuchElementException
        assertThrows(java.util.NoSuchElementException.class, () -> {
            companyService.delete(nonExistentId);
        });
    }
}

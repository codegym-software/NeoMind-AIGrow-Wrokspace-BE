package com.springboot.demo.repository;

import com.springboot.demo.model.Epic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface EpicRepository extends JpaRepository<Epic, Integer>, JpaSpecificationExecutor<Epic> {
    Page<Epic> findByProjectId(String projectId, Pageable pageable);
    List<Epic> findByProjectId(String projectId);
}

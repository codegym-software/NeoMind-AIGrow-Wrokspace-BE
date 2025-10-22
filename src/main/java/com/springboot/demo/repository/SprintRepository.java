package com.springboot.demo.repository;

import com.springboot.demo.model.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SprintRepository extends JpaRepository<Sprint, Integer> {
    List<Sprint> findByProjectIdOrderByStartDateAsc(String projectId);
}

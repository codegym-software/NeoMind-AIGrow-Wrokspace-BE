package com.springboot.demo.repository;

import com.springboot.demo.model.Sprint;
import com.springboot.demo.model.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SprintRepository extends JpaRepository<Sprint, Integer> {
    List<Sprint> findByProjectIdOrderByStartDateAsc(String projectId);
    Optional<Sprint> findByProjectIdAndStatus(String projectId, SprintStatus status);
}

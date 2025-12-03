package com.springboot.demo.repository;

import com.springboot.demo.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {
    java.util.List<ActivityLog> findByProjectIdOrderByCreatedAtDesc(String projectId);
}

package com.springboot.demo.repository;

import com.springboot.demo.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {
    
    List<TaskAttachment> findByTaskIdOrderByUploadedAtDesc(Long taskId);
    
    void deleteByTaskId(Long taskId);
}

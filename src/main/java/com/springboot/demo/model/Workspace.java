package com.springboot.demo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "workspaces",
        indexes = {@Index(name = "idx_ws_company", columnList = "company_id"),
                   @Index(name = "idx_ws_name_company", columnList = "company_id,name")})
public class Workspace {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "company_id", length = 36, nullable = false)
    private String companyId;

    @Column(name = "created_by", length = 36, nullable = false)
    private String createdBy;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @PrePersist
    void prePersist(){
        if (id == null) id = UUID.randomUUID().toString();
    }

    // Business helpers
    public boolean isArchived(){ return archivedAt != null; }
    public void archive(){ this.archivedAt = LocalDateTime.now(); }
    public void unarchive(){ this.archivedAt = null; }
}
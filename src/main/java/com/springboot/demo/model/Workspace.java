package com.springboot.demo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workspaces",
        indexes = {
                @Index(name = "idx_workspaces_company", columnList = "company_id"),
                @Index(name = "idx_workspaces_name", columnList = "name")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Workspace {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "company_id", length = 36, nullable = false)
    private String companyId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Lob
    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
    }
}

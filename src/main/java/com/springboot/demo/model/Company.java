package com.springboot.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "companies")
@Getter
@Setter
public class Company {
    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
    }
}


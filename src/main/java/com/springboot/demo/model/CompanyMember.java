package com.springboot.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "company_members",
        indexes = {
                @Index(name = "idx_company_user", columnList = "company_id,user_id")
        })
public class CompanyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "company_id", length = 36, nullable = false)
    private String companyId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "invited_email")
    private String invitedEmail;

    @Column(name = "invited_by", length = 36)
    private String invitedBy;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "is_owner", nullable = false)
    private boolean isOwner = false;

    @PrePersist
    void prePersist() {
        if (joinedAt == null) joinedAt = LocalDateTime.now();
    }
}


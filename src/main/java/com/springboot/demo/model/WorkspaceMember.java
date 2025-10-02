package com.springboot.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "workspace_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class WorkspaceMember {
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "workspace_id", length = 36, nullable = false)
    private String workspaceId;

    @Column(name = "user_id", length = 255, nullable = false)
    private String userId;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "is_owner", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isOwner = false;

}


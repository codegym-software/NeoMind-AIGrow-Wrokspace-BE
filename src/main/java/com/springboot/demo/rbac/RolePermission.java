package com.springboot.demo.rbac;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_permission")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RolePermission {
    @EmbeddedId
    private RolePermissionId id;
}

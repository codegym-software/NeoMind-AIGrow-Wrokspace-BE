package com.springboot.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class RolePermissionId implements Serializable {
    @Column(name = "role_id")       private Integer roleId;
    @Column(name = "permission_id") private Integer permissionId;
}

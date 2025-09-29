package com.springboot.demo.rbac;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
    List<RolePermission> findByIdRoleId(Long roleId);
    void deleteByIdRoleId(Long roleId);
}

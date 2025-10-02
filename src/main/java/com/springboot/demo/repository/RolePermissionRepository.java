package com.springboot.demo.repository;

import com.springboot.demo.model.RolePermission;
import com.springboot.demo.model.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
    List<RolePermission> findByIdRoleId(Integer roleId);
    void deleteByIdRoleId(Integer roleId);
}

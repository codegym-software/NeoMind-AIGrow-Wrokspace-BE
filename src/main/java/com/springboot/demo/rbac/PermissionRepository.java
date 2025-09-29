package com.springboot.demo.rbac;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    List<Permission> findByScope(Scope scope);
}

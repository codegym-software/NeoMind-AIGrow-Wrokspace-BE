package com.springboot.demo.repository;

import com.springboot.demo.model.Permission;
import com.springboot.demo.model.enums.Scope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    List<Permission> findByScope(Scope scope);
}

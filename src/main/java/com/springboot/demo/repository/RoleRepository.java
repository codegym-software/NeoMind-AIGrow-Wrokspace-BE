package com.springboot.demo.repository;

import com.springboot.demo.model.Role;
import com.springboot.demo.model.Scope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByScopeAndTargetId(Scope scope, String targetId);
    List<Role> findByScopeAndTargetIdIsNull(Scope scope); // template roles của scope
    boolean existsByScopeAndTargetIdAndCode(Scope scope, String targetId, String code);
}

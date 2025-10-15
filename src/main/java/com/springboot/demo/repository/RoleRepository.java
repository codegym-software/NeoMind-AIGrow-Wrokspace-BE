package com.springboot.demo.repository;

import com.springboot.demo.model.Role;
import com.springboot.demo.model.enums.Scope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    List<Role> findByScopeAndTargetIdIsNull(Scope scope); // template roles của scope
    boolean existsByScopeAndTargetIdAndCode(Scope scope, String targetId, String code);
    Optional<Role> findByScopeAndCode(String scope, String code);
    Optional<Role> findByScopeAndTargetIdAndCode(String scope, String targetId, String code);
    List<Role> findByScopeAndTargetId(String scope, String targetId);
}

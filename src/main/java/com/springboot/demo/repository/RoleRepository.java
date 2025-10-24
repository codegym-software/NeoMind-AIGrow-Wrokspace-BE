package com.springboot.demo.repository;

import com.springboot.demo.model.Role;
import com.springboot.demo.model.enums.Scope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    List<Role> findByScopeAndTargetId(Scope scope, String targetId);
    List<Role> findByScopeAndTargetIdIsNull(Scope scope); // template roles của scope
    boolean existsByScopeAndTargetIdAndCode(Scope scope, String targetId, String code);
    Optional<Role> findByScopeAndCode(Scope scope, String code);
    Optional<Role> findByNameAndScope(String name, Scope scope);
}

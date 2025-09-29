package com.springboot.demo.repository;

import com.springboot.demo.model.Role;
import com.springboot.demo.model.Scope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
//import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByScope(Scope scope);
    boolean existsByScopeAndCode(Scope scope, String code);
    //Optional<Role> findByScopeAndCode(String scope, String code);
}

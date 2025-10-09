package com.springboot.demo.repository;

import com.springboot.demo.model.enums.Scope;

import java.util.Set;

public interface RbacRepository {

    boolean userHasPermissionInScope(String userId, Scope scope, String targetId, String permCode);

    Set<String> findPermissionCodesOfUserInScope(String userId, Scope scope, String targetId);


}

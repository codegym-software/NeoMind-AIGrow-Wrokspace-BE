package com.springboot.demo.repository;

import java.util.Set;

import com.springboot.demo.model.Enum.Scope;

public interface RbacRepository {

    boolean userHasPermissionInScope(String userId, Scope scope, String targetId, String permCode);

    Set<String> findPermissionCodesOfUserInScope(String userId, Scope scope, String targetId);


}

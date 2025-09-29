package com.springboot.demo.repository;

import com.springboot.demo.model.Scope;

import java.util.Set;

public interface RbacRepository {
    /** Trả về toàn bộ permission code của user trong 1 scope cụ thể */
    Set<String> findPermissionCodesOfUserInScope(String userId, Scope scope, String scopeId);
}

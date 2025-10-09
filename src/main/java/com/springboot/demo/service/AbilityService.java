package com.springboot.demo.service;

import com.springboot.demo.model.enums.Scope;

public interface AbilityService {
    /**
     * Kiểm tra user có permCode trong 1 scope cụ thể không.
     * @param userId   id (sub trong JWT)
     * @param scope    company | workspace | project
     * @param scopeId  id của scope (companyId/workspaceId/projectId)
     * @param permCode ví dụ "project:create", "task:read", "project:crud"
     */
    boolean hasPermission(String userId, Scope scope, String scopeId, String permCode);
}

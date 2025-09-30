package com.springboot.demo.service.impl;

import com.springboot.demo.model.Scope;
import com.springboot.demo.repository.RbacRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RbacRepositoryImpl implements RbacRepository {

    private final JdbcTemplate jdbc;

    @Override
    public Set<String> findPermissionCodesOfUserInScope(String userId, Scope scope, String scopeId) {
        String sql;
        Object[] args;

        switch (scope) {
            case company -> {
                // company_members (company_id, user_id, role_id)
                sql = """
            SELECT p.code
            FROM company_members m
            JOIN roles r ON r.id = m.role_id AND r.scope = 'company'
            JOIN role_permission rp ON rp.role_id = r.id
            JOIN permissions p ON p.id = rp.permission_id AND p.scope = 'company'
            WHERE m.company_id = ? AND m.user_id = ?""";
                args = new Object[]{ scopeId, userId };
            }
            case workspace -> {
                // workspace_members (workspace_id, user_id, role_id)
                sql = """
            SELECT p.code
            FROM workspace_members m
            JOIN roles r ON r.id = m.role_id AND r.scope = 'workspace'
            JOIN role_permission rp ON rp.role_id = r.id
            JOIN permissions p ON p.id = rp.permission_id AND p.scope = 'workspace'
            WHERE m.workspace_id = ? AND m.user_id = ?""";
                args = new Object[]{ scopeId, userId };
            }
            case project -> {
                // project_members (project_id, user_id, role_id)
                sql = """
            SELECT p.code
            FROM project_members m
            JOIN roles r ON r.id = m.role_id AND r.scope = 'project'
            JOIN role_permission rp ON rp.role_id = r.id
            JOIN permissions p ON p.id = rp.permission_id AND p.scope = 'project'
            WHERE m.project_id = ? AND m.user_id = ?""";
                args = new Object[]{ scopeId, userId };
            }
            default -> {
                return Set.of();
            }
        }

        return new HashSet<>(jdbc.queryForList(sql, String.class, args));
    }
}

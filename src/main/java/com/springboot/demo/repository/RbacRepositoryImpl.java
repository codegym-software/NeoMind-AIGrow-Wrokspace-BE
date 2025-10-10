package com.springboot.demo.repository;

import com.springboot.demo.model.enums.Scope;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RbacRepositoryImpl implements RbacRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public boolean userHasPermissionInScope(String userId, Scope scope, String targetId, String permCode) {
        String sql = switch (scope) {
            case company -> """
        SELECT EXISTS(
          SELECT 1
          FROM company_members cm
          JOIN roles r            ON r.id = cm.role_id AND r.scope = 'company'
          JOIN role_permission rp ON rp.role_id = r.id
          JOIN permissions p      ON p.id = rp.permission_id AND p.scope = 'company'
          WHERE cm.user_id = :userId
            AND cm.company_id = :targetId
            AND (r.target_id IS NULL OR r.target_id = :targetId)
            AND p.code = :perm
        )
        """;
            case workspace -> """
        SELECT EXISTS(
          SELECT 1
          FROM workspace_members wm
          JOIN roles r            ON r.id = wm.role_id AND r.scope = 'workspace'
          JOIN role_permission rp ON rp.role_id = r.id
          JOIN permissions p      ON p.id = rp.permission_id AND p.scope = 'workspace'
          WHERE wm.user_id = :userId
            AND wm.workspace_id = :targetId
            AND (r.target_id IS NULL OR r.target_id = :targetId)
            AND p.code = :perm
        )
        """;
            case project -> """
        SELECT EXISTS(
          SELECT 1
          FROM project_members pm
          JOIN roles r            ON r.id = pm.role_id AND r.scope = 'project'
          JOIN role_permission rp ON rp.role_id = r.id
          JOIN permissions p      ON p.id = rp.permission_id AND p.scope = 'project'
          WHERE pm.user_id = :userId
            AND pm.project_id = :targetId
            AND (r.target_id IS NULL OR r.target_id = :targetId)
            AND p.code = :perm
        )
        """;
        };

        var params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("targetId", targetId)
                .addValue("perm", permCode);

        Boolean ok = jdbc.queryForObject(sql, params, Boolean.class);
        return Boolean.TRUE.equals(ok);
    }

    @Override
    public Set<String> findPermissionCodesOfUserInScope(String userId, Scope scope, String targetId) {
        String sql = switch (scope) {
            case company -> """
        SELECT DISTINCT p.code
        FROM company_members cm
        JOIN roles r            ON r.id = cm.role_id AND r.scope = 'company'
        LEFT JOIN role_permission rp ON rp.role_id = r.id
        LEFT JOIN permissions p      ON p.id = rp.permission_id AND p.scope = 'company'
        WHERE cm.user_id = :userId
          AND cm.company_id = :targetId
          AND (r.target_id IS NULL OR r.target_id = :targetId)
          AND p.code IS NOT NULL
        """;
            case workspace -> """
        SELECT DISTINCT p.code
        FROM workspace_members wm
        JOIN roles r            ON r.id = wm.role_id AND r.scope = 'workspace'
        LEFT JOIN role_permission rp ON rp.role_id = r.id
        LEFT JOIN permissions p      ON p.id = rp.permission_id AND p.scope = 'workspace'
        WHERE wm.user_id = :userId
          AND wm.workspace_id = :targetId
          AND (r.target_id IS NULL OR r.target_id = :targetId)
          AND p.code IS NOT NULL
        """;
            case project -> """
        SELECT DISTINCT p.code
        FROM project_members pm
        JOIN roles r            ON r.id = pm.role_id AND r.scope = 'project'
        LEFT JOIN role_permission rp ON rp.role_id = r.id
        LEFT JOIN permissions p      ON p.id = rp.permission_id AND p.scope = 'project'
        WHERE pm.user_id = :userId
          AND pm.project_id = :targetId
          AND (r.target_id IS NULL OR r.target_id = :targetId)
          AND p.code IS NOT NULL
        """;
        };

        var params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("targetId", targetId);

        return new HashSet<>(jdbc.queryForList(sql, params, String.class));
    }
}

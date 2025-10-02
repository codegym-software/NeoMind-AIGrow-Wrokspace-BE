package com.springboot.demo.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class MembershipCleanupRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Transactional
    public int deleteWorkspaceMembershipsByCompanyForUser(String companyId, String userId) {
        String sql = """
            DELETE FROM workspace_members
            WHERE user_id = :userId
              AND workspace_id IN (
                  SELECT id FROM workspaces WHERE company_id = :companyId
              )
        """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("companyId", companyId));
    }

    @Transactional
    public int deleteProjectMembershipsByCompanyForUser(String companyId, String userId) {
        String sql = """
            DELETE FROM project_members
            WHERE user_id = :userId
              AND project_id IN (
                  SELECT p.id
                  FROM projects p
                  JOIN workspaces w ON w.id = p.workspace_id
                  WHERE w.company_id = :companyId
              )
        """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("companyId", companyId));
    }
}

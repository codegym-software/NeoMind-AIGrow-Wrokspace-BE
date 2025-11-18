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

    // ========== Company deletion cleanup methods ==========

    @Transactional
    public int deleteCommentMentionsByCompany(String companyId) {
        String sql = """
            DELETE cm FROM comment_mentions cm
            INNER JOIN comments c ON c.id = cm.comment_id
            INNER JOIN tasks t ON t.id = c.task_id
            INNER JOIN projects p ON p.id = t.project_id
            INNER JOIN workspaces w ON w.id = p.workspace_id
            WHERE w.company_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteCommentsByCompany(String companyId) {
        String sql = """
            DELETE c FROM comments c
            INNER JOIN tasks t ON t.id = c.task_id
            INNER JOIN projects p ON p.id = t.project_id
            INNER JOIN workspaces w ON w.id = p.workspace_id
            WHERE w.company_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteTaskTagsByCompany(String companyId) {
        String sql = """
            DELETE tt FROM task_tags tt
            INNER JOIN tasks t ON t.id = tt.task_id
            INNER JOIN projects p ON p.id = t.project_id
            INNER JOIN workspaces w ON w.id = p.workspace_id
            WHERE w.company_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteTagsByCompany(String companyId) {
        String sql = """
            DELETE t FROM tags t
            INNER JOIN projects p ON p.id = t.project_id
            INNER JOIN workspaces w ON w.id = p.workspace_id
            WHERE w.company_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteTasksByCompany(String companyId) {
        String sql = """
            DELETE t FROM tasks t
            INNER JOIN projects p ON p.id = t.project_id
            INNER JOIN workspaces w ON w.id = p.workspace_id
            WHERE w.company_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteSprintsByCompany(String companyId) {
        String sql = """
            DELETE s FROM sprints s
            INNER JOIN projects p ON p.id = s.project_id
            INNER JOIN workspaces w ON w.id = p.workspace_id
            WHERE w.company_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteEpicsByCompany(String companyId) {
        String sql = """
            DELETE e FROM epics e
            INNER JOIN projects p ON p.id = e.project_id
            INNER JOIN workspaces w ON w.id = p.workspace_id
            WHERE w.company_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteBoardColumnsByCompany(String companyId) {
        String sql = """
            DELETE bc FROM board_columns bc
            INNER JOIN boards b ON b.id = bc.board_id
            INNER JOIN projects p ON p.id = b.project_id
            INNER JOIN workspaces w ON w.id = p.workspace_id
            WHERE w.company_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteBoardsByCompany(String companyId) {
        String sql = """
            DELETE b FROM boards b
            INNER JOIN projects p ON p.id = b.project_id
            INNER JOIN workspaces w ON w.id = p.workspace_id
            WHERE w.company_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteProjectMembersByCompany(String companyId) {
        String sql = """
            DELETE pm FROM project_members pm
            INNER JOIN projects p ON p.id = pm.project_id
            INNER JOIN workspaces w ON w.id = p.workspace_id
            WHERE w.company_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteProjectRolesByCompany(String companyId) {
        String sql = """
            DELETE r FROM roles r
            INNER JOIN projects p ON p.id = r.target_id
            INNER JOIN workspaces w ON w.id = p.workspace_id
            WHERE r.scope = 'project' AND w.company_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteProjectRolePermissionsByCompany(String companyId) {
        String sql = """
            DELETE rp FROM role_permission rp
            INNER JOIN roles r ON r.id = rp.role_id
            INNER JOIN projects p ON p.id = r.target_id
            INNER JOIN workspaces w ON w.id = p.workspace_id
            WHERE r.scope = 'project' AND w.company_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteProjectsByCompany(String companyId) {
        String sql = """
            DELETE p FROM projects p
            INNER JOIN workspaces w ON w.id = p.workspace_id
            WHERE w.company_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteWorkspaceMembersByCompany(String companyId) {
        String sql = """
            DELETE wm FROM workspace_members wm
            INNER JOIN workspaces w ON w.id = wm.workspace_id
            WHERE w.company_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteWorkspaceRolesByCompany(String companyId) {
        String sql = """
            DELETE r FROM roles r
            INNER JOIN workspaces w ON w.id = r.target_id
            WHERE r.scope = 'workspace' AND w.company_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteWorkspaceRolePermissionsByCompany(String companyId) {
        String sql = """
            DELETE rp FROM role_permission rp
            INNER JOIN roles r ON r.id = rp.role_id
            INNER JOIN workspaces w ON w.id = r.target_id
            WHERE r.scope = 'workspace' AND w.company_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteCompanyRolesByCompany(String companyId) {
        String sql = """
            DELETE r FROM roles r
            WHERE r.scope = 'company' AND r.target_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }

    @Transactional
    public int deleteCompanyRolePermissionsByCompany(String companyId) {
        String sql = """
            DELETE rp FROM role_permission rp
            INNER JOIN roles r ON r.id = rp.role_id
            WHERE r.scope = 'company' AND r.target_id = :companyId
        """;
        return jdbc.update(sql, new MapSqlParameterSource().addValue("companyId", companyId));
    }
}

package com.springboot.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionService {

    private final JdbcTemplate jdbc;
    private static final String DELETE_SQL = "DELETE FROM role_permission WHERE role_id = ?";
    private static final String INSERT_SQL = "INSERT INTO role_permission(role_id, permission_id) VALUES(?, ?)";

    @Transactional
    public void replaceRolePermissions(Long roleId, List<Long> permissionIds) {
        jdbc.update(DELETE_SQL, roleId);
        if (permissionIds == null || permissionIds.isEmpty()) return;

        jdbc.batchUpdate(INSERT_SQL, permissionIds, permissionIds.size(),
                (ps, pid) -> { ps.setLong(1, roleId); ps.setLong(2, pid); });
    }
}

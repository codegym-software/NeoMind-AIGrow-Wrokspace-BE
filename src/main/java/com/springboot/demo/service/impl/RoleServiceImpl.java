package com.springboot.demo.service.impl;

import com.springboot.demo.dto.RolePermission.CreateRoleReq;
import com.springboot.demo.dto.RolePermission.UpdateRoleReq;
import com.springboot.demo.dto.RolePermission.RoleDto;
import com.springboot.demo.model.Permission;
import com.springboot.demo.model.Role;
import com.springboot.demo.model.enums.Scope;
import com.springboot.demo.repository.RoleRepository;
import com.springboot.demo.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.springboot.demo.util.ScopeUtils.parseScope;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository repo;
    private final NamedParameterJdbcTemplate jdbc;

    public List<Permission> getProjectRolePermissions(String projectId, Integer roleId) {
        // Validate role thuộc project (hoặc template project)
        Role r = repo.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role không tồn tại"));
        if (r.getScope() != Scope.project) {
            throw new IllegalArgumentException("Role không thuộc scope project");
        }
        if (r.getTargetId() != null && !r.getTargetId().equals(projectId)) {
            throw new IllegalArgumentException("Role không thuộc project này");
        }

        return jdbc.query("""
            SELECT p.* FROM permissions p
            JOIN role_permission rp ON p.id = rp.permission_id
            WHERE rp.role_id = :roleId
            """, new MapSqlParameterSource("roleId", roleId), new BeanPropertyRowMapper<>(Permission.class));
    }

    @Override
    public RoleDto getProjectRoleById(String projectId, Integer roleId) {
        Role role = repo.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role không tồn tại"));
        if (role.getScope() != Scope.project) {
            throw new IllegalArgumentException("Role không thuộc scope project");
        }
        if (role.getTargetId() != null && !role.getTargetId().equals(projectId)) {
            throw new IllegalArgumentException("Role không thuộc project này");
        }
        return RoleDto.from(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> list(String scopeStr, String targetId, boolean includeGlobal) {
        Scope scope = parseScope(scopeStr);
        var roles = new ArrayList<Role>();
        if (targetId == null || targetId.isBlank()) {
            roles.addAll(repo.findByScopeAndTargetIdIsNull(scope));
        } else {
            roles.addAll(repo.findByScopeAndTargetId(scope, targetId));
            if (includeGlobal) roles.addAll(repo.findByScopeAndTargetIdIsNull(scope));
        }
        return roles.stream().map(RoleDto::from).toList();
    }

    @Override
    @Transactional
    public RoleDto create(CreateRoleReq req) {
        Scope scope = parseScope(req.scope());
        String targetId = (req.targetId() == null || req.targetId().isBlank()) ? null : req.targetId().trim();
        String code = req.code().trim();

        if (repo.existsByScopeAndTargetIdAndCode(scope, targetId, code)) {
            throw new IllegalArgumentException("Role code đã tồn tại trong scope/target này");
        }

        Role r = new Role();
        r.setScope(scope);
        r.setTargetId(targetId);
        r.setCode(code);
        r.setName(req.name().trim());
        r.setDescription(req.description());
        return RoleDto.from(repo.save(r));
    }

    @Override
    @Transactional
    public RoleDto update(Integer id, UpdateRoleReq req) {
        Role r = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Role không tồn tại"));
        r.setName(req.name().trim());
        r.setDescription(req.description());
        return RoleDto.from(repo.save(r));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> listCompanyRoles(String companyId, boolean includeGlobal) {
        Scope scope = Scope.company;
        var roles = new ArrayList<Role>();
        roles.addAll(repo.findByScopeAndTargetId(scope, companyId));
        if (includeGlobal) {
            roles.addAll(repo.findByScopeAndTargetIdIsNull(scope));
        }
        return roles.stream().map(RoleDto::from).toList();
    }

    @Override
    @Transactional
    public RoleDto createCompanyRole(String companyId, CreateRoleReq req) {
        Scope scope = Scope.company;
        String code = req.code().trim();

        if (repo.existsByScopeAndTargetIdAndCode(scope, companyId, code)) {
            throw new IllegalArgumentException("Role code đã tồn tại trong company này");
        }

        Role r = new Role();
        r.setScope(scope);
        r.setTargetId(companyId);
        r.setCode(code);
        r.setName(req.name().trim());
        r.setDescription(req.description());
        return RoleDto.from(repo.save(r));
    }

    @Override
    @Transactional
    public RoleDto updateCompanyRole(String companyId, Integer roleId, UpdateRoleReq req) {
        Role r = repo.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role không tồn tại"));
        if (r.getScope() != Scope.company) {
            throw new IllegalArgumentException("Role không thuộc scope company");
        }
        if (r.getTargetId() != null && !r.getTargetId().equals(companyId)) {
            throw new IllegalArgumentException("Role không thuộc company này");
        }
        r.setName(req.name().trim());
        r.setDescription(req.description());
        return RoleDto.from(repo.save(r));
    }

    @Override
    @Transactional
    public void deleteCompanyRole(String companyId, Integer roleId) {
        Role r = repo.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role không tồn tại"));
        if (r.getScope() != Scope.company) {
            throw new IllegalArgumentException("Role không thuộc scope company");
        }
        if (r.getTargetId() != null && !r.getTargetId().equals(companyId)) {
            throw new IllegalArgumentException("Role không thuộc company này");
        }
        repo.delete(r);
    }

    @Override
    @Transactional
    public void replaceRolePermissions(String companyId, Integer roleId, List<Integer> permissionIds) {
        // Validate role thuộc company (hoặc template company)
        Role r = repo.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role không tồn tại"));
        if (r.getScope() != Scope.company) {
            throw new IllegalArgumentException("Role không thuộc scope company");
        }
        if (r.getTargetId() != null && !r.getTargetId().equals(companyId)) {
            throw new IllegalArgumentException("Role không thuộc company này");
        }

        // Chặn sửa permissions của role mặc định: chỉ ADMIN
        if (r.getTargetId() != null && ("admin".equalsIgnoreCase(r.getCode()))) {
            throw new AccessDeniedException("Không thể sửa permissions của role mặc định (admin)");
        }

        // Validate permissionIds đều thuộc scope 'company'
        if (permissionIds != null && !permissionIds.isEmpty()) {
            Integer cnt = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM permissions WHERE id IN (:ids) AND scope = 'company'",
                    new MapSqlParameterSource().addValue("ids", permissionIds),
                    Integer.class
            );
            if (cnt == null || cnt != permissionIds.size()) {
                throw new IllegalArgumentException("Danh sách permission chứa phần tử không thuộc scope company");
            }
        }

        // Thay toàn bộ mapping
        jdbc.update("DELETE FROM role_permission WHERE role_id = :roleId",
                new MapSqlParameterSource().addValue("roleId", roleId));

        if (permissionIds != null && !permissionIds.isEmpty()) {
            String sql = "INSERT INTO role_permission(role_id, permission_id) VALUES (:rid, :pid)";
            for (Integer pid : permissionIds) {
                jdbc.update(sql, new MapSqlParameterSource()
                        .addValue("rid", roleId)
                        .addValue("pid", pid));
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> listWorkspaceRoles(String workspaceId) {
        Scope scope = Scope.workspace;
        var roles = new ArrayList<Role>();
        roles.addAll(repo.findByScopeAndTargetId(scope, workspaceId));
        roles.addAll(repo.findByScopeAndTargetIdIsNull(scope));
        return roles.stream().map(RoleDto::from).toList();
    }

    @Override
    @Transactional
    public RoleDto createWorkspaceRole(String workspaceId, CreateRoleReq req) {
        Scope scope = Scope.workspace;
        String code = req.code().trim();

        if (repo.existsByScopeAndTargetIdAndCode(scope, workspaceId, code)) {
            throw new IllegalArgumentException("Role code đã tồn tại trong workspace này");
        }

        Role r = new Role();
        r.setScope(scope);
        r.setTargetId(workspaceId);
        r.setCode(code);
        r.setName(req.name().trim());
        r.setDescription(req.description());
        return RoleDto.from(repo.save(r));
    }

    @Override
    @Transactional
    public RoleDto updateWorkspaceRole(String workspaceId, Integer roleId, UpdateRoleReq req) {
        Role r = repo.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role không tồn tại"));
        if (r.getScope() != Scope.workspace) {
            throw new IllegalArgumentException("Role không thuộc scope workspace");
        }
        if (r.getTargetId() != null && !r.getTargetId().equals(workspaceId)) {
            throw new IllegalArgumentException("Role không thuộc workspace này");
        }
        r.setName(req.name().trim());
        r.setDescription(req.description());
        return RoleDto.from(repo.save(r));
    }

    @Override
    @Transactional
    public void deleteWorkspaceRole(String workspaceId, Integer roleId) {
        Role r = repo.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role không tồn tại"));
        if (r.getScope() != Scope.workspace) {
            throw new IllegalArgumentException("Role không thuộc scope workspace");
        }
        if (r.getTargetId() != null && !r.getTargetId().equals(workspaceId)) {
            throw new IllegalArgumentException("Role không thuộc workspace này");
        }
        repo.delete(r);
    }

    @Override
    @Transactional
    public void replaceWorkspaceRolePermissions(String workspaceId, Integer roleId, List<Integer> permissionIds) {
        // Validate role thuộc workspace (hoặc template workspace)
        Role r = repo.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role không tồn tại"));
        if (r.getScope() != Scope.workspace) {
            throw new IllegalArgumentException("Role không thuộc scope workspace");
        }
        if (r.getTargetId() != null && !r.getTargetId().equals(workspaceId)) {
            throw new IllegalArgumentException("Role không thuộc workspace này");
        }

        // Chặn sửa permissions của role mặc định: chỉ ADMIN
        if (r.getTargetId() != null && ("admin".equalsIgnoreCase(r.getCode()))) {
            throw new AccessDeniedException("Không thể sửa permissions của role mặc định (admin)");
        }

        // Thực thi thay đổi (đã kiểm tra guard ở trên)
        replaceWorkspaceRolePermissionsUnchecked(workspaceId, roleId, permissionIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> listProjectRoles(String projectId) {
        Scope scope = Scope.project;
        var roles = new ArrayList<Role>();
        roles.addAll(repo.findByScopeAndTargetId(scope, projectId));
        roles.addAll(repo.findByScopeAndTargetIdIsNull(scope));
        return roles.stream().map(RoleDto::from).toList();
    }

    @Override
    @Transactional
    public RoleDto createProjectRole(String projectId, CreateRoleReq req) {
        Scope scope = Scope.project;
        String code = req.code().trim();

        if (repo.existsByScopeAndTargetIdAndCode(scope, projectId, code)) {
            throw new IllegalArgumentException("Role code đã tồn tại trong project này");
        }

        Role r = new Role();
        r.setScope(scope);
        r.setTargetId(projectId);
        r.setCode(code);
        r.setName(req.name().trim());
        r.setDescription(req.description());
        return RoleDto.from(repo.save(r));
    }

    @Override
    @Transactional
    public RoleDto updateProjectRole(String projectId, Integer roleId, UpdateRoleReq req) {
        Role r = repo.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role không tồn tại"));
        if (r.getScope() != Scope.project) {
            throw new IllegalArgumentException("Role không thuộc scope project");
        }
        if (r.getTargetId() != null && !r.getTargetId().equals(projectId)) {
            throw new IllegalArgumentException("Role không thuộc project này");
        }
        r.setName(req.name().trim());
        r.setDescription(req.description());
        return RoleDto.from(repo.save(r));
    }

    @Override
    @Transactional
    public void deleteProjectRole(String projectId, Integer roleId) {
        Role r = repo.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role không tồn tại"));
        if (r.getScope() != Scope.project) {
            throw new IllegalArgumentException("Role không thuộc scope project");
        }
        if (r.getTargetId() != null && !r.getTargetId().equals(projectId)) {
            throw new IllegalArgumentException("Role không thuộc project này");
        }
        repo.delete(r);
    }

    @Override
    @Transactional
    public void replaceProjectRolePermissions(String projectId, Integer roleId, List<Integer> permissionIds) {
        // Validate role thuộc project (hoặc template project)
        Role r = repo.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role không tồn tại"));
        if (r.getScope() != Scope.project) {
            throw new IllegalArgumentException("Role không thuộc scope project");
        }
        if (r.getTargetId() != null && !r.getTargetId().equals(projectId)) {
            throw new IllegalArgumentException("Role không thuộc project này");
        }
        // Chặn sửa permissions của role admin (admin code): chỉ ADMIN không cho sửa trực tiếp ở đây!
        if (r.getTargetId() != null && ("admin".equalsIgnoreCase(r.getCode()))) {
            throw new AccessDeniedException("Không thể sửa permissions của role mặc định (admin)");
        }
        // Cho phép mọi quyền hợp lệ (bao gồm CRUD epic) khi gán cho các role bình thường!
        replaceProjectRolePermissionsUnchecked(projectId, roleId, permissionIds);
    }
    

    
    /**
     * Tạo mặc định 2 role admin và member cho workspace mới
     * @param workspaceId ID của workspace mới được tạo
     */
    @Override
    @Transactional
    public void createDefaultWorkspaceRoles(String workspaceId) {
        // Tạo role admin với tất cả quyền
        CreateRoleReq adminRoleReq = new CreateRoleReq("workspace", workspaceId, "admin", "Workspace Admin", "Quản trị viên workspace");
        RoleDto adminRole = createWorkspaceRole(workspaceId, adminRoleReq);
        
        // Lấy tất cả permission của scope workspace
        List<String> adminPermissionCodes = Arrays.asList(
                "workspace:read",
                "workspace:update",
                "workspace:delete",
                "workspace:member:invite",
                "workspace:member:read",
                "workspace:member:set_user_permissions",
                "workspace:member:delete",
                "workspace:role:read",
                "workspace:role:create",
                "workspace:role:update",
                "workspace:role:delete",
                "workspace:role:grant_permission",
                "workspace:project:create",
                "workspace:crud"
        );
        
        // Lấy ID của các permission cho admin
        List<Integer> adminPermissionIds = jdbc.queryForList(
                "SELECT id FROM permissions WHERE scope = 'workspace' AND code IN (:codes)",
                new MapSqlParameterSource().addValue("codes", adminPermissionCodes),
                Integer.class
        );
        
        // Gán tất cả permission cho role admin (bỏ qua guard mặc định vì đây là khởi tạo hệ thống)
        replaceWorkspaceRolePermissionsUnchecked(workspaceId, adminRole.id(), adminPermissionIds);
        
        // Tạo role member với các quyền cơ bản
        CreateRoleReq memberRoleReq = new CreateRoleReq("workspace", workspaceId, "member", "Workspace Member", "Thành viên workspace");
        RoleDto memberRole = createWorkspaceRole(workspaceId, memberRoleReq);
        
        // Lấy các permission cơ bản cho member
        List<String> memberPermissionCodes = Arrays.asList(
                "workspace:read",
                "workspace:member:read",
                "workspace:role:read",
                "workspace:project:create"
        );
        
        // Lấy ID của các permission cho member
        List<Integer> memberPermissionIds = jdbc.queryForList(
                "SELECT id FROM permissions WHERE scope = 'workspace' AND code IN (:codes)",
                new MapSqlParameterSource().addValue("codes", memberPermissionCodes),
                Integer.class
        );
        
        // Gán permission cho role member (bỏ qua guard mặc định vì đây là khởi tạo hệ thống)
        replaceWorkspaceRolePermissionsUnchecked(workspaceId, memberRole.id(), memberPermissionIds);
    }

    /**
     * Internal helper: thay permissions cho role workspace, bỏ qua chặn role mặc định.
     * Dùng cho quá trình khởi tạo role mặc định khi tạo workspace.
     */
    private void replaceWorkspaceRolePermissionsUnchecked(String workspaceId, Integer roleId, List<Integer> permissionIds) {
        // Validate role thuộc workspace (hoặc template workspace)
        Role r = repo.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role không tồn tại"));
        if (r.getScope() != Scope.workspace) {
            throw new IllegalArgumentException("Role không thuộc scope workspace");
        }
        if (r.getTargetId() != null && !r.getTargetId().equals(workspaceId)) {
            throw new IllegalArgumentException("Role không thuộc workspace này");
        }

        // Validate permissionIds đều thuộc scope 'workspace'
        if (permissionIds != null && !permissionIds.isEmpty()) {
            Integer cnt = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM permissions WHERE id IN (:ids) AND scope = 'workspace'",
                    new MapSqlParameterSource().addValue("ids", permissionIds),
                    Integer.class
            );
            if (cnt == null || cnt != permissionIds.size()) {
                throw new IllegalArgumentException("Danh sách permission chứa phần tử không thuộc scope workspace");
            }
        }

        // Thay toàn bộ mapping
        jdbc.update("DELETE FROM role_permission WHERE role_id = :roleId",
                new MapSqlParameterSource().addValue("roleId", roleId));

        if (permissionIds != null && !permissionIds.isEmpty()) {
            String sql = "INSERT INTO role_permission(role_id, permission_id) VALUES (:rid, :pid)";
            for (Integer pid : permissionIds) {
                jdbc.update(sql, new MapSqlParameterSource()
                        .addValue("rid", roleId)
                        .addValue("pid", pid));
            }
        }
    }

    /**
     * Tạo mặc định 2 role admin và member cho project mới
     * @param projectId ID của project mới được tạo
     */
    @Override
    @Transactional
    public void createDefaultProjectRoles(String projectId) {
        // Tạo role admin với các quyền theo yêu cầu
        CreateRoleReq adminRoleReq = new CreateRoleReq("project", projectId, "admin", "Project Admin", "Quản trị viên project");
        RoleDto adminRole = createProjectRole(projectId, adminRoleReq);

        List<String> adminPermissionCodes = Arrays.asList(
                "project:read",
                "project:update",
                "project:delete",
                "project:task:read",
                "project:task:update",
                "project:task:create",
                "project:task:delete",
                "project:sprint:read",
                "project:sprint:update",
                "project:sprint:create",
                "project:sprint:delete",
                "project:role:read",
                "project:role:create",
                "project:role:update",
                "project:role:delete",
                "project:role:grant_permission",
                "project:member:invite",
                "project:member:read",
                "project:member:set_user_permissions",
                "project:member:delete",
                "project:member:transfer_ownership",
                "project:epic:read",
                "project:epic:update",
                "project:epic:create",
                "project:epic:delete"
        );

        List<Integer> adminPermissionIds = jdbc.queryForList(
                "SELECT id FROM permissions WHERE scope = 'project' AND code IN (:codes)",
                new MapSqlParameterSource().addValue("codes", adminPermissionCodes),
                Integer.class
        );

        // Gán quyền cho role admin (bỏ qua guard mặc định vì đây là khởi tạo hệ thống)
        replaceProjectRolePermissionsUnchecked(projectId, adminRole.id(), adminPermissionIds);

        // Tạo role member với các quyền theo yêu cầu
        CreateRoleReq memberRoleReq = new CreateRoleReq("project", projectId, "member", "Project Member", "Thành viên project");
        RoleDto memberRole = createProjectRole(projectId, memberRoleReq);

        List<String> memberPermissionCodes = Arrays.asList(
                "project:read",
                "project:task:read",
                "project:task:update",
                "project:task:create",
                "project:task:delete",
                "project:sprint:read",
                "project:sprint:update",
                "project:sprint:create",
                "project:sprint:delete",
                "project:role:read",
                "project:member:read"
        );

        List<Integer> memberPermissionIds = jdbc.queryForList(
                "SELECT id FROM permissions WHERE scope = 'project' AND code IN (:codes)",
                new MapSqlParameterSource().addValue("codes", memberPermissionCodes),
                Integer.class
        );

        // Gán quyền cho role member (bỏ qua guard mặc định vì đây là khởi tạo hệ thống)
        replaceProjectRolePermissionsUnchecked(projectId, memberRole.id(), memberPermissionIds);
    }
    /**
     * Internal helper: thay permissions cho role project, bỏ qua chặn role mặc định.
     * Dùng cho quá trình khởi tạo role mặc định khi tạo project.
     */
    private void replaceProjectRolePermissionsUnchecked(String projectId, Integer roleId, List<Integer> permissionIds) {
        // Validate role thuộc project (hoặc template project)
        Role r = repo.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role không tồn tại"));
        if (r.getScope() != Scope.project) {
            throw new IllegalArgumentException("Role không thuộc scope project");
        }
        if (r.getTargetId() != null && !r.getTargetId().equals(projectId)) {
            throw new IllegalArgumentException("Role không thuộc project này");
        }

        // Validate permissionIds đều thuộc scope 'project'
        if (permissionIds != null && !permissionIds.isEmpty()) {
            Integer cnt = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM permissions WHERE id IN (:ids) AND scope = 'project'",
                    new MapSqlParameterSource().addValue("ids", permissionIds),
                    Integer.class
            );
            if (cnt == null || cnt != permissionIds.size()) {
                throw new IllegalArgumentException("Danh sách permission chứa phần tử không thuộc scope project");
            }
        }

        // Thay toàn bộ mapping
        jdbc.update("DELETE FROM role_permission WHERE role_id = :roleId",
                new MapSqlParameterSource().addValue("roleId", roleId));

        if (permissionIds != null && !permissionIds.isEmpty()) {
            String sql = "INSERT INTO role_permission(role_id, permission_id) VALUES (:rid, :pid)";
            for (Integer pid : permissionIds) {
                jdbc.update(sql, new MapSqlParameterSource()
                        .addValue("rid", roleId)
                        .addValue("pid", pid));
            }
        }
    }
}

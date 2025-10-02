package com.springboot.demo.service.impl;

import com.springboot.demo.dto.RolePermission.CreateRoleReq;
import com.springboot.demo.dto.RolePermission.UpdateRoleReq;
import com.springboot.demo.dto.RolePermission.RoleDto;
import com.springboot.demo.model.Scope;
import com.springboot.demo.model.Role;
import com.springboot.demo.repository.RoleRepository;
import com.springboot.demo.service.RoleService;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.springboot.demo.util.ScopeUtils.parseScope;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository repo;
    private final NamedParameterJdbcTemplate jdbc;

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
    public RoleDto update(Long id, UpdateRoleReq req) {
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
    public RoleDto updateCompanyRole(String companyId, Long roleId, UpdateRoleReq req) {
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
    public void deleteCompanyRole(String companyId, Long roleId) {
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
    public void replaceRolePermissions(String companyId, Long roleId, List<Long> permissionIds) {
        // Validate role thuộc company (hoặc template company)
        Role r = repo.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role không tồn tại"));
        if (r.getScope() != Scope.company) {
            throw new IllegalArgumentException("Role không thuộc scope company");
        }
        if (r.getTargetId() != null && !r.getTargetId().equals(companyId)) {
            throw new IllegalArgumentException("Role không thuộc company này");
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
            for (Long pid : permissionIds) {
                jdbc.update(sql, new MapSqlParameterSource()
                        .addValue("rid", roleId)
                        .addValue("pid", pid));
            }
        }
    }
}

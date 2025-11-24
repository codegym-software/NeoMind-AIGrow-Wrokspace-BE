package com.springboot.demo.service.impl;

import com.springboot.demo.model.Role;
import com.springboot.demo.model.RolePermission;
import com.springboot.demo.model.RolePermissionId;
import com.springboot.demo.model.enums.Scope;
import com.springboot.demo.repository.RolePermissionRepository;
import com.springboot.demo.repository.RoleRepository;
import com.springboot.demo.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RolePermissionRepository rpRepo;
    private final RoleRepository roleRepo;


    @Override
    public List<Integer> getPermissionIdsByRole(Integer roleId) {
        return rpRepo.findByIdRoleId(roleId)
                .stream()
                .map(rp -> rp.getId().getPermissionId())
                .toList();
    }

    @Override
    @Transactional
    public void replaceRolePermissions(Integer roleId, List<Integer> permissionIds) {
        Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

        String codeLower = role.getCode() == null ? "" : role.getCode().toLowerCase();
        boolean isDefault = "admin".equals(codeLower) || "member".equals(codeLower);
        if (isDefault && (role.getScope() == Scope.workspace || role.getScope() == Scope.project)) {
            throw new IllegalStateException("Default roles (admin/member) cannot be modified for this scope.");
        }

        rpRepo.deleteByIdRoleId(roleId);

        List<RolePermission> toSave = permissionIds.stream()
                .map(pid -> new RolePermission(new RolePermissionId(roleId, pid)))
                .toList();

        rpRepo.saveAll(toSave);
    }
}

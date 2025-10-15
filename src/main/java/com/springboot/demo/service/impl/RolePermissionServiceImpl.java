package com.springboot.demo.service.impl;

import com.springboot.demo.model.RolePermission;
import com.springboot.demo.model.RolePermissionId;
import com.springboot.demo.repository.RbacRepository;
import com.springboot.demo.repository.RolePermissionRepository;
import com.springboot.demo.service.PermissionService;
import com.springboot.demo.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RolePermissionRepository rpRepo;
    private final RbacRepository rbacRepository;
    private final PermissionService permissionService;

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
        rpRepo.deleteByIdRoleId(roleId);

        List<RolePermission> toSave = permissionIds.stream()
                .map(pid -> new RolePermission(new RolePermissionId(roleId, pid)))
                .toList();

        rpRepo.saveAll(toSave);
    }

    @Override
    public Set<String> getPermissionCodesByRoleId(int roleId) {
        Set<String> codes = rbacRepository.getPermissionCodesByRoleId(roleId);
        if (codes != null && !codes.isEmpty()) return codes;
    }
}

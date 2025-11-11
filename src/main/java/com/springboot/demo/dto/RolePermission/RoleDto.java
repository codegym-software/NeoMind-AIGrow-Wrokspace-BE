package com.springboot.demo.dto.RolePermission;

import com.springboot.demo.model.Role;

public record RoleDto(
        Integer id,
        String scope,     // "company" | "workspace" | "project"
        String targetId,  // null = role template theo scope
        String code,
        String name,
        String description,
        boolean permissionsLocked // FE dùng để disable tick permissions với role mặc định
) {
    public static RoleDto from(Role r) {
        boolean locked = r.getTargetId() != null
                && ("admin".equalsIgnoreCase(r.getCode()));
        return new RoleDto(
                r.getId(),
                r.getScope() == null ? null : r.getScope().name(),
                r.getTargetId(),
                r.getCode(),
                r.getName(),
                r.getDescription(),
                locked
        );
    }
}

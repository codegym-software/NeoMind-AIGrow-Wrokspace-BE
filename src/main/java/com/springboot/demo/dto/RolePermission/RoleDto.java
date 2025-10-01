package com.springboot.demo.dto.RolePermission;

import com.springboot.demo.model.Role;

public record RoleDto(
        Long id,
        String scope,     // "company" | "workspace" | "project"
        String targetId,  // null = role template theo scope
        String code,
        String name,
        String description
) {
    public static RoleDto from(Role r) {
        return new RoleDto(
                r.getId(),
                r.getScope() == null ? null : r.getScope().name(),
                r.getTargetId(),
                r.getCode(),
                r.getName(),
                r.getDescription()
        );
    }
}

package com.springboot.demo.dto.workspace;

public record WorkspaceTransferOwnershipRequest(
        String toUserId,
        Integer downgradeRoleId
) {
}
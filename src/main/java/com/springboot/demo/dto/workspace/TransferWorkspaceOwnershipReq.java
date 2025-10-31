package com.springboot.demo.dto.workspace;

public record TransferWorkspaceOwnershipReq(
        String toUserId,           // userId của người nhận ownership
        Integer downgradeRoleId     // roleId cho Owner cũ sau khi chuyển (scope=workspace)
) {}



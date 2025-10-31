package com.springboot.demo.dto.workspace;

public record AddWorkspaceMemberReq(
        String userId,      // userId của user đã là member trong company
        Integer roleId       // roleId cho workspace (scope=workspace)
) {}



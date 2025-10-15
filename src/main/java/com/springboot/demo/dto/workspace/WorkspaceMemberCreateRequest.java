package com.springboot.demo.dto.workspace;


public record WorkspaceMemberCreateRequest(
        String userId,
        Integer roleId
) {
}

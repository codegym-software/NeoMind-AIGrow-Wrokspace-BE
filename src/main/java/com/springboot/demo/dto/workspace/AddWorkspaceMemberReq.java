package com.springboot.demo.dto.workspace;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record AddWorkspaceMemberReq(
        @Email(message = "Email không hợp lệ")
        @NotNull(message = "Email là bắt buộc")
        String email,
        
        @NotNull(message = "Role ID là bắt buộc")
        Integer roleId
) {
}




package com.mycompany.backend.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    // Mật khẩu thô (chỉ nhận ở create/register). Có thể null cho Google-login.
    private String password;

    private String displayName;
}

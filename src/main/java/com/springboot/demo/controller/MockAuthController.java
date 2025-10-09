package com.springboot.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Mock Authentication", description = "APIs để test authentication với mock token")
public class MockAuthController {

    @PostMapping("/mock-login")
    @Operation(summary = "Tạo mock JWT token", description = "Tạo mock JWT token để test (chỉ dùng cho development)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mock token được tạo thành công")
    })
    public Map<String, Object> mockLogin(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        // Mock authentication - chấp nhận bất kỳ username/password nào
        String mockJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJlbWFpbCI6InRlc3RAdGVzdC5jb20iLCJhdWQiOiJ0ZXN0LWNsaWVudCIsImlzcyI6Imh0dHBzOi8vZXhhbXBsZS5jb20ifQ." +
                "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        
        return Map.of(
                "access_token", mockJwtToken,
                "token_type", "Bearer",
                "expires_in", 3600,
                "message", "Mock login thành công! Sử dụng access_token để test các API khác",
                "note", "⚠️ Đây là mock token chỉ để test, không sử dụng trong production"
        );
    }

    @GetMapping("/mock-user")
    @Operation(summary = "Lấy thông tin mock user", description = "Trả về thông tin user từ mock token")
    public Map<String, Object> getMockUser() {
        return Map.of(
                "id", "1234567890",
                "name", "John Doe",
                "email", "test@test.com",
                "message", "Đây là thông tin mock user",
                "note", "Trong thực tế, thông tin này sẽ được lấy từ JWT token"
        );
    }
}



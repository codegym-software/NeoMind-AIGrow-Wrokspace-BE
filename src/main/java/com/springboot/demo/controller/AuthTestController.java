package com.springboot.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test Authentication", description = "APIs để test authentication và các chức năng cơ bản")
public class AuthTestController {

    @GetMapping("/public")
    @Operation(summary = "Test endpoint công khai", description = "Endpoint này không cần authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Map<String, Object> publicEndpoint() {
        return Map.of(
                "message", "Đây là endpoint công khai - không cần authentication",
                "timestamp", System.currentTimeMillis(),
                "status", "success"
        );
    }

    @GetMapping("/protected")
    @Operation(summary = "Test endpoint bảo vệ", description = "Endpoint này cần JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public Map<String, Object> protectedEndpoint() {
        return Map.of(
                "message", "Đây là endpoint được bảo vệ - cần JWT token",
                "timestamp", System.currentTimeMillis(),
                "status", "success"
        );
    }

    @PostMapping("/session-test")
    @Operation(summary = "Test session endpoint", description = "Endpoint để test session với JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session được tạo thành công"),
            @ApiResponse(responseCode = "401", description = "JWT token không hợp lệ")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public Map<String, Object> sessionTest() {
        return Map.of(
                "message", "Session test thành công!",
                "timestamp", System.currentTimeMillis(),
                "status", "success",
                "note", "Nếu bạn thấy message này, JWT token của bạn đã hoạt động"
        );
    }
}


package com.springboot.demo.dto.user;

public record ChangePasswordReq(String currentPassword, String newPassword, String accessToken) {
}

package com.springboot.demo.dto.company;

public class InviteRequest {
    private String email;
    private Long roleId;

    public InviteRequest() {}

    public InviteRequest(String email, Long roleId) {
        this.email = email;
        this.roleId = roleId;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}



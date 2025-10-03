package com.springboot.demo.dto.company;

public class InviteRequest {
    private String email;
    private long roleId;

    public InviteRequest() {}

    public InviteRequest(String email, long roleId) {
        this.email = email;
        this.roleId = roleId;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public long getRoleId() { return roleId; }
    public void setRoleId(long roleId) { this.roleId = roleId; }
}



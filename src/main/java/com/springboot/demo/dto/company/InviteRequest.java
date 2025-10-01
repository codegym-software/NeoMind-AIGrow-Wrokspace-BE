package com.springboot.demo.dto.company;

public class InviteRequest {
    private String email;
    private Integer roleId;

    public InviteRequest() {}

    public InviteRequest(String email, Integer roleId) {
        this.email = email;
        this.roleId = roleId;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
}



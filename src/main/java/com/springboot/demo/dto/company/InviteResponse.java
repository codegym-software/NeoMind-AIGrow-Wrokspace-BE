package com.springboot.demo.dto.company;

import com.springboot.demo.model.CompanyMember;

public class InviteResponse {
    private String status;
    private CompanyMember member;

    public InviteResponse() {}

    public InviteResponse(String status, CompanyMember member) {
        this.status = status;
        this.member = member;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public CompanyMember getMember() { return member; }
    public void setMember(CompanyMember member) { this.member = member; }
}



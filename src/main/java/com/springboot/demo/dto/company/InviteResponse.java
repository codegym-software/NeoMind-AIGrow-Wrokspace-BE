package com.springboot.demo.dto.company;

import com.springboot.demo.model.CompanyMember;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteResponse {
    private String status;
    private CompanyMember member;
}

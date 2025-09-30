package com.springboot.demo.dto.company;

import com.springboot.demo.model.CompanyMember;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InviteResponse {
    private String status; // created | existing_invite | already_member
    private CompanyMember member;
}

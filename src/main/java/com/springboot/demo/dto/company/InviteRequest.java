package com.springboot.demo.dto.company;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InviteRequest {
    private String email;
    private Integer roleId;
}

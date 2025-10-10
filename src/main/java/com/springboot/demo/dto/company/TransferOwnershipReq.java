package com.springboot.demo.dto.company;

public record TransferOwnershipReq(
        String toUserId,
        Integer downgradeRoleId // role cho Owner cũ sau khi chuyển
) {}

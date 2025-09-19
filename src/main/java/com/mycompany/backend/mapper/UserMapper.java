package com.mycompany.backend.mapper;

import com.mycompany.backend.dto.UserRequest;
import com.mycompany.backend.dto.UserResponse;
import com.mycompany.backend.model.entity.User;

public class UserMapper {

    public static User toEntity(UserRequest req) {
        if (req == null) return null;
        User u = new User();
        u.setEmail(req.getEmail().toLowerCase().trim());
        u.setDisplayName(req.getDisplayName());
        // passwordHash set ở Service sau khi hash
        return u;
    }

    public static UserResponse toResponse(User user) {
        if (user == null) return null;
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setEmail(user.getEmail());
        r.setDisplayName(user.getDisplayName());
        r.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
        r.setLastLoginAt(user.getLastLoginAt());
        r.setCreatedAt(user.getCreatedAt());
        r.setUpdatedAt(user.getUpdatedAt());
        return r;
    }
}


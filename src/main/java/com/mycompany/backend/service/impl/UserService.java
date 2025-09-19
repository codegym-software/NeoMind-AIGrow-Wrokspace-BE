package com.mycompany.backend.service.impl;

import com.mycompany.backend.dto.UserRequest;
import com.mycompany.backend.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(String id);
    UserResponse createUser(UserRequest request);
    UserResponse updateUser(String id, UserRequest request);
    void deleteUser(String id);
}

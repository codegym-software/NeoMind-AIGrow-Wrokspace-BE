package com.mycompany.backend.service.impl;

import com.mycompany.backend.dto.UserRequest;
import com.mycompany.backend.dto.UserResponse;
import com.mycompany.backend.model.entity.User;
import com.mycompany.backend.exception.BadRequestException;
import com.mycompany.backend.exception.ResourceNotFoundException;
import com.mycompany.backend.mapper.UserMapper;
import com.mycompany.backend.repository.UserRepository;
import com.mycompany.backend.service.impl.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return new PageImpl<>(
                page.getContent().stream().map(UserMapper::toResponse).collect(Collectors.toList()),
                pageable,
                page.getTotalElements()
        );
    }

    @Override
    public UserResponse getUserById(String id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserMapper.toResponse(u);
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        userRepository.findByEmail(request.getEmail().toLowerCase().trim()).ifPresent(u -> {
            throw new BadRequestException("Email already exists");
        });

        User entity = UserMapper.toEntity(request);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        entity.setStatus(User.Status.active);
        User saved = userRepository.save(entity);
        return UserMapper.toResponse(saved);
    }

    @Override
    public UserResponse updateUser(String id, UserRequest request) {
        User exist = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if (request.getEmail() != null) exist.setEmail(request.getEmail().toLowerCase().trim());
        if (request.getDisplayName() != null) exist.setDisplayName(request.getDisplayName());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            exist.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        User updated = userRepository.save(exist);
        return UserMapper.toResponse(updated);
    }

    @Override
    public void deleteUser(String id) {
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.deleteById(id);
    }
}

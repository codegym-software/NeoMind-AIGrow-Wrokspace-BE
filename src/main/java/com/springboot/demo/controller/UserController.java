package com.springboot.demo.controller;

import com.springboot.demo.dto.user.ChangePasswordReq;
import com.springboot.demo.model.User;
import com.springboot.demo.service.AccountService;
import com.springboot.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final AccountService accountService;
    private final UserService userService;
    private final com.springboot.demo.repository.UserRepository userRepository;

    @PostMapping("/session")
    public Map<String, Object> session(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            User u = userService.ensureFromJwt(jwt);

            return Map.of(
                    "id", u.getId(),
                    "email", u.getEmail(),
                    "displayName", u.getDisplayName(),
                    "isAdmin", u.isAdmin()
            );
        }
        throw new RuntimeException("Unauthenticated");
    }

    @GetMapping("/users")
    public List<Map<String, String>> getUsersByIds(@RequestParam(name = "ids") List<String> ids) {
        return userRepository.findAllById(ids).stream()
                .map(u -> Map.of(
                        "id", u.getId(),
                        "email", u.getEmail()
                ))
                .collect(Collectors.toList());
    }

    @PostMapping("account/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordReq req, Authentication auth) {
        if (!(auth instanceof JwtAuthenticationToken jwt)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        accountService.changePassword(jwt.getToken(), req.currentPassword(), req.newPassword(), req.accessToken());
        return ResponseEntity.ok(Map.of("message", "Password changed"));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequest.class)
    public Map<String, String> handleBadRequest(BadRequest ex) {
        return Map.of("error", ex.getMessage());
    }

    static class BadRequest extends RuntimeException {
        public BadRequest(String m) {
            super(m);
        }
    }
}


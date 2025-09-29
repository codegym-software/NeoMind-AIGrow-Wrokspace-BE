package com.springboot.demo.user;

import com.springboot.demo.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final AccountService accountService;
    @PostMapping("/session")
    public Map<String, Object> session(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String email = jwt.getClaimAsString("email");
            User u = userRepository.findByEmail(email).orElseThrow(
                    () -> new RuntimeException("User not ensured (filter missing?)")
            );
            return Map.of(
                    "id", u.getId(),
                    "email", u.getEmail(),
                    "displayName", u.getDisplayName(),
                    "isAdmin", u.isAdmin()
            );
        }
        throw new RuntimeException("Unauthenticated");
    }

    @PostMapping("account/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordReq req, Authentication auth) {
        if (!(auth instanceof JwtAuthenticationToken jwt)) {
            return ResponseEntity.status(401).body(Map.of("error","Unauthorized"));
        }
        accountService.changePassword(jwt.getToken(), req.currentPassword(), req.newPassword(), req.accessToken());
        return ResponseEntity.ok(Map.of("message","Password changed"));
    }

    public record ChangePasswordReq(String currentPassword, String newPassword, String accessToken) {}

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequest.class)
    public Map<String, String> handleBadRequest(BadRequest ex) {
        return Map.of("error", ex.getMessage());
    }
    static class BadRequest extends RuntimeException { public BadRequest(String m){ super(m);} }
}

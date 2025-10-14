package  com.springboot.demo.security;

import com.springboot.demo.model.User;
import com.springboot.demo.repository.UserRepository;
import com.springboot.demo.security.CognitoUserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepo;
    private final CognitoUserInfoService cognitoUserInfoService;

    public User resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new AccessDeniedException("Unauthenticated");
        }

        Jwt jwt = (Jwt) jwtAuth.getToken();
        String emailResolved = jwt.getClaimAsString("email");
        if (emailResolved == null || emailResolved.isBlank()) {
            try {
                Map<String, Object> info = cognitoUserInfoService.fetch(jwt.getTokenValue());
                Object e = info.get("email");
                if (e instanceof String s && !s.isBlank()) emailResolved = s;
            } catch (Exception ignored) {}
        }

        if (emailResolved == null || emailResolved.isBlank()) {
            throw new IllegalStateException("Cannot resolve current user (no email/username in JWT).");
        }

        Optional<User> existing = userRepo.findByEmail(emailResolved);
        if (existing.isPresent()) return existing.get();

        User u = new User();
        u.setEmail(emailResolved);
        String name = jwt.getClaimAsString("name");
        if (name == null || name.isBlank()) name = emailResolved;
        u.setDisplayName(name);
        return userRepo.save(u);
    }

    public String requireCurrentUserId() {
        return resolveCurrentUser().getId();
    }
}

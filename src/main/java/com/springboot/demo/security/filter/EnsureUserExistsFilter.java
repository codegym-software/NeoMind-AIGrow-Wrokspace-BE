package com.springboot.demo.security.filter;

import com.springboot.demo.model.User;
import com.springboot.demo.repository.UserRepository;
import com.springboot.demo.security.CognitoUserInfoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class EnsureUserExistsFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final CognitoUserInfoService cognitoUserInfoService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getRequestURI();
        return p.startsWith("/auth/") || p.startsWith("/public/") || p.equals("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = (Jwt) jwtAuth.getToken(); // Access Token đã verify
            String sub = jwt.getClaimAsString("sub");

            if (sub != null && !sub.isBlank()) {
                // 1) Lấy email/name qua UserInfo (vì access token thường không có email)
                String accessToken = jwt.getTokenValue();
                Map<String, Object> info = Map.of();
                try {
                    info = cognitoUserInfoService.fetch(accessToken); // inject bean này
                } catch (Exception ignored) {}

                String email = (String) info.getOrDefault("email", jwt.getClaimAsString("email"));
                String name  = (String) info.getOrDefault("name",
                        email != null ? email : sub);

                // 2) Nếu DB chưa có user -> tạo; nếu có thì bỏ qua
                userRepository.findByEmail(email).orElseGet(() -> {
                    User u = new User();
                    // KHÔNG ép id = sub nếu hệ thống của bạn đã dùng UUID cho id nội bộ
                    // để tránh xung đột với người dùng cũ; để null cho @PrePersist tự set UUID
                    u.setEmail(email);
                    u.setDisplayName(name);
                    try { return userRepository.save(u); }
                    catch (DataIntegrityViolationException e) { return null; }
                });

                // (tuỳ chọn) nếu bạn đang dùng RequestUserContext để lưu internal id cho request,
                // thì resolve và set vào đây (đã có sẵn trong dự án của bạn):
                // ctx.setInternalUserId(userRepository.findByEmail(email).map(User::getId).orElse(null));
            }
        }

        filterChain.doFilter(request, response);
    }


    private boolean isSuitableAsId(String sub) {
        return sub != null && !sub.isBlank() && sub.length() <= 36;
    }
}

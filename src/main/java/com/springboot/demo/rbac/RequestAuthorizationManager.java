package com.springboot.demo.rbac;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * AuthorizationManager check quyền theo URL.
 * Dùng trong SecurityConfig:
 *   .requestMatchers(HttpMethod.POST, "/api/projects/**")
 *     .access(ram.require(Scope.project, "project:create", "^/api/projects/([^/]+).*$"))
 */
@Component
@RequiredArgsConstructor
public class RequestAuthorizationManager {

    private final AbilityService ability;

    /**
     * @param scope   company|workspace|project
     * @param perm    ví dụ "project:create"
     * @param regex   regex chứa group(1) = scopeId, vd "^/api/projects/([^/]+).*$"
     */
    public AuthorizationManager<RequestAuthorizationContext> require(Scope scope, String perm, String regex) {
        Pattern path = Pattern.compile(regex);

        return (Supplier<Authentication> auth, RequestAuthorizationContext ctx) -> {
            var uri = ctx.getRequest().getRequestURI();
            var m = path.matcher(uri);
            if (!m.matches()) return new AuthorizationDecision(false);

            String scopeId = m.group(1);
            String userId = auth.get().getName(); // sub trong JWT

            boolean ok = ability.hasPermission(userId, scope, scopeId, perm);
            return new AuthorizationDecision(ok);
        };
    }
}

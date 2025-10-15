package com.springboot.demo.security.gate;

import com.springboot.demo.model.enums.Scope;
import com.springboot.demo.model.User;
import com.springboot.demo.repository.*;
import com.springboot.demo.security.CognitoUserInfoService;
import com.springboot.demo.service.AbilityService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import static com.springboot.demo.util.ScopeUtils.parseScope;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@Component("gate")
@RequiredArgsConstructor
public class GateManagerImpl implements GateManager {

    private final RbacRepository rbacRepo;
    private final CompanyMemberRepository companyMemberRepo;
    private final WorkspaceMemberRepository workspaceMemberRepo;
    private final UserRepository userRepo;
    private final ProjectRepository projectRepo;
    private final ProjectMemberRepository pmRepo;
    private final AbilityService abilityService;
    private final CognitoUserInfoService  cognitoUserInfoService;
    private final Map<String, BiFunction<AuthCtx, String, Boolean>> dynamicGates = new HashMap<>();

    @PostConstruct
    void initGates() {
        dynamicGates.put("company:transfer_ownership", (ctx, companyId) -> {
            if (companyId == null || companyId.isBlank()) return false;
            boolean isOwner = companyMemberRepo.isOwner(companyId, ctx.userId);
            if (isOwner) return true;
            return rbacRepo.userHasPermissionInScope(
                    ctx.userId, Scope.company, companyId, "company:transfer_ownership");
        });

        dynamicGates.put("system:admin", (ctx, ignore) ->
                userRepo.findById(ctx.userId)
                        .map(User::isAdmin)
                        .orElse(false)
        );

        dynamicGates.put("workspace:transfer_ownership", (ctx, workspaceId) -> {
            if (workspaceId == null || workspaceId.isBlank()) return false;
            boolean isOwner = workspaceMemberRepo.isOwner(workspaceId, ctx.userId);
            if (isOwner) return true;
            return rbacRepo.userHasPermissionInScope(
                    ctx.userId, Scope.company, workspaceId, "workspace:transfer_ownership");
        });
    }

    @Override
    public boolean allows(String ability, Authentication authentication) {
        var parsed = parseAbility(ability);
        var ctx = resolveAuth(authentication);
        return rbacRepo.userHasPermissionInScope(ctx.userId, parsed.scope, null, ability);
    }

    @Override
    public boolean allows(String ability, Authentication authentication, String targetId) {
        var scope = parse_scope(ability);
        var ctx   = resolveAuth(authentication);

        if (isOwner(scope, targetId, ctx.userId)) return true;

        var dyn = dynamicGates.get(ability);
        if (dyn != null && dyn.apply(ctx, targetId)) return true;

        return rbacRepo.userHasPermissionInScope(ctx.userId, scope, targetId, ability);
    }

    private Scope parse_scope(String ability) {
        int i = ability.indexOf(':');
        if (i < 0) throw new IllegalArgumentException("Invalid ability: " + ability);
        return Scope.valueOf(ability.substring(0, i));
    }

    private boolean isOwner(Scope scope, String targetId, String userId) {
        return switch (scope) {
            case company   -> companyMemberRepo.isOwner(targetId, userId);
            case workspace -> workspaceMemberRepo.isOwner(targetId, userId);
            case project   -> pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(targetId, userId);
        };
    }

    private record ParsedAbility(Scope scope, String code) {}
    private record AuthCtx(String userId, String email) {}

    private ParsedAbility parseAbility(String ability) {
        if (ability == null || ability.isBlank())
            throw new IllegalArgumentException("ability is null/blank");
        String prefix = ability.split(":", 2)[0];
        return new ParsedAbility(parseScope(prefix), ability);
    }

    private AuthCtx resolveAuth(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new RuntimeException("Unauthenticated");
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

        String email = emailResolved;
        String userId = userRepo.findByEmail(emailResolved)
                .orElseThrow(() -> new AccessDeniedException("User not ensured in DB for email" + email))
                .getId();

        return new AuthCtx(userId, email);
    }
}

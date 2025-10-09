package com.springboot.demo.security.gate;

import com.springboot.demo.model.Scope;
import com.springboot.demo.model.User;
import com.springboot.demo.repository.*;
import com.springboot.demo.service.AbilityService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static com.springboot.demo.util.ScopeUtils.parseScope;

@Component("gate") // dùng @PreAuthorize("@gate.allows(...)")
@RequiredArgsConstructor
public class GateManagerImpl implements GateManager {

    private final RbacRepository rbacRepo;
    private final CompanyMemberRepository companyMemberRepo;
    private final UserRepository userRepo;
    private final ProjectRepository projectRepo;
    private final ProjectMemberRepository pmRepo;
    private final AbilityService abilityService;
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

        dynamicGates.put("project:create", (ctx, target) -> {
            String workspaceId = (String) target;
            String userId = ctx.userId();

            return abilityService.hasPermission(userId, Scope.workspace, "project:create", workspaceId)
                    || abilityService.hasPermission(userId, Scope.workspace, "project:crud", workspaceId)
                    || abilityService.hasPermission(userId, Scope.workspace, "workspace:update", workspaceId);
        });

        dynamicGates.put("project:read", (ctx, target) -> {
            // target: workspaceId (list) — còn xem detail project sẽ đi nhánh "project:read" với projectId (ở fallback DB)
            String workspaceId = (String) target;
            String userId = ctx.userId();

            return abilityService.hasPermission(userId, Scope.workspace, "project:read", workspaceId)
                    || abilityService.hasPermission(userId, Scope.workspace, "project:crud", workspaceId)
                    || abilityService.hasPermission(userId, Scope.workspace, "workspace:read", workspaceId);
        });

        dynamicGates.put("project:update", (ctx, target) -> {
            // target: projectId
            String projectId = (String) target;
            String userId = ctx.userId();

            if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(projectId, userId)) return true;

            String wsId = projectRepo.findWorkspaceIdByProjectId(projectId).orElse(null);
            return abilityService.hasPermission(userId, Scope.project, "project:update", projectId)
                    || abilityService.hasPermission(userId, Scope.project, "project:crud", projectId)
                    || (wsId != null && (
                    abilityService.hasPermission(userId, Scope.project, "project:update", wsId) ||
                            abilityService.hasPermission(userId, Scope.project, "project:crud", wsId)
            ));
        });

        dynamicGates.put("project:delete", (ctx, target) -> {
            String projectId = (String) target;
            String userId = ctx.userId();

            if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(projectId, userId)) return true;

            String wsId = projectRepo.findWorkspaceIdByProjectId(projectId).orElse(null);
            return abilityService.hasPermission(userId, Scope.project, "project:delete", projectId)
                    || abilityService.hasPermission(userId, Scope.project, "project:crud", projectId)
                    || (wsId != null && (
                    abilityService.hasPermission(userId, Scope.workspace, "project:delete", wsId) ||
                            abilityService.hasPermission(userId, Scope.workspace, "project:crud", wsId)
            ));
        });
    }

    @Override
    public boolean allows(String ability, Authentication authentication) {
        // ability không gắn target (hiếm dùng cho company/workspace/project)
        var parsed = parseAbility(ability);
        var ctx = resolveAuth(authentication);
        return rbacRepo.userHasPermissionInScope(ctx.userId, parsed.scope, null, ability);
    }

    @Override
    public boolean allows(String ability, Authentication authentication, String targetId) {
        var parsed = parseAbility(ability);            // lấy scope từ tiền tố ability
        var ctx = resolveAuth(authentication);         // trích userId từ token/email
        // gate dynamic trước
        var dyn = dynamicGates.get(ability);
        if (dyn != null) {
            if (!dyn.apply(ctx, targetId)) return false;
            return true; // nếu qua gate phức tạp => OK
        }
        // gate thường -> hỏi DB
        return rbacRepo.userHasPermissionInScope(ctx.userId, parsed.scope, targetId, ability);
    }

    private record ParsedAbility(Scope scope, String code) {
    }

    private record AuthCtx(String userId, String email) {
    }

    private ParsedAbility parseAbility(String ability) {
        // eg: "company:role:create" => scope "company"
        if (ability == null || ability.isBlank())
            throw new IllegalArgumentException("ability is null/blank");
        String prefix = ability.split(":", 2)[0];
        return new ParsedAbility(parseScope(prefix), ability);
    }

    private AuthCtx resolveAuth(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwt)) {
            throw new RuntimeException("Cannot resolve email from Authentication");
        }
        var token = jwt.getToken();

        final String email = firstNonBlank(
                token.getClaimAsString("email")
        );
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Missing email in token");
        }

        String userId = userRepo.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not ensured in DB for email: " + email));

        return new AuthCtx(userId, email);
    }

    private static String firstNonBlank(String... vals) {
        if (vals == null) return null;
        for (String v : vals) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }


}

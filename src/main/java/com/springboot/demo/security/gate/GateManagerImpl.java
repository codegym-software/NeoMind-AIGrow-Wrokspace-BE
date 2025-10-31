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
    private final CognitoUserInfoService cognitoUserInfoService;
    private final Map<String, BiFunction<AuthCtx, String, Boolean>> dynamicGates = new HashMap<>();

    @PostConstruct
    void initGates() {
        dynamicGates.put("workspace:create", (ctx, target) -> {
            String companyId = (String) target;
            String userId = ctx.userId();

            // Allow if user is the owner of the company (highest priority)
            if (companyMemberRepo.isOwner(companyId, userId)) {
                return true;
            }

            // Otherwise, allow if user has 'workspace:create' or a broader 'company:update'
            // permission
            return abilityService.hasPermission(userId, Scope.company, companyId, "workspace:create")
                    || abilityService.hasPermission(userId, Scope.company, companyId, "company:update");
        });

        dynamicGates.put("company:transfer_ownership", (ctx, companyId) -> {
            if (companyId == null || companyId.isBlank())
                return false;
            boolean isOwner = companyMemberRepo.isOwner(companyId, ctx.userId);
            if (isOwner)
                return true;
            return rbacRepo.userHasPermissionInScope(
                    ctx.userId, Scope.company, companyId, "company:transfer_ownership");
        });

        dynamicGates.put("system:admin", (ctx, ignore) -> userRepo.findById(ctx.userId)
                .map(User::isAdmin)
                .orElse(false));

        dynamicGates.put("project:create", (ctx, target) -> {
            String workspaceId = (String) target;
            String userId = ctx.userId();

            // Allow if user is the owner of the workspace (highest priority)
            if (workspaceMemberRepo.isOwner(workspaceId, userId)) {
                return true;
            }

            
            return abilityService.hasPermission(userId, Scope.workspace, workspaceId, "workspace:project:create")
                    || abilityService.hasPermission(userId, Scope.workspace, workspaceId, "workspace:update");
        });

        dynamicGates.put("project:read", (ctx, target) -> {
            // target can be either workspaceId (listing) or projectId (detail)
            String userId = ctx.userId();
            String maybeProjectId = (String) target;

            // Try resolve as projectId -> workspaceId
            String wsId = projectRepo.findWorkspaceIdByProjectId(maybeProjectId).orElse(null);
            String workspaceId = wsId != null ? wsId : maybeProjectId; // if not a projectId, treat as workspaceId

            // Owner of workspace always allowed
            if (workspaceMemberRepo.isOwner(workspaceId, userId)) return true;

            // Allow via workspace-level permissions
            if (abilityService.hasPermission(userId, Scope.workspace, workspaceId, "project:read")
                    || abilityService.hasPermission(userId, Scope.workspace, workspaceId, "project:crud")
                    || abilityService.hasPermission(userId, Scope.workspace, workspaceId, "workspace:read")) {
                return true;
            }

            // If target was a concrete projectId, allow via project-scope permission as well
            if (wsId != null) {
                return abilityService.hasPermission(userId, Scope.project, maybeProjectId, "project:read")
                        || abilityService.hasPermission(userId, Scope.project, maybeProjectId, "project:crud");
            }

            return false;
        });

        dynamicGates.put("project:update", (ctx, target) -> {
            
            String projectId = (String) target;
            String userId = ctx.userId();

            if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(projectId, userId))
                return true;

            String wsId = projectRepo.findWorkspaceIdByProjectId(projectId).orElse(null);
            return abilityService.hasPermission(userId, Scope.project, projectId, "project:update")
                    || abilityService.hasPermission(userId, Scope.project, projectId, "project:crud")
                    || (wsId != null && (abilityService.hasPermission(userId, Scope.project, wsId, "project:update") ||
                            abilityService.hasPermission(userId, Scope.project, wsId, "project:crud")));
        });

        dynamicGates.put("project:delete", (ctx, target) -> {
            String projectId = (String) target;
            String userId = ctx.userId();

            if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(projectId, userId))
                return true;

            String wsId = projectRepo.findWorkspaceIdByProjectId(projectId).orElse(null);
            return abilityService.hasPermission(userId, Scope.project, projectId, "project:delete")
                    || abilityService.hasPermission(userId, Scope.project, projectId, "project:crud")
                    || (wsId != null
                            && (abilityService.hasPermission(userId, Scope.workspace, wsId, "project:delete") ||
                                    abilityService.hasPermission(userId, Scope.workspace, wsId, "project:crud")));
        });

        

        dynamicGates.put("project:task:read", (ctx, target) -> {
            String projectId = (String) target;
            String userId = ctx.userId();
           
            if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(projectId, userId)) {
                return true; 
            }

            
            return abilityService.hasPermission(userId, Scope.project, projectId, "project:task:read")
                    || abilityService.hasPermission(userId, Scope.project, projectId, "project:read");
        });

        dynamicGates.put("project:task:update", (ctx, target) -> {
            String projectId = (String) target;
            String userId = ctx.userId();
            if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(projectId, userId)) {
                return true; 
            }

           
            return abilityService.hasPermission(userId, Scope.project, projectId, "project:task:update")
                    || abilityService.hasPermission(userId, Scope.project, projectId, "project:update");
        });

        dynamicGates.put("project:task:create", (ctx, target) -> {
            String projectId = (String) target;
            String userId = ctx.userId();
            if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(projectId, userId)) {
                return true; 
            }

            
            return abilityService.hasPermission(userId, Scope.project, projectId, "project:task:create")
                   
                    || abilityService.hasPermission(userId, Scope.project, projectId, "project:update");
        });

        dynamicGates.put("project:task:delete", (ctx, target) -> {
            String projectId = (String) target;
            String userId = ctx.userId();

            
            if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(projectId, userId)) {
                return true;
            }

           
            return abilityService.hasPermission(userId, Scope.project, projectId, "project:task:delete")
                    // Thêm logic: Ai có quyền sửa project cũng có thể xóa task
                    || abilityService.hasPermission(userId, Scope.project, projectId, "project:update");
        });

        

        dynamicGates.put("project:sprint:create", (ctx, target) -> {
            String projectId = (String) target;
            String userId = ctx.userId();

            if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(projectId, userId))
                return true;

            return abilityService.hasPermission(userId, Scope.project, projectId, "project:sprint:create");
        });

        dynamicGates.put("project:sprint:read", (ctx, target) -> {
            String projectId = (String) target;
            String userId = ctx.userId();
if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(projectId, userId))
                return true;
            return abilityService.hasPermission(userId, Scope.project, projectId, "project:sprint:read");
        });

        dynamicGates.put("project:sprint:update", (ctx, target) -> {
            String projectId = (String) target;
            String userId = ctx.userId();

            if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(projectId, userId))
                return true;

            return abilityService.hasPermission(userId, Scope.project, projectId, "project:sprint:update");
        });

        dynamicGates.put("project:sprint:delete", (ctx, target) -> {
            String projectId = (String) target;
            String userId = ctx.userId();

            if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(projectId, userId))
                return true;

            return abilityService.hasPermission(userId, Scope.project, projectId, "project:sprint:delete");
        });

        dynamicGates.put("workspace:member:create", (ctx, target) -> {
            String workspaceId = (String) target;
            // Owner always has permission
            if (workspaceMemberRepo.isOwner(workspaceId, ctx.userId())) return true;
            // Check DB permission
            return abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:member:create");
        });

        dynamicGates.put("workspace:member:read", (ctx, target) -> {
            String workspaceId = (String) target;
            // Owner always has permission
            if (workspaceMemberRepo.isOwner(workspaceId, ctx.userId())) return true;
            // Check DB permissions
            return abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:member:read")
                    || abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:read");
        });

        dynamicGates.put("workspace:member:update", (ctx, target) -> {
            String workspaceId = (String) target;
            // Owner always has permission
            if (workspaceMemberRepo.isOwner(workspaceId, ctx.userId())) return true;
            // Check DB permission
            return abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:member:update");
        });

        dynamicGates.put("workspace:member:delete", (ctx, target) -> {
            String workspaceId = (String) target;
            // Owner always has permission
            if (workspaceMemberRepo.isOwner(workspaceId, ctx.userId())) return true;
            // Check DB permission
            return abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:member:delete");
        });

        dynamicGates.put("workspace:member:transfer_ownership", (ctx, target) -> {
            String workspaceId = (String) target;
            // Only owner can transfer ownership - this is hardcoded, not in DB
            return workspaceMemberRepo.isOwner(workspaceId, ctx.userId());
        });

        // Support aggregate permission for workspace update/delete
        dynamicGates.put("workspace:update", (ctx, target) -> {
            String workspaceId = (String) target;
            if (workspaceMemberRepo.isOwner(workspaceId, ctx.userId())) return true;
            return abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:update")
                    || abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:crud");
        });

        dynamicGates.put("workspace:delete", (ctx, target) -> {
            String workspaceId = (String) target;
            if (workspaceMemberRepo.isOwner(workspaceId, ctx.userId())) return true;
            return abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:delete")
                    || abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:crud");
        });

        // Workspace role management (list/create/update/delete/grant permissions)
        dynamicGates.put("workspace:role:read", (ctx, target) -> {
            String workspaceId = (String) target;
            if (workspaceMemberRepo.isOwner(workspaceId, ctx.userId())) return true;
            return abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:role:read")
                    || abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:update")
                    || abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:crud");
        });

        dynamicGates.put("workspace:role:create", (ctx, target) -> {
            String workspaceId = (String) target;
            if (workspaceMemberRepo.isOwner(workspaceId, ctx.userId())) return true;
            return abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:role:create")
                    || abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:update")
                    || abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:crud");
        });

        dynamicGates.put("workspace:role:update", (ctx, target) -> {
            String workspaceId = (String) target;
            if (workspaceMemberRepo.isOwner(workspaceId, ctx.userId())) return true;
            return abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:role:update")
                    || abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:update")
                    || abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:crud");
        });

        dynamicGates.put("workspace:role:delete", (ctx, target) -> {
            String workspaceId = (String) target;
            if (workspaceMemberRepo.isOwner(workspaceId, ctx.userId())) return true;
            return abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:role:delete")
                    || abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:update")
                    || abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:crud");
        });

        dynamicGates.put("workspace:role:grant_permission", (ctx, target) -> {
            String workspaceId = (String) target;
            if (workspaceMemberRepo.isOwner(workspaceId, ctx.userId())) return true;
            return abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:role:grant_permission")
                    || abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:update")
                    || abilityService.hasPermission(ctx.userId(), Scope.workspace, workspaceId, "workspace:crud");
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
        var ctx = resolveAuth(authentication);

        if (isOwner(scope, targetId, ctx.userId))
            return true;

        var dyn = dynamicGates.get(ability);
        if (dyn != null && dyn.apply(ctx, targetId))
            return true;

        return rbacRepo.userHasPermissionInScope(ctx.userId, scope, targetId, ability);
    }

    private Scope parse_scope(String ability) {
        int i = ability.indexOf(':');
        if (i < 0)
            throw new IllegalArgumentException("Invalid ability: " + ability);
        return Scope.valueOf(ability.substring(0, i));
    }

    // Giữ nguyên hàm isOwner (vẫn kiểm tra Workspace Owner cho Project)
    private boolean isOwner(Scope scope, String targetId, String userId) {
        return switch (scope) {
            case company -> companyMemberRepo.isOwner(targetId, userId);
            case workspace -> workspaceMemberRepo.isOwner(targetId, userId);
            case project -> {
                String workspaceId = projectRepo.findWorkspaceIdByProjectId(targetId).orElse(null);
                if (workspaceId == null)
                    yield false;
                yield workspaceMemberRepo.isOwner(workspaceId, userId);
            }
        };
    }

    private record ParsedAbility(Scope scope, String code) {
    }

    private record AuthCtx(String userId, String email) {
    }

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
                if (e instanceof String s && !s.isBlank())
                    emailResolved = s;
            } catch (Exception ignored) {
            }
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
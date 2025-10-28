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

            // SỬA LẠI: Dùng đúng tên quyền 'workspace:project:create'
            return abilityService.hasPermission(userId, Scope.workspace, workspaceId, "workspace:project:create")
                    || abilityService.hasPermission(userId, Scope.workspace, workspaceId, "workspace:update");
        });

        dynamicGates.put("project:read", (ctx, target) -> {
            // target có thể là workspaceId (khi list) hoặc projectId (khi xem detail)
            String targetId = (String) target;
            String userId = ctx.userId();

            // Trường hợp 1: Nếu target là projectId (xem detail project)
            // Kiểm tra xem user có phải project owner không
            if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(targetId, userId)) {
                return true;
            }

            // Kiểm tra quyền trực tiếp trên project
            if (abilityService.hasPermission(userId, Scope.project, targetId, "project:read")
                    || abilityService.hasPermission(userId, Scope.project, targetId, "project:crud")) {
                return true;
            }

            // Trường hợp 2: Target có thể là workspaceId (khi list projects)
            // Kiểm tra quyền trên workspace
            if (abilityService.hasPermission(userId, Scope.workspace, targetId, "project:read")
                    || abilityService.hasPermission(userId, Scope.workspace, targetId, "project:crud")
                    || abilityService.hasPermission(userId, Scope.workspace, targetId, "workspace:read")) {
                return true;
            }

            // Trường hợp 3: Nếu target là projectId, lấy workspaceId và check quyền workspace
            String wsId = projectRepo.findWorkspaceIdByProjectId(targetId).orElse(null);
            if (wsId != null) {
                // Kiểm tra user có phải workspace owner không
                if (workspaceMemberRepo.isOwner(wsId, userId)) {
                    return true;
                }
                // Kiểm tra quyền workspace
                return abilityService.hasPermission(userId, Scope.workspace, wsId, "project:read")
                        || abilityService.hasPermission(userId, Scope.workspace, wsId, "project:crud")
                        || abilityService.hasPermission(userId, Scope.workspace, wsId, "workspace:read");
            }

            return false;
        });

        dynamicGates.put("project:update", (ctx, target) -> {
            // target: projectId
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

        // --- BẮT ĐẦU SỬA LẠI LOGIC TASK ---

        dynamicGates.put("project:task:read", (ctx, target) -> {
            String projectId = (String) target;
            String userId = ctx.userId();
            // 1. Kiểm tra Project Owner trước
            if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(projectId, userId)) {
                return true; // Sửa lỗi "Unreachable code": Xóa 'return true' ở dòng dưới
            }

            // 2. Kiểm tra quyền chi tiết (dùng đúng tên quyền)
            return abilityService.hasPermission(userId, Scope.project, projectId, "project:task:read")
                    || abilityService.hasPermission(userId, Scope.project, projectId, "project:read");
        });

        dynamicGates.put("project:task:update", (ctx, target) -> {
            String projectId = (String) target;
            String userId = ctx.userId();
            if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(projectId, userId)) {
                return true; // Sửa lỗi "Unreachable code": Xóa 'return true' ở dòng dưới
            }

            // Kiểm tra đúng tên quyền
            return abilityService.hasPermission(userId, Scope.project, projectId, "project:task:update")
                    || abilityService.hasPermission(userId, Scope.project, projectId, "project:update");
        });

        dynamicGates.put("project:task:create", (ctx, target) -> {
            String projectId = (String) target;
            String userId = ctx.userId();
            if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(projectId, userId)) {
                return true; // Sửa lỗi "Unreachable code": Xóa 'return true' ở dòng dưới
            }

            // Kiểm tra đúng tên quyền 'project:task:create'
            return abilityService.hasPermission(userId, Scope.project, projectId, "project:task:create")
                    // Thêm logic: Ai có quyền sửa project cũng có thể tạo task
                    || abilityService.hasPermission(userId, Scope.project, projectId, "project:update");
        });

        dynamicGates.put("project:task:delete", (ctx, target) -> {
            String projectId = (String) target;
            String userId = ctx.userId();

            // Sửa lỗi: Gọi đúng tên hàm (IsTrue)
            if (pmRepo.existsByProjectIdAndUserIdAndOwnerIsTrue(projectId, userId)) {
                return true;
            }

            // Kiểm tra đúng tên quyền 'project:task:delete'
            return abilityService.hasPermission(userId, Scope.project, projectId, "project:task:delete")
                    // Thêm logic: Ai có quyền sửa project cũng có thể xóa task
                    || abilityService.hasPermission(userId, Scope.project, projectId, "project:update");
        });

        // --- KẾT THÚC SỬA LOGIC TASK ---

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

    // Giữ nguyên hàm isOwner của bạn (vẫn kiểm tra Workspace Owner cho Project)
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
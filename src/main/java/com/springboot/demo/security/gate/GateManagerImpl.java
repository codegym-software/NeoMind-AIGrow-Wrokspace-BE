package com.springboot.demo.security.gate;

import com.springboot.demo.model.Scope;
import com.springboot.demo.model.User;
import com.springboot.demo.repository.RbacRepository;
import com.springboot.demo.repository.CompanyMemberRepository;
import com.springboot.demo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import static com.springboot.demo.util.ScopeUtils.parseScope;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@Component("gate") // dùng @PreAuthorize("@gate.allows(...)")
@RequiredArgsConstructor
public class GateManagerImpl implements GateManager {

    private final RbacRepository rbacRepo;
    private final CompanyMemberRepository companyMemberRepo;
    private final UserRepository userRepo;

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

    private record ParsedAbility(Scope scope, String code) {}
    private record AuthCtx(String userId, String email) {}

    private ParsedAbility parseAbility(String ability) {
        // eg: "company:role:create" => scope "company"
        if (ability == null || ability.isBlank())
            throw new IllegalArgumentException("ability is null/blank");
        String prefix = ability.split(":", 2)[0];
        return new ParsedAbility(parseScope(prefix), ability);
    }

    private AuthCtx resolveAuth(Authentication authentication) {
        String email = null;
        String userId = null;

        if (authentication instanceof JwtAuthenticationToken jwt) {
            var token = jwt.getToken();
            // cognito, ưu tiên sub = userId; fallback qua email
            userId = token.getClaimAsString("sub");
            email  = token.getClaimAsString("email");
        }

        if ((userId == null || userId.isBlank()) && email != null) {
            userId = userRepo.findByEmail(email).map(u -> u.getId()).orElse(null);
        }
        if (userId == null) throw new RuntimeException("Cannot resolve userId from Authentication");

        return new AuthCtx(userId, email);
    }
}

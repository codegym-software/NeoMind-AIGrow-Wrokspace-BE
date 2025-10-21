package com.springboot.demo.service.impl;

import com.springboot.demo.model.enums.Scope;
import com.springboot.demo.repository.RbacRepository;
import com.springboot.demo.service.AbilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AbilityServiceImpl implements AbilityService {

    private final RbacRepository rbacRepository;

    private static final Map<String, Set<String>> ALIASES = Map.of(
            "crud", Set.of("create", "read", "update", "delete")
    );

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(String userId, Scope scope, String scopeId, String permCode) {
        Set<String> granted = rbacRepository.findPermissionCodesOfUserInScope(userId, scope, scopeId);
        return matches(granted, permCode);
    }

    private boolean matches(Set<String> granted, String perm) {
        if (granted.contains(perm)) return true;

        if (perm.contains(":") && perm.endsWith(":crud")) {
            String module = perm.substring(0, perm.indexOf(':'));
            return ALIASES.get("crud").stream()
                    .allMatch(a -> granted.contains(module + ":" + a));
        }

        if (ALIASES.containsKey(perm)) {
            return ALIASES.get(perm).stream().allMatch(granted::contains);
        }
        return false;
    }
}

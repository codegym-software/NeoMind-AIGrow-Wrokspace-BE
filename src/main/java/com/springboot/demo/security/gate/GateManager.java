package com.springboot.demo.security.gate;

import org.springframework.security.core.Authentication;

public interface GateManager {
    boolean allows(String ability, Authentication authentication);                // không target
    boolean allows(String ability, Authentication authentication, String target); // có targetId (company/workspace/project)
}
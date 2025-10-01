package com.springboot.demo.service;

import com.springboot.demo.model.Permission;
import java.util.List;

public interface PermissionService {
    List<Permission> listByScope(String scopeStr);
}
package com.springboot.demo.controller;

import com.springboot.demo.dto.RolePermission.CreateRoleReq;
import com.springboot.demo.dto.RolePermission.RoleDto;
import com.springboot.demo.dto.RolePermission.UpdateRoleReq;
import com.springboot.demo.dto.RolePermission.UpsertRolePermissionsReq;
import com.springboot.demo.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/roles")
@Tag(name = "Workspace Roles", description = "Quản lý role trong workspace")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class WorkspaceRoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "Lấy danh sách role của workspace", 
               description = "Lấy tất cả role thuộc workspace, có thể bao gồm role global")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách role thành công"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Thiếu quyền workspace:role:read")
    })
    @PreAuthorize("@gate.allows('workspace:role:read', authentication, #workspaceId)")
    public List<RoleDto> list(
            @Parameter(description = "Workspace ID") @PathVariable String workspaceId,
            @Parameter(description = "Bao gồm role global") @RequestParam(defaultValue = "true") boolean includeGlobal) {
        return roleService.listWorkspaceRoles(workspaceId, includeGlobal);
    }

    @PostMapping
    @Operation(summary = "Tạo role mới cho workspace", 
               description = "Tạo role riêng cho workspace cụ thể")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo role thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Thiếu quyền workspace:role:create")
    })
    @PreAuthorize("@gate.allows('workspace:role:create', authentication, #workspaceId)")
    public RoleDto create(
            @Parameter(description = "Workspace ID") @PathVariable String workspaceId, 
            @RequestBody CreateRoleReq req) {
        return roleService.createWorkspaceRole(workspaceId, req);
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Cập nhật role của workspace", 
               description = "Cập nhật thông tin role thuộc workspace")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật role thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Thiếu quyền workspace:role:update"),
            @ApiResponse(responseCode = "404", description = "Role không tồn tại")
    })
    @PreAuthorize("@gate.allows('workspace:role:update', authentication, #workspaceId)")
    public RoleDto update(
            @Parameter(description = "Workspace ID") @PathVariable String workspaceId, 
            @Parameter(description = "Role ID") @PathVariable Long roleId, 
            @RequestBody UpdateRoleReq req) {
        return roleService.updateWorkspaceRole(workspaceId, roleId, req);
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Xóa role của workspace", 
               description = "Xóa role riêng của workspace (không ảnh hưởng role global)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa role thành công"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Thiếu quyền workspace:role:delete"),
            @ApiResponse(responseCode = "404", description = "Role không tồn tại")
    })
    @PreAuthorize("@gate.allows('workspace:role:delete', authentication, #workspaceId)")
    public void delete(
            @Parameter(description = "Workspace ID") @PathVariable String workspaceId, 
            @Parameter(description = "Role ID") @PathVariable Long roleId) {
        roleService.deleteWorkspaceRole(workspaceId, roleId);
    }

    @PutMapping("/{roleId}/permissions")
    @Operation(summary = "Gán quyền cho role", 
               description = "Thay đổi danh sách quyền được gán cho role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gán quyền thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Thiếu quyền workspace:role:grant_permission"),
            @ApiResponse(responseCode = "404", description = "Role không tồn tại")
    })
    @PreAuthorize("@gate.allows('workspace:role:grant_permission', authentication, #workspaceId)")
    public void upsertPermissions(
            @Parameter(description = "Workspace ID") @PathVariable String workspaceId, 
            @Parameter(description = "Role ID") @PathVariable Long roleId,
            @RequestBody UpsertRolePermissionsReq req) {
        roleService.replaceWorkspaceRolePermissions(workspaceId, roleId, req.permissionIds());
    }
}

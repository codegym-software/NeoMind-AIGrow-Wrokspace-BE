package com.springboot.demo.controller;

import com.springboot.demo.dto.workspace.AddWorkspaceMemberReq;
import com.springboot.demo.dto.workspace.AssignWorkspaceRoleReq;
import com.springboot.demo.dto.workspace.WorkspaceMemberResponse;
import com.springboot.demo.service.WorkspaceMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/members")
@Tag(name = "Workspace Members", description = "Quản lý thành viên workspace")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class WorkspaceMemberController {

    private final WorkspaceMemberService service;

    @GetMapping
    @Operation(summary = "Lấy danh sách thành viên workspace", 
               description = "Lấy danh sách tất cả thành viên trong workspace")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành viên thành công"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public List<WorkspaceMemberResponse> list(
            @Parameter(description = "Workspace ID") @PathVariable String workspaceId) {
        return service.list(workspaceId);
    }

    @PostMapping
    @Operation(summary = "Thêm thành viên mới vào workspace", 
               description = "Mời user vào workspace bằng email và gán role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thêm thành viên thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Thiếu quyền workspace:set_user_permissions")
    })
    @PreAuthorize("@gate.allows('workspace:set_user_permissions', authentication, #workspaceId)")
    public ResponseEntity<Map<String, String>> add(
            @Parameter(description = "Workspace ID") @PathVariable String workspaceId,
            @Valid @RequestBody AddWorkspaceMemberReq req,
            Authentication auth) {
        String message = service.addMember(workspaceId, req, auth.getName());
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PutMapping("/{memberUserId}/role")
    @Operation(summary = "Gán role cho thành viên", 
               description = "Thay đổi role của thành viên trong workspace")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gán role thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Thiếu quyền workspace:set_user_permissions")
    })
    @PreAuthorize("@gate.allows('workspace:set_user_permissions', authentication, #workspaceId)")
    public ResponseEntity<Map<String, String>> assign(
            @Parameter(description = "Workspace ID") @PathVariable String workspaceId,
            @Parameter(description = "User ID của thành viên") @PathVariable String memberUserId,
            @Valid @RequestBody AssignWorkspaceRoleReq req,
            Authentication auth) {
        String message = service.assignRole(workspaceId, memberUserId, req, auth.getName());
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/transfer-ownership")
    @Operation(summary = "Chuyển quyền sở hữu workspace", 
               description = "Chuyển quyền Owner cho thành viên khác")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chuyển quyền sở hữu thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Chỉ Owner được phép chuyển quyền sở hữu")
    })
    @PreAuthorize("@gate.allows('workspace:transfer_ownership', authentication, #workspaceId)")
    public ResponseEntity<Map<String, String>> transferOwnership(
            @Parameter(description = "Workspace ID") @PathVariable String workspaceId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String toUserId = body.get("toUserId");
        if (toUserId == null || toUserId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "toUserId là bắt buộc"));
        }
        
        String message = service.transferOwnership(workspaceId, toUserId, auth.getName());
        return ResponseEntity.ok(Map.of("message", message));
    }
}

package com.springboot.demo.controller;

import com.springboot.demo.dto.workspace.WorkspaceCreateInCompanyRequest;
import com.springboot.demo.dto.workspace.WorkspaceDto;
import com.springboot.demo.dto.workspace.WorkspaceUpdateRequest;
import com.springboot.demo.model.Workspace;
import com.springboot.demo.security.UserIdResolver;
import com.springboot.demo.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final UserIdResolver userIdResolver;

    // Tạo workspace trong công ty (người có quyền company.workspace:create hoặc owner)
    @PostMapping("/companies/{companyId}/workspaces")
    @PreAuthorize("@gate.allows('workspace:create', authentication, #companyId)")
    public WorkspaceDto createInCompany(@PathVariable String companyId,
                                        @Valid @RequestBody WorkspaceCreateInCompanyRequest req,
                                        Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        Workspace created = workspaceService.createInCompany(companyId, req, actorId);
        return WorkspaceDto.fromEntity(created);
    }

    // Xem 1 workspace
    @GetMapping("/workspaces/{id}")
    @PreAuthorize("@gate.allows('workspace:read', authentication, #id)")
    public WorkspaceDto get(@PathVariable String id) {
        return WorkspaceDto.fromEntity(workspaceService.getById(id));
    }

    // Sửa thông tin workspace
    @PutMapping("/workspaces/{id}")
    @PreAuthorize("@gate.allows('workspace:update', authentication, #id)")
    public WorkspaceDto update(@PathVariable String id,
                               @Valid @RequestBody WorkspaceUpdateRequest req) {
        return WorkspaceDto.fromEntity(workspaceService.update(id, req));
    }

    // Xoá CỨNG workspace
    @DeleteMapping("/workspaces/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@gate.allows('workspace:delete', authentication, #id)")
    public void deleteHard(@PathVariable String id) {
        workspaceService.deleteHard(id);
    }

    // Danh sách workspace của 1 company (có tìm kiếm q) - chỉ owner hoặc có quyền company:read
    @GetMapping("/companies/{companyId}/workspaces")
    @PreAuthorize("@gate.allows('company:read', authentication, #companyId)")
    public List<WorkspaceDto> listInCompany(@PathVariable String companyId,
                                            @RequestParam(required = false) String q) {
        return workspaceService.searchByCompanyId(companyId, q)
                .stream().map(WorkspaceDto::fromEntity).toList();
    }

    // Các workspace tôi thuộc trong 1 company (member/owner)
    @GetMapping("/companies/{companyId}/workspaces/my")
    @PreAuthorize("@gate.allows('company:read', authentication, #companyId) or isAuthenticated()")
    public List<WorkspaceDto> myWorkspacesInCompany(@PathVariable String companyId,
                                                    @RequestParam(defaultValue = "false") boolean includeArchived,
                                                    Authentication auth) {
        String actorId = userIdResolver.resolveInternalId(auth);
        return workspaceService.listMyWorkspacesInCompany(companyId, actorId, includeArchived);
    }

}

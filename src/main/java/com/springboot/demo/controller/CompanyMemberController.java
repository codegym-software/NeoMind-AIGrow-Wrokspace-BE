package com.springboot.demo.controller;

import com.springboot.demo.dto.company.InviteRequest;
import com.springboot.demo.model.CompanyMember;
import com.springboot.demo.model.User;
import com.springboot.demo.repository.UserRepository;
import com.springboot.demo.service.CompanyMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/company")
@Tag(name = "Company Member Management", description = "APIs for managing company members and invitations")
@SecurityRequirement(name = "Bearer Authentication")
public class CompanyMemberController {

    private final CompanyMemberService companyMemberService;
    private final UserRepository userRepository;

    public CompanyMemberController(CompanyMemberService companyMemberService,
                                   UserRepository userRepository) {
        this.companyMemberService = companyMemberService;
        this.userRepository = userRepository;
    }

    @PostMapping("/invite")
    @Operation(summary = "Invite member to company", description = "Send invitation email to join the company")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or user not found"),
            @ApiResponse(responseCode = "409", description = "Conflict - user already member or invited")
    })
    public ResponseEntity<?> inviteMember(@RequestBody InviteRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        String inviterEmail = jwt.getClaimAsString("email");
        if (inviterEmail == null || inviterEmail.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing email claim in token"));
        }

        var inviterOpt = userRepository.findByEmail(inviterEmail);
        if (inviterOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not found. Please login first"
            ));
        }

        String inviterId = inviterOpt.get().getId();
        String companyId = companyMemberService.getCompanyIdByUserId(inviterId);
        if (companyId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User is not a member of any company"));
        }

        try {
            var resp = companyMemberService.inviteMember(
                    companyId,
                    inviterId,
                    request.getEmail(),
                    request.getRoleId()
            );
            // Trả về entity giống BE cũ để FE tương thích
            return ResponseEntity.ok(resp.getMember());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", ex.getMessage()
            ));
        }
    }

    @GetMapping("/members")
    @Operation(summary = "Get company members", description = "Retrieve list of all company members")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Members retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or user not found")
    })
    public ResponseEntity<?> getMembers(@AuthenticationPrincipal Jwt jwt) {
        String inviterEmail = jwt.getClaimAsString("email");
        if (inviterEmail == null || inviterEmail.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing email claim in token"));
        }
        var inviterOpt = userRepository.findByEmail(inviterEmail);
        if (inviterOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found. Please login first"));
        }
        String inviterId = inviterOpt.get().getId();
        String companyId = companyMemberService.getCompanyIdByUserId(inviterId);
        if (companyId == null) {
            return ResponseEntity.ok(Map.of(
                    "companyId", null,
                    "members", List.of(),
                    "total", 0
            ));
        }
        List<CompanyMember> members = companyMemberService.getMembers(companyId);
        return ResponseEntity.ok(Map.of(
                "companyId", companyId,
                "members", members,
                "total", members.size()
        ));
    }
}

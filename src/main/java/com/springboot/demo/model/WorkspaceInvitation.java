package com.springboot.demo.model;

import com.springboot.demo.model.enums.InvitationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name = "workspace_invitations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkspaceInvitation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="workspace_id", nullable=false, length=36)
    private String workspaceId;

    @Column(name="invited_email", nullable=false, length=255)
    private String invitedEmail;

    @Column(name="invited_by", nullable=false, length=36)
    private String invitedByUserId;

    @Column(name="role_id", nullable=false)
    private Integer roleId;

    @Column(name="token", nullable=false, length=64, unique=true)
    private String token;   // để accept qua link

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private InvitationStatus status; // PENDING / ACCEPTED / CANCELLED / EXPIRED

    @CreationTimestamp private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
}

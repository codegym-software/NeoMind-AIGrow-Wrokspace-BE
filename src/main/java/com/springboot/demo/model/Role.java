package com.springboot.demo.model;

import com.springboot.demo.model.enums.Scope;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "roles",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_roles_scope_target_code",
                columnNames = {"scope","target_id","code"}
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, columnDefinition="ENUM('company','workspace','project')")
    private Scope scope;

    @Column(name = "target_id", length = 36)
    private String targetId; // null = role template toàn cục cho scope

    @Column(nullable=false, length=100)
    private String code; // "admin", "member", ...

    @Column(nullable=false, length=150)
    private String name;

    @Column(length=500)
    private String description;
}


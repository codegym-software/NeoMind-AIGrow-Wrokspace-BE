package com.springboot.demo.model;

import com.springboot.demo.model.enums.Scope;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions",
        uniqueConstraints = @UniqueConstraint(name = "uk_perms_scope_code", columnNames = {"scope","code"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Permission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Scope scope;

    @Column(nullable = false, length = 150)
    private String code;     // "project:create", "task:read"...

    @Column(nullable = false, length = 150)
    private String name;
}

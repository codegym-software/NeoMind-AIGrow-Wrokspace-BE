package com.springboot.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles",
        uniqueConstraints = @UniqueConstraint(name = "uk_roles_scope_code", columnNames = {"scope","code"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Scope scope;        // company|workspace|project

    @Column(nullable = false, length = 100)
    private String code;        // ví dụ: admin, member...

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;
}

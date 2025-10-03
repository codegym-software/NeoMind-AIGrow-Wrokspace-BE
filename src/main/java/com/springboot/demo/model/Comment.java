package com.springboot.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Table(name="comments")
@Getter @Setter
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="task_id", nullable=false) private Integer taskId;
    @Column(name="author_id", length=36, nullable=false) private String authorId;

    @Column(name="content", columnDefinition="TEXT", nullable=false)
    private String content;

    @Column(name="created_at", insertable=false, updatable=false)
    private LocalDateTime createdAt;
}

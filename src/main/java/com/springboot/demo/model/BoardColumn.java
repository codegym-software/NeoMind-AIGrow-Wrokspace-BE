package com.springboot.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * BoardColumn Entity - Cột trạng thái động (Status Column)
 * Đây là "kẹp tài liệu" trong ngăn kéo (Board)
 * Ví dụ: "To Do", "In Progress", "Done", "Code Review"...
 */
@Entity
@Table(
    name = "board_columns",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_board_columns_board_name",
        columnNames = {"board_id", "name"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumn {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ❌ XÓA field boardId - gây conflict với @JoinColumn bên dưới
    // @Column(name = "board_id", nullable = false, insertable = false, updatable = false)
    // private Integer boardId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "position", nullable = false)
    private Integer position = 0;

    @Column(name = "color", length = 7)
    private String color = "#6B7280"; // Default gray color

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationship: BoardColumn thuộc về một Board
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    @JsonIgnore
    private Board board;

    // Relationship: BoardColumn có nhiều Task
    @OneToMany(mappedBy = "statusColumn")
    @OrderBy("orderIndex ASC")
    @JsonIgnore
    private List<Task> tasks = new ArrayList<>();

    // Helper methods
    public int getTaskCount() {
        return tasks != null ? tasks.size() : 0;
    }
    
    // Helper: Get boardId from relationship
    public Integer getBoardId() {
        return board != null ? board.getId() : null;
    }
    
    // Helper: Set board from boardId
    public void setBoardId(Integer boardId) {
        if (boardId != null) {
            Board b = new Board();
            b.setId(boardId);
            this.board = b;
        } else {
            this.board = null;
        }
    }
}
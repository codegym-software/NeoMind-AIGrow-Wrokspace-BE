package com.springboot.demo.model;

// Import TaskPriority, nhưng XÓA TaskStatus
import com.springboot.demo.model.enums.TaskPriority;
// import com.springboot.demo.model.enums.TaskStatus; // <-- ĐÃ XÓA

// THÊM CÁC IMPORT MỚI
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
// Giả sử bạn có Entity tên là BoardColumn (từ bảng board_columns)
import com.springboot.demo.model.BoardColumn; 

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", columnDefinition = "CHAR(36)", nullable = false)
    private String projectId;

    // SỬA LẠI: Xóa 'columnDefinition' vì kiểu dữ liệu là INT, không phải CHAR(36)
    @Column(name = "sprint_id") 
    private Integer sprintId;

    @Column(name = "assigned_to", columnDefinition = "CHAR(36)")
    private String assignedTo; // User ID

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // --- BẮT ĐẦU THAY ĐỔI LỚN ---
    // Xóa trường @Enumerated TaskStatus cũ
    
    // THAY THẾ BẰNG: Mối quan hệ với bảng BoardColumn
    @ManyToOne 
    @JoinColumn(name = "column_id") // Đây là cột khóa ngoại mới trong bảng 'tasks'
    private BoardColumn statusColumn; // Đặt tên là statusColumn (hoặc status)
    
    // --- KẾT THÚC THAY ĐỔI LỚN ---

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, columnDefinition = "ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM'")
    private TaskPriority priority = TaskPriority.medium; // Giữ nguyên vì Enum Priority của bạn dùng chữ thường

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "actual_hours")
    private Integer actualHours;

    @Column(name = "tags", length = 500) // Comma-separated tags
    private String tags;

    @Column(name = "order_index", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer orderIndex = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // SỬA LẠI: Thêm @Column
    @Column(name = "completed_at") 
    private LocalDateTime completedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    // --- SỬA LẠI CÁC BUSINESS METHODS ---
    
    public boolean isArchived() {
        return archivedAt != null;
    }

    public void archive() {
        this.archivedAt = LocalDateTime.now();
    }

    public void unarchive() {
        this.archivedAt = null;
    }

    // SỬA LẠI: Logic kiểm tra hoàn thành (giả sử cột "Done" có tên là "Done")
    public boolean isCompleted() {
        return statusColumn != null && "Done".equalsIgnoreCase(statusColumn.getName());
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && !isCompleted();
    }

    // SỬA LẠI: Logic kiểm tra "In Progress" (giả sử tên cột là "In Progress")
    public boolean isInProgress() {
        return statusColumn != null && "In Progress".equalsIgnoreCase(statusColumn.getName());
    }

    public boolean isAssigned() {
        return assignedTo != null;
    }
}
package com.springboot.demo.model;

// Import TaskPriority, nhưng XÓA TaskStatus
import com.springboot.demo.model.enums.TaskPriority;
// import com.springboot.demo.model.enums.TaskStatus; // <-- ĐÃ XÓA

// THÊM CÁC IMPORT MỚI
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

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

    @Column(name = "epic_id")
    private Integer epicId;

    // SỬA LẠI: Xóa 'columnDefinition' vì kiểu dữ liệu là INT, không phải CHAR(36)
    @Column(name = "sprint_id") 
    private Integer sprintId;

    // CHUẨN HÓA: Dùng assignee_id thay vì assigned_to (Foreign Key → users.id)
    @Column(name = "assignee_id", columnDefinition = "CHAR(36)")
    private String assigneeId; // User ID (FK)

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
    @Column(name = "priority", nullable = false, columnDefinition = "ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM'")
    private TaskPriority priority; // Removed default = TaskPriority.medium to avoid overwriting during PATCH updates

    @Column(name = "start_date")
    private LocalDate startDate; // Timeline: Ngày bắt đầu thực tế của task

    @Column(name = "due_date")
    private LocalDate dueDate; // Timeline: Kế hoạch - Ngày dự kiến hoàn thành

    @Column(name = "end_date")
    private LocalDateTime endDate; // Timeline: Thực tế - Thời điểm hoàn thành

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "tags", length = 500) // Comma-separated tags
    private String tags;

    @Column(name = "order_index", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer orderIndex; // Removed default = 0 to avoid overwriting during PATCH updates

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "attachments", columnDefinition = "JSON")
    private String attachments; // Lưu danh sách file dạng JSON: [{"filename":"abc.pdf","url":"...","size":123}]

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

    
    public boolean isCompleted() {
        return statusColumn != null && "Done".equalsIgnoreCase(statusColumn.getName());
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && !isCompleted();
    }

    
    public boolean isInProgress() {
        return statusColumn != null && "In Progress".equalsIgnoreCase(statusColumn.getName());
    }

    public boolean isAssigned() {
        return assigneeId != null;
    }
}

# Timeline API Documentation (Enhanced with Sprint Data)

**Version:** 2.0  
**Date:** 2025-12-04  
**Feature:** Timeline/Gantt Chart with Sprint Headers

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Response Structure](#response-structure)
3. [API Endpoints](#api-endpoints)
4. [Data Models](#data-models)
5. [Business Logic](#business-logic)
6. [Frontend Integration Guide](#frontend-integration-guide)
7. [Examples](#examples)

---

## 🎯 Overview

Timeline API cung cấp dữ liệu cho **Gantt Chart visualization** với các tính năng:

- ✅ **Hierarchical structure**: Epic → Task
- ✅ **Soft date inheritance**: Task fallback to Sprint dates
- ✅ **Sprint headers**: Sprint blocks hiển thị ở top của Gantt Chart
- ✅ **Drag & Drop**: Update start_date và due_date
- ✅ **Task completion**: Mark task as done (end_date)

### Key Features

| Feature | Description |
|---------|-------------|
| **Sprint Visualization** | Hiển thị Sprint như background layers trên Gantt Chart |
| **Epic Progress** | So sánh Epic timeline với Sprint cycles |
| **Soft Inheritance** | Task không có date → lấy date từ Sprint |
| **Real-time Update** | Drag & Drop để update dates |

---

## 📦 Response Structure

### New Structure (v2.0)

```json
{
  "message": "Timeline retrieved successfully",
  "data": {
    "items": [...],     // Epic + Task data
    "sprints": [...]    // Sprint headers for Gantt Chart
  }
}
```

**Changes from v1.0:**
- ❌ Old: `List<TimelineItemDto>` (flat array)
- ✅ New: `TimelineResponse { items: [], sprints: [] }` (structured object)

---

## 🔌 API Endpoints

### 1. GET Timeline Data

**Endpoint:** `GET /api/projects/{projectId}/timeline`

**Description:** Lấy toàn bộ Epic/Task + Sprint headers cho Gantt Chart

#### Request

```http
GET /api/projects/123/timeline
Authorization: Bearer {token}
```

#### Response

```json
{
  "message": "Timeline retrieved successfully",
  "data": {
    "items": [
      {
        "id": "epic-1",
        "text": "User Authentication Module",
        "startDate": "2025-12-01",
        "endDate": "2026-03-31",
        "parent": null,
        "progress": 0.65,
        "type": "epic"
      },
      {
        "id": "task-10",
        "text": "Implement JWT Login",
        "startDate": "2025-12-01",
        "endDate": "2025-12-15",
        "parent": "epic-1",
        "progress": 1.0,
        "type": "task",
        "sprintId": 22
      },
      {
        "id": "task-11",
        "text": "Create User Registration",
        "startDate": "2025-12-15",
        "endDate": "2025-12-28",
        "parent": "epic-1",
        "progress": 0.0,
        "type": "task",
        "sprintId": 23
      }
    ],
    "sprints": [
      {
        "id": 22,
        "name": "Sprint 1: Authentication Core",
        "startDate": "2025-12-01",
        "endDate": "2025-12-14",
        "status": "active"
      },
      {
        "id": 23,
        "name": "Sprint 2: User Management",
        "startDate": "2025-12-15",
        "endDate": "2025-12-28",
        "status": "planned"
      },
      {
        "id": 24,
        "name": "Sprint 3: Profile & Settings",
        "startDate": "2025-12-29",
        "endDate": "2026-01-11",
        "status": "planned"
      }
    ]
  }
}
```

#### Response Fields

**items[]** (TimelineItemDto):
| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Format: `epic-{id}` hoặc `task-{id}` |
| `text` | string | Epic/Task title |
| `startDate` | LocalDate | Start date (YYYY-MM-DD) |
| `endDate` | LocalDate | End date (YYYY-MM-DD) |
| `parent` | string | Parent ID (null for Epic) |
| `progress` | double | 0.0 - 1.0 (0% - 100%) |
| `type` | string | `"epic"` or `"task"` |
| `sprintId` | Long | Sprint ID (chỉ có trong Task) |

**sprints[]** (SprintDto):
| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Sprint ID |
| `name` | string | Sprint name |
| `startDate` | LocalDate | Sprint start date |
| `endDate` | LocalDate | Sprint end date |
| `status` | SprintStatus | `active`, `planned`, `completed` |

---

### 2. Update Task Timeline (Drag & Drop)

**Endpoint:** `PATCH /api/projects/{projectId}/timeline/tasks/{taskId}`

**Description:** Update task dates khi user drag & drop trên Gantt Chart

#### Request

```http
PATCH /api/projects/123/timeline/tasks/10
Content-Type: application/json
Authorization: Bearer {token}

{
  "startDate": "2025-12-05",
  "dueDate": "2025-12-18"
}
```

#### Validation Rules

- ✅ `dueDate >= startDate` (via @ValidDateRange)
- ✅ Both dates can be null
- ✅ Single date can be updated

#### Response

```json
{
  "message": "Task timeline updated successfully",
  "data": {
    "id": "task-10",
    "text": "Implement JWT Login",
    "startDate": "2025-12-05",
    "endDate": "2025-12-18",
    "parent": "epic-1",
    "progress": 0.0,
    "type": "task",
    "sprintId": 22
  }
}
```

---

### 3. Mark Task as Complete

**Endpoint:** `PATCH /api/projects/{projectId}/timeline/tasks/{taskId}/complete`

**Description:** Đánh dấu task hoàn thành (set end_date = now), **KHÔNG thay đổi due_date**

#### Request

```http
PATCH /api/projects/123/timeline/tasks/10/complete
Content-Type: application/json
Authorization: Bearer {token}

{
  "completed": true
}
```

#### Business Logic

```java
// Nếu completed = true
task.setEndDate(LocalDateTime.now());  // Thời gian thực tế hoàn thành
// due_date GIỮ NGUYÊN (để so sánh plan vs actual)

// Nếu completed = false
task.setEndDate(null);  // Clear completion date
```

#### Response

```json
{
  "message": "Task marked as complete",
  "data": {
    "id": "task-10",
    "text": "Implement JWT Login",
    "startDate": "2025-12-01",
    "endDate": "2025-12-13",  // Actual completion date
    "parent": "epic-1",
    "progress": 1.0,
    "type": "task",
    "sprintId": 22
  }
}
```

---

## 📊 Data Models

### Entity Models

```java
// Task.java
@Entity
public class Task {
    private Long id;
    private String title;
    
    @Column(name = "start_date")
    private LocalDate startDate;      // When task started
    
    @Column(name = "due_date")
    private LocalDate dueDate;        // Planned completion date
    
    @Column(name = "end_date")
    private LocalDateTime endDate;    // Actual completion timestamp
    
    @ManyToOne
    private Sprint sprint;
    
    @ManyToOne
    private Epic epic;
    
    // ...
}

// Sprint.java
@Entity
public class Sprint {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    private SprintStatus status;  // active, planned, completed
    
    private Boolean isBacklog;    // Backlog sprint không có dates
    
    // ...
}

// Epic.java
@Entity
public class Epic {
    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    
    @OneToMany(mappedBy = "epic")
    private List<Task> tasks;
    
    // ...
}
```

### DTO Models

```java
// TimelineResponse.java (NEW in v2.0)
public record TimelineResponse(
    List<TimelineItemDto> items,
    List<SprintDto> sprints
) {}

// SprintDto.java (NEW in v2.0)
public record SprintDto(
    Long id,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    SprintStatus status
) {}

// TimelineItemDto.java
public record TimelineItemDto(
    String id,        // "epic-1" or "task-10"
    String text,
    LocalDate startDate,
    LocalDate endDate,
    String parent,    // "epic-1" or null
    Double progress,  // 0.0 - 1.0
    String type,      // "epic" or "task"
    Long sprintId     // Only for tasks
) {}

// TimelineUpdateRequest.java
public record TimelineUpdateRequest(
    @ValidDateRange
    LocalDate startDate,
    LocalDate dueDate
) {}

// TaskCompleteRequest.java
public record TaskCompleteRequest(
    @NotNull(message = "Completed status is required")
    Boolean completed
) {}
```

---

## 🧠 Business Logic

### 1. Soft Date Inheritance

**Rule:** Nếu Task không có start_date/due_date → lấy từ Sprint

```java
// TimelineServiceImpl.java
COALESCE(t.start_date, s.start_date) as start_date
COALESCE(t.due_date, s.end_date) as due_date
```

**Example:**
- Task có `startDate = null`, belongs to Sprint (2025-12-01 → 2025-12-14)
- → Timeline API trả về `startDate = "2025-12-01"` (from Sprint)

### 2. Sprint Filtering

**Rule:** Chỉ hiển thị Sprint có đầy đủ dates

```java
// TimelineServiceImpl.getSprintsForTimeline()
sprints.stream()
    .filter(s -> s.getStartDate() != null && s.getEndDate() != null)
    .collect(Collectors.toList());
```

**Why?**
- Backlog Sprint thường có `is_backlog = true` và `dates = null`
- Gantt Chart cần dates để vẽ → skip Backlog Sprint

### 3. Progress Calculation

**Epic Progress:**
```java
double progress = (completedTasks / totalTasks);
// completed = tasks with column.name = "Done"
```

**Task Progress:**
```java
double progress = (endDate != null) ? 1.0 : 0.0;
// Binary: hoặc xong (1.0) hoặc chưa xong (0.0)
```

### 4. Date Validation

**Constraint:** `endDate >= startDate`

```java
@ValidDateRange
public record TimelineUpdateRequest(
    LocalDate startDate,
    LocalDate dueDate
) {}

// DateRangeValidator.java
if (startDate != null && endDate != null) {
    return !endDate.isBefore(startDate);
}
```

---

## 🎨 Frontend Integration Guide

### Gantt Chart Library Setup

Recommended: **DHTMLX Gantt**, **Bryntum Gantt**, or **Frappe Gantt**

#### 1. Parse Response Data

```typescript
interface TimelineResponse {
  items: TimelineItem[];
  sprints: Sprint[];
}

interface TimelineItem {
  id: string;           // "epic-1", "task-10"
  text: string;
  startDate: string;    // "2025-12-01"
  endDate: string;      // "2025-12-15"
  parent: string | null;
  progress: number;     // 0.0 - 1.0
  type: "epic" | "task";
  sprintId?: number;
}

interface Sprint {
  id: number;
  name: string;
  startDate: string;
  endDate: string;
  status: "active" | "planned" | "completed";
}
```

#### 2. Render Sprint Headers

**Option A: Background Layers** (Recommended)
```typescript
// Vẽ Sprint như background bands
sprints.forEach(sprint => {
  gantt.addLayer({
    renderer: {
      render: function(item) {
        if (item.type === "sprint-header") {
          return `<div class="sprint-layer" 
                       style="left: ${getX(sprint.startDate)}px; 
                              width: ${getDuration(sprint)}px;">
                    ${sprint.name}
                  </div>`;
        }
      }
    }
  });
});
```

**Option B: Special Rows** (Alternative)
```typescript
// Thêm Sprint như row đầu tiên trong Gantt
const ganttData = {
  data: [
    // Sprint rows (read-only)
    ...sprints.map(s => ({
      id: `sprint-${s.id}`,
      text: s.name,
      start_date: s.startDate,
      end_date: s.endDate,
      type: "sprint-header",
      readonly: true,
      color: "#E3F2FD"  // Light blue background
    })),
    
    // Epic/Task rows (editable)
    ...items
  ]
};
```

#### 3. Handle Drag & Drop

```typescript
gantt.attachEvent("onAfterTaskDrag", async (id, mode, e) => {
  const task = gantt.getTask(id);
  
  if (task.type === "task") {
    await fetch(`/api/projects/${projectId}/timeline/tasks/${taskId}`, {
      method: "PATCH",
      body: JSON.stringify({
        startDate: task.start_date,
        dueDate: task.end_date
      })
    });
  }
});
```

#### 4. Mark Task Complete

```typescript
async function completeTask(taskId: number, completed: boolean) {
  const response = await fetch(
    `/api/projects/${projectId}/timeline/tasks/${taskId}/complete`,
    {
      method: "PATCH",
      body: JSON.stringify({ completed })
    }
  );
  
  // Update UI
  gantt.refreshTask(taskId);
}
```

#### 5. Sprint Color Coding

```typescript
function getSprintColor(status: string): string {
  switch (status) {
    case "active":    return "#4CAF50";  // Green
    case "planned":   return "#2196F3";  // Blue
    case "completed": return "#9E9E9E";  // Grey
    default:          return "#FFC107";  // Amber
  }
}

// Apply color to Sprint layer
sprints.forEach(sprint => {
  element.style.backgroundColor = getSprintColor(sprint.status);
});
```

---

## 📝 Examples

### Example 1: Simple Project with 2 Sprints

```json
{
  "data": {
    "items": [
      {
        "id": "epic-1",
        "text": "Login Feature",
        "startDate": "2025-12-01",
        "endDate": "2025-12-28",
        "parent": null,
        "progress": 0.5,
        "type": "epic"
      },
      {
        "id": "task-10",
        "text": "Design Login UI",
        "startDate": "2025-12-01",
        "endDate": "2025-12-14",
        "parent": "epic-1",
        "progress": 1.0,
        "type": "task",
        "sprintId": 1
      },
      {
        "id": "task-11",
        "text": "Implement Login API",
        "startDate": "2025-12-15",
        "endDate": "2025-12-28",
        "parent": "epic-1",
        "progress": 0.0,
        "type": "task",
        "sprintId": 2
      }
    ],
    "sprints": [
      {
        "id": 1,
        "name": "Sprint 1",
        "startDate": "2025-12-01",
        "endDate": "2025-12-14",
        "status": "completed"
      },
      {
        "id": 2,
        "name": "Sprint 2",
        "startDate": "2025-12-15",
        "endDate": "2025-12-28",
        "status": "active"
      }
    ]
  }
}
```

### Example 2: Task with Soft Inheritance

**Database State:**
```sql
-- Sprint
id=1, start_date='2025-12-01', end_date='2025-12-14'

-- Task (no dates)
id=10, start_date=NULL, due_date=NULL, sprint_id=1
```

**API Response:**
```json
{
  "id": "task-10",
  "text": "My Task",
  "startDate": "2025-12-01",  // ← From Sprint
  "endDate": "2025-12-14",    // ← From Sprint
  "sprintId": 1
}
```

### Example 3: Update Task Dates

**Request:**
```bash
curl -X PATCH http://localhost:8080/api/projects/123/timeline/tasks/10 \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2025-12-05",
    "dueDate": "2025-12-20"
  }'
```

**Response:**
```json
{
  "message": "Task timeline updated successfully",
  "data": {
    "id": "task-10",
    "startDate": "2025-12-05",
    "endDate": "2025-12-20",
    "progress": 0.0
  }
}
```

---

## 🔧 Testing with Postman

### Collection Structure

```
NeoMind Timeline APIs
├── GET Timeline Data
├── UPDATE Task Timeline
└── MARK Task Complete
```

### Test Scenarios

1. **Empty Project**: No Epics/Tasks → `{ items: [], sprints: [] }`
2. **Task without dates**: Verify soft inheritance from Sprint
3. **Drag & Drop**: Update dates, verify validation
4. **Complete Task**: Mark done, check `endDate` set
5. **Sprint Filtering**: Verify Backlog Sprint excluded

---

## 🚀 Deployment Notes

### Database Indexes

```sql
-- Performance optimization
CREATE INDEX idx_tasks_project_dates ON tasks(project_id, start_date, due_date);
CREATE INDEX idx_sprints_project_dates ON sprints(project_id, start_date, end_date);
CREATE INDEX idx_epics_project_dates ON epics(project_id, start_date, end_date);
```

### Configuration

```yaml
# application.yml
spring:
  jackson:
    serialization:
      write-dates-as-timestamps: false  # ISO-8601 format
```

---

## 📞 Support

**Documentation Version:** 2.0  
**Last Updated:** 2025-12-04  
**API Version:** v1

For questions, contact: dev-team@neomind.com

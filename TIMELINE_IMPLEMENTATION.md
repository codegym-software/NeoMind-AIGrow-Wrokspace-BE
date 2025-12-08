# 📊 Timeline/Gantt Chart Implementation Guide

## 🎯 Tổng Quan

Hệ thống Timeline/Gantt Chart được thiết kế theo chuẩn Jira với các tính năng:
- Hiển thị Epic và Task trong cùng một view
- Soft Inheritance: Task kế thừa date từ Sprint nếu không có date riêng
- So sánh Kế hoạch (due_date) vs Thực tế (completed_at)
- Kéo thả để cập nhật timeline
- Đánh dấu hoàn thành task

---

## 🗄️ Database Schema

### Bảng: `tasks`
```sql
-- Cột mới được thêm vào
start_date      DATE         NULL  -- Ngày bắt đầu thực tế của task
completed_at    DATETIME     NULL  -- Thời điểm hoàn thành thực tế

-- Cột đã có sẵn (giữ nguyên)
due_date        DATE         NULL  -- KẾ HOẠCH: Ngày dự kiến hoàn thành
```

**Logic so sánh:**
- `due_date` = Kế hoạch (KHÔNG thay đổi khi task Done)
- `completed_at` = Thực tế (Update khi mark Done)
- So sánh để biết: On-time / Delay / Early

---

## 📡 API Endpoints

### 1. GET /api/projects/{projectId}/timeline
**Mục đích:** Lấy dữ liệu Timeline cho Gantt Chart

**Authorization:** `project:task:read`

**Response:**
```json
{
  "message": "Timeline retrieved successfully",
  "data": [
    {
      "id": "epic-1",
      "text": "User Management Module",
      "startDate": "2025-12-01",
      "endDate": "2026-03-31",
      "parent": null,
      "progress": 0.6,
      "type": "epic",
      "projectId": "proj-123",
      "status": null
    },
    {
      "id": "task-10",
      "text": "Implement Login API",
      "startDate": "2025-12-01",
      "endDate": "2025-12-15",
      "parent": "epic-1",
      "progress": 1.0,
      "type": "task",
      "projectId": "proj-123",
      "sprintId": 5,
      "status": "Done",
      "completedDate": "2025-12-14"
    }
  ]
}
```

**Logic Soft Inheritance:**
```
Task.view_start_date = Task.start_date ?? Sprint.start_date
Task.view_end_date   = Task.due_date    ?? Sprint.end_date
```

**Progress Calculation:**
- Task: `1.0` (100%) nếu Done, `0.0` (0%) nếu chưa Done
- Epic: `done_tasks / total_tasks` (0.0 - 1.0)

---

### 2. PATCH /api/projects/{projectId}/timeline/tasks/{taskId}
**Mục đích:** Cập nhật Timeline khi kéo thả task trên Gantt Chart

**Authorization:** `project:task:update`

**Request Body:**
```json
{
  "startDate": "2025-12-05",
  "dueDate": "2025-12-20"
}
```

**Validation:**
- ✅ `startDate` và `dueDate` là required
- ✅ `dueDate >= startDate` (dùng `@ValidDateRange`)

**Hành động:**
```sql
UPDATE tasks 
SET start_date = '2025-12-05',
    due_date = '2025-12-20'
WHERE id = {taskId}
```

**Ý nghĩa:** Ghi đè kế hoạch mặc định của Sprint bằng kế hoạch cụ thể của Task

**Response:**
```json
{
  "message": "Task timeline updated successfully",
  "data": {
    "id": "task-10",
    "text": "Implement Login API",
    "startDate": "2025-12-05",
    "endDate": "2025-12-20",
    ...
  }
}
```

---

### 3. PATCH /api/projects/{projectId}/timeline/tasks/{taskId}/complete
**Mục đích:** Đánh dấu task hoàn thành hoặc uncomplete

**Authorization:** `project:task:update`

**Request Body:**
```json
{
  "completed": true  // true = Done, false = Uncomplete
}
```

**Hành động khi `completed = true`:**
```sql
UPDATE tasks 
SET completed_at = NOW(),
    column_id = (SELECT id FROM board_columns WHERE name = 'Done' LIMIT 1)
WHERE id = {taskId}

-- ⚠️ LƯU Ý: KHÔNG UPDATE due_date
-- Giữ nguyên due_date để so sánh Kế hoạch vs Thực tế
```

**Hành động khi `completed = false`:**
```sql
UPDATE tasks 
SET completed_at = NULL,
    column_id = (SELECT id FROM board_columns WHERE name = 'In Progress' LIMIT 1)
WHERE id = {taskId}
```

**Response:**
```json
{
  "message": "Task completion status updated successfully",
  "data": {
    "id": "task-10",
    "text": "Implement Login API",
    "startDate": "2025-12-01",
    "endDate": "2025-12-15",
    "completedDate": "2025-12-14",
    "progress": 1.0,
    "status": "Done",
    ...
  }
}
```

---

## 🏗️ Architecture

### Files Created/Modified:

**Models:**
- ✅ `Task.java` - Added: `startDate`, `completedAt`

**DTOs:**
- ✅ `dto/timeline/TimelineItemDto.java` - Response DTO
- ✅ `dto/timeline/TimelineUpdateRequest.java` - Update request
- ✅ `dto/timeline/TaskCompleteRequest.java` - Complete request

**Services:**
- ✅ `service/TimelineService.java` - Interface
- ✅ `service/impl/TimelineServiceImpl.java` - Implementation

**Controllers:**
- ✅ `controller/TimelineController.java` - REST endpoints

**Repository:**
- ✅ `repository/BoardColumnRepository.java` - Added: `findByProjectIdAndName()`

**Validation:**
- ✅ `validation/ValidDateRange.java` - Custom validator (already exists)

**Migration:**
- ✅ `db/migration/add-timeline-columns.sql` - SQL migration

---

## 🔄 Business Logic Flow

### 1. Get Timeline Flow
```
1. Check project access (Authorization)
2. Fetch all Epics of project
3. Fetch all Tasks (non-archived) of project
4. Fetch all Sprints of project (for soft inheritance)
5. Map Epics → TimelineItemDto
   - Calculate progress = done_tasks / total_tasks
6. Map Tasks → TimelineItemDto
   - Apply soft inheritance for dates:
     * start_date = task.start_date ?? sprint.start_date
     * end_date = task.due_date ?? sprint.end_date
   - Set parent = "epic-{id}" if epic_id exists
   - Set progress = 1.0 if Done, else 0.0
7. Sort: Epics first, then Tasks, by start_date
8. Return flat list
```

### 2. Update Timeline Flow
```
1. Check task exists
2. Check project access
3. Validate: dueDate >= startDate (@ValidDateRange)
4. Update task.start_date = request.startDate
5. Update task.due_date = request.dueDate
6. Save to database
7. Return updated TimelineItemDto
```

### 3. Complete Task Flow
```
1. Check task exists
2. Check project access
3. If completed = true:
   - Set completed_at = NOW()
   - Find "Done" column and set as status
4. If completed = false:
   - Set completed_at = NULL
   - Find "In Progress" column and set as status
5. ⚠️ IMPORTANT: Do NOT update due_date
6. Save to database
7. Return updated TimelineItemDto
```

---

## 🎨 Frontend Integration

### Gantt Chart Library Recommendations:
1. **dhtmlxGantt** (Recommended)
2. **Frappe Gantt**
3. **BRYNTUM Gantt**

### Data Mapping:
```javascript
// Backend response → Gantt Chart format
const ganttTasks = timelineData.data.map(item => ({
  id: item.id,                    // "epic-1" or "task-10"
  text: item.text,                // Display name
  start_date: item.startDate,     // "2025-12-01"
  end_date: item.endDate,         // "2025-12-15"
  parent: item.parent,            // "epic-1" or null
  progress: item.progress,        // 0.0 - 1.0
  type: item.type,                // "epic" or "task"
  
  // Custom fields
  status: item.status,
  completedDate: item.completedDate,
  isDelayed: item.completedDate > item.endDate
}));
```

### Events to Handle:
```javascript
// 1. On drag task (resize)
gantt.attachEvent("onAfterTaskDrag", (id, mode) => {
  const task = gantt.getTask(id);
  
  // Call API update
  fetch(`/api/projects/${projectId}/timeline/tasks/${taskId}`, {
    method: 'PATCH',
    body: JSON.stringify({
      startDate: task.start_date,
      dueDate: task.end_date
    })
  });
});

// 2. On double click (complete task)
gantt.attachEvent("onTaskDblClick", (id) => {
  const task = gantt.getTask(id);
  
  // Toggle complete
  fetch(`/api/projects/${projectId}/timeline/tasks/${taskId}/complete`, {
    method: 'PATCH',
    body: JSON.stringify({
      completed: task.progress < 1.0
    })
  });
});
```

---

## 📊 SQL Query Optimization

### Original Query (UNION approach - for reference):
```sql
-- Epic part
SELECT 
  CONCAT('epic-', id) as id,
  title as text,
  start_date,
  end_date,
  NULL as parent,
  'epic' as type,
  project_id
FROM epics
WHERE project_id = ?

UNION ALL

-- Task part with soft inheritance
SELECT 
  CONCAT('task-', t.id) as id,
  t.title as text,
  COALESCE(t.start_date, s.start_date) as start_date,
  COALESCE(t.due_date, s.end_date) as end_date,
  CASE WHEN t.epic_id IS NOT NULL 
       THEN CONCAT('epic-', t.epic_id) 
       ELSE NULL END as parent,
  'task' as type,
  t.project_id
FROM tasks t
LEFT JOIN sprints s ON t.sprint_id = s.id
WHERE t.project_id = ?
  AND t.archived_at IS NULL

ORDER BY 
  CASE type WHEN 'epic' THEN 0 ELSE 1 END,
  start_date ASC
```

**Note:** Implementation hiện tại dùng JPA để flexibility, nhưng có thể optimize bằng native query nếu cần performance cao.

---

## ✅ Testing Checklist

### Unit Tests:
- [ ] TimelineService.getTimeline() - Empty project
- [ ] TimelineService.getTimeline() - With epics and tasks
- [ ] TimelineService.getTimeline() - Soft inheritance works
- [ ] TimelineService.updateTaskTimeline() - Valid dates
- [ ] TimelineService.updateTaskTimeline() - Invalid dates (endDate < startDate)
- [ ] TimelineService.completeTask() - Mark as Done
- [ ] TimelineService.completeTask() - Unmark
- [ ] Progress calculation for Epic

### Integration Tests:
- [ ] GET /timeline - Returns correct data
- [ ] PATCH /timeline/tasks/:id - Updates dates
- [ ] PATCH /timeline/tasks/:id/complete - Marks Done without changing due_date
- [ ] Authorization checks (non-member cannot access)

### Manual Testing:
```bash
# 1. Get timeline
curl -X GET "http://localhost:8080/api/projects/{projectId}/timeline" \
  -H "Authorization: Bearer {token}"

# 2. Update timeline (drag task)
curl -X PATCH "http://localhost:8080/api/projects/{projectId}/timeline/tasks/10" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2025-12-05",
    "dueDate": "2025-12-20"
  }'

# 3. Complete task
curl -X PATCH "http://localhost:8080/api/projects/{projectId}/timeline/tasks/10/complete" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "completed": true
  }'
```

---

## 🚀 Deployment Steps

1. **Run SQL Migration:**
```sql
-- Apply migration
SOURCE db/migration/add-timeline-columns.sql
```

2. **Build & Deploy:**
```bash
./mvnw clean package -DskipTests
java -jar target/NeoMind_BE-0.0.1-SNAPSHOT.jar
```

3. **Verify:**
```bash
# Health check
curl http://localhost:8080/actuator/health

# Test timeline endpoint
curl -X GET "http://localhost:8080/api/projects/{projectId}/timeline" \
  -H "Authorization: Bearer {token}"
```

---

## 🎯 Key Features Implemented

✅ **Soft Inheritance:** Task date fallback to Sprint date  
✅ **Flat List:** Epic + Task in same structure for Gantt Chart  
✅ **Progress Tracking:** Auto-calculate Epic progress from tasks  
✅ **Drag & Drop Support:** Update timeline via PATCH API  
✅ **Complete Task:** Mark Done without changing due_date  
✅ **Plan vs Actual:** Compare due_date vs completed_at  
✅ **Validation:** Date range validation with custom validator  
✅ **Authorization:** Project member access control  

---

## 📝 Important Notes

### ⚠️ CRITICAL: Do NOT Change due_date on Complete
```java
// ❌ WRONG
if (completed) {
    task.setDueDate(LocalDate.now()); // NO!
}

// ✅ CORRECT
if (completed) {
    task.setCompletedAt(LocalDateTime.now()); // YES!
    // due_date giữ nguyên để so sánh
}
```

### 💡 Soft Inheritance Behavior:
- Task có `start_date` riêng → Dùng `start_date`
- Task không có `start_date` + có `sprint_id` → Dùng `sprint.start_date`
- Task không có cả 2 → `null` (Frontend xử lý)

### 🔄 State Transitions:
```
Task states:
┌─────────┐  drag   ┌──────────┐  complete  ┌──────┐
│ To Do   │ ------> │In Progress│ ---------> │ Done │
└─────────┘         └──────────┘            └──────┘
                           ^                     |
                           |     uncomplete      |
                           └─────────────────────┘
```

---

## 🎉 Implementation Complete!

All features have been implemented successfully:
- ✅ Models updated
- ✅ DTOs created
- ✅ Services implemented
- ✅ Controllers created
- ✅ Validations added
- ✅ SQL migration ready
- ✅ Build successful

**Next Steps:**
1. Apply SQL migration to database
2. Test endpoints with Postman
3. Integrate with Frontend Gantt Chart library
4. Write unit tests

---

**Author:** GitHub Copilot  
**Date:** December 4, 2025  
**Version:** 1.0.0

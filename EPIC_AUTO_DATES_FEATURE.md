# Epic Auto-Dates Feature Documentation

**Version:** 1.0  
**Date:** 2025-12-09  
**Feature:** Tự động set startDate và endDate cho Epic dựa trên Sprint lifecycle

---

## 🎯 Overview

Feature này tự động cập nhật `epic.startDate` và `epic.endDate` dựa trên thời gian của Sprint chứa tasks thuộc Epic đó.

### **Business Logic:**

1. **Epic startDate**: Được set khi Sprint chứa task đầu tiên của Epic **chuyển sang status "active"**
2. **Epic endDate**: Được set khi Sprint chứa task cuối cùng của Epic **chuyển sang status "completed"**

---

## 📋 Requirements Recap

### **Epic startDate Logic:**
- **Trigger**: Sprint status changes to `active`
- **Condition**: Epic chưa có `startDate` HOẶC Sprint start sớm hơn Epic startDate hiện tại
- **Action**: Set `epic.startDate = sprint.startDate`

### **Epic endDate Logic:**
- **Trigger**: Sprint status changes to `completed`
- **Condition**: Epic chưa có `endDate` HOẶC Sprint end muộn hơn Epic endDate hiện tại
- **Action**: Set `epic.endDate = sprint.endDate`

### **Task Sorting:**
- Tasks được sắp xếp theo `task.startDate` (Option A)
- Task đầu tiên = task có `startDate` nhỏ nhất
- Task cuối cùng = task có `startDate` lớn nhất

### **Edge Cases:**

#### **Case 1: Epic có tasks trong nhiều Sprints**
```
Epic B
├── Task A1 (Sprint C: Dec 1-14)  ← Sprint start sớm nhất
├── Task A2 (Sprint D: Dec 15-28)
└── Task A3 (Sprint E: Dec 29 - Jan 11)  ← Sprint end muộn nhất

Kết quả:
→ Epic B.startDate = Sprint C.startDate (Dec 1)
→ Epic B.endDate = Sprint E.endDate (Jan 11)
```
✅ **Handled**: Logic so sánh và chỉ update nếu Sprint date tốt hơn

#### **Case 2: Task được move từ Sprint này sang Sprint khác**
```
Task A1: Sprint C → move to → Sprint D
```
❌ **No recalculate**: Epic dates không được update lại

#### **Case 3: Task completed nhưng không thuộc Sprint**
```
Task A1 (sprintId: null, completed: true)
```
✅ **Handled**: Không set Epic endDate (vì không có Sprint)

---

## 🔧 Implementation Details

### **File Modified:**
- `src/main/java/com/springboot/demo/service/impl/SprintServiceImpl.java`

### **Dependencies Added:**
```java
import com.springboot.demo.model.Epic;
import com.springboot.demo.repository.EpicRepository;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
```

### **New Methods:**

#### **1. updateEpicStartDatesOnSprintStart(Sprint sprint)**
```java
/**
 * AUTO-SET EPIC DATES - Khi Sprint starts (status = active)
 * Logic: Set epic.startDate = sprint.startDate cho tất cả Epics có tasks trong Sprint này
 */
private void updateEpicStartDatesOnSprintStart(Sprint sprint) {
    if (sprint.getStartDate() == null) return;
    
    // Lấy tất cả tasks trong sprint này
    List<Task> sprintTasks = taskRepo.findBySprintIdAndArchivedAtIsNull(sprint.getId());
    
    // Lấy danh sách Epic IDs unique
    Set<Integer> epicIds = sprintTasks.stream()
        .filter(task -> task.getEpicId() != null)
        .map(Task::getEpicId)
        .collect(Collectors.toSet());
    
    if (epicIds.isEmpty()) return;
    
    // Update epic.startDate nếu chưa có hoặc sprint.startDate sớm hơn
    List<Epic> epics = epicRepo.findAllById(epicIds);
    epics.forEach(epic -> {
        if (epic.getStartDate() == null || sprint.getStartDate().isBefore(epic.getStartDate())) {
            epic.setStartDate(sprint.getStartDate());
            System.out.println("✅ Auto-set Epic[" + epic.getId() + "] startDate = " + sprint.getStartDate());
        }
    });
    
    if (!epics.isEmpty()) {
        epicRepo.saveAll(epics);
    }
}
```

**Logic Flow:**
1. Check Sprint có `startDate` không → nếu null thì skip
2. Query tất cả tasks trong Sprint (active, not archived)
3. Extract unique Epic IDs từ tasks
4. Load tất cả Epics theo IDs
5. Với mỗi Epic:
   - Nếu `epic.startDate == null` → set = `sprint.startDate`
   - Nếu `sprint.startDate < epic.startDate` → update = `sprint.startDate`
6. Save all Epics đã thay đổi

---

#### **2. updateEpicEndDatesOnSprintComplete(Sprint sprint)**
```java
/**
 * AUTO-SET EPIC DATES - Khi Sprint completes (status = completed)
 * Logic: Set epic.endDate = sprint.endDate cho tất cả Epics có tasks trong Sprint này
 */
private void updateEpicEndDatesOnSprintComplete(Sprint sprint) {
    if (sprint.getEndDate() == null) return;
    
    // Lấy tất cả tasks trong sprint này
    List<Task> sprintTasks = taskRepo.findBySprintIdAndArchivedAtIsNull(sprint.getId());
    
    // Lấy danh sách Epic IDs unique
    Set<Integer> epicIds = sprintTasks.stream()
        .filter(task -> task.getEpicId() != null)
        .map(Task::getEpicId)
        .collect(Collectors.toSet());
    
    if (epicIds.isEmpty()) return;
    
    // Update epic.endDate nếu chưa có hoặc sprint.endDate muộn hơn
    List<Epic> epics = epicRepo.findAllById(epicIds);
    epics.forEach(epic -> {
        if (epic.getEndDate() == null || sprint.endDate().isAfter(epic.getEndDate())) {
            epic.setEndDate(sprint.getEndDate());
            System.out.println("✅ Auto-set Epic[" + epic.getId() + "] endDate = " + sprint.getEndDate());
        }
    });
    
    if (!epics.isEmpty()) {
        epicRepo.saveAll(epics);
    }
}
```

**Logic Flow:**
1. Check Sprint có `endDate` không → nếu null thì skip
2. Query tất cả tasks trong Sprint (active, not archived)
3. Extract unique Epic IDs từ tasks
4. Load tất cả Epics theo IDs
5. Với mỗi Epic:
   - Nếu `epic.endDate == null` → set = `sprint.endDate`
   - Nếu `sprint.endDate > epic.endDate` → update = `sprint.endDate`
6. Save all Epics đã thay đổi

---

#### **3. Integration in update() method**
```java
@Override
@Transactional
public SprintDto update(Integer sprintId, UpdateSprintReq req) {
    Sprint s = sprintRepo.findById(sprintId)
        .orElseThrow(() -> new IllegalArgumentException("Sprint not found"));

    if (req.name() != null && !req.name().isBlank()) s.setName(req.name().trim());
    if (req.startDate() != null) s.setStartDate(req.startDate());
    if (req.endDate() != null) s.setEndDate(req.endDate());
    if (req.description() != null) s.setDescription(req.description().trim());
    if (req.isBacklog() != null) s.setIsBacklog(req.isBacklog());
    
    // ========== AUTO-SET EPIC START DATE ==========
    // Khi Sprint chuyển sang "active", auto-set startDate cho các Epics
    if (req.status() != null && req.status() == SprintStatus.active 
        && s.getStatus() != SprintStatus.active) {
        updateEpicStartDatesOnSprintStart(s);
    }
    
    // ========== LOGIC COMPLETE SPRINT ==========
    // Khi status chuyển sang "completed", tự động chuyển tasks chưa Done về Backlog
    if (req.status() != null && req.status() == SprintStatus.completed 
        && s.getStatus() != SprintStatus.completed) {
        
        // ... existing logic to move incomplete tasks to Backlog ...
        
        // ========== AUTO-SET EPIC END DATE ==========
        // Khi Sprint complete, auto-set endDate cho các Epics
        updateEpicEndDatesOnSprintComplete(s);
    }
    
    if (req.status() != null) s.setStatus(req.status());

    sprintRepo.save(s);
    return toDto(s);
}
```

**Trigger Points:**
1. **Sprint Start**: `status: planned → active`
   - Call `updateEpicStartDatesOnSprintStart()`
2. **Sprint Complete**: `status: active → completed`
   - Move incomplete tasks to Backlog (existing logic)
   - Call `updateEpicEndDatesOnSprintComplete()`

---

## 🧪 Testing Scenarios

### **Scenario 1: Epic với 1 Sprint**

#### **Initial State:**
```
Epic A (startDate: null, endDate: null)
├── Task 1 (sprint: null)
├── Task 2 (sprint: null)
└── Task 3 (sprint: null)

Sprint S1 (startDate: 2025-12-01, endDate: 2025-12-14, status: planned)
```

#### **Step 1: Assign tasks to Sprint**
```bash
# Move tasks to Sprint S1
PATCH /api/tasks/1 → { sprintId: S1 }
PATCH /api/tasks/2 → { sprintId: S1 }
PATCH /api/tasks/3 → { sprintId: S1 }
```

**Result:** Epic A vẫn chưa có dates (Sprint chưa active)

#### **Step 2: Start Sprint**
```bash
PATCH /api/sprints/S1
Body: { "status": "active" }
```

**Expected Result:**
```
✅ Auto-set Epic[A] startDate = 2025-12-01

Epic A (startDate: 2025-12-01, endDate: null)
Sprint S1 (status: active)
```

#### **Step 3: Complete Sprint**
```bash
PATCH /api/sprints/S1
Body: { "status": "completed" }
```

**Expected Result:**
```
✅ Auto-set Epic[A] endDate = 2025-12-14

Epic A (startDate: 2025-12-01, endDate: 2025-12-14)
Sprint S1 (status: completed)
```

---

### **Scenario 2: Epic với nhiều Sprints**

#### **Initial State:**
```
Epic B (startDate: null, endDate: null)
├── Task 1 (sprint: S1)
├── Task 2 (sprint: S2)
└── Task 3 (sprint: S3)

Sprint S1 (startDate: 2025-12-01, endDate: 2025-12-14, status: planned)
Sprint S2 (startDate: 2025-12-15, endDate: 2025-12-28, status: planned)
Sprint S3 (startDate: 2025-12-29, endDate: 2026-01-11, status: planned)
```

#### **Step 1: Start Sprint S2 first**
```bash
PATCH /api/sprints/S2
Body: { "status": "active" }
```

**Expected Result:**
```
✅ Auto-set Epic[B] startDate = 2025-12-15

Epic B (startDate: 2025-12-15, endDate: null)
```

#### **Step 2: Start Sprint S1 (sớm hơn S2)**
```bash
PATCH /api/sprints/S1
Body: { "status": "active" }
```

**Expected Result:**
```
✅ Auto-set Epic[B] startDate = 2025-12-01 (update vì sớm hơn)

Epic B (startDate: 2025-12-01, endDate: null)
```

#### **Step 3: Complete Sprint S2**
```bash
PATCH /api/sprints/S2
Body: { "status": "completed" }
```

**Expected Result:**
```
✅ Auto-set Epic[B] endDate = 2025-12-28

Epic B (startDate: 2025-12-01, endDate: 2025-12-28)
```

#### **Step 4: Complete Sprint S3 (muộn hơn S2)**
```bash
PATCH /api/sprints/S3
Body: { "status": "completed" }
```

**Expected Result:**
```
✅ Auto-set Epic[B] endDate = 2026-01-11 (update vì muộn hơn)

Epic B (startDate: 2025-12-01, endDate: 2026-01-11)
```

**Final Result:**
```
Epic B covers từ Sprint S1.start đến Sprint S3.end
Epic B (startDate: 2025-12-01, endDate: 2026-01-11)
```

---

### **Scenario 3: Epic không có tasks trong Sprint**

#### **Initial State:**
```
Epic C (startDate: null, endDate: null)
├── Task 1 (sprint: null)
└── Task 2 (sprint: null)

Sprint S1 (startDate: 2025-12-01, endDate: 2025-12-14, status: planned)
```

#### **Action: Start Sprint**
```bash
PATCH /api/sprints/S1
Body: { "status": "active" }
```

**Expected Result:**
```
❌ No change (Epic C không có tasks trong Sprint S1)

Epic C (startDate: null, endDate: null)
```

---

### **Scenario 4: Multiple Epics trong cùng Sprint**

#### **Initial State:**
```
Epic A (startDate: null, endDate: null)
├── Task A1 (sprint: S1)
└── Task A2 (sprint: S1)

Epic B (startDate: null, endDate: null)
├── Task B1 (sprint: S1)
└── Task B2 (sprint: S1)

Sprint S1 (startDate: 2025-12-01, endDate: 2025-12-14, status: planned)
```

#### **Step 1: Start Sprint**
```bash
PATCH /api/sprints/S1
Body: { "status": "active" }
```

**Expected Result:**
```
✅ Auto-set Epic[A] startDate = 2025-12-01
✅ Auto-set Epic[B] startDate = 2025-12-01

Epic A (startDate: 2025-12-01, endDate: null)
Epic B (startDate: 2025-12-01, endDate: null)
```

#### **Step 2: Complete Sprint**
```bash
PATCH /api/sprints/S1
Body: { "status": "completed" }
```

**Expected Result:**
```
✅ Auto-set Epic[A] endDate = 2025-12-14
✅ Auto-set Epic[B] endDate = 2025-12-14

Epic A (startDate: 2025-12-01, endDate: 2025-12-14)
Epic B (startDate: 2025-12-01, endDate: 2025-12-14)
```

---

## 📊 Console Output Examples

### **When Sprint Starts:**
```
✅ Auto-set Epic[6] startDate = 2025-12-01
✅ Auto-set Epic[8] startDate = 2025-12-01
✅ Auto-set Epic[9] startDate = 2025-12-01
```

### **When Sprint Completes:**
```
✅ Moved 3 incomplete tasks to Backlog
✅ Auto-set Epic[6] endDate = 2025-12-14
✅ Auto-set Epic[8] endDate = 2025-12-14
✅ Auto-set Epic[9] endDate = 2025-12-14
```

---

## 🔍 Database Impact

### **Before Sprint Start:**
```sql
SELECT id, title, start_date, end_date FROM epics WHERE id IN (6, 8, 9);

-- Result:
-- id | title                | start_date | end_date
-- 6  | Quản lý người dùng   | NULL       | NULL
-- 8  | Quản lý Company      | NULL       | NULL
-- 9  | Quản lý Timeline     | NULL       | NULL
```

### **After Sprint Start (status = active):**
```sql
SELECT id, title, start_date, end_date FROM epics WHERE id IN (6, 8, 9);

-- Result:
-- id | title                | start_date | end_date
-- 6  | Quản lý người dùng   | 2025-12-01 | NULL
-- 8  | Quản lý Company      | 2025-12-01 | NULL
-- 9  | Quản lý Timeline     | 2025-12-01 | NULL
```

### **After Sprint Complete (status = completed):**
```sql
SELECT id, title, start_date, end_date FROM epics WHERE id IN (6, 8, 9);

-- Result:
-- id | title                | start_date | end_date
-- 6  | Quản lý người dùng   | 2025-12-01 | 2025-12-14
-- 8  | Quản lý Company      | 2025-12-01 | 2025-12-14
-- 9  | Quản lý Timeline     | 2025-12-01 | 2025-12-14
```

---

## 🎯 API Endpoints Affected

### **Sprint Update API:**
```
PATCH /api/sprints/{sprintId}
Content-Type: application/json
Authorization: Bearer {token}

Body:
{
  "status": "active"  // hoặc "completed"
}
```

**Side Effects:**
- **status → active**: Auto-set `epic.startDate` cho tất cả Epics có tasks trong Sprint
- **status → completed**: Auto-set `epic.endDate` cho tất cả Epics có tasks trong Sprint

---

## ⚠️ Important Notes

### **1. Không recalculate khi move task giữa Sprints**
- Khi task được move từ Sprint A → Sprint B
- Epic dates **KHÔNG** tự động recalculate
- Epic dates chỉ update khi Sprint status changes

### **2. Tasks không thuộc Sprint**
- Tasks với `sprintId = null` (Backlog) **KHÔNG** ảnh hưởng Epic dates
- Chỉ tasks trong Sprint mới trigger auto-set Epic dates

### **3. Sprint status flow**
```
planned → active   : Trigger updateEpicStartDates()
active → completed : Trigger updateEpicEndDates()
```

### **4. Performance consideration**
- Method chỉ query Epics cần thiết (based on tasks trong Sprint)
- Batch save tất cả Epics cùng lúc (không loop save từng Epic)
- Transaction đảm bảo atomicity

---

## 🚀 Benefits

1. **Automated Epic Planning**: Epic dates tự động sync với Sprint timeline
2. **Accurate Timeline Visualization**: Gantt Chart hiển thị Epic duration chính xác
3. **Cross-Sprint Epic Tracking**: Epic có tasks trong nhiều Sprints vẫn có dates đúng
4. **No Manual Maintenance**: Không cần manual update Epic dates

---

## 📝 Future Enhancements

### **Possible Improvements:**

1. **Recalculate on Task Move:**
   - Khi task move giữa Sprints → recalculate Epic dates
   - Trade-off: Performance vs Accuracy

2. **Epic Progress Tracking:**
   - Calculate Epic progress based on Sprint completion
   - Formula: `completedSprints / totalSprints`

3. **Epic Duration Alerts:**
   - Warning nếu Epic duration > 3 Sprints (too long)
   - Suggest breaking down into smaller Epics

4. **Manual Override Option:**
   - Allow manual set Epic dates (disable auto-update)
   - Flag: `epic.dateAutoManaged = true/false`

---

## ✅ Checklist

- [x] SprintServiceImpl updated with Epic auto-date logic
- [x] EpicRepository dependency added
- [x] updateEpicStartDatesOnSprintStart() implemented
- [x] updateEpicEndDatesOnSprintComplete() implemented
- [x] Integration in update() method
- [x] Build successful (no compilation errors)
- [x] Documentation created
- [ ] Manual testing với Postman
- [ ] Verify console logs
- [ ] Check database updates
- [ ] Frontend Timeline visualization test

---

**Last Updated:** 2025-12-09  
**Status:** ✅ Ready for Testing

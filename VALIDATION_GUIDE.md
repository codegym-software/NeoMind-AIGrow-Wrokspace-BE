# 🎯 Date Validation Guide

## ✅ Các Validation Đã Được Thêm

### 1. **Sprint (CreateSprintReq & UpdateSprintReq)**
- ✅ `@ValidDateRange`: Kiểm tra `endDate >= startDate`
- ✅ Cho phép cả 2 trường nullable (không bắt buộc phải có date)

**Ví dụ Request HỢP LỆ:**
```json
{
  "name": "Sprint 1",
  "startDate": "2025-12-01",
  "endDate": "2025-12-15",
  "status": "active",
  "description": "First sprint"
}
```

**Ví dụ Request KHÔNG HỢP LỆ:**
```json
{
  "name": "Sprint 1",
  "startDate": "2025-12-15",
  "endDate": "2025-12-01",  ❌ endDate < startDate
  "status": "active"
}
```

**Response Lỗi:**
```json
{
  "message": "Validation failed",
  "errors": {
    "endDate": "End date (2025-12-01) must be after or equal to start date (2025-12-15)"
  }
}
```

---

### 2. **Epic (EpicCreateRequest & EpicUpdateRequest)**
- ✅ `@ValidDateRange`: Kiểm tra `endDate >= startDate`
- ✅ `@NotBlank` cho title (trong Create)
- ✅ Cho phép cả 2 date nullable

**Ví dụ Request HỢP LỆ:**
```json
{
  "title": "Epic: User Management",
  "description": "Complete user management system",
  "startDate": "2025-12-01",
  "endDate": "2026-03-31"
}
```

**Ví dụ Request KHÔNG HỢP LỆ:**
```json
{
  "title": "",  ❌ Title rỗng
  "startDate": "2026-01-01",
  "endDate": "2025-12-01"  ❌ endDate < startDate
}
```

**Response Lỗi:**
```json
{
  "message": "Validation failed",
  "errors": {
    "title": "Title is required",
    "endDate": "End date (2025-12-01) must be after or equal to start date (2026-01-01)"
  }
}
```

---

### 3. **Task**
- ℹ️ Task **chỉ có dueDate**, không có startDate/endDate
- ✅ dueDate là nullable (không bắt buộc)
- ✅ Business logic tự động check `isOverdue()` nếu `dueDate < LocalDate.now()`

---

## 🔧 Chi Tiết Implementation

### Custom Validator: `@ValidDateRange`

**Location:** `com.springboot.demo.validation.ValidDateRange`

**Cách hoạt động:**
```java
@ValidDateRange(
    startField = "startDate", 
    endField = "endDate", 
    message = "End date must be after or equal to start date"
)
public class YourDto {
    private LocalDate startDate;
    private LocalDate endDate;
}
```

**Validator Logic:**
1. ✅ Nếu `startDate` hoặc `endDate` là `null` → **PASS** (cho phép nullable)
2. ✅ Nếu cả 2 đều có giá trị → Kiểm tra `endDate >= startDate`
3. ❌ Nếu `endDate < startDate` → **FAIL** với message tùy chỉnh

**Hỗ trợ:**
- ✅ Java Records (như `CreateSprintReq`)
- ✅ Regular Classes (như `EpicCreateRequest`)

---

## 🧪 Test Cases Đề Xuất

### Test Sprint Validation:
```bash
# ✅ PASS: Cả 2 date đều null
POST /api/projects/{projectId}/sprints
{
  "name": "Backlog",
  "status": "planned",
  "isBacklog": true
}

# ✅ PASS: startDate = endDate
POST /api/projects/{projectId}/sprints
{
  "name": "Hot Fix Sprint",
  "startDate": "2025-12-10",
  "endDate": "2025-12-10",
  "status": "active"
}

# ❌ FAIL: endDate < startDate
POST /api/projects/{projectId}/sprints
{
  "name": "Invalid Sprint",
  "startDate": "2025-12-15",
  "endDate": "2025-12-10",
  "status": "active"
}
```

### Test Epic Validation:
```bash
# ✅ PASS: Chỉ có startDate
POST /api/projects/{projectId}/epics
{
  "title": "Phase 1",
  "startDate": "2025-12-01"
}

# ✅ PASS: Chỉ có endDate
POST /api/projects/{projectId}/epics
{
  "title": "Deadline Epic",
  "endDate": "2026-01-31"
}

# ❌ FAIL: endDate < startDate
PUT /api/projects/{projectId}/epics/{epicId}
{
  "startDate": "2025-12-20",
  "endDate": "2025-12-01"
}
```

---

## 📝 Lưu Ý Quan Trọng

### 1. **Validation Scope**
- ✅ Chỉ validate **mối quan hệ giữa start và end date**
- ⚠️ **KHÔNG validate** date trong quá khứ
- ⚠️ **KHÔNG validate** dueDate của Task có nằm trong Sprint/Epic không

### 2. **Nullable Design**
- Sprint và Epic cho phép tạo **không có date** → Linh hoạt với planning
- Task chỉ có `dueDate` → Đơn giản hơn

### 3. **Business Logic Existing**
- Sprint Complete: Tự động chuyển task chưa Done về Backlog
- Epic Progress: Tính % dựa trên task Done/Total
- Task Overdue: Tự động detect nếu `dueDate < now()` và chưa Done

---

## 🚀 Next Steps (Nếu Cần)

### Nếu muốn thêm validation cho "Không cho phép date trong quá khứ":
```java
@FutureOrPresent(message = "Start date must be today or in the future")
private LocalDate startDate;

@FutureOrPresent(message = "End date must be today or in the future")
private LocalDate endDate;
```

### Nếu muốn validate Task.dueDate phải nằm trong Sprint range:
- Cần thêm logic trong `TaskServiceImpl.create()` và `update()`
- Fetch Sprint entity và check range

---

## 📚 References

**Files Modified:**
- `dto/sprint/CreateSprintReq.java`
- `dto/sprint/UpdateSprintReq.java`
- `dto/epic/EpicCreateRequest.java`
- `dto/epic/EpicUpdateRequest.java`

**Files Created:**
- `validation/ValidDateRange.java` (Annotation)
- `validation/DateRangeValidator.java` (Validator Logic)

**Controllers Updated:**
- `EpicController.java` - Added `@Valid` annotation
- `SprintController.java` - Already had `@Valid` annotation

---

**✅ Validation đã được implement hoàn chỉnh và tested!**

# 🚀 TEST NEOM-69 QUA INTELLIJ

## BƯỚC 1: CHẠY APPLICATION
1. Mở IntelliJ IDEA
2. Import project NeoMind-BE
3. Tìm file `NeoMindBeApplication.java`
4. Right-click → Run 'NeoMindBeApplication'
5. Đợi application start (xem console log)

## BƯỚC 2: TEST QUA SWAGGER UI
1. Mở browser: http://localhost:8080/swagger-ui.html
2. Tìm section "Project Controller"
3. Test các APIs:

### 📝 CREATE PROJECT
```
POST /api/projects
Body:
{
  "workspaceId": "workspace-001",
  "name": "Test Project NEOM-69",
  "description": "Project test cho NEOM-69",
  "priority": "HIGH",
  "startDate": "2025-10-02",
  "endDate": "2025-12-02",
  "color": "#FF5733"
}
```

### 📋 GET ALL PROJECTS
```
GET /api/projects?workspaceId=workspace-001
```

### 🔍 GET PROJECT BY ID
```
GET /api/projects/{projectId}
```

### ✏️ UPDATE PROJECT
```
PUT /api/projects/{projectId}
Body:
{
  "name": "Updated Project Name",
  "status": "ACTIVE"
}
```

### 🗑️ DELETE PROJECT
```
DELETE /api/projects/{projectId}
```

## BƯỚC 3: VERIFY RESULTS
- Check response status codes (200, 201, 404, etc.)
- Verify response data structure
- Test error cases (invalid data, missing fields)

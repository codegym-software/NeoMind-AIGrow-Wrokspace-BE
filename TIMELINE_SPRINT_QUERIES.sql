-- ============================================================================
-- TIMELINE API - SPRINT QUERY (Upgraded Version)
-- ============================================================================
-- Purpose: Query để lấy danh sách Sprints cho Timeline/Gantt Chart
-- Date: 2025-12-04
-- Feature: Sprint headers hiển thị trên đầu biểu đồ Gantt
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Query 1: GET SPRINTS FOR TIMELINE
-- ----------------------------------------------------------------------------
-- Điều kiện:
-- 1. Chỉ lấy Sprint của project cụ thể
-- 2. Chỉ lấy Sprint có đầy đủ start_date và end_date (NOT NULL)
-- 3. Sắp xếp theo start_date tăng dần

SELECT 
    s.id,
    s.name,
    s.start_date,
    s.end_date,
    s.status,
    s.description,
    s.is_backlog
FROM sprints s
WHERE s.project_id = :projectId
  AND s.start_date IS NOT NULL      -- Bắt buộc có start_date
  AND s.end_date IS NOT NULL        -- Bắt buộc có end_date
ORDER BY s.start_date ASC;          -- Sort theo thời gian


-- ----------------------------------------------------------------------------
-- Query 2: FULL TIMELINE RESPONSE (Combined View)
-- ----------------------------------------------------------------------------
-- Response structure:
-- {
--   "items": [...],    // Epic + Task
--   "sprints": [...]   // Sprint headers
-- }

-- Part A: Items (Epic + Task) - Query cũ giữ nguyên
SELECT * FROM (
    -- Epics
    SELECT 
        CONCAT('epic-', e.id) as id,
        e.title as text,
        e.start_date,
        e.end_date,
        NULL as parent,
        'epic' as type,
        e.project_id
    FROM epics e
    WHERE e.project_id = :projectId
    
    UNION ALL
    
    -- Tasks với Soft Inheritance
    SELECT 
        CONCAT('task-', t.id) as id,
        t.title as text,
        COALESCE(t.start_date, s.start_date) as start_date,
        COALESCE(t.due_date, s.end_date) as end_date,
        CASE 
            WHEN t.epic_id IS NOT NULL THEN CONCAT('epic-', t.epic_id)
            ELSE NULL 
        END as parent,
        'task' as type,
        t.project_id
    FROM tasks t
    LEFT JOIN sprints s ON t.sprint_id = s.id
    WHERE t.project_id = :projectId
      AND t.archived_at IS NULL
) AS items
ORDER BY 
    CASE type WHEN 'epic' THEN 0 ELSE 1 END,
    start_date ASC;

-- Part B: Sprints (NEW)
SELECT 
    s.id,
    s.name,
    s.start_date,
    s.end_date,
    s.status
FROM sprints s
WHERE s.project_id = :projectId
  AND s.start_date IS NOT NULL
  AND s.end_date IS NOT NULL
ORDER BY s.start_date ASC;


-- ----------------------------------------------------------------------------
-- Query 3: SPRINT WITH TASK COUNT (Analytics)
-- ----------------------------------------------------------------------------
-- Đếm số lượng task trong mỗi Sprint

SELECT 
    s.id,
    s.name,
    s.start_date,
    s.end_date,
    s.status,
    
    -- Thống kê task
    COUNT(t.id) as total_tasks,
    SUM(CASE WHEN bc.name = 'Done' THEN 1 ELSE 0 END) as done_tasks,
    
    -- Progress
    ROUND(
        COALESCE(
            SUM(CASE WHEN bc.name = 'Done' THEN 1 ELSE 0 END) * 100.0 / 
            NULLIF(COUNT(t.id), 0),
            0
        ),
        2
    ) as progress_percentage

FROM sprints s
LEFT JOIN tasks t ON t.sprint_id = s.id AND t.archived_at IS NULL
LEFT JOIN board_columns bc ON t.column_id = bc.id

WHERE s.project_id = :projectId
  AND s.start_date IS NOT NULL
  AND s.end_date IS NOT NULL

GROUP BY s.id, s.name, s.start_date, s.end_date, s.status
ORDER BY s.start_date ASC;


-- ----------------------------------------------------------------------------
-- Query 4: SPRINT OVERLAP DETECTION
-- ----------------------------------------------------------------------------
-- Tìm các Sprint có thời gian overlap (để warning trên UI)

SELECT 
    s1.id as sprint1_id,
    s1.name as sprint1_name,
    s1.start_date as sprint1_start,
    s1.end_date as sprint1_end,
    
    s2.id as sprint2_id,
    s2.name as sprint2_name,
    s2.start_date as sprint2_start,
    s2.end_date as sprint2_end,
    
    -- Tính số ngày overlap
    DATEDIFF(
        LEAST(s1.end_date, s2.end_date),
        GREATEST(s1.start_date, s2.start_date)
    ) + 1 as overlap_days

FROM sprints s1
INNER JOIN sprints s2 ON s1.project_id = s2.project_id
                      AND s1.id < s2.id  -- Tránh duplicate
                      
WHERE s1.project_id = :projectId
  AND s1.start_date IS NOT NULL
  AND s1.end_date IS NOT NULL
  AND s2.start_date IS NOT NULL
  AND s2.end_date IS NOT NULL
  
  -- Điều kiện overlap: start1 <= end2 AND end1 >= start2
  AND s1.start_date <= s2.end_date
  AND s1.end_date >= s2.start_date

ORDER BY s1.start_date, s2.start_date;


-- ----------------------------------------------------------------------------
-- Query 5: SPRINT GANTT VISUALIZATION DATA
-- ----------------------------------------------------------------------------
-- Query tối ưu cho Frontend Gantt Chart library

SELECT 
    -- Sprint metadata
    s.id,
    s.name,
    s.start_date,
    s.end_date,
    s.status,
    
    -- Tính duration (số ngày)
    DATEDIFF(s.end_date, s.start_date) + 1 as duration_days,
    
    -- Check sprint đã quá hạn chưa
    CASE 
        WHEN s.end_date < CURDATE() AND s.status != 'completed' THEN TRUE
        ELSE FALSE
    END as is_overdue,
    
    -- Task statistics
    COUNT(DISTINCT t.id) as total_tasks,
    COUNT(DISTINCT CASE WHEN bc.name = 'Done' THEN t.id END) as done_tasks,
    
    -- Progress
    COALESCE(
        COUNT(DISTINCT CASE WHEN bc.name = 'Done' THEN t.id END) * 1.0 / 
        NULLIF(COUNT(DISTINCT t.id), 0),
        0
    ) as progress,
    
    -- Epic coverage (có bao nhiêu Epic trong Sprint)
    COUNT(DISTINCT t.epic_id) as epic_count

FROM sprints s
LEFT JOIN tasks t ON t.sprint_id = s.id AND t.archived_at IS NULL
LEFT JOIN board_columns bc ON t.column_id = bc.id

WHERE s.project_id = :projectId
  AND s.start_date IS NOT NULL
  AND s.end_date IS NOT NULL

GROUP BY s.id, s.name, s.start_date, s.end_date, s.status
ORDER BY s.start_date ASC;


-- ----------------------------------------------------------------------------
-- EXAMPLE RESPONSE
-- ----------------------------------------------------------------------------

-- JSON Response structure:
-- {
--   "message": "Timeline retrieved successfully",
--   "data": {
--     "items": [
--       {
--         "id": "epic-1",
--         "text": "User Management Module",
--         "startDate": "2025-12-01",
--         "endDate": "2026-03-31",
--         "parent": null,
--         "progress": 0.6,
--         "type": "epic"
--       },
--       {
--         "id": "task-10",
--         "text": "Implement Login API",
--         "startDate": "2025-12-01",
--         "endDate": "2025-12-15",
--         "parent": "epic-1",
--         "progress": 1.0,
--         "type": "task",
--         "sprintId": 22
--       }
--     ],
--     "sprints": [
--       {
--         "id": 22,
--         "name": "Sprint 1: Login",
--         "startDate": "2025-12-01",
--         "endDate": "2025-12-14",
--         "status": "active"
--       },
--       {
--         "id": 23,
--         "name": "Sprint 2: Dashboard",
--         "startDate": "2025-12-15",
--         "endDate": "2025-12-28",
--         "status": "planned"
--       }
--     ]
--   }
-- }


-- ----------------------------------------------------------------------------
-- PERFORMANCE TIPS
-- ----------------------------------------------------------------------------

-- 1. Index recommendations:
CREATE INDEX idx_sprints_project_dates ON sprints(project_id, start_date, end_date);
CREATE INDEX idx_sprints_status ON sprints(status);

-- 2. Filter Backlog Sprint (nếu không muốn hiển thị):
WHERE s.project_id = :projectId
  AND s.start_date IS NOT NULL
  AND s.end_date IS NOT NULL
  AND s.is_backlog = FALSE  -- Exclude backlog

-- 3. Limit số lượng Sprint (nếu cần):
LIMIT 20;  -- Chỉ lấy 20 Sprint gần nhất


-- ----------------------------------------------------------------------------
-- NOTES
-- ----------------------------------------------------------------------------

-- 1. Sprint Headers:
--    - Hiển thị ở đầu Gantt Chart như background layers
--    - Giúp so sánh Epic/Task progress với Sprint timeline

-- 2. Filtering:
--    - Chỉ lấy Sprint có đầy đủ start_date và end_date
--    - Backlog Sprint (is_backlog = TRUE) thường không có date → tự động filtered

-- 3. Sorting:
--    - Sort theo start_date ASC để hiển thị đúng thứ tự timeline

-- 4. Frontend Integration:
--    - Sprint data dùng để vẽ background bands
--    - items data dùng để vẽ tasks/epics
--    - sprintId trong Task để link với Sprint

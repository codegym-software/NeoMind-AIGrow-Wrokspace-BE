-- ============================================================================
-- TIMELINE/GANTT CHART - OPTIMIZED SQL QUERIES
-- ============================================================================
-- Purpose: Các query SQL tối ưu cho Timeline/Gantt Chart
-- Features: UNION, COALESCE, Soft Inheritance
-- Date: 2025-12-04
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Query 1: GET TIMELINE DATA (UNION approach)
-- ----------------------------------------------------------------------------
-- Mục đích: Lấy dữ liệu phẳng bao gồm Epic và Task với soft inheritance
-- Performance: Single query, optimized với index

SELECT 
    id,
    text,
    start_date,
    end_date,
    parent,
    progress,
    type,
    project_id,
    sprint_id,
    status,
    completed_date
FROM (
    -- Part 1: EPICS
    SELECT 
        CONCAT('epic-', e.id) as id,
        e.title as text,
        e.start_date,
        e.end_date,
        NULL as parent,
        -- Progress calculation: % tasks Done trong Epic này
        (
            SELECT COALESCE(
                SUM(CASE WHEN bc.name = 'Done' THEN 1 ELSE 0 END) / COUNT(*),
                0
            )
            FROM tasks t2
            LEFT JOIN board_columns bc ON t2.column_id = bc.id
            WHERE t2.epic_id = e.id 
              AND t2.archived_at IS NULL
        ) as progress,
        'epic' as type,
        e.project_id,
        NULL as sprint_id,
        NULL as status,
        NULL as completed_date
    FROM epics e
    WHERE e.project_id = :projectId
    
    UNION ALL
    
    -- Part 2: TASKS với Soft Inheritance
    SELECT 
        CONCAT('task-', t.id) as id,
        t.title as text,
        
        -- 🔥 SOFT INHERITANCE: start_date
        -- Ưu tiên: task.start_date → sprint.start_date → NULL
        COALESCE(t.start_date, s.start_date) as start_date,
        
        -- 🔥 SOFT INHERITANCE: due_date (end_date)
        -- Ưu tiên: task.due_date → sprint.end_date → NULL
        COALESCE(t.due_date, s.end_date) as end_date,
        
        -- Parent = Epic ID (format: "epic-{id}")
        CASE 
            WHEN t.epic_id IS NOT NULL THEN CONCAT('epic-', t.epic_id)
            ELSE NULL 
        END as parent,
        
        -- Progress: Done = 1.0, còn lại = 0.0
        CASE 
            WHEN bc.name = 'Done' THEN 1.0
            ELSE 0.0
        END as progress,
        
        'task' as type,
        t.project_id,
        t.sprint_id,
        bc.name as status,
        
        -- Completed date (để so sánh với due_date)
        DATE(t.completed_at) as completed_date
        
    FROM tasks t
    LEFT JOIN sprints s ON t.sprint_id = s.id
    LEFT JOIN board_columns bc ON t.column_id = bc.id
    WHERE t.project_id = :projectId
      AND t.archived_at IS NULL
      
) AS timeline

-- Sắp xếp: Epic trước, Task sau, theo start_date
ORDER BY 
    CASE type WHEN 'epic' THEN 0 ELSE 1 END,
    start_date ASC NULLS LAST;


-- ----------------------------------------------------------------------------
-- Query 2: GET TIMELINE WITH FILTERS
-- ----------------------------------------------------------------------------
-- Thêm filters: sprint, epic, status, date range

SELECT * FROM (
    -- Epic part (same as above)
    SELECT 
        CONCAT('epic-', e.id) as id,
        e.title as text,
        e.start_date,
        e.end_date,
        NULL as parent,
        0.0 as progress, -- Simplified for demo
        'epic' as type,
        e.project_id,
        NULL as sprint_id,
        NULL as status,
        NULL as epic_id,
        NULL as completed_date
    FROM epics e
    WHERE e.project_id = :projectId
    
    UNION ALL
    
    -- Task part (same as above)
    SELECT 
        CONCAT('task-', t.id) as id,
        t.title as text,
        COALESCE(t.start_date, s.start_date) as start_date,
        COALESCE(t.due_date, s.end_date) as end_date,
        CASE 
            WHEN t.epic_id IS NOT NULL THEN CONCAT('epic-', t.epic_id)
            ELSE NULL 
        END as parent,
        CASE 
            WHEN bc.name = 'Done' THEN 1.0
            ELSE 0.0
        END as progress,
        'task' as type,
        t.project_id,
        t.sprint_id,
        bc.name as status,
        t.epic_id,
        DATE(t.completed_at) as completed_date
    FROM tasks t
    LEFT JOIN sprints s ON t.sprint_id = s.id
    LEFT JOIN board_columns bc ON t.column_id = bc.id
    WHERE t.project_id = :projectId
      AND t.archived_at IS NULL
      
) AS timeline

-- Apply filters
WHERE 1=1
    -- Filter by sprint
    AND (:sprintId IS NULL OR sprint_id = :sprintId)
    
    -- Filter by epic
    AND (:epicId IS NULL OR epic_id = :epicId OR id = CONCAT('epic-', :epicId))
    
    -- Filter by status
    AND (:status IS NULL OR status = :status)
    
    -- Filter by date range
    AND (:startDateFrom IS NULL OR start_date >= :startDateFrom)
    AND (:startDateTo IS NULL OR start_date <= :startDateTo)
    
ORDER BY 
    CASE type WHEN 'epic' THEN 0 ELSE 1 END,
    start_date ASC;


-- ----------------------------------------------------------------------------
-- Query 3: UPDATE TASK TIMELINE (Drag & Drop)
-- ----------------------------------------------------------------------------
-- Update start_date và due_date khi kéo thả trên Gantt Chart

UPDATE tasks 
SET 
    start_date = :newStartDate,
    due_date = :newDueDate,
    updated_at = NOW()
WHERE id = :taskId
  AND project_id = :projectId
  AND archived_at IS NULL;

-- Validation check (run before update)
SELECT 
    CASE 
        WHEN :newDueDate < :newStartDate THEN 'ERROR: Due date must be >= start date'
        ELSE 'OK'
    END as validation_result;


-- ----------------------------------------------------------------------------
-- Query 4: COMPLETE TASK (Mark as Done)
-- ----------------------------------------------------------------------------
-- Update completed_at và status, KHÔNG thay đổi due_date

-- Step 1: Mark as completed
UPDATE tasks t
SET 
    t.completed_at = NOW(),
    t.column_id = (
        SELECT bc.id 
        FROM board_columns bc
        INNER JOIN boards b ON bc.board_id = b.id
        WHERE b.project_id = t.project_id 
          AND bc.name = 'Done'
          AND b.is_default = TRUE
        LIMIT 1
    ),
    t.updated_at = NOW()
WHERE t.id = :taskId
  AND t.project_id = :projectId
  AND t.archived_at IS NULL;

-- ⚠️ IMPORTANT: due_date GIỮ NGUYÊN để so sánh kế hoạch vs thực tế
-- ❌ WRONG: UPDATE tasks SET due_date = NOW() ...


-- ----------------------------------------------------------------------------
-- Query 5: UNCOMPLETE TASK (Unmark Done)
-- ----------------------------------------------------------------------------
-- Set completed_at = NULL, đưa về status "In Progress"

UPDATE tasks t
SET 
    t.completed_at = NULL,
    t.column_id = (
        SELECT bc.id 
        FROM board_columns bc
        INNER JOIN boards b ON bc.board_id = b.id
        WHERE b.project_id = t.project_id 
          AND bc.name = 'In Progress'
          AND b.is_default = TRUE
        LIMIT 1
    ),
    t.updated_at = NOW()
WHERE t.id = :taskId
  AND t.project_id = :projectId
  AND t.archived_at IS NULL;


-- ----------------------------------------------------------------------------
-- Query 6: ANALYTICS - Compare Plan vs Actual
-- ----------------------------------------------------------------------------
-- So sánh due_date (kế hoạch) vs completed_at (thực tế)

SELECT 
    t.id,
    t.title,
    t.due_date as planned_date,
    DATE(t.completed_at) as actual_date,
    
    -- Tính số ngày chênh lệch
    DATEDIFF(t.completed_at, t.due_date) as delay_days,
    
    -- Phân loại
    CASE 
        WHEN t.completed_at IS NULL THEN 'Not Completed'
        WHEN DATE(t.completed_at) < t.due_date THEN 'Early'
        WHEN DATE(t.completed_at) = t.due_date THEN 'On Time'
        WHEN DATE(t.completed_at) > t.due_date THEN 'Delayed'
    END as delivery_status,
    
    -- % delay
    CASE 
        WHEN t.due_date IS NOT NULL AND t.completed_at IS NOT NULL THEN
            ROUND(DATEDIFF(t.completed_at, t.due_date) * 100.0 / 
                  DATEDIFF(t.due_date, t.start_date), 2)
        ELSE NULL
    END as delay_percentage

FROM tasks t
WHERE t.project_id = :projectId
  AND t.archived_at IS NULL
  AND bc.name = 'Done'
ORDER BY delay_days DESC;


-- ----------------------------------------------------------------------------
-- Query 7: EPIC PROGRESS CALCULATION
-- ----------------------------------------------------------------------------
-- Tính progress cho Epic dựa trên % tasks Done

SELECT 
    e.id,
    e.title,
    COUNT(t.id) as total_tasks,
    SUM(CASE WHEN bc.name = 'Done' THEN 1 ELSE 0 END) as done_tasks,
    
    -- Progress: done_tasks / total_tasks
    ROUND(
        COALESCE(
            SUM(CASE WHEN bc.name = 'Done' THEN 1 ELSE 0 END) * 100.0 / 
            NULLIF(COUNT(t.id), 0),
            0
        ),
        2
    ) as progress_percentage,
    
    -- Auto-calculate Epic dates from Tasks
    MIN(COALESCE(t.start_date, s.start_date)) as actual_start_date,
    MAX(COALESCE(t.due_date, s.end_date)) as actual_end_date

FROM epics e
LEFT JOIN tasks t ON t.epic_id = e.id AND t.archived_at IS NULL
LEFT JOIN sprints s ON t.sprint_id = s.id
LEFT JOIN board_columns bc ON t.column_id = bc.id

WHERE e.project_id = :projectId

GROUP BY e.id, e.title
ORDER BY e.start_date;


-- ----------------------------------------------------------------------------
-- Query 8: PERFORMANCE INDEXES
-- ----------------------------------------------------------------------------
-- Tạo indexes để tối ưu performance

-- Index cho tasks.start_date (soft inheritance)
CREATE INDEX idx_tasks_start_date ON tasks(start_date);

-- Index cho tasks.completed_at (analytics)
CREATE INDEX idx_tasks_completed_at ON tasks(completed_at);

-- Index cho tasks.due_date (sorting)
CREATE INDEX idx_tasks_due_date ON tasks(due_date);

-- Composite index cho timeline query
CREATE INDEX idx_tasks_timeline ON tasks(project_id, archived_at, start_date);

-- Index cho soft inheritance (sprint join)
CREATE INDEX idx_tasks_sprint_id ON tasks(sprint_id);

-- Index cho parent relationship
CREATE INDEX idx_tasks_epic_id ON tasks(epic_id);


-- ----------------------------------------------------------------------------
-- NOTES
-- ----------------------------------------------------------------------------

-- 1. COALESCE: Dùng để implement soft inheritance
--    COALESCE(task.start_date, sprint.start_date) 
--    → Trả về giá trị đầu tiên NOT NULL

-- 2. UNION: Gộp Epic và Task thành 1 resultset
--    Phải có cùng số cột và type matching

-- 3. Performance: 
--    - Sử dụng indexes cho project_id, start_date, archived_at
--    - LEFT JOIN thay vì INNER JOIN để không bỏ sót data
--    - LIMIT 1 khi lấy single value

-- 4. Validation:
--    - Check due_date >= start_date trước khi update
--    - Check project_id để đảm bảo access control

-- 5. Important:
--    - KHÔNG update due_date khi complete task
--    - Giữ nguyên due_date để so sánh plan vs actual

# Frontend Update Prompt: Epic Auto-Dates Logic

**Version:** 2.0  
**Date:** 2025-12-09  
**Feature:** Handle Epic dates tự động từ Sprint lifecycle

---

## 🎯 Objective

Cập nhật Frontend Timeline component để hiển thị Epic dates theo logic mới:
- **Epic startDate/endDate**: Tự động được set từ Sprint lifecycle (không còn null)
- **Epic dates**: Có thể expand qua nhiều Sprints
- **Backend logic**: Epic dates được auto-update khi Sprint starts/completes

---

## 📊 Backend Logic Recap

### **Epic Dates Auto-Update Rules:**

| Event | Action | Epic Date Update |
|-------|--------|-----------------|
| **Sprint Start** (status → active) | Auto-set Epic startDate | `epic.startDate = sprint.startDate` (nếu null hoặc Sprint start sớm hơn) |
| **Sprint Complete** (status → completed) | Auto-set Epic endDate | `epic.endDate = sprint.endDate` (nếu null hoặc Sprint end muộn hơn) |

### **Example Flow:**

```
Initial State:
Epic A (startDate: null, endDate: null)
├── Task 1 (Sprint S1: Dec 1-14)
├── Task 2 (Sprint S2: Dec 15-28)
└── Task 3 (Sprint S3: Dec 29 - Jan 11)

After Sprint S1 starts:
Epic A (startDate: 2025-12-01, endDate: null)

After Sprint S2 starts:
Epic A (startDate: 2025-12-01, endDate: null)  // Không đổi (Dec 1 sớm hơn Dec 15)

After Sprint S3 completes:
Epic A (startDate: 2025-12-01, endDate: 2026-01-11)  // Epic covers toàn bộ 3 Sprints
```

---

## 🔧 Frontend Changes Required

### **1. Remove Epic Date Null Handling**

**Before (❌ Old Logic):**
```typescript
// Epic dates có thể null → cần fallback
const epic = timelineData.items.find(item => item.type === 'epic');

const epicStartDate = epic.startDate || '2025-01-01';  // ← Fallback
const epicEndDate = epic.endDate || '2025-12-31';      // ← Fallback

if (!epic.startDate || !epic.endDate) {
  console.warn('Epic missing dates, using fallback');
}
```

**After (✅ New Logic):**
```typescript
// Epic dates luôn có giá trị (auto-set từ Sprint)
const epic = timelineData.items.find(item => item.type === 'epic');

const epicStartDate = epic.startDate;  // ← Always has value
const epicEndDate = epic.endDate;      // ← Always has value

// No fallback needed!
```

---

### **2. Update Epic Bar Rendering**

**Before (❌ Old Logic):**
```typescript
// Skip render nếu Epic không có dates
const renderEpicBar = (epic: TimelineItem) => {
  if (!epic.startDate || !epic.endDate) {
    return null;  // ← Skip render
  }
  
  return (
    <GanttBar
      startDate={epic.startDate}
      endDate={epic.endDate}
      color="#9C27B0"
    />
  );
};
```

**After (✅ New Logic):**
```typescript
// Luôn render Epic bar (dates luôn có)
const renderEpicBar = (epic: TimelineItem) => {
  // No null check needed!
  return (
    <GanttBar
      startDate={epic.startDate}  // Always valid
      endDate={epic.endDate}      // Always valid
      color="#9C27B0"
    />
  );
};
```

---

### **3. Epic Date Display Logic**

**Before (❌ Old Logic):**
```tsx
// Show placeholder nếu Epic chưa có dates
<div className="epic-dates">
  {epic.startDate ? (
    format(epic.startDate, 'MMM dd, yyyy')
  ) : (
    <span className="placeholder">Not started</span>
  )}
  
  {' - '}
  
  {epic.endDate ? (
    format(epic.endDate, 'MMM dd, yyyy')
  ) : (
    <span className="placeholder">Not completed</span>
  )}
</div>
```

**After (✅ New Logic):**
```tsx
// Luôn hiển thị dates (không cần placeholder)
<div className="epic-dates">
  {format(epic.startDate, 'MMM dd, yyyy')}
  {' - '}
  {format(epic.endDate, 'MMM dd, yyyy')}
</div>

// Optional: Show status badge
<div className="epic-status">
  {isEpicInProgress(epic) ? (
    <span className="badge badge-active">In Progress</span>
  ) : isEpicCompleted(epic) ? (
    <span className="badge badge-done">Completed</span>
  ) : (
    <span className="badge badge-planned">Planned</span>
  )}
</div>
```

---

### **4. Epic Timeline Calculation**

**Updated Helper Functions:**

```typescript
// utils/epicHelpers.ts

/**
 * Check nếu Epic đang trong progress (có startDate, chưa có endDate hoặc endDate > today)
 */
export const isEpicInProgress = (epic: TimelineItem): boolean => {
  if (!epic.startDate) return false;
  
  const today = new Date();
  const startDate = new Date(epic.startDate);
  const endDate = epic.endDate ? new Date(epic.endDate) : null;
  
  // Epic đã start và chưa kết thúc
  return startDate <= today && (!endDate || endDate >= today);
};

/**
 * Check nếu Epic đã completed (có cả startDate và endDate, và endDate < today)
 */
export const isEpicCompleted = (epic: TimelineItem): boolean => {
  if (!epic.startDate || !epic.endDate) return false;
  
  const today = new Date();
  const endDate = new Date(epic.endDate);
  
  return endDate < today;
};

/**
 * Tính duration của Epic (số ngày)
 */
export const getEpicDuration = (epic: TimelineItem): number => {
  if (!epic.startDate || !epic.endDate) return 0;
  
  const start = new Date(epic.startDate);
  const end = new Date(epic.endDate);
  
  return differenceInDays(end, start) + 1;
};

/**
 * Get danh sách Sprints mà Epic covers
 */
export const getEpicSprints = (
  epic: TimelineItem, 
  sprints: Sprint[]
): Sprint[] => {
  if (!epic.startDate || !epic.endDate) return [];
  
  const epicStart = new Date(epic.startDate);
  const epicEnd = new Date(epic.endDate);
  
  return sprints.filter(sprint => {
    const sprintStart = new Date(sprint.startDate);
    const sprintEnd = new Date(sprint.endDate);
    
    // Sprint overlaps với Epic
    return (
      (sprintStart <= epicEnd && sprintEnd >= epicStart)
    );
  });
};
```

---

### **5. Timeline API Response Handling**

**API Response Structure (Unchanged):**
```typescript
interface TimelineResponse {
  items: TimelineItem[];
  sprints: Sprint[];
}

interface TimelineItem {
  id: string;
  text: string;
  startDate: string;      // ✅ ALWAYS has value for Epics (auto-set)
  endDate: string;        // ✅ ALWAYS has value for Epics (auto-set)
  parent: string | null;
  progress: number;
  type: "epic" | "task";
  sprintId?: number;
}
```

**Updated Data Fetching:**
```typescript
// hooks/useTimelineData.ts

export const useTimelineData = (projectId: string) => {
  return useQuery({
    queryKey: ['timeline', projectId],
    queryFn: async () => {
      const response = await axios.get<TimelineResponse>(
        `/api/projects/${projectId}/timeline`
      );
      
      const data = response.data.data;
      
      // ✅ Validate: Tất cả Epics phải có dates
      const epics = data.items.filter(item => item.type === 'epic');
      epics.forEach(epic => {
        if (!epic.startDate || !epic.endDate) {
          console.warn('⚠️ Epic missing dates:', epic.id, epic.text);
          console.warn('→ Make sure Sprint has been started/completed');
        }
      });
      
      return data;
    },
  });
};
```

---

### **6. Epic Tooltip/Popover Update**

**Enhanced Epic Info Display:**

```tsx
// components/EpicTooltip.tsx

interface EpicTooltipProps {
  epic: TimelineItem;
  sprints: Sprint[];
  tasks: TimelineItem[];
}

export const EpicTooltip: React.FC<EpicTooltipProps> = ({ epic, sprints, tasks }) => {
  const epicSprints = getEpicSprints(epic, sprints);
  const epicTasks = tasks.filter(t => t.parent === epic.id);
  const duration = getEpicDuration(epic);
  
  return (
    <div className="epic-tooltip">
      <h3>{epic.text}</h3>
      
      <div className="epic-info">
        <div className="info-row">
          <span className="label">Duration:</span>
          <span className="value">{duration} days</span>
        </div>
        
        <div className="info-row">
          <span className="label">Start:</span>
          <span className="value">{format(epic.startDate, 'MMM dd, yyyy')}</span>
        </div>
        
        <div className="info-row">
          <span className="label">End:</span>
          <span className="value">{format(epic.endDate, 'MMM dd, yyyy')}</span>
        </div>
        
        <div className="info-row">
          <span className="label">Progress:</span>
          <span className="value">{(epic.progress * 100).toFixed(0)}%</span>
        </div>
      </div>
      
      <div className="epic-sprints">
        <h4>Sprints ({epicSprints.length})</h4>
        <ul>
          {epicSprints.map(sprint => (
            <li key={sprint.id}>
              <span className={`sprint-badge sprint-${sprint.status}`}>
                {sprint.name}
              </span>
              <span className="sprint-dates">
                {format(sprint.startDate, 'MMM dd')} - {format(sprint.endDate, 'MMM dd')}
              </span>
            </li>
          ))}
        </ul>
      </div>
      
      <div className="epic-tasks">
        <h4>Tasks ({epicTasks.length})</h4>
        <div className="progress-breakdown">
          <span>Done: {epicTasks.filter(t => t.progress === 1.0).length}</span>
          <span>In Progress: {epicTasks.filter(t => t.progress > 0 && t.progress < 1.0).length}</span>
          <span>To Do: {epicTasks.filter(t => t.progress === 0).length}</span>
        </div>
      </div>
    </div>
  );
};
```

**CSS Styling:**
```css
.epic-tooltip {
  min-width: 300px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.epic-info .info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.epic-sprints ul {
  list-style: none;
  padding: 0;
}

.epic-sprints li {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
}

.sprint-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.sprint-active { background: #4CAF50; color: white; }
.sprint-completed { background: #9E9E9E; color: white; }
.sprint-planned { background: #2196F3; color: white; }

.progress-breakdown {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #666;
}
```

---

### **7. Visual Indicators for Epic Coverage**

**Show Epic spanning multiple Sprints:**

```tsx
// components/TimelineGrid.tsx

const renderEpicWithSprintOverlay = (epic: TimelineItem, sprints: Sprint[]) => {
  const epicSprints = getEpicSprints(epic, sprints);
  
  return (
    <div className="epic-row">
      {/* Epic bar */}
      <GanttBar
        id={epic.id}
        startDate={epic.startDate}
        endDate={epic.endDate}
        progress={epic.progress}
        color="#9C27B0"
        type="epic"
      />
      
      {/* Sprint overlay indicators */}
      <div className="epic-sprint-indicators">
        {epicSprints.map(sprint => {
          const sprintX = getXPosition(sprint.startDate);
          const sprintWidth = getBarWidth(sprint.startDate, sprint.endDate);
          
          return (
            <div
              key={sprint.id}
              className="sprint-indicator"
              style={{
                left: `${sprintX}px`,
                width: `${sprintWidth}px`,
              }}
              title={sprint.name}
            />
          );
        })}
      </div>
    </div>
  );
};
```

**CSS for Sprint Indicators:**
```css
.epic-row {
  position: relative;
}

.epic-sprint-indicators {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.sprint-indicator {
  position: absolute;
  height: 4px;
  bottom: 0;
  background: rgba(33, 150, 243, 0.3);
  border-left: 2px solid #2196F3;
  border-right: 2px solid #2196F3;
}
```

---

## 🧪 Testing Scenarios

### **Scenario 1: Epic với dates từ Backend**

#### **API Response:**
```json
{
  "items": [
    {
      "id": "epic-6",
      "text": "Quản lý người dùng",
      "startDate": "2025-12-01",  // ← Auto-set từ Sprint
      "endDate": "2025-12-14",    // ← Auto-set từ Sprint
      "type": "epic",
      "progress": 0.5
    }
  ]
}
```

#### **Expected Frontend Rendering:**
```tsx
// ✅ Epic bar renders with correct width
<GanttBar
  startDate="2025-12-01"  // Valid date
  endDate="2025-12-14"    // Valid date
  width={280}             // 14 days * 20px/day
  color="#9C27B0"
/>

// ✅ Epic info displays correctly
<div className="epic-dates">
  Dec 01, 2025 - Dec 14, 2025
</div>
<div className="epic-duration">
  14 days
</div>
```

---

### **Scenario 2: Epic covering multiple Sprints**

#### **API Response:**
```json
{
  "items": [
    {
      "id": "epic-12",
      "text": "User Authentication Module",
      "startDate": "2025-12-01",  // Sprint 1 start
      "endDate": "2026-01-11",    // Sprint 3 end
      "type": "epic"
    }
  ],
  "sprints": [
    {
      "id": 22,
      "name": "Sprint 1",
      "startDate": "2025-12-01",
      "endDate": "2025-12-14"
    },
    {
      "id": 23,
      "name": "Sprint 2",
      "startDate": "2025-12-15",
      "endDate": "2025-12-28"
    },
    {
      "id": 24,
      "name": "Sprint 3",
      "startDate": "2025-12-29",
      "endDate": "2026-01-11"
    }
  ]
}
```

#### **Expected Frontend Rendering:**
```tsx
// ✅ Epic bar spans across all 3 Sprints
<GanttBar
  startDate="2025-12-01"
  endDate="2026-01-11"
  width={840}  // 42 days
/>

// ✅ Tooltip shows all covered Sprints
<EpicTooltip>
  <h4>Sprints (3)</h4>
  <ul>
    <li>Sprint 1 (Dec 01 - Dec 14)</li>
    <li>Sprint 2 (Dec 15 - Dec 28)</li>
    <li>Sprint 3 (Dec 29 - Jan 11)</li>
  </ul>
</EpicTooltip>
```

---

### **Scenario 3: Warning for Epic without dates**

#### **API Response (Edge case):**
```json
{
  "items": [
    {
      "id": "epic-99",
      "text": "New Epic (No Sprint started yet)",
      "startDate": null,  // ← Sprint chưa start
      "endDate": null,
      "type": "epic"
    }
  ]
}
```

#### **Expected Frontend Behavior:**
```typescript
// ⚠️ Console warning
console.warn('⚠️ Epic missing dates:', 'epic-99', 'New Epic (No Sprint started yet)');
console.warn('→ Make sure Sprint has been started/completed');

// Show placeholder UI
<div className="epic-placeholder">
  <span className="icon">⏳</span>
  <span className="message">Epic dates will be set when Sprint starts</span>
</div>
```

**Placeholder CSS:**
```css
.epic-placeholder {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #FFF3E0;
  border-left: 4px solid #FF9800;
  border-radius: 4px;
  color: #E65100;
  font-size: 13px;
}

.epic-placeholder .icon {
  font-size: 20px;
}
```

---

## 📊 Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    BACKEND (Spring Boot)                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Sprint.status = "active"                                   │
│        ↓                                                     │
│  SprintServiceImpl.update()                                 │
│        ↓                                                     │
│  updateEpicStartDatesOnSprintStart()                        │
│        ↓                                                     │
│  Epic.startDate = Sprint.startDate                          │
│                                                              │
│  Sprint.status = "completed"                                │
│        ↓                                                     │
│  SprintServiceImpl.update()                                 │
│        ↓                                                     │
│  updateEpicEndDatesOnSprintComplete()                       │
│        ↓                                                     │
│  Epic.endDate = Sprint.endDate                              │
│                                                              │
└─────────────────────────────────────────────────────────────┘
                           ↓
                  API: GET /timeline
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND (React/Next.js)                  │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  useTimelineData(projectId)                                 │
│        ↓                                                     │
│  response.data.items[] ← Epic with dates                    │
│        ↓                                                     │
│  Validate: epic.startDate && epic.endDate                   │
│        ↓                                                     │
│  Render Epic Bar                                            │
│    - Calculate X position from startDate                    │
│    - Calculate width from duration                          │
│    - Show Sprint indicators                                 │
│        ↓                                                     │
│  Display Epic Info                                          │
│    - Format dates: "MMM dd, yyyy"                           │
│    - Show duration: "X days"                                │
│    - List covered Sprints                                   │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔄 Migration Checklist

### **Phase 1: Code Updates**
- [ ] Remove Epic date null fallbacks
- [ ] Update Epic bar rendering (remove null checks)
- [ ] Add Epic date validation in data fetch
- [ ] Update Epic tooltip/popover content
- [ ] Add Sprint coverage visualization

### **Phase 2: Styling Updates**
- [ ] Update Epic placeholder styles (for edge cases)
- [ ] Add Sprint indicator overlay styles
- [ ] Update Epic tooltip CSS
- [ ] Add status badges for Epic state

### **Phase 3: Testing**
- [ ] Test Epic với 1 Sprint (dates match Sprint)
- [ ] Test Epic với nhiều Sprints (dates span multiple)
- [ ] Test Epic chưa có dates (Sprint chưa start)
- [ ] Test Epic info tooltip (shows Sprint list)
- [ ] Test Timeline zoom (Epic bar scales correctly)

### **Phase 4: Documentation**
- [ ] Update component props documentation
- [ ] Add JSDoc comments for Epic helpers
- [ ] Update Storybook examples (if using)
- [ ] Create developer guide for Epic date logic

---

## 💡 Best Practices

### **1. Type Safety:**
```typescript
// Define strict types
interface Epic extends TimelineItem {
  type: 'epic';
  startDate: string;  // Required (auto-set by backend)
  endDate: string;    // Required (auto-set by backend)
}

// Type guard
function isEpic(item: TimelineItem): item is Epic {
  return item.type === 'epic';
}

// Usage
const epics = timelineData.items.filter(isEpic);
epics.forEach(epic => {
  // TypeScript knows epic.startDate is string, not null
  const start = new Date(epic.startDate);
});
```

### **2. Error Handling:**
```typescript
// Handle edge case: Epic chưa có dates
const renderEpicBar = (epic: TimelineItem) => {
  if (!epic.startDate || !epic.endDate) {
    // Log warning to console
    console.warn(`Epic ${epic.id} missing dates. Sprint not started yet?`);
    
    // Show placeholder
    return <EpicPlaceholder epic={epic} />;
  }
  
  // Normal render
  return <GanttBar {...epic} />;
};
```

### **3. Performance Optimization:**
```typescript
// Memoize Epic calculations
const epicHelpers = useMemo(() => ({
  duration: getEpicDuration(epic),
  sprints: getEpicSprints(epic, sprints),
  isInProgress: isEpicInProgress(epic),
  isCompleted: isEpicCompleted(epic),
}), [epic, sprints]);

// Use in component
<EpicTooltip 
  epic={epic} 
  duration={epicHelpers.duration}
  sprints={epicHelpers.sprints}
/>
```

---

## 🎯 Acceptance Criteria

### **Definition of Done:**
- [ ] Epic bars render với correct dates (no null handling)
- [ ] Epic tooltip shows Sprint coverage list
- [ ] Epic dates format: "MMM dd, yyyy"
- [ ] Epic duration calculated correctly
- [ ] Sprint indicators visible on Epic bar
- [ ] Warning logged cho Epic without dates
- [ ] Placeholder UI shown cho Epic chưa có dates
- [ ] No console errors
- [ ] Timeline zoom works correctly with Epic dates
- [ ] Epic hover effects show enhanced info

---

## 🚀 Quick Start Code

**Complete Example Component:**

```tsx
// components/Timeline/EpicRow.tsx

import React, { useMemo } from 'react';
import { format, differenceInDays } from 'date-fns';
import { TimelineItem, Sprint } from '@/types';

interface EpicRowProps {
  epic: TimelineItem;
  sprints: Sprint[];
  tasks: TimelineItem[];
  onEpicClick: (epicId: string) => void;
}

export const EpicRow: React.FC<EpicRowProps> = ({ 
  epic, 
  sprints, 
  tasks,
  onEpicClick 
}) => {
  // Calculate Epic metrics
  const metrics = useMemo(() => {
    if (!epic.startDate || !epic.endDate) {
      return null;
    }
    
    const start = new Date(epic.startDate);
    const end = new Date(epic.endDate);
    const duration = differenceInDays(end, start) + 1;
    
    // Find covered Sprints
    const coveredSprints = sprints.filter(sprint => {
      const sprintStart = new Date(sprint.startDate);
      const sprintEnd = new Date(sprint.endDate);
      return sprintStart <= end && sprintEnd >= start;
    });
    
    // Calculate Epic tasks
    const epicTasks = tasks.filter(t => t.parent === epic.id);
    const doneTasks = epicTasks.filter(t => t.progress === 1.0);
    
    return {
      duration,
      coveredSprints,
      totalTasks: epicTasks.length,
      doneTasks: doneTasks.length,
      progress: epicTasks.length > 0 ? doneTasks.length / epicTasks.length : 0,
    };
  }, [epic, sprints, tasks]);
  
  // Handle Epic without dates
  if (!metrics) {
    return (
      <div className="epic-row epic-pending">
        <div className="epic-info">
          <span className="epic-icon">◊</span>
          <span className="epic-title">{epic.text}</span>
          <span className="epic-warning">⏳ Waiting for Sprint to start</span>
        </div>
      </div>
    );
  }
  
  return (
    <div className="epic-row" onClick={() => onEpicClick(epic.id)}>
      <div className="epic-info">
        <span className="epic-icon">◊</span>
        <span className="epic-title">{epic.text}</span>
        <span className="epic-dates">
          {format(new Date(epic.startDate), 'MMM dd')} - 
          {format(new Date(epic.endDate), 'MMM dd, yyyy')}
        </span>
        <span className="epic-duration">{metrics.duration} days</span>
      </div>
      
      <div className="epic-chart">
        <GanttBar
          id={epic.id}
          startDate={epic.startDate}
          endDate={epic.endDate}
          progress={metrics.progress}
          color="#9C27B0"
          type="epic"
        />
        
        {/* Sprint indicators */}
        {metrics.coveredSprints.map(sprint => (
          <SprintIndicator key={sprint.id} sprint={sprint} />
        ))}
      </div>
      
      <div className="epic-stats">
        <span>{metrics.doneTasks}/{metrics.totalTasks} tasks</span>
        <span>{metrics.coveredSprints.length} sprints</span>
      </div>
    </div>
  );
};
```

---

## 📚 References

- **Backend Documentation**: `EPIC_AUTO_DATES_FEATURE.md`
- **Timeline API**: `TIMELINE_API_DOCUMENTATION.md`
- **Postman Collection**: `Timeline_APIs.postman_collection.json`

---

**Ready to implement! 🚀**

**Questions?** Check Backend logs for Epic auto-date messages:
```
✅ Auto-set Epic[6] startDate = 2025-12-01
✅ Auto-set Epic[8] endDate = 2025-12-14
```

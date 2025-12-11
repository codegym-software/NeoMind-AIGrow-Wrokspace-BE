# Frontend Prompt: Hiển thị Sprint Headers trên Timeline/Gantt Chart

## 🎯 Mục tiêu
Bổ sung phần hiển thị **Sprint Headers** (các thanh Sprint) trên Timeline/Gantt Chart, cho phép người dùng nhìn thấy rõ ràng:
- Các Sprint đang chạy trong project
- Thời gian bắt đầu và kết thúc của mỗi Sprint
- Trạng thái của Sprint (planned, active, completed)
- Task nào thuộc Sprint nào

## 📊 API Response Structure (Đã thay đổi)

### Trước đây (v1.0):
```typescript
// GET /api/projects/{projectId}/timeline
{
  "message": "string",
  "data": [  // ← Mảng phẳng chỉ có items
    {
      "id": 1,
      "type": "epic",
      "title": "Epic 1",
      "startDate": "2025-01-01",
      "dueDate": "2025-01-15"
    }
  ]
}
```

### Hiện tại (v2.0):
```typescript
// GET /api/projects/{projectId}/timeline
{
  "message": "string",
  "data": {
    "items": [  // ← Danh sách Epic + Task
      {
        "id": 1,
        "type": "epic",
        "title": "Epic 1",
        "startDate": "2025-01-01",
        "dueDate": "2025-01-15",
        "sprintId": null
      },
      {
        "id": 101,
        "type": "task",
        "title": "Task 1",
        "startDate": "2025-01-01",  // Có thể là từ Sprint (soft inheritance)
        "dueDate": "2025-01-05",
        "sprintId": 1,  // ← Task thuộc Sprint nào
        "epicId": 1
      }
    ],
    "sprints": [  // ← Danh sách Sprint (MỚI)
      {
        "id": 1,
        "name": "Sprint 1",
        "startDate": "2025-01-01",
        "endDate": "2025-01-15",
        "status": "active"  // planned | active | completed
      },
      {
        "id": 2,
        "name": "Sprint 2",
        "startDate": "2025-01-16",
        "endDate": "2025-01-30",
        "status": "planned"
      }
    ]
  }
}
```

## 🔧 TypeScript Types

### Cập nhật Types
```typescript
// types/timeline.ts

// Enum cho Sprint Status
export enum SprintStatus {
  PLANNED = 'planned',
  ACTIVE = 'active',
  COMPLETED = 'completed'
}

// Sprint DTO (MỚI)
export interface SprintDto {
  id: number;
  name: string;
  startDate: string;  // ISO date string: "2025-01-01"
  endDate: string;    // ISO date string: "2025-01-15"
  status: SprintStatus;
}

// Timeline Item (giữ nguyên)
export interface TimelineItem {
  id: number;
  type: 'epic' | 'task';
  title: string;
  startDate: string | null;
  dueDate: string | null;
  sprintId: number | null;  // ← Task thuộc Sprint nào
  epicId?: number | null;
  // ... các field khác
}

// Timeline Response (CẬP NHẬT)
export interface TimelineResponse {
  items: TimelineItem[];    // Epic + Task
  sprints: SprintDto[];     // Sprint headers (MỚI)
}

// API Response Wrapper
export interface ApiResponse<T> {
  message: string;
  data: T;
}
```

## 🎨 UI Design - Sprint Headers

### Visual Layout
```
┌─────────────────────────────────────────────────────────────────┐
│ Timeline/Gantt Chart                                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│ [Sprint 1 - Active]════════════[Sprint 2 - Planned]═══════════ │ ← Sprint Headers Row
│  Jan 1 - Jan 15                 Jan 16 - Jan 30                │
│                                                                 │
│ ────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────┬────── │ ← Date Grid
│  1  │  3 │  5 │  7 │  9 │ 11 │ 13 │ 15 │ 17 │ 19 │ 21 │ 23   │
│                                                                 │
│ Epic 1      [═══════════════════════]                          │ ← Epic Row
│                                                                 │
│   Task 1    [═══════]                                          │ ← Task Row (Sprint 1)
│   Task 2         [════════]                                    │ ← Task Row (Sprint 1)
│   Task 3                     [═══════]                         │ ← Task Row (Sprint 2)
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Sprint Header Design Specs

#### 1. **Sprint Bar (Thanh Sprint)**
```tsx
// Visual: Thanh ngang hiển thị toàn bộ timeline của Sprint
<div className="sprint-header">
  <div 
    className={`sprint-bar sprint-bar--${sprint.status}`}
    style={{
      left: calculatePosition(sprint.startDate),
      width: calculateWidth(sprint.startDate, sprint.endDate)
    }}
  >
    <span className="sprint-name">{sprint.name}</span>
    <span className="sprint-dates">
      {formatDate(sprint.startDate)} - {formatDate(sprint.endDate)}
    </span>
    <span className={`sprint-status-badge sprint-status-badge--${sprint.status}`}>
      {sprint.status}
    </span>
  </div>
</div>
```

#### 2. **Color Coding theo Status**
```css
/* Sprint Status Colors */
.sprint-bar--planned {
  background: linear-gradient(to right, #E3F2FD, #BBDEFB);
  border: 2px solid #2196F3;
  color: #1976D2;
}

.sprint-bar--active {
  background: linear-gradient(to right, #E8F5E9, #C8E6C9);
  border: 2px solid #4CAF50;
  color: #388E3C;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
  animation: pulse 2s infinite;
}

.sprint-bar--completed {
  background: linear-gradient(to right, #F3E5F5, #E1BEE7);
  border: 2px solid #9C27B0;
  color: #7B1FA2;
  opacity: 0.8;
}

/* Active Sprint Animation */
@keyframes pulse {
  0%, 100% { box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3); }
  50% { box-shadow: 0 4px 12px rgba(76, 175, 80, 0.5); }
}
```

## 💻 Implementation Steps

### Step 1: Cập nhật API Call
```typescript
// services/timelineService.ts

export const getTimeline = async (projectId: string): Promise<TimelineResponse> => {
  const response = await api.get<ApiResponse<TimelineResponse>>(
    `/api/projects/${projectId}/timeline`
  );
  
  return response.data.data;  // ← Trả về { items, sprints }
};
```

### Step 2: Component Structure
```
components/
├── Timeline/
│   ├── TimelineView.tsx          ← Main container
│   ├── SprintHeaders.tsx         ← NEW: Sprint headers row
│   ├── SprintBar.tsx              ← NEW: Single sprint bar
│   ├── TimelineGrid.tsx           ← Date grid
│   ├── TimelineItems.tsx          ← Epic + Task rows
│   └── TimelineItem.tsx           ← Single item bar
```

### Step 3: SprintHeaders Component
```tsx
// components/Timeline/SprintHeaders.tsx

import React from 'react';
import { SprintDto } from '@/types/timeline';
import SprintBar from './SprintBar';

interface SprintHeadersProps {
  sprints: SprintDto[];
  startDate: Date;  // Timeline start date
  endDate: Date;    // Timeline end date
  pixelsPerDay: number;  // Scale factor
}

export const SprintHeaders: React.FC<SprintHeadersProps> = ({
  sprints,
  startDate,
  endDate,
  pixelsPerDay
}) => {
  return (
    <div className="sprint-headers-container">
      {sprints.map(sprint => (
        <SprintBar
          key={sprint.id}
          sprint={sprint}
          timelineStart={startDate}
          pixelsPerDay={pixelsPerDay}
        />
      ))}
    </div>
  );
};
```

### Step 4: SprintBar Component
```tsx
// components/Timeline/SprintBar.tsx

import React from 'react';
import { SprintDto, SprintStatus } from '@/types/timeline';
import { differenceInDays, parseISO } from 'date-fns';

interface SprintBarProps {
  sprint: SprintDto;
  timelineStart: Date;
  pixelsPerDay: number;
}

export const SprintBar: React.FC<SprintBarProps> = ({
  sprint,
  timelineStart,
  pixelsPerDay
}) => {
  const sprintStart = parseISO(sprint.startDate);
  const sprintEnd = parseISO(sprint.endDate);
  
  // Calculate position and width
  const offsetDays = differenceInDays(sprintStart, timelineStart);
  const durationDays = differenceInDays(sprintEnd, sprintStart);
  
  const left = offsetDays * pixelsPerDay;
  const width = durationDays * pixelsPerDay;
  
  // Status badge text
  const statusLabels = {
    [SprintStatus.PLANNED]: 'Planned',
    [SprintStatus.ACTIVE]: 'Active',
    [SprintStatus.COMPLETED]: 'Completed'
  };
  
  return (
    <div
      className={`sprint-bar sprint-bar--${sprint.status}`}
      style={{
        position: 'absolute',
        left: `${left}px`,
        width: `${width}px`,
        height: '60px',
        borderRadius: '8px',
        padding: '8px 12px',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        zIndex: 1
      }}
    >
      {/* Sprint Name */}
      <div className="sprint-name" style={{ fontWeight: 600, fontSize: '14px' }}>
        {sprint.name}
      </div>
      
      {/* Sprint Dates */}
      <div className="sprint-dates" style={{ fontSize: '12px', opacity: 0.8 }}>
        {formatDate(sprint.startDate)} - {formatDate(sprint.endDate)}
      </div>
      
      {/* Status Badge */}
      <div 
        className={`sprint-status-badge sprint-status-badge--${sprint.status}`}
        style={{
          position: 'absolute',
          top: '4px',
          right: '8px',
          padding: '2px 8px',
          borderRadius: '12px',
          fontSize: '11px',
          fontWeight: 600,
          textTransform: 'uppercase'
        }}
      >
        {statusLabels[sprint.status]}
      </div>
    </div>
  );
};

// Helper function
const formatDate = (dateStr: string): string => {
  const date = parseISO(dateStr);
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
};
```

### Step 5: Integrate vào TimelineView
```tsx
// components/Timeline/TimelineView.tsx

import React, { useState, useEffect } from 'react';
import { getTimeline } from '@/services/timelineService';
import { TimelineResponse } from '@/types/timeline';
import SprintHeaders from './SprintHeaders';
import TimelineGrid from './TimelineGrid';
import TimelineItems from './TimelineItems';

export const TimelineView: React.FC<{ projectId: string }> = ({ projectId }) => {
  const [data, setData] = useState<TimelineResponse | null>(null);
  const [loading, setLoading] = useState(true);
  
  // Timeline settings
  const pixelsPerDay = 40;  // Scale factor
  const startDate = calculateTimelineStart(data);
  const endDate = calculateTimelineEnd(data);
  
  useEffect(() => {
    loadTimeline();
  }, [projectId]);
  
  const loadTimeline = async () => {
    try {
      const response = await getTimeline(projectId);
      setData(response);
    } catch (error) {
      console.error('Failed to load timeline:', error);
    } finally {
      setLoading(false);
    }
  };
  
  if (loading) return <LoadingSpinner />;
  if (!data) return <ErrorMessage />;
  
  return (
    <div className="timeline-container">
      {/* Sprint Headers Row */}
      <div className="timeline-section timeline-section--sprints">
        <SprintHeaders
          sprints={data.sprints}
          startDate={startDate}
          endDate={endDate}
          pixelsPerDay={pixelsPerDay}
        />
      </div>
      
      {/* Date Grid */}
      <div className="timeline-section timeline-section--grid">
        <TimelineGrid
          startDate={startDate}
          endDate={endDate}
          pixelsPerDay={pixelsPerDay}
        />
      </div>
      
      {/* Items (Epic + Task) */}
      <div className="timeline-section timeline-section--items">
        <TimelineItems
          items={data.items}
          startDate={startDate}
          pixelsPerDay={pixelsPerDay}
        />
      </div>
    </div>
  );
};

// Helper functions
const calculateTimelineStart = (data: TimelineResponse | null): Date => {
  if (!data) return new Date();
  
  const allDates = [
    ...data.sprints.map(s => parseISO(s.startDate)),
    ...data.items.filter(i => i.startDate).map(i => parseISO(i.startDate!))
  ];
  
  return min(allDates);
};

const calculateTimelineEnd = (data: TimelineResponse | null): Date => {
  if (!data) return new Date();
  
  const allDates = [
    ...data.sprints.map(s => parseISO(s.endDate)),
    ...data.items.filter(i => i.dueDate).map(i => parseISO(i.dueDate!))
  ];
  
  return max(allDates);
};
```

## 🎨 CSS Styling

```css
/* Sprint Headers Container */
.sprint-headers-container {
  position: relative;
  height: 80px;
  background: #FAFAFA;
  border-bottom: 2px solid #E0E0E0;
  margin-bottom: 16px;
  overflow: visible;
}

/* Sprint Bar Base */
.sprint-bar {
  position: absolute;
  height: 60px;
  border-radius: 8px;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  box-sizing: border-box;
  transition: all 0.3s ease;
  cursor: pointer;
}

.sprint-bar:hover {
  transform: translateY(-2px);
  filter: brightness(1.05);
}

/* Sprint Status Colors */
.sprint-bar--planned {
  background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%);
  border: 2px solid #2196F3;
  color: #1565C0;
}

.sprint-bar--active {
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  border: 2px solid #4CAF50;
  color: #2E7D32;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
  animation: pulse 2s infinite;
}

.sprint-bar--completed {
  background: linear-gradient(135deg, #F3E5F5 0%, #E1BEE7 100%);
  border: 2px solid #9C27B0;
  color: #6A1B9A;
  opacity: 0.85;
}

/* Sprint Name */
.sprint-name {
  font-weight: 600;
  font-size: 14px;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Sprint Dates */
.sprint-dates {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 2px;
}

/* Status Badge */
.sprint-status-badge {
  position: absolute;
  top: 4px;
  right: 8px;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.sprint-status-badge--planned {
  background: #2196F3;
  color: white;
}

.sprint-status-badge--active {
  background: #4CAF50;
  color: white;
}

.sprint-status-badge--completed {
  background: #9C27B0;
  color: white;
}

/* Active Sprint Animation */
@keyframes pulse {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
  }
  50% {
    box-shadow: 0 4px 16px rgba(76, 175, 80, 0.5);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .sprint-bar {
    height: 50px;
    padding: 6px 8px;
  }
  
  .sprint-name {
    font-size: 12px;
  }
  
  .sprint-dates {
    font-size: 10px;
  }
}
```

## 🔄 Task-Sprint Visual Relationship

### Hiển thị Task thuộc Sprint nào
```tsx
// components/Timeline/TimelineItem.tsx

interface TimelineItemProps {
  item: TimelineItem;
  sprints: SprintDto[];  // ← Truyền thêm sprints
  // ... other props
}

export const TimelineItem: React.FC<TimelineItemProps> = ({ item, sprints }) => {
  // Tìm Sprint mà Task này thuộc về
  const sprint = sprints.find(s => s.id === item.sprintId);
  
  return (
    <div className="timeline-item">
      {/* Task Bar */}
      <div 
        className={`task-bar ${sprint ? `task-bar--sprint-${sprint.status}` : ''}`}
        style={calculatePosition(item)}
      >
        <span className="task-title">{item.title}</span>
        
        {/* Sprint Badge (nếu Task thuộc Sprint) */}
        {sprint && (
          <span className="task-sprint-badge" title={sprint.name}>
            {sprint.name}
          </span>
        )}
      </div>
    </div>
  );
};
```

### CSS cho Task-Sprint Relationship
```css
/* Task bar color based on Sprint status */
.task-bar--sprint-planned {
  border-left: 4px solid #2196F3;
}

.task-bar--sprint-active {
  border-left: 4px solid #4CAF50;
}

.task-bar--sprint-completed {
  border-left: 4px solid #9C27B0;
}

/* Sprint Badge on Task */
.task-sprint-badge {
  position: absolute;
  bottom: 2px;
  right: 4px;
  font-size: 10px;
  padding: 2px 6px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  opacity: 0.7;
}
```

## 🧪 Test Cases

### Test Scenarios
```typescript
// Test 1: Empty Sprints
const emptyResponse: TimelineResponse = {
  items: [...],
  sprints: []  // ← Không có Sprint nào
};
// Expected: Không hiển thị Sprint headers, chỉ hiển thị items

// Test 2: Single Active Sprint
const singleSprintResponse: TimelineResponse = {
  items: [...],
  sprints: [
    { id: 1, name: "Sprint 1", startDate: "2025-01-01", endDate: "2025-01-15", status: "active" }
  ]
};
// Expected: Hiển thị 1 Sprint bar màu xanh lá với animation

// Test 3: Multiple Sprints (Different Status)
const multiSprintsResponse: TimelineResponse = {
  items: [...],
  sprints: [
    { id: 1, name: "Sprint 1", startDate: "2025-01-01", endDate: "2025-01-15", status: "completed" },
    { id: 2, name: "Sprint 2", startDate: "2025-01-16", endDate: "2025-01-30", status: "active" },
    { id: 3, name: "Sprint 3", startDate: "2025-02-01", endDate: "2025-02-15", status: "planned" }
  ]
};
// Expected: 3 Sprint bars với màu sắc khác nhau, Sprint 2 có animation

// Test 4: Overlapping Sprints (Edge Case)
const overlappingResponse: TimelineResponse = {
  items: [...],
  sprints: [
    { id: 1, name: "Sprint A", startDate: "2025-01-01", endDate: "2025-01-20", status: "active" },
    { id: 2, name: "Sprint B", startDate: "2025-01-10", endDate: "2025-01-25", status: "planned" }
  ]
};
// Expected: 2 Sprint bars overlap nhau, cần adjust z-index hoặc stacking
```

## 📝 Checklist Implementation

- [ ] **Step 1:** Cập nhật Types (`SprintDto`, `TimelineResponse`)
- [ ] **Step 2:** Cập nhật API Service (`getTimeline` return type)
- [ ] **Step 3:** Tạo `SprintHeaders.tsx` component
- [ ] **Step 4:** Tạo `SprintBar.tsx` component
- [ ] **Step 5:** Thêm CSS styling cho Sprint bars
- [ ] **Step 6:** Integrate Sprint Headers vào `TimelineView`
- [ ] **Step 7:** Thêm Task-Sprint relationship visual
- [ ] **Step 8:** Test với các scenarios khác nhau
- [ ] **Step 9:** Responsive design cho mobile
- [ ] **Step 10:** Add animations cho Active Sprint

## 🎁 Bonus Features (Optional)

### 1. Sprint Tooltip
```tsx
<Tooltip
  content={
    <div>
      <strong>{sprint.name}</strong>
      <div>Status: {sprint.status}</div>
      <div>Duration: {calculateDuration(sprint)} days</div>
      <div>Tasks: {countTasksInSprint(sprint.id)}</div>
    </div>
  }
>
  <SprintBar sprint={sprint} />
</Tooltip>
```

### 2. Click to Filter Tasks
```tsx
const handleSprintClick = (sprintId: number) => {
  // Filter timeline to show only tasks in this Sprint
  setFilteredItems(items.filter(item => item.sprintId === sprintId));
};
```

### 3. Today Line Indicator
```tsx
const TodayLine: React.FC = () => {
  const today = new Date();
  const offsetDays = differenceInDays(today, timelineStart);
  const left = offsetDays * pixelsPerDay;
  
  return (
    <div
      className="today-line"
      style={{
        position: 'absolute',
        left: `${left}px`,
        width: '2px',
        height: '100%',
        background: 'red',
        zIndex: 100
      }}
    />
  );
};
```

---

## 🚀 Summary

**Backend đã sẵn sàng:**
- ✅ API trả về `sprints` với `status`, `startDate`, `endDate`
- ✅ Task có `sprintId` để link với Sprint

**Frontend cần implement:**
1. Cập nhật Types cho Sprint
2. Tạo `SprintHeaders` component hiển thị Sprint bars
3. Style Sprint bars theo status (planned/active/completed)
4. Hiển thị Sprint headers trên Timeline
5. Visual relationship giữa Task và Sprint

**Expected Result:**
```
[Sprint 1 - Active]══════[Sprint 2 - Planned]══════[Sprint 3 - Completed]
  Jan 1 - Jan 15           Jan 16 - Jan 30           Feb 1 - Feb 15
```

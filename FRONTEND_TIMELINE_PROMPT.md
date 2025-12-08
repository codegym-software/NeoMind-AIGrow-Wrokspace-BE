# Frontend Development Prompt: Timeline/Gantt Chart Tab

## 🎯 Objective

Build a **Timeline Tab** component in React/Next.js that displays project tasks and epics in a Gantt Chart format, similar to Jira's Timeline view, with Sprint headers visualization.

---

## 📸 Reference Design

**UI Layout (từ ảnh Jira):**

```
┌─────────────────────────────────────────────────────────────────────┐
│  Timeline Tab                                     [Today] [Filters]  │
├─────────────┬───────────────────────────────────────────────────────┤
│   Sprints   │    Aug    │   Sept   │   Oct    │   Nov    │   Dec    │
│             ├───────────┴──────────┴──────────┴──────────┴──────────┤
│   □  NEOM.. │  [NE...] [NEOM.] [N..] [NEOM.] [HE..] [NEOM..] [NE.]  │ ← Sprint headers
├─────────────┼───────────────────────────────────────────────────────┤
│ Work        │                                                        │
│             │                                                        │
│  □ ◊ Epic-2 │              [████████████████]                       │ ← Epic bar
│     □ Task-7│              [████████]                                │ ← Task bar
│     □ Task-8│              [████████]                                │
│     □ Task-4│              [████████]                                │
│             │                                                        │
│  □ ◊ Epic-16│                      [████████████████]               │
│     □ Task.│                      [████████]                         │
│             │                                                        │
│  □ ◊ Epic-33│                              [████████████]           │
│             │                                                        │
│  □ ◊ Epic-19│                                  [██████]             │
│             │                                                        │
│  [+ Create Epic]                                                    │
└─────────────┴───────────────────────────────────────────────────────┘
```

**Key Visual Elements:**
1. **Sprint Headers Row**: Horizontal blocks ở top, hiển thị tên Sprint
2. **Epic Rows**: Có icon ◊ (diamond), indentation level 0, purple bar
3. **Task Rows**: Có icon □ (checkbox), indented under Epic, purple bar
4. **Timeline Grid**: Monthly/weekly columns với vertical grid lines
5. **Today Marker**: Vertical blue line showing current date
6. **Sprint Info Panel** (right side): Sprint details on hover/click

---

## 🔌 Backend API Integration

### API Endpoint
```
GET /api/projects/{projectId}/timeline
Authorization: Bearer {jwt_token}
```

### Response Structure (v2.0)
```typescript
interface TimelineResponse {
  items: TimelineItem[];
  sprints: Sprint[];
}

interface TimelineItem {
  id: string;              // "epic-1" or "task-10"
  text: string;            // Epic/Task title
  startDate: string;       // "2025-12-01" (ISO date)
  endDate: string;         // "2025-12-28"
  parent: string | null;   // "epic-1" for tasks, null for epics
  progress: number;        // 0.0 - 1.0 (0% - 100%)
  type: "epic" | "task";   
  sprintId?: number;       // Only for tasks
}

interface Sprint {
  id: number;
  name: string;            // "Sprint 1: Authentication Core"
  startDate: string;       // "2025-12-01"
  endDate: string;         // "2025-12-14"
  status: "active" | "planned" | "completed";
}
```

### Example Response
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
      }
    ],
    "sprints": [
      {
        "id": 22,
        "name": "Sprint 1: Authentication Core",
        "startDate": "2025-12-01",
        "endDate": "2025-12-14",
        "status": "active"
      }
    ]
  }
}
```

---

## 🎨 Component Structure

### File Structure
```
src/
├── components/
│   ├── Timeline/
│   │   ├── TimelineTab.tsx              # Main container
│   │   ├── TimelineHeader.tsx           # Top toolbar (filters, today button)
│   │   ├── TimelineGrid.tsx             # Calendar grid (months/weeks)
│   │   ├── SprintHeadersRow.tsx         # Sprint blocks row
│   │   ├── EpicRow.tsx                  # Epic item with bar
│   │   ├── TaskRow.tsx                  # Task item with bar
│   │   ├── GanttBar.tsx                 # Reusable bar component
│   │   ├── TodayMarker.tsx              # Vertical line for today
│   │   └── SprintInfoPanel.tsx          # Sprint details sidebar
│   └── hooks/
│       ├── useTimelineData.ts           # Fetch data from API
│       ├── useGanttScale.ts             # Date-to-pixel calculations
│       └── useTimelineDragDrop.ts       # Drag & drop logic
└── types/
    └── timeline.types.ts                # TypeScript interfaces
```

---

## 🛠️ Technical Requirements

### 1. Timeline Grid Rendering

**Date Scale Calculation:**
```typescript
// Example: Calculate pixel position from date
function getXPosition(date: Date, viewStart: Date, pixelsPerDay: number): number {
  const daysDiff = differenceInDays(date, viewStart);
  return daysDiff * pixelsPerDay;
}

// Example: Calculate bar width
function getBarWidth(startDate: Date, endDate: Date, pixelsPerDay: number): number {
  const duration = differenceInDays(endDate, startDate) + 1;
  return duration * pixelsPerDay;
}
```

**Grid Layout:**
- Container: `display: flex`
- Left panel (items list): Fixed width ~300px
- Right panel (chart area): Scrollable horizontally
- Sticky header for Sprint row

### 2. Sprint Headers Row

**Visual Design:**
```css
.sprint-header {
  background: linear-gradient(90deg, #E3F2FD 0%, #BBDEFB 100%);
  border: 1px solid #2196F3;
  border-radius: 4px;
  height: 40px;
  padding: 8px 12px;
  font-size: 12px;
  font-weight: 600;
  color: #1565C0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Sprint status colors */
.sprint-active { background: #4CAF50; color: white; }
.sprint-planned { background: #2196F3; color: white; }
.sprint-completed { background: #9E9E9E; color: white; }
```

**Rendering Logic:**
```typescript
// Map sprints to horizontal blocks
sprints.map(sprint => (
  <div
    className={`sprint-header sprint-${sprint.status}`}
    style={{
      left: getXPosition(sprint.startDate),
      width: getBarWidth(sprint.startDate, sprint.endDate),
      position: 'absolute',
      top: 0,
    }}
  >
    {sprint.name}
  </div>
))
```

### 3. Epic/Task Hierarchical List

**Hierarchical Rendering:**
```typescript
// Group tasks by epic
const epicTree = items.reduce((acc, item) => {
  if (item.type === 'epic') {
    acc[item.id] = { epic: item, tasks: [] };
  }
  return acc;
}, {});

items.forEach(item => {
  if (item.type === 'task' && item.parent) {
    epicTree[item.parent]?.tasks.push(item);
  }
});

// Render tree
Object.values(epicTree).map(node => (
  <>
    <EpicRow epic={node.epic} />
    {node.tasks.map(task => (
      <TaskRow task={task} indented />
    ))}
  </>
))
```

**Row Structure:**
```tsx
// EpicRow.tsx
<div className="timeline-row epic-row">
  <div className="row-left">
    <input type="checkbox" />
    <span className="epic-icon">◊</span>
    <span className="epic-title">{epic.text}</span>
  </div>
  <div className="row-right">
    <GanttBar
      startDate={epic.startDate}
      endDate={epic.endDate}
      progress={epic.progress}
      color="#9C27B0"
      type="epic"
    />
  </div>
</div>

// TaskRow.tsx
<div className="timeline-row task-row" style={{ paddingLeft: '40px' }}>
  <div className="row-left">
    <input type="checkbox" />
    <span className="task-icon">□</span>
    <span className="task-title">{task.text}</span>
  </div>
  <div className="row-right">
    <GanttBar
      startDate={task.startDate}
      endDate={task.endDate}
      progress={task.progress}
      color="#BA68C8"
      type="task"
      sprintId={task.sprintId}
    />
  </div>
</div>
```

### 4. Gantt Bar Component

**Bar Visual Design:**
```tsx
// GanttBar.tsx
const GanttBar = ({ startDate, endDate, progress, color, type }) => {
  const x = getXPosition(startDate);
  const width = getBarWidth(startDate, endDate);
  
  return (
    <div
      className={`gantt-bar gantt-bar-${type}`}
      style={{
        left: `${x}px`,
        width: `${width}px`,
        background: color,
      }}
    >
      {/* Progress fill */}
      <div
        className="gantt-bar-progress"
        style={{
          width: `${progress * 100}%`,
          background: darken(color, 0.2),
        }}
      />
      
      {/* Resize handles (for drag & drop) */}
      <div className="gantt-bar-handle-left" />
      <div className="gantt-bar-handle-right" />
    </div>
  );
};
```

**CSS:**
```css
.gantt-bar {
  position: absolute;
  height: 32px;
  top: 50%;
  transform: translateY(-50%);
  border-radius: 6px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.2s;
}

.gantt-bar:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  transform: translateY(-50%) scale(1.02);
}

.gantt-bar-progress {
  height: 100%;
  border-radius: 5px;
  transition: width 0.3s;
}

/* Resize handles */
.gantt-bar-handle-left,
.gantt-bar-handle-right {
  position: absolute;
  width: 8px;
  height: 100%;
  cursor: ew-resize;
  background: transparent;
}

.gantt-bar-handle-left { left: 0; }
.gantt-bar-handle-right { right: 0; }
```

### 5. Drag & Drop Functionality

**Implementation using react-dnd or custom hooks:**
```typescript
// useTimelineDragDrop.ts
const useTimelineDragDrop = (item: TimelineItem, onUpdate: Function) => {
  const [isDragging, setIsDragging] = useState(false);
  
  const handleDragStart = (e: React.DragEvent) => {
    setIsDragging(true);
    e.dataTransfer.setData('itemId', item.id);
  };
  
  const handleDrop = async (e: React.DragEvent) => {
    const newX = e.clientX - chartOffsetX;
    const newDate = getDateFromPosition(newX);
    
    // Calculate new dates
    const duration = differenceInDays(item.endDate, item.startDate);
    const newStartDate = newDate;
    const newEndDate = addDays(newDate, duration);
    
    // API call
    await updateTaskTimeline(item.id, {
      startDate: format(newStartDate, 'yyyy-MM-dd'),
      dueDate: format(newEndDate, 'yyyy-MM-dd'),
    });
    
    onUpdate();
  };
  
  return { isDragging, handleDragStart, handleDrop };
};
```

**API Integration:**
```typescript
// Update task timeline
async function updateTaskTimeline(taskId: string, data: {
  startDate: string;
  dueDate: string;
}) {
  const response = await fetch(
    `/api/projects/${projectId}/timeline/tasks/${taskId}`,
    {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify(data),
    }
  );
  
  if (!response.ok) {
    throw new Error('Failed to update timeline');
  }
  
  return response.json();
}
```

### 6. Today Marker

**Implementation:**
```tsx
// TodayMarker.tsx
const TodayMarker = ({ viewStartDate }: { viewStartDate: Date }) => {
  const today = new Date();
  const x = getXPosition(today, viewStartDate, PIXELS_PER_DAY);
  
  return (
    <div
      className="today-marker"
      style={{ left: `${x}px` }}
    >
      <div className="today-marker-line" />
      <div className="today-marker-label">Today</div>
    </div>
  );
};
```

**CSS:**
```css
.today-marker {
  position: absolute;
  top: 0;
  bottom: 0;
  z-index: 10;
  pointer-events: none;
}

.today-marker-line {
  width: 2px;
  height: 100%;
  background: #2196F3;
  box-shadow: 0 0 4px rgba(33, 150, 243, 0.5);
}

.today-marker-label {
  position: absolute;
  top: -24px;
  left: 50%;
  transform: translateX(-50%);
  background: #2196F3;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}
```

### 7. Sprint Info Panel (Right Sidebar)

**Trigger:** Click vào Sprint header hoặc Task có `sprintId`

**Panel Content:**
```tsx
// SprintInfoPanel.tsx
const SprintInfoPanel = ({ sprint, onClose }) => {
  const tasks = items.filter(item => item.sprintId === sprint.id);
  const completedTasks = tasks.filter(t => t.progress === 1.0);
  
  return (
    <div className="sprint-panel">
      <div className="sprint-panel-header">
        <h3>{sprint.name}</h3>
        <button onClick={onClose}>×</button>
      </div>
      
      <div className="sprint-panel-body">
        <div className="sprint-dates">
          <div>Start: {formatDate(sprint.startDate)}</div>
          <div>End: {formatDate(sprint.endDate)}</div>
        </div>
        
        <div className="sprint-status">
          <span className={`status-badge status-${sprint.status}`}>
            {sprint.status.toUpperCase()}
          </span>
        </div>
        
        <div className="sprint-progress">
          <div className="progress-label">
            Progress: {completedTasks.length} / {tasks.length} tasks
          </div>
          <div className="progress-bar">
            <div
              className="progress-bar-fill"
              style={{ width: `${(completedTasks.length / tasks.length) * 100}%` }}
            />
          </div>
        </div>
        
        <div className="sprint-task-list">
          <h4>Tasks in Sprint:</h4>
          {tasks.map(task => (
            <div key={task.id} className="sprint-task-item">
              <input
                type="checkbox"
                checked={task.progress === 1.0}
                readOnly
              />
              <span>{task.text}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
```

**CSS:**
```css
.sprint-panel {
  position: fixed;
  right: 0;
  top: 0;
  width: 400px;
  height: 100vh;
  background: white;
  box-shadow: -4px 0 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from { transform: translateX(100%); }
  to { transform: translateX(0); }
}

.sprint-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #E0E0E0;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-active { background: #4CAF50; color: white; }
.status-planned { background: #2196F3; color: white; }
.status-completed { background: #9E9E9E; color: white; }
```

---

## 🎯 Feature Implementation Checklist

### Phase 1: Basic Rendering ✅
- [ ] Fetch data from API endpoint
- [ ] Parse response into Epic/Task tree structure
- [ ] Render hierarchical list (left panel)
- [ ] Render calendar grid (right panel)
- [ ] Display Sprint headers row
- [ ] Render Epic bars
- [ ] Render Task bars
- [ ] Show "Today" marker

### Phase 2: Interactivity ✅
- [ ] Hover effects on bars (tooltip with dates, progress)
- [ ] Click Epic → expand/collapse tasks
- [ ] Click Sprint header → open Sprint info panel
- [ ] Checkbox selection (multi-select)
- [ ] Zoom controls (day/week/month view)
- [ ] Horizontal scroll with mousewheel
- [ ] "Scroll to Today" button

### Phase 3: Drag & Drop ✅
- [ ] Drag entire bar → move dates
- [ ] Drag left handle → change start date
- [ ] Drag right handle → change end date
- [ ] Validate: `endDate >= startDate`
- [ ] API call: PATCH `/timeline/tasks/{id}`
- [ ] Optimistic UI update
- [ ] Error handling (validation failed)

### Phase 4: Advanced Features 🚀
- [ ] Complete/Uncomplete task (checkbox in list)
- [ ] API call: PATCH `/timeline/tasks/{id}/complete`
- [ ] Visual indicator: completed tasks (strikethrough, grey color)
- [ ] Filter by Epic, Sprint, Status
- [ ] Search tasks by title
- [ ] Export to PNG/PDF
- [ ] Real-time updates (WebSocket for multi-user)

---

## 📦 Recommended Libraries

### Core Dependencies
```json
{
  "react": "^18.2.0",
  "next": "^14.0.0",
  "typescript": "^5.0.0",
  "date-fns": "^3.0.0",           // Date calculations
  "react-dnd": "^16.0.0",         // Drag & drop
  "react-dnd-html5-backend": "^16.0.0",
  "zustand": "^4.4.0",            // State management
  "axios": "^1.6.0",              // API calls
  "react-query": "^5.0.0",        // Data fetching & caching
  "framer-motion": "^10.0.0",     // Animations
  "lucide-react": "^0.300.0",     // Icons
  "tailwindcss": "^3.4.0"         // Styling
}
```

### Optional (For Advanced Features)
```json
{
  "html2canvas": "^1.4.1",        // Export to image
  "jspdf": "^2.5.1",              // Export to PDF
  "react-virtualized": "^9.22.0", // Virtual scrolling (large datasets)
  "socket.io-client": "^4.6.0"    // Real-time collaboration
}
```

---

## 🎨 Design Tokens

### Colors
```css
:root {
  /* Epic colors */
  --epic-color: #9C27B0;
  --epic-color-hover: #7B1FA2;
  --epic-color-progress: #6A1B9A;
  
  /* Task colors */
  --task-color: #BA68C8;
  --task-color-hover: #AB47BC;
  --task-color-progress: #9C27B0;
  
  /* Sprint colors */
  --sprint-active: #4CAF50;
  --sprint-planned: #2196F3;
  --sprint-completed: #9E9E9E;
  
  /* UI colors */
  --today-marker: #2196F3;
  --grid-line: #E0E0E0;
  --background: #FAFAFA;
  --text-primary: #212121;
  --text-secondary: #757575;
}
```

### Spacing
```css
--row-height: 48px;
--sprint-row-height: 60px;
--bar-height: 32px;
--indent-width: 40px;
--left-panel-width: 300px;
--grid-column-width: 100px; /* Per week */
```

---

## 📊 Example API Call Flow

### 1. Initial Load
```typescript
// useTimelineData.ts
const useTimelineData = (projectId: string) => {
  return useQuery({
    queryKey: ['timeline', projectId],
    queryFn: async () => {
      const response = await axios.get(
        `/api/projects/${projectId}/timeline`,
        {
          headers: {
            Authorization: `Bearer ${getToken()}`,
          },
        }
      );
      return response.data.data; // { items, sprints }
    },
    refetchInterval: 30000, // Auto-refresh every 30s
  });
};
```

### 2. Drag & Drop Update
```typescript
const mutation = useMutation({
  mutationFn: async ({ taskId, startDate, dueDate }) => {
    const response = await axios.patch(
      `/api/projects/${projectId}/timeline/tasks/${taskId}`,
      { startDate, dueDate },
      {
        headers: {
          Authorization: `Bearer ${getToken()}`,
          'Content-Type': 'application/json',
        },
      }
    );
    return response.data;
  },
  onSuccess: () => {
    queryClient.invalidateQueries(['timeline', projectId]);
    toast.success('Timeline updated successfully');
  },
  onError: (error) => {
    toast.error('Failed to update timeline');
    console.error(error);
  },
});
```

### 3. Complete Task
```typescript
const completeTaskMutation = useMutation({
  mutationFn: async ({ taskId, completed }) => {
    const response = await axios.patch(
      `/api/projects/${projectId}/timeline/tasks/${taskId}/complete`,
      { completed },
      {
        headers: {
          Authorization: `Bearer ${getToken()}`,
          'Content-Type': 'application/json',
        },
      }
    );
    return response.data;
  },
  onSuccess: () => {
    queryClient.invalidateQueries(['timeline', projectId]);
  },
});
```

---

## 🧪 Testing Scenarios

### Unit Tests
```typescript
describe('TimelineTab', () => {
  it('should render Sprint headers correctly', () => {
    const sprints = [
      { id: 1, name: 'Sprint 1', startDate: '2025-12-01', endDate: '2025-12-14', status: 'active' }
    ];
    render(<SprintHeadersRow sprints={sprints} />);
    expect(screen.getByText('Sprint 1')).toBeInTheDocument();
  });
  
  it('should calculate bar width correctly', () => {
    const start = new Date('2025-12-01');
    const end = new Date('2025-12-15');
    const width = getBarWidth(start, end, 10); // 10px per day
    expect(width).toBe(150); // 15 days * 10px
  });
  
  it('should render Epic with child Tasks', () => {
    const items = [
      { id: 'epic-1', text: 'Epic 1', type: 'epic', parent: null },
      { id: 'task-1', text: 'Task 1', type: 'task', parent: 'epic-1' }
    ];
    render(<TimelineGrid items={items} />);
    expect(screen.getByText('Epic 1')).toBeInTheDocument();
    expect(screen.getByText('Task 1')).toBeInTheDocument();
  });
});
```

### E2E Tests (Playwright/Cypress)
```typescript
describe('Timeline Drag & Drop', () => {
  it('should update task dates when dragged', () => {
    cy.visit('/projects/123/timeline');
    
    // Wait for data load
    cy.get('.gantt-bar-task').first().as('taskBar');
    
    // Drag bar
    cy.get('@taskBar').trigger('mousedown', { which: 1 });
    cy.get('.timeline-chart').trigger('mousemove', { clientX: 500 });
    cy.get('@taskBar').trigger('mouseup');
    
    // Verify API call
    cy.wait('@updateTimeline').its('request.body').should('deep.equal', {
      startDate: '2025-12-05',
      dueDate: '2025-12-20'
    });
  });
});
```

---

## 🚀 Performance Optimization

### 1. Virtual Scrolling
```typescript
// For large datasets (1000+ items)
import { FixedSizeList } from 'react-window';

<FixedSizeList
  height={600}
  itemCount={items.length}
  itemSize={48}
  width="100%"
>
  {({ index, style }) => (
    <div style={style}>
      {renderRow(items[index])}
    </div>
  )}
</FixedSizeList>
```

### 2. Memoization
```typescript
const MemoizedGanttBar = React.memo(GanttBar, (prev, next) => {
  return (
    prev.startDate === next.startDate &&
    prev.endDate === next.endDate &&
    prev.progress === next.progress
  );
});
```

### 3. Debounced API Calls
```typescript
const debouncedUpdate = useDebouncedCallback(
  async (taskId, dates) => {
    await updateTaskTimeline(taskId, dates);
  },
  500 // 500ms delay
);
```

---

## 📝 Additional Notes

### Accessibility
- [ ] Keyboard navigation (arrow keys to move between rows)
- [ ] Screen reader support (ARIA labels)
- [ ] Focus indicators
- [ ] High contrast mode support

### Responsive Design
- [ ] Mobile: Collapse to list view (no Gantt chart)
- [ ] Tablet: Simplified Gantt with horizontal scroll
- [ ] Desktop: Full Gantt chart with all features

### Browser Compatibility
- [ ] Chrome/Edge (Chromium) ✅
- [ ] Firefox ✅
- [ ] Safari ⚠️ (test drag & drop carefully)

---

## 🎓 Learning Resources

### Gantt Chart Libraries (Optional)
- **DHTMLX Gantt** (Commercial): https://dhtmlx.com/docs/products/dhtmlxGantt/
- **Bryntum Gantt** (Commercial): https://www.bryntum.com/products/gantt/
- **Frappe Gantt** (Free/Open Source): https://github.com/frappe/gantt

**Note:** Có thể tự build Gantt chart từ đầu với React để có full control, hoặc dùng library để nhanh hơn.

### Date Calculations
- `date-fns` documentation: https://date-fns.org/docs/Getting-Started

### Drag & Drop
- React DnD: https://react-dnd.github.io/react-dnd/

---

## ✅ Acceptance Criteria

### Definition of Done
- [ ] Sprint headers visible at top of chart
- [ ] Epic/Task hierarchy rendered correctly
- [ ] Bars display with correct position and width
- [ ] "Today" marker visible
- [ ] Drag & drop updates dates via API
- [ ] Sprint info panel opens on click
- [ ] Complete/Uncomplete task works
- [ ] Responsive on desktop (1920x1080)
- [ ] No console errors
- [ ] Loading states handled
- [ ] Error states handled (API failure)

---

## 🎯 Final Deliverable

**Main Component:**
```tsx
// pages/projects/[projectId]/timeline.tsx
export default function TimelinePage() {
  const { projectId } = useRouter().query;
  const { data, isLoading, error } = useTimelineData(projectId as string);
  
  if (isLoading) return <TimelineLoader />;
  if (error) return <TimelineError />;
  
  return (
    <div className="timeline-container">
      <TimelineHeader projectId={projectId} />
      <div className="timeline-content">
        <SprintHeadersRow sprints={data.sprints} />
        <TimelineGrid
          items={data.items}
          sprints={data.sprints}
        />
        <TodayMarker />
      </div>
    </div>
  );
}
```

---

**Good luck building the Timeline feature! 🚀**

If you have questions, refer to:
- Backend API docs: `TIMELINE_API_DOCUMENTATION.md`
- SQL queries: `TIMELINE_SPRINT_QUERIES.sql`
- Postman collection: `Timeline_APIs.postman_collection.json`

# Frontend Prompt: Kéo Thả Epic Gantt Bar để Cập Nhật Thời Gian

## 🎯 Mục tiêu
Implement chức năng **kéo thả Epic Gantt Bar** trên Timeline/Gantt Chart để thay đổi `startDate` và `endDate` của Epic, với các tính năng:
- ✅ Kéo toàn bộ thanh Epic để di chuyển (move)
- ✅ Kéo đầu trái để thay đổi startDate (resize left)
- ✅ Kéo đầu phải để thay đổi endDate (resize right)
- ❌ **KHÔNG cho phép** kéo thả Task (read-only cho Task)

## 🚫 Business Rules

### **Quy tắc quan trọng:**
```typescript
// ✅ EPIC: Có thể kéo thả
const canDragEpic = true;
const canResizeEpic = true;

// ❌ TASK: KHÔNG được kéo thả
const canDragTask = false;
const canResizeTask = false;
```

### **Lý do:**
- Epic là high-level planning → User có quyền điều chỉnh
- Task dates được quản lý tự động (auto-set từ Sprint khi tạo)
- Task dates nên được update qua Sprint/Board, không phải Timeline

## 📊 API Endpoint (Backend đã sẵn sàng)

### **PATCH `/api/projects/{projectId}/timeline/epics/{epicId}`**

#### Request Body:
```typescript
interface TimelineUpdateRequest {
  startDate?: string;  // ISO date: "2025-01-01" (optional)
  dueDate?: string;    // ISO date: "2025-01-31" (optional)
}
```

#### Response:
```typescript
interface ApiResponse<T> {
  message: string;
  data: T;
}

// Response type
{
  "message": "Epic timeline updated successfully",
  "data": {
    "id": "epic-1",
    "text": "Epic 1",
    "type": "epic",
    "startDate": "2025-01-01",
    "endDate": "2025-01-31",
    "progress": 0.5,
    "projectId": "proj-123"
    // ... other fields
  }
}
```

#### Use Cases:
| Hành động | Request Body | Mô tả |
|-----------|-------------|--------|
| Kéo toàn bộ thanh sang phải | `{ "startDate": "2025-01-05", "dueDate": "2025-02-04" }` | Di chuyển cả Epic (giữ nguyên duration) |
| Resize đầu trái (startDate) | `{ "startDate": "2025-01-03" }` | Chỉ thay đổi ngày bắt đầu |
| Resize đầu phải (endDate) | `{ "dueDate": "2025-02-15" }` | Chỉ thay đổi ngày kết thúc |

## 🎨 UI/UX Design

### Visual States

```
┌─────────────────────────────────────────────────────────────┐
│ EPIC ROW (Draggable ✅)                                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ Epic 1  [◄═══════════════════════════════════════════►]    │
│         ↑                                              ↑    │
│      Resize Left                                Resize Right│
│         (startDate)                               (endDate) │
│                                                             │
│ ────────────────────────────────────────────────────────── │
│                                                             │
│   Task 1   [════════════════]  ← READ-ONLY ❌              │
│   Task 2        [═══════════]  ← READ-ONLY ❌              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Cursor States
```css
/* Epic Bar - Different cursor for different zones */
.gantt-bar--epic {
  cursor: move;  /* Default: Move entire bar */
}

.gantt-bar--epic .resize-handle-left {
  cursor: ew-resize;  /* Resize left edge */
}

.gantt-bar--epic .resize-handle-right {
  cursor: ew-resize;  /* Resize right edge */
}

/* Task Bar - Disabled */
.gantt-bar--task {
  cursor: not-allowed;  /* Cannot drag */
  opacity: 0.8;
}
```

### Visual Feedback
```typescript
// Epic: Show drag feedback
<div className="gantt-bar gantt-bar--epic gantt-bar--dragging">
  {/* Shadow/ghost effect during drag */}
</div>

// Task: Show disabled state
<div className="gantt-bar gantt-bar--task gantt-bar--readonly">
  <Tooltip content="Task dates cannot be changed from Timeline">
    {/* Task content */}
  </Tooltip>
</div>
```

## 💻 Implementation

### Step 1: TypeScript Types

```typescript
// types/timeline.ts

export interface TimelineItem {
  id: string;  // "epic-1" | "task-10"
  type: 'epic' | 'task';
  text: string;
  startDate: string | null;
  endDate: string | null;
  progress: number;
  projectId: string;
  sprintId?: number | null;
  parent?: string | null;
  status?: string;
  completedDate?: string | null;
}

export interface DragState {
  isDragging: boolean;
  itemId: string | null;
  dragType: 'move' | 'resize-left' | 'resize-right' | null;
  startX: number;
  originalStartDate: Date | null;
  originalEndDate: Date | null;
}

export interface TimelineUpdatePayload {
  startDate?: string;
  dueDate?: string;
}
```

### Step 2: Drag & Drop Hook

```typescript
// hooks/useEpicDragDrop.ts

import { useState, useCallback } from 'react';
import { differenceInDays, addDays, format } from 'date-fns';
import { TimelineItem, DragState } from '@/types/timeline';

interface UseEpicDragDropProps {
  item: TimelineItem;
  pixelsPerDay: number;
  timelineStart: Date;
  onUpdate: (itemId: string, payload: TimelineUpdatePayload) => Promise<void>;
}

export const useEpicDragDrop = ({
  item,
  pixelsPerDay,
  timelineStart,
  onUpdate
}: UseEpicDragDropProps) => {
  const [dragState, setDragState] = useState<DragState>({
    isDragging: false,
    itemId: null,
    dragType: null,
    startX: 0,
    originalStartDate: null,
    originalEndDate: null
  });

  // Check if item is draggable
  const isDraggable = item.type === 'epic';

  // Start drag/resize
  const handleDragStart = useCallback((
    e: React.MouseEvent,
    dragType: 'move' | 'resize-left' | 'resize-right'
  ) => {
    if (!isDraggable) return;

    e.stopPropagation();
    
    setDragState({
      isDragging: true,
      itemId: item.id,
      dragType,
      startX: e.clientX,
      originalStartDate: item.startDate ? new Date(item.startDate) : null,
      originalEndDate: item.endDate ? new Date(item.endDate) : null
    });
  }, [isDraggable, item]);

  // Handle drag/resize
  const handleDragMove = useCallback((e: MouseEvent) => {
    if (!dragState.isDragging || !dragState.originalStartDate || !dragState.originalEndDate) {
      return;
    }

    const deltaX = e.clientX - dragState.startX;
    const deltaDays = Math.round(deltaX / pixelsPerDay);

    let newStartDate = dragState.originalStartDate;
    let newEndDate = dragState.originalEndDate;

    switch (dragState.dragType) {
      case 'move':
        // Move entire bar (keep duration)
        newStartDate = addDays(dragState.originalStartDate, deltaDays);
        newEndDate = addDays(dragState.originalEndDate, deltaDays);
        break;

      case 'resize-left':
        // Resize left edge (change startDate only)
        newStartDate = addDays(dragState.originalStartDate, deltaDays);
        
        // Validate: startDate must be before endDate
        if (newStartDate >= dragState.originalEndDate) {
          newStartDate = addDays(dragState.originalEndDate, -1);
        }
        break;

      case 'resize-right':
        // Resize right edge (change endDate only)
        newEndDate = addDays(dragState.originalEndDate, deltaDays);
        
        // Validate: endDate must be after startDate
        if (newEndDate <= dragState.originalStartDate) {
          newEndDate = addDays(dragState.originalStartDate, 1);
        }
        break;
    }

    // Update preview (temporary visual feedback)
    return {
      startDate: newStartDate,
      endDate: newEndDate
    };
  }, [dragState, pixelsPerDay]);

  // End drag/resize
  const handleDragEnd = useCallback(async (e: MouseEvent) => {
    if (!dragState.isDragging || !dragState.originalStartDate || !dragState.originalEndDate) {
      return;
    }

    const deltaX = e.clientX - dragState.startX;
    const deltaDays = Math.round(deltaX / pixelsPerDay);

    // Calculate final dates
    let newStartDate = dragState.originalStartDate;
    let newEndDate = dragState.originalEndDate;

    switch (dragState.dragType) {
      case 'move':
        newStartDate = addDays(dragState.originalStartDate, deltaDays);
        newEndDate = addDays(dragState.originalEndDate, deltaDays);
        break;

      case 'resize-left':
        newStartDate = addDays(dragState.originalStartDate, deltaDays);
        if (newStartDate >= dragState.originalEndDate) {
          newStartDate = addDays(dragState.originalEndDate, -1);
        }
        break;

      case 'resize-right':
        newEndDate = addDays(dragState.originalEndDate, deltaDays);
        if (newEndDate <= dragState.originalStartDate) {
          newEndDate = addDays(dragState.originalStartDate, 1);
        }
        break;
    }

    // Prepare payload
    const payload: TimelineUpdatePayload = {};

    if (dragState.dragType === 'move' || dragState.dragType === 'resize-left') {
      payload.startDate = format(newStartDate, 'yyyy-MM-dd');
    }

    if (dragState.dragType === 'move' || dragState.dragType === 'resize-right') {
      payload.dueDate = format(newEndDate, 'yyyy-MM-dd');
    }

    // Reset drag state
    setDragState({
      isDragging: false,
      itemId: null,
      dragType: null,
      startX: 0,
      originalStartDate: null,
      originalEndDate: null
    });

    // Call API
    try {
      await onUpdate(item.id, payload);
    } catch (error) {
      console.error('Failed to update epic timeline:', error);
      // TODO: Show error toast
    }
  }, [dragState, pixelsPerDay, item.id, onUpdate]);

  return {
    isDraggable,
    dragState,
    handleDragStart,
    handleDragMove,
    handleDragEnd
  };
};
```

### Step 3: GanttBar Component

```typescript
// components/Timeline/GanttBar.tsx

import React, { useEffect, useRef } from 'react';
import { TimelineItem } from '@/types/timeline';
import { useEpicDragDrop } from '@/hooks/useEpicDragDrop';
import { differenceInDays, parseISO } from 'date-fns';
import { Tooltip } from '@/components/ui/Tooltip';

interface GanttBarProps {
  item: TimelineItem;
  timelineStart: Date;
  pixelsPerDay: number;
  onUpdate: (itemId: string, payload: TimelineUpdatePayload) => Promise<void>;
}

export const GanttBar: React.FC<GanttBarProps> = ({
  item,
  timelineStart,
  pixelsPerDay,
  onUpdate
}) => {
  const {
    isDraggable,
    dragState,
    handleDragStart,
    handleDragMove,
    handleDragEnd
  } = useEpicDragDrop({ item, pixelsPerDay, timelineStart, onUpdate });

  // Register global mouse events
  useEffect(() => {
    if (dragState.isDragging) {
      window.addEventListener('mousemove', handleDragMove);
      window.addEventListener('mouseup', handleDragEnd);

      return () => {
        window.removeEventListener('mousemove', handleDragMove);
        window.removeEventListener('mouseup', handleDragEnd);
      };
    }
  }, [dragState.isDragging, handleDragMove, handleDragEnd]);

  // Calculate position
  const startDate = item.startDate ? parseISO(item.startDate) : null;
  const endDate = item.endDate ? parseISO(item.endDate) : null;

  if (!startDate || !endDate) return null;

  const offsetDays = differenceInDays(startDate, timelineStart);
  const durationDays = differenceInDays(endDate, startDate);

  const left = offsetDays * pixelsPerDay;
  const width = durationDays * pixelsPerDay;

  // Render
  const barContent = (
    <div
      className={`
        gantt-bar 
        gantt-bar--${item.type}
        ${isDraggable ? 'gantt-bar--draggable' : 'gantt-bar--readonly'}
        ${dragState.isDragging ? 'gantt-bar--dragging' : ''}
      `}
      style={{
        position: 'absolute',
        left: `${left}px`,
        width: `${width}px`,
        height: '32px',
        borderRadius: '4px',
        display: 'flex',
        alignItems: 'center',
        padding: '0 8px',
        userSelect: 'none'
      }}
      onMouseDown={(e) => isDraggable && handleDragStart(e, 'move')}
    >
      {/* Left Resize Handle (Epic only) */}
      {isDraggable && (
        <div
          className="resize-handle resize-handle-left"
          style={{
            position: 'absolute',
            left: 0,
            top: 0,
            bottom: 0,
            width: '8px',
            cursor: 'ew-resize',
            zIndex: 10
          }}
          onMouseDown={(e) => {
            e.stopPropagation();
            handleDragStart(e, 'resize-left');
          }}
        >
          <div style={{
            width: '2px',
            height: '100%',
            background: 'currentColor',
            margin: '0 auto',
            opacity: 0.5
          }} />
        </div>
      )}

      {/* Bar Content */}
      <div className="gantt-bar-content" style={{ flex: 1, overflow: 'hidden' }}>
        <span style={{ fontSize: '12px', fontWeight: 500 }}>
          {item.text}
        </span>
      </div>

      {/* Right Resize Handle (Epic only) */}
      {isDraggable && (
        <div
          className="resize-handle resize-handle-right"
          style={{
            position: 'absolute',
            right: 0,
            top: 0,
            bottom: 0,
            width: '8px',
            cursor: 'ew-resize',
            zIndex: 10
          }}
          onMouseDown={(e) => {
            e.stopPropagation();
            handleDragStart(e, 'resize-right');
          }}
        >
          <div style={{
            width: '2px',
            height: '100%',
            background: 'currentColor',
            margin: '0 auto',
            opacity: 0.5
          }} />
        </div>
      )}

      {/* Progress Bar */}
      <div
        className="gantt-bar-progress"
        style={{
          position: 'absolute',
          left: 0,
          bottom: 0,
          height: '3px',
          width: `${item.progress * 100}%`,
          background: 'currentColor',
          opacity: 0.6,
          transition: 'width 0.3s ease'
        }}
      />
    </div>
  );

  // Wrap Task in Tooltip (readonly message)
  if (!isDraggable) {
    return (
      <Tooltip content="Task dates cannot be changed from Timeline">
        {barContent}
      </Tooltip>
    );
  }

  return barContent;
};
```

### Step 4: CSS Styling

```css
/* styles/gantt-bar.css */

/* Epic Bar - Draggable */
.gantt-bar--epic {
  background: linear-gradient(135deg, #9C27B0 0%, #BA68C8 100%);
  color: white;
  border: 2px solid #7B1FA2;
  cursor: move;
  transition: all 0.2s ease;
}

.gantt-bar--epic:hover {
  box-shadow: 0 4px 12px rgba(156, 39, 176, 0.3);
  transform: translateY(-1px);
}

.gantt-bar--epic.gantt-bar--dragging {
  opacity: 0.7;
  box-shadow: 0 8px 24px rgba(156, 39, 176, 0.4);
  z-index: 1000;
}

/* Resize Handles - Only visible on hover */
.gantt-bar--epic .resize-handle {
  opacity: 0;
  transition: opacity 0.2s ease;
}

.gantt-bar--epic:hover .resize-handle {
  opacity: 1;
}

.gantt-bar--epic .resize-handle:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* Task Bar - Read-only */
.gantt-bar--task {
  background: linear-gradient(135deg, #2196F3 0%, #64B5F6 100%);
  color: white;
  border: 2px solid #1976D2;
  cursor: not-allowed;
  opacity: 0.85;
}

.gantt-bar--task:hover {
  opacity: 1;
}

/* Progress Bar */
.gantt-bar-progress {
  border-radius: 0 0 2px 2px;
}

/* Dragging Cursor Override */
.gantt-bar--dragging,
.gantt-bar--dragging * {
  cursor: grabbing !important;
}

/* Disabled State */
.gantt-bar--readonly {
  pointer-events: auto; /* Allow hover for tooltip */
}

/* Visual feedback during drag */
@keyframes pulse-drag {
  0%, 100% {
    box-shadow: 0 8px 24px rgba(156, 39, 176, 0.4);
  }
  50% {
    box-shadow: 0 12px 32px rgba(156, 39, 176, 0.6);
  }
}

.gantt-bar--dragging {
  animation: pulse-drag 1s ease-in-out infinite;
}
```

### Step 5: API Service

```typescript
// services/timelineService.ts

import { api } from '@/lib/api';
import { TimelineUpdatePayload, TimelineItem } from '@/types/timeline';

export const updateEpicTimeline = async (
  projectId: string,
  epicId: string,
  payload: TimelineUpdatePayload
): Promise<TimelineItem> => {
  // Extract numeric ID from "epic-123" format
  const numericId = parseInt(epicId.replace('epic-', ''), 10);
  
  const response = await api.patch<ApiResponse<TimelineItem>>(
    `/api/projects/${projectId}/timeline/epics/${numericId}`,
    payload
  );
  
  return response.data.data;
};

export const updateTaskTimeline = async (
  projectId: string,
  taskId: string,
  payload: TimelineUpdatePayload
): Promise<TimelineItem> => {
  // This should NOT be called if Task drag is disabled
  throw new Error('Task timeline updates are not allowed from Timeline view');
};
```

### Step 6: Integrate in TimelineView

```typescript
// components/Timeline/TimelineView.tsx

import React, { useState } from 'react';
import { TimelineResponse, TimelineItem, TimelineUpdatePayload } from '@/types/timeline';
import { getTimeline, updateEpicTimeline } from '@/services/timelineService';
import { GanttBar } from './GanttBar';
import { SprintHeaders } from './SprintHeaders';
import { toast } from '@/components/ui/toast';

export const TimelineView: React.FC<{ projectId: string }> = ({ projectId }) => {
  const [data, setData] = useState<TimelineResponse | null>(null);
  const [loading, setLoading] = useState(false);

  const pixelsPerDay = 40;
  const timelineStart = calculateTimelineStart(data);
  const timelineEnd = calculateTimelineEnd(data);

  // Handle Epic update
  const handleEpicUpdate = async (itemId: string, payload: TimelineUpdatePayload) => {
    if (!data) return;

    try {
      setLoading(true);
      
      // Call API
      const updatedItem = await updateEpicTimeline(projectId, itemId, payload);
      
      // Update local state
      setData(prevData => {
        if (!prevData) return prevData;
        
        return {
          ...prevData,
          items: prevData.items.map(item =>
            item.id === itemId ? updatedItem : item
          )
        };
      });
      
      toast.success('Epic timeline updated successfully');
    } catch (error) {
      console.error('Failed to update epic timeline:', error);
      toast.error('Failed to update epic timeline');
      
      // Reload to revert changes
      await loadTimeline();
    } finally {
      setLoading(false);
    }
  };

  const loadTimeline = async () => {
    try {
      const response = await getTimeline(projectId);
      setData(response);
    } catch (error) {
      console.error('Failed to load timeline:', error);
    }
  };

  return (
    <div className="timeline-container">
      {/* Sprint Headers */}
      <SprintHeaders
        sprints={data?.sprints || []}
        startDate={timelineStart}
        endDate={timelineEnd}
        pixelsPerDay={pixelsPerDay}
      />

      {/* Gantt Bars */}
      <div className="gantt-rows">
        {data?.items.map(item => (
          <div key={item.id} className="gantt-row">
            {/* Row Label */}
            <div className="gantt-row-label">
              {item.text}
              {item.type === 'task' && (
                <span className="readonly-badge">Read-only</span>
              )}
            </div>

            {/* Gantt Bar */}
            <div className="gantt-row-timeline">
              <GanttBar
                item={item}
                timelineStart={timelineStart}
                pixelsPerDay={pixelsPerDay}
                onUpdate={handleEpicUpdate}
              />
            </div>
          </div>
        ))}
      </div>

      {/* Loading Overlay */}
      {loading && (
        <div className="loading-overlay">
          <Spinner />
        </div>
      )}
    </div>
  );
};
```

## 🧪 Test Scenarios

### Test Cases
```typescript
// Test 1: Epic - Move entire bar
describe('Epic Drag & Drop', () => {
  it('should move epic bar and update both dates', async () => {
    const epic = { id: 'epic-1', startDate: '2025-01-01', endDate: '2025-01-15' };
    
    // Drag right by 5 days
    await dragBar(epic, { deltaX: 200 }); // 200px / 40px = 5 days
    
    // Expected API call
    expect(updateEpicTimeline).toHaveBeenCalledWith('proj-1', 'epic-1', {
      startDate: '2025-01-06',
      dueDate: '2025-01-20'
    });
  });

  it('should resize epic start date (left edge)', async () => {
    const epic = { id: 'epic-1', startDate: '2025-01-01', endDate: '2025-01-15' };
    
    // Drag left edge right by 3 days
    await dragHandle(epic, 'left', { deltaX: 120 });
    
    // Expected API call
    expect(updateEpicTimeline).toHaveBeenCalledWith('proj-1', 'epic-1', {
      startDate: '2025-01-04'
      // dueDate not included (only startDate changed)
    });
  });

  it('should resize epic end date (right edge)', async () => {
    const epic = { id: 'epic-1', startDate: '2025-01-01', endDate: '2025-01-15' };
    
    // Drag right edge left by 2 days
    await dragHandle(epic, 'right', { deltaX: -80 });
    
    // Expected API call
    expect(updateEpicTimeline).toHaveBeenCalledWith('proj-1', 'epic-1', {
      dueDate: '2025-01-13'
      // startDate not included (only endDate changed)
    });
  });
});

// Test 2: Task - Should be disabled
describe('Task Drag & Drop', () => {
  it('should NOT allow dragging task', async () => {
    const task = { id: 'task-10', type: 'task', startDate: '2025-01-05' };
    
    const canDrag = isDraggable(task);
    
    expect(canDrag).toBe(false);
  });

  it('should show tooltip on task hover', async () => {
    const task = { id: 'task-10', type: 'task' };
    
    await hoverBar(task);
    
    expect(screen.getByText('Task dates cannot be changed from Timeline')).toBeVisible();
  });
});

// Test 3: Validation
describe('Date Validation', () => {
  it('should prevent startDate from being after endDate', async () => {
    const epic = { id: 'epic-1', startDate: '2025-01-01', endDate: '2025-01-05' };
    
    // Try to drag left edge to after endDate
    await dragHandle(epic, 'left', { deltaX: 200 }); // Would make startDate > endDate
    
    // Should auto-correct to 1 day before endDate
    expect(updateEpicTimeline).toHaveBeenCalledWith('proj-1', 'epic-1', {
      startDate: '2025-01-04' // 1 day before endDate
    });
  });
});
```

## 📝 Checklist

- [ ] **Step 1:** Create `useEpicDragDrop` hook với logic drag/resize
- [ ] **Step 2:** Create `GanttBar` component với resize handles
- [ ] **Step 3:** Add CSS styling cho Epic (draggable) vs Task (readonly)
- [ ] **Step 4:** Integrate `updateEpicTimeline` API call
- [ ] **Step 5:** Add Tooltip cho Task bars (explain read-only)
- [ ] **Step 6:** Test Epic move (toàn bộ thanh)
- [ ] **Step 7:** Test Epic resize left (startDate)
- [ ] **Step 8:** Test Epic resize right (endDate)
- [ ] **Step 9:** Verify Task KHÔNG thể kéo thả
- [ ] **Step 10:** Add loading state và error handling
- [ ] **Step 11:** Add visual feedback (cursor, shadow, animation)
- [ ] **Step 12:** Test validation (startDate < endDate)

## 🎁 Bonus Features (Optional)

### 1. Snap to Grid
```typescript
// Snap to nearest day when dragging
const snappedDeltaDays = Math.round(deltaX / pixelsPerDay);
```

### 2. Undo/Redo
```typescript
const [history, setHistory] = useState<TimelineItem[]>([]);

const handleUndo = () => {
  if (history.length > 0) {
    const previousState = history[history.length - 1];
    // Restore previous state
  }
};
```

### 3. Keyboard Shortcuts
```typescript
useEffect(() => {
  const handleKeyPress = (e: KeyboardEvent) => {
    if (e.key === 'Escape' && dragState.isDragging) {
      // Cancel drag
      setDragState({ ...initialDragState });
    }
  };

  window.addEventListener('keydown', handleKeyPress);
  return () => window.removeEventListener('keydown', handleKeyPress);
}, [dragState]);
```

### 4. Multi-select & Bulk Update (Future)
```typescript
// Currently disabled, but can be enabled later
const [selectedEpics, setSelectedEpics] = useState<string[]>([]);

const handleBulkMove = async (deltaX: number) => {
  await Promise.all(
    selectedEpics.map(epicId => updateEpicTimeline(projectId, epicId, payload))
  );
};
```

## 🚀 Summary

**Backend:**
- ✅ API `PATCH /timeline/epics/{epicId}` đã sẵn sàng
- ✅ Support update `startDate`, `endDate`, hoặc cả 2
- ✅ Validation: `dueDate >= startDate`

**Frontend cần implement:**
1. ✅ Drag & Drop cho **Epic only** (3 modes: move, resize-left, resize-right)
2. ❌ **Disable** drag & drop cho Task (read-only)
3. ✅ Visual feedback (cursor, shadow, animation)
4. ✅ API integration với error handling
5. ✅ Tooltip cho Task (explain why read-only)

**User Experience:**
```
┌─────────────────────────────────────────────────────┐
│ EPIC: Hover → Cursor: move                         │
│       Hover left edge → Cursor: ew-resize           │
│       Hover right edge → Cursor: ew-resize          │
│       Drag → Shadow effect + API update             │
│                                                     │
│ TASK: Hover → Cursor: not-allowed                  │
│       Click → Tooltip: "Cannot change from Timeline"│
│       Drag → Nothing happens                        │
└─────────────────────────────────────────────────────┘
```

**Expected Result:**
- User có thể kéo thả Epic để thay đổi timeline ✅
- User KHÔNG thể kéo thả Task (bảo vệ dữ liệu) ❌
- Mọi thay đổi được lưu vào database ngay lập tức ✅

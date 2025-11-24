package com.springboot.demo.service;

import com.springboot.demo.dto.request.CreateTaskReq;
import com.springboot.demo.dto.request.MoveTaskReq;
import com.springboot.demo.dto.request.UpdateTaskReq;
import com.springboot.demo.dto.response.TaskResponse;
import com.springboot.demo.model.*;
import com.springboot.demo.repository.*;
import com.springboot.demo.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;
    @Mock private BoardRepository boardRepository;
    @Mock private BoardColumnRepository boardColumnRepository;
    @Mock private SprintRepository sprintRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private TaskServiceImpl taskService;

    private final Long projectId = 10L;
    private final Long userId = 100L;

    private Board defaultBoard;
    private BoardColumn toDoColumn;
    private BoardColumn doingColumn;
    private Sprint activeSprint;

    @BeforeEach
    void setup() {
        defaultBoard = new Board();
        defaultBoard.setId(1L);
        defaultBoard.setProjectId(projectId);
        defaultBoard.setIsDefault(true);

        toDoColumn = new BoardColumn();
        toDoColumn.setId(11L);
        toDoColumn.setBoard(defaultBoard);
        toDoColumn.setName("To Do");
        toDoColumn.setPosition(0);

        doingColumn = new BoardColumn();
        doingColumn.setId(12L);
        doingColumn.setBoard(defaultBoard);
        doingColumn.setName("In Progress");
        doingColumn.setPosition(1);

        activeSprint = new Sprint();
        activeSprint.setId(1000L);
        activeSprint.setProjectId(projectId);
        activeSprint.setStatus(SprintStatus.ACTIVE);

        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)).thenReturn(true);
        when(boardRepository.findByProjectIdAndIsDefaultTrue(projectId)).thenReturn(Optional.of(defaultBoard));
        when(boardColumnRepository.findByBoardIdOrderByPositionAsc(defaultBoard.getId()))
                .thenReturn(Arrays.asList(toDoColumn, doingColumn));
        when(sprintRepository.findByProjectIdAndStatus(projectId, SprintStatus.ACTIVE))
                .thenReturn(Optional.of(activeSprint));
    }

    @Test
    void create_shouldAssignDefaultsAndAttachToToDoColumnAndActiveSprint() {
        CreateTaskReq req = new CreateTaskReq();
        req.setProjectId(projectId);
        req.setTitle("New task");
        req.setAssigneeId(userId);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId(999L);
            return t;
        });

        TaskResponse res = taskService.create(req, userId);

        verify(taskRepository).save(taskCaptor.capture());
        Task saved = taskCaptor.getValue();
        assertThat(saved.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(saved.getOrderIndex()).isEqualTo(0);
        assertThat(saved.getStatusColumn()).isNotNull();
        assertThat(saved.getStatusColumn().getId()).isEqualTo(toDoColumn.getId());
        assertThat(saved.getSprintId()).isEqualTo(activeSprint.getId());
        assertThat(res.getId()).isEqualTo(999L);
        assertThat(res.getStatusColumnId()).isEqualTo(toDoColumn.getId());
    }

    @Test
    void update_boardDragChangesColumnButNotSprintId() {
        Task existing = new Task();
        existing.setId(200L);
        existing.setProjectId(projectId);
        existing.setSprintId(activeSprint.getId());
        existing.setStatusColumn(toDoColumn);
        existing.setOrderIndex(0);

        when(taskRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateTaskReq req = new UpdateTaskReq();
        req.setStatusColumnId(doingColumn.getId());
        req.setOrderIndex(1);

        when(boardColumnRepository.findById(doingColumn.getId())).thenReturn(Optional.of(doingColumn));

        TaskResponse res = taskService.update(projectId, existing.getId(), req, userId);

        assertThat(res.getStatusColumnId()).isEqualTo(doingColumn.getId());
        // SprintId must remain unchanged for board drag
        assertThat(res.getSprintId()).isEqualTo(activeSprint.getId());
    }

    @Test
    void update_sprintDragUpdatesSprintIdToNullWhenDroppedToBacklog() {
        Task existing = new Task();
        existing.setId(201L);
        existing.setProjectId(projectId);
        existing.setSprintId(activeSprint.getId());
        existing.setStatusColumn(toDoColumn);
        existing.setOrderIndex(0);

        when(taskRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateTaskReq req = new UpdateTaskReq();
        // Simulate sprint drag: orderIndex provided but statusColumnId not changed
        req.setOrderIndex(0);
        req.setSprintId(null);

        TaskResponse res = taskService.update(projectId, existing.getId(), req, userId);
        assertThat(res.getSprintId()).isNull();
    }

    @Test
    void moveTask_reorderWithinSameColumn_updatesOrderIndices() {
        BoardColumn column = toDoColumn;
        Task t1 = new Task(); t1.setId(1L); t1.setProjectId(projectId); t1.setStatusColumn(column); t1.setOrderIndex(0);
        Task t2 = new Task(); t2.setId(2L); t2.setProjectId(projectId); t2.setStatusColumn(column); t2.setOrderIndex(1);
        Task t3 = new Task(); t3.setId(3L); t3.setProjectId(projectId); t3.setStatusColumn(column); t3.setOrderIndex(2);

        when(taskRepository.findById(2L)).thenReturn(Optional.of(t2));
        when(taskRepository.findByProjectIdAndStatusColumnIdOrderByOrderIndexAsc(projectId, column.getId()))
                .thenReturn(Arrays.asList(t1, t2, t3));
        when(taskRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        MoveTaskReq req = new MoveTaskReq();
        req.setTargetColumnId(column.getId());
        req.setTargetOrderIndex(0); // move t2 to top

        taskService.moveTask(projectId, 2L, req, userId);

        assertThat(t2.getOrderIndex()).isEqualTo(0);
        assertThat(t1.getOrderIndex()).isEqualTo(1);
        assertThat(t3.getOrderIndex()).isEqualTo(2);
    }

    @Test
    void moveTask_moveToDifferentColumn_setsNewColumnAndReordersBothColumns() {
        Task moving = new Task(); moving.setId(5L); moving.setProjectId(projectId); moving.setStatusColumn(toDoColumn); moving.setOrderIndex(1);
        Task c1a = new Task(); c1a.setId(6L); c1a.setProjectId(projectId); c1a.setStatusColumn(toDoColumn); c1a.setOrderIndex(0);
        Task c1b = new Task(); c1b.setId(7L); c1b.setProjectId(projectId); c1b.setStatusColumn(toDoColumn); c1b.setOrderIndex(2);
        Task c2a = new Task(); c2a.setId(8L); c2a.setProjectId(projectId); c2a.setStatusColumn(doingColumn); c2a.setOrderIndex(0);

        when(taskRepository.findById(5L)).thenReturn(Optional.of(moving));
        when(taskRepository.findByProjectIdAndStatusColumnIdOrderByOrderIndexAsc(projectId, toDoColumn.getId()))
                .thenReturn(Arrays.asList(c1a, moving, c1b));
        when(taskRepository.findByProjectIdAndStatusColumnIdOrderByOrderIndexAsc(projectId, doingColumn.getId()))
                .thenReturn(Collections.singletonList(c2a));
        when(boardColumnRepository.findById(doingColumn.getId())).thenReturn(Optional.of(doingColumn));
        when(taskRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        MoveTaskReq req = new MoveTaskReq();
        req.setTargetColumnId(doingColumn.getId());
        req.setTargetOrderIndex(1);

        taskService.moveTask(projectId, 5L, req, userId);

        assertThat(moving.getStatusColumn().getId()).isEqualTo(doingColumn.getId());
        assertThat(moving.getOrderIndex()).isEqualTo(1);
        // Source column reindexed
        assertThat(c1a.getOrderIndex()).isEqualTo(0);
        assertThat(c1b.getOrderIndex()).isEqualTo(1);
        // Target column reindexed with inserted task
        assertThat(c2a.getOrderIndex()).isEqualTo(0);
    }
}
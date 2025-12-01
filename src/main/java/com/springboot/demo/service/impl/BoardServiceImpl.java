package com.springboot.demo.service.impl;

import com.springboot.demo.dto.board.*;
import com.springboot.demo.dto.task.TaskResponse;
import com.springboot.demo.model.Board;
import com.springboot.demo.model.BoardColumn;
import com.springboot.demo.model.Sprint;
import com.springboot.demo.model.Task;
import com.springboot.demo.model.enums.SprintStatus;
import com.springboot.demo.repository.BoardRepository;
import com.springboot.demo.repository.BoardColumnRepository;
import com.springboot.demo.repository.SprintRepository;
import com.springboot.demo.repository.TaskRepository;
import com.springboot.demo.repository.EpicRepository;
import com.springboot.demo.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;

/**
 * Implementation của BoardService
 * Logic chuẩn Jira: Project → Board → BoardColumn → Task
 */
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final EpicRepository epicRepository;

    /**
     * API 1: Lấy toàn bộ Board với columns và tasks (Quan trọng nhất)
     * Endpoint: GET /api/projects/{projectId}/board
     * 
     * Logic:
     * 1. Tìm board mặc định của project
     * 2. Tìm sprint đang active (status = 'active')
     * 3. Lấy tất cả columns của board (sắp xếp theo position)
     * 4. Lấy tasks của sprint đang active (hoặc tất cả nếu không có sprint active)
     * 5. Gom tasks theo column_id
     * 6. Trả về JSON lồng nhau (nested)
     */
    @Override
    @Transactional(readOnly = true)
    public BoardResponse getBoardByProjectId(String projectId) {
        // 1. Tìm board mặc định
        Board board = boardRepository.findByProjectIdAndIsDefaultTrue(projectId)
            .orElseThrow(() -> new RuntimeException("Board not found for project: " + projectId));

        // 2. Tìm sprint đang active
        Optional<Sprint> activeSprint = sprintRepository.findByProjectIdAndStatus(projectId, SprintStatus.active);

        // 3. Lấy tất cả columns của board
        List<BoardColumn> columns = boardColumnRepository.findByBoardIdOrderByPositionAsc(board.getId());

        // 4. Lấy tasks theo sprint
        List<Task> allTasks;
        if (activeSprint.isPresent()) {
            // Chỉ lấy tasks của sprint đang active
            Integer sprintId = activeSprint.get().getId();
            allTasks = taskRepository.findByProjectIdAndArchivedAtIsNull(projectId).stream()
                .filter(task -> task.getSprintId() != null && task.getSprintId().equals(sprintId))
                .collect(Collectors.toList());
        } else {
            // Nếu không có sprint active, lấy tất cả tasks (fallback)
            allTasks = taskRepository.findByProjectIdAndArchivedAtIsNull(projectId);
        }

        // 5. Gom tasks theo column_id
        Map<Integer, List<Task>> tasksByColumn = allTasks.stream()
            .filter(task -> task.getStatusColumn() != null)
            .collect(Collectors.groupingBy(task -> task.getStatusColumn().getId()));

        // 6. Convert sang DTO
        List<BoardColumnResponse> columnResponses = columns.stream()
            .map(column -> {
                List<Task> tasksInColumn = tasksByColumn.getOrDefault(column.getId(), new ArrayList<>());
                List<TaskResponse> taskResponses = tasksInColumn.stream()
                    .sorted(Comparator.comparing(Task::getOrderIndex, Comparator.nullsLast(Integer::compareTo)))
                    .map(this::mapTaskToResponse)
                    .collect(Collectors.toList());

                return new BoardColumnResponse(
                    column.getId(),
                    column.getBoardId(),
                    column.getName(),
                    column.getPosition(),
                    column.getColor(),
                    taskResponses
                );
            })
            .collect(Collectors.toList());

        return new BoardResponse(
            board.getId(),
            board.getProjectId(),
            board.getName(),
            board.getDescription(),
            board.getIsDefault(),
            columnResponses,
            activeSprint.map(Sprint::getId).orElse(null),
            activeSprint.map(Sprint::getName).orElse(null)
        );
    }

    @Override
    @Transactional
    public BoardResponse createBoard(BoardCreateRequest request) {
        Board board = new Board();
        board.setProjectId(request.getProjectId());
        board.setName(request.getName());
        board.setDescription(request.getDescription());
        board.setIsDefault(request.getIsDefault());

        Board saved = boardRepository.save(board);

        // Tạo 3 cột mặc định: TO DO, IN PROGRESS, DONE
        createDefaultColumns(saved.getId());

        return getBoardByProjectId(saved.getProjectId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardResponse> getBoardsByProjectId(String projectId) {
        List<Board> boards = boardRepository.findByProjectId(projectId);
        return boards.stream()
            .map(board -> getBoardByProjectId(projectId))
            .collect(Collectors.toList());
    }

    /**
     * Helper: Tạo 3 cột mặc định khi tạo board mới
     */
    private void createDefaultColumns(Integer boardId) {
        List<BoardColumn> defaultColumns = List.of(
            createColumn(boardId, "To Do", 0, "#6B7280"),
            createColumn(boardId, "In Progress", 1, "#3B82F6"),
            createColumn(boardId, "Done", 2, "#10B981")
        );
        boardColumnRepository.saveAll(defaultColumns);
    }

    private BoardColumn createColumn(Integer boardId, String name, Integer position, String color) {
        BoardColumn column = new BoardColumn();
        column.setBoardId(boardId);
        column.setName(name);
        column.setPosition(position);
        column.setColor(color);
        return column;
    }

    /**
     * Helper: Convert Task entity → TaskResponse DTO
     */
    private TaskResponse mapTaskToResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatusColumn() != null ? task.getStatusColumn().getName() : null);
        response.setPriority(task.getPriority() != null ? task.getPriority().name() : null);
        response.setDueDate(task.getDueDate());
        response.setEstimatedHours(task.getEstimatedHours());
        
        // CHUẨN HÓA: Đổi assignedTo → assigneeId
        response.setAssigneeId(task.getAssigneeId());
        response.setEpicId(task.getEpicId());
        
        // Populate epic title if epic exists
        if (task.getEpicId() != null) {
            epicRepository.findById(task.getEpicId())
                .ifPresent(epic -> response.setEpicTitle(epic.getTitle()));
        }
        
        response.setSprintId(task.getSprintId() != null ? task.getSprintId().toString() : null);
        response.setTags(task.getTags());
        response.setOrderIndex(task.getOrderIndex());
        response.setProjectId(task.getProjectId());
        return response;
    }
}

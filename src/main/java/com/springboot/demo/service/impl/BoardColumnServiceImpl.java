package com.springboot.demo.service.impl;

import com.springboot.demo.dto.board.*;
import com.springboot.demo.model.BoardColumn;
import com.springboot.demo.model.Task;
import com.springboot.demo.repository.BoardColumnRepository;
import com.springboot.demo.repository.TaskRepository;
import com.springboot.demo.service.BoardColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của BoardColumnService
 * Quản lý các cột trạng thái động (Status Columns)
 */
@Service
@RequiredArgsConstructor
public class BoardColumnServiceImpl implements BoardColumnService {

    private final BoardColumnRepository boardColumnRepository;
    private final TaskRepository taskRepository;

    /**
     * API 2: Tạo column mới
     * POST /api/boards/{boardId}/columns
     */
    @Override
    @Transactional
    public BoardColumnResponse createColumn(BoardColumnCreateRequest request) {
        // Validate: Kiểm tra tên column có trùng không
        if (boardColumnRepository.existsByBoardIdAndName(request.getBoardId(), request.getName())) {
            throw new RuntimeException("Column name '" + request.getName() + "' already exists in this board");
        }

        // Tự động tính position nếu không có
        Integer position = request.getPosition();
        if (position == null) {
            Integer maxPosition = boardColumnRepository.findMaxPositionByBoardId(request.getBoardId());
            position = maxPosition + 1;
        }

        // Tạo column mới
        BoardColumn column = new BoardColumn();
        column.setBoardId(request.getBoardId());
        column.setName(request.getName());
        column.setPosition(position);
        column.setColor(request.getColor() != null ? request.getColor() : "#6B7280");

        BoardColumn saved = boardColumnRepository.save(column);
        return mapToResponse(saved);
    }

    /**
     * API 3: Cập nhật column
     * PUT /api/columns/{columnId}
     */
    @Override
    @Transactional
    public BoardColumnResponse updateColumn(Integer columnId, BoardColumnUpdateRequest request) {
        BoardColumn column = boardColumnRepository.findById(columnId)
            .orElseThrow(() -> new RuntimeException("Column not found: " + columnId));

        // Cập nhật các field nếu có
        if (request.getName() != null && !request.getName().isEmpty()) {
            // Kiểm tra trùng tên (ngoại trừ chính nó)
            if (boardColumnRepository.existsByBoardIdAndName(column.getBoardId(), request.getName())) {
                BoardColumn existing = boardColumnRepository.findByBoardIdOrderByPositionAsc(column.getBoardId())
                    .stream()
                    .filter(c -> c.getName().equals(request.getName()))
                    .findFirst()
                    .orElse(null);
                
                if (existing != null && !existing.getId().equals(columnId)) {
                    throw new RuntimeException("Column name '" + request.getName() + "' already exists");
                }
            }
            column.setName(request.getName());
        }

        if (request.getPosition() != null) {
            column.setPosition(request.getPosition());
        }

        if (request.getColor() != null) {
            column.setColor(request.getColor());
        }

        BoardColumn updated = boardColumnRepository.save(column);
        return mapToResponse(updated);
    }

    /**
     * API 4: Xóa column
     * DELETE /api/columns/{columnId}
     * 
     * Lưu ý: Khi xóa column, các tasks trong column đó sẽ chuyển về column đầu tiên (TODO)
     */
    @Override
    @Transactional
    public void deleteColumn(Integer columnId) {
        BoardColumn column = boardColumnRepository.findById(columnId)
            .orElseThrow(() -> new RuntimeException("Column not found: " + columnId));

        long activeCount = taskRepository.countByStatusColumn_IdAndArchivedAtIsNull(columnId);
        if (activeCount > 0) {
            throw new RuntimeException("Cannot delete column because it still contains " + activeCount + " active tasks. Please transfer them first.");
        }

        boardColumnRepository.deleteById(columnId);
    }

    /**
     * API 5: Sắp xếp lại thứ tự columns (Khi user kéo thả)
     * PATCH /api/boards/{boardId}/columns/reorder
     * 
     * Request body: [3, 1, 2, 4] (columnIds theo thứ tự mới)
     * 
     * Logic:
     * - columnIds[0] sẽ có position = 0
     * - columnIds[1] sẽ có position = 1
     * - ...
     */
    @Override
    @Transactional
    public void reorderColumns(Integer boardId, List<Integer> columnIds) {
        if (columnIds == null || columnIds.isEmpty()) {
            return;
        }

        List<BoardColumn> existingColumns = boardColumnRepository.findByBoardIdOrderByPositionAsc(boardId);
        if (existingColumns.size() != columnIds.size()) {
            throw new IllegalArgumentException("Column list does not match board state");
        }

        // Ensure all provided ids belong to the target board
        java.util.Set<Integer> existingIds = existingColumns.stream()
                .map(BoardColumn::getId)
                .collect(java.util.stream.Collectors.toSet());
        if (!existingIds.containsAll(columnIds)) {
            throw new IllegalArgumentException("Invalid column id in reorder request");
        }

        // Bump positions to temporary values to avoid unique (board_id, position) collisions
        int offset = columnIds.size();
        for (int i = 0; i < columnIds.size(); i++) {
            Integer columnId = columnIds.get(i);
            boardColumnRepository.updatePosition(columnId, offset + i);
        }

        // Apply final ordering
        for (int i = 0; i < columnIds.size(); i++) {
            Integer columnId = columnIds.get(i);
            boardColumnRepository.updatePosition(columnId, i);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardColumnResponse> getColumnsByBoardId(Integer boardId) {
        List<BoardColumn> columns = boardColumnRepository.findByBoardIdOrderByPositionAsc(boardId);
        return columns.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void transferTasks(Integer sourceColumnId, Integer targetColumnId) {
        BoardColumn source = boardColumnRepository.findById(sourceColumnId)
                .orElseThrow(() -> new RuntimeException("Source column not found: " + sourceColumnId));
        BoardColumn target = boardColumnRepository.findById(targetColumnId)
                .orElseThrow(() -> new RuntimeException("Target column not found: " + targetColumnId));

        if (!source.getBoardId().equals(target.getBoardId())) {
            throw new RuntimeException("Columns must belong to the same board");
        }

        List<Task> tasksInTarget = taskRepository.findByStatusColumn_IdAndArchivedAtIsNull(targetColumnId);
        int nextIndex = tasksInTarget.stream()
                .map(Task::getOrderIndex)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
        // If there are tasks, next index should be last+1
        nextIndex = tasksInTarget.isEmpty() ? 0 : (nextIndex + 1);

        List<Task> toMove = taskRepository.findByStatusColumn_IdAndArchivedAtIsNull(sourceColumnId);
        for (Task t : toMove) {
            t.setStatusColumn(target);
            t.setOrderIndex(nextIndex);
            nextIndex++;
        }
        if (!toMove.isEmpty()) {
            taskRepository.saveAll(toMove);
        }
    }

    /**
     * Helper: Convert Entity → DTO
     */
    private BoardColumnResponse mapToResponse(BoardColumn column) {
        BoardColumnResponse response = new BoardColumnResponse();
        response.setId(column.getId());
        response.setBoardId(column.getBoardId());
        response.setName(column.getName());
        response.setPosition(column.getPosition());
        response.setColor(column.getColor());
        response.setTasks(new ArrayList<>()); // Empty list (load from board endpoint)
        return response;
    }
}

package com.springboot.demo.controller;

import com.springboot.demo.dto.board.*;
import com.springboot.demo.repository.BoardRepository;
import com.springboot.demo.repository.BoardColumnRepository;
import com.springboot.demo.service.BoardColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BoardColumn Controller - REST API endpoints
 * Quản lý các cột trạng thái động (Status Columns)
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BoardColumnController {

    private final BoardColumnService boardColumnService;
    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;

    /**
     * API 2: Tạo column mới
     * POST /api/boards/{boardId}/columns
     * 
     * Request body:
     * {
     *   "boardId": 1,
     *   "name": "Code Review",
     *   "position": 3,
     *   "color": "#F59E0B"
     * }
     */
    @PreAuthorize("@gate.allows('project:update', authentication, @boardRepository.findProjectIdByBoardId(#boardId).orElse(''))")
    @PostMapping("/boards/{boardId}/columns")
    public ResponseEntity<BoardColumnResponse> createColumn(
            @PathVariable Integer boardId,
            @RequestBody BoardColumnCreateRequest request,
            Authentication authentication) {
        // Đảm bảo boardId từ path khớp với request body
        request.setBoardId(boardId);
        BoardColumnResponse response = boardColumnService.createColumn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API 3: Cập nhật column (đổi tên, màu sắc)
     * PUT /api/columns/{columnId}
     * 
     * Request body:
     * {
     *   "name": "QA Testing",
     *   "color": "#8B5CF6"
     * }
     */
    @PreAuthorize("@gate.allows('project:update', authentication, @boardColumnRepository.findProjectIdByColumnId(#columnId).orElse(''))")
    @PutMapping("/columns/{columnId}")
    public ResponseEntity<BoardColumnResponse> updateColumn(
            @PathVariable Integer columnId,
            @RequestBody BoardColumnUpdateRequest request,
            Authentication authentication) {
        BoardColumnResponse response = boardColumnService.updateColumn(columnId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * API 4: Xóa column
     * DELETE /api/columns/{columnId}
     */
    @PreAuthorize("@gate.allows('project:update', authentication, @boardColumnRepository.findProjectIdByColumnId(#columnId).orElse(''))")
    @DeleteMapping("/columns/{columnId}")
    public ResponseEntity<Void> deleteColumn(
            @PathVariable Integer columnId,
            Authentication authentication) {
        boardColumnService.deleteColumn(columnId);
        return ResponseEntity.noContent().build();
    }

    /**
     * API 5: Sắp xếp lại thứ tự columns (Khi user kéo thả)
     * PATCH /api/boards/{boardId}/columns/reorder
     * 
     * Request body: [3, 1, 2, 4] (columnIds theo thứ tự mới)
     */
    @PreAuthorize("@gate.allows('project:update', authentication, @boardRepository.findProjectIdByBoardId(#boardId).orElse(''))")
    @PatchMapping("/boards/{boardId}/columns/reorder")
    public ResponseEntity<Void> reorderColumns(
            @PathVariable Integer boardId,
            @RequestBody List<Integer> columnIds,
            Authentication authentication) {
        boardColumnService.reorderColumns(boardId, columnIds);
        return ResponseEntity.ok().build();
    }

    /**
     * Lấy danh sách columns của board
     * GET /api/boards/{boardId}/columns
     */
    @PreAuthorize("@gate.allows('project:read', authentication, @boardRepository.findProjectIdByBoardId(#boardId).orElse(''))")
    @GetMapping("/boards/{boardId}/columns")
    public ResponseEntity<List<BoardColumnResponse>> getColumnsByBoardId(
            @PathVariable Integer boardId,
            Authentication authentication) {
        List<BoardColumnResponse> responses = boardColumnService.getColumnsByBoardId(boardId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Transfer tất cả tasks từ source column sang target column
     * PATCH /api/columns/{sourceColumnId}/transfer/{targetColumnId}
     */
    @PreAuthorize("@gate.allows('project:update', authentication, @boardColumnRepository.findProjectIdByColumnId(#sourceColumnId).orElse(''))")
    @PatchMapping("/columns/{sourceColumnId}/transfer/{targetColumnId}")
    public ResponseEntity<Void> transferColumnTasks(
            @PathVariable Integer sourceColumnId,
            @PathVariable Integer targetColumnId,
            Authentication authentication) {
        boardColumnService.transferTasks(sourceColumnId, targetColumnId);
        return ResponseEntity.ok().build();
    }
}

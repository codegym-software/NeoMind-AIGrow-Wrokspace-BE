package com.springboot.demo.controller;

import com.springboot.demo.dto.board.*;
import com.springboot.demo.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Board Controller - REST API endpoints
 * Chuẩn Jira: Project → Board → BoardColumn → Task
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * API 1: Lấy toàn bộ Board với columns và tasks (Quan trọng nhất)
     * GET /api/projects/{projectId}/board
     * 
     * Response JSON:
     * {
     *   "id": 1,
     *   "name": "Kanban Board",
     *   "columns": [
     *     {
     *       "id": 10,
     *       "name": "To Do",
     *       "tasks": [...]
     *     },
     *     ...
     *   ]
     * }
     */
    @PreAuthorize("@gate.allows('project:read', authentication, #projectId)")
    @GetMapping("/projects/{projectId}/board")
    public ResponseEntity<BoardResponse> getBoardByProjectId(@PathVariable String projectId) {
        try {
            BoardResponse response = boardService.getBoardByProjectId(projectId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Return 404 when board not found - frontend will catch this and auto-create board
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Tạo board mới cho project
     * POST /api/boards
     */
    @PreAuthorize("@gate.allows('project:update', authentication, #request.projectId)")
    @PostMapping("/boards")
    public ResponseEntity<BoardResponse> createBoard(@RequestBody BoardCreateRequest request) {
        BoardResponse response = boardService.createBoard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lấy tất cả boards của project
     * GET /api/projects/{projectId}/boards
     */
    @PreAuthorize("@gate.allows('project:read', authentication, #projectId)")
    @GetMapping("/projects/{projectId}/boards")
    public ResponseEntity<List<BoardResponse>> getBoardsByProjectId(@PathVariable String projectId) {
        List<BoardResponse> responses = boardService.getBoardsByProjectId(projectId);
        return ResponseEntity.ok(responses);
    }
}

package com.springboot.demo.service;

import com.springboot.demo.dto.board.*;

import java.util.List;

/**
 * Service interface cho Board operations
 */
public interface BoardService {
    
    /**
     * API 1: Lấy toàn bộ Board với columns và tasks (Quan trọng nhất)
     * GET /api/projects/{projectId}/board
     */
    BoardResponse getBoardByProjectId(String projectId);
    
    /**
     * Tạo board mới
     */
    BoardResponse createBoard(BoardCreateRequest request);
    
    /**
     * Lấy tất cả boards của project
     */
    List<BoardResponse> getBoardsByProjectId(String projectId);
}

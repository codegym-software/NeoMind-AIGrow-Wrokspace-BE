package com.springboot.demo.service;

import com.springboot.demo.dto.board.*;

import java.util.List;

/**
 * Service interface cho BoardColumn operations
 */
public interface BoardColumnService {
    
    /**
     * API 2: Tạo column mới
     * POST /api/boards/{boardId}/columns
     */
    BoardColumnResponse createColumn(BoardColumnCreateRequest request);
    
    /**
     * API 3: Cập nhật column (đổi tên, màu sắc)
     * PUT /api/columns/{columnId}
     */
    BoardColumnResponse updateColumn(Integer columnId, BoardColumnUpdateRequest request);
    
    /**
     * API 4: Xóa column
     * DELETE /api/columns/{columnId}
     */
    void deleteColumn(Integer columnId);
    
    /**
     * API 5: Sắp xếp lại thứ tự columns
     * PATCH /api/boards/{boardId}/columns/reorder
     */
    void reorderColumns(Integer boardId, List<Integer> columnIds);
    
    /**
     * Lấy danh sách columns của board
     */
    List<BoardColumnResponse> getColumnsByBoardId(Integer boardId);

    void transferTasks(Integer sourceColumnId, Integer targetColumnId);
}

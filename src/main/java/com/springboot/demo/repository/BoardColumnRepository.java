package com.springboot.demo.repository;

import com.springboot.demo.model.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho BoardColumn entity
 * ✅ Dùng board.id thay vì boardId vì đã xóa field boardId trong entity
 */
@Repository
public interface BoardColumnRepository extends JpaRepository<BoardColumn, Integer> {
    
    /**
     * Tìm tất cả columns của một board, sắp xếp theo position
     * Dùng board.id vì @JoinColumn(name = "board_id") trong entity
     */
    @Query("SELECT bc FROM BoardColumn bc WHERE bc.board.id = :boardId ORDER BY bc.position ASC")
    List<BoardColumn> findByBoardIdOrderByPositionAsc(@Param("boardId") Integer boardId);
    
    /**
     * Kiểm tra tên column có tồn tại trong board không
     */
    @Query("SELECT COUNT(bc) > 0 FROM BoardColumn bc WHERE bc.board.id = :boardId AND bc.name = :name")
    boolean existsByBoardIdAndName(@Param("boardId") Integer boardId, @Param("name") String name);
    
    /**
     * Đếm số lượng columns trong board
     */
    @Query("SELECT COUNT(bc) FROM BoardColumn bc WHERE bc.board.id = :boardId")
    long countByBoardId(@Param("boardId") Integer boardId);
    
    /**
     * Lấy position lớn nhất trong board (để thêm column mới vào cuối)
     */
    @Query("SELECT COALESCE(MAX(bc.position), -1) FROM BoardColumn bc WHERE bc.board.id = :boardId")
    Integer findMaxPositionByBoardId(@Param("boardId") Integer boardId);
    
    /**
     * Cập nhật position của các columns (dùng khi reorder)
     */
    @Modifying
    @Query("UPDATE BoardColumn bc SET bc.position = :position WHERE bc.id = :columnId")
    void updatePosition(@Param("columnId") Integer columnId, @Param("position") Integer position);
    
    /**
     * Lấy projectId từ columnId (qua board)
     */
    @Query("SELECT bc.board.projectId FROM BoardColumn bc WHERE bc.id = :columnId")
    Optional<String> findProjectIdByColumnId(@Param("columnId") Integer columnId);
    
    /**
     * Tìm column theo projectId và name (để complete task)
     */
    @Query("SELECT bc FROM BoardColumn bc WHERE bc.board.projectId = :projectId AND bc.name = :name")
    List<BoardColumn> findByProjectIdAndName(@Param("projectId") String projectId, @Param("name") String name);
}

package com.springboot.demo.repository;

import com.springboot.demo.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Board entity
 */
@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {
    
    /**
     * Tìm tất cả boards của một project
     */
    List<Board> findByProjectId(String projectId);
    
    /**
     * Tìm board mặc định của project
     */
    Optional<Board> findByProjectIdAndIsDefaultTrue(String projectId);
    
    /**
     * Kiểm tra project đã có board mặc định chưa
     */
    boolean existsByProjectIdAndIsDefaultTrue(String projectId);
    
    /**
     * Lấy projectId từ boardId
     */
    @Query("SELECT b.projectId FROM Board b WHERE b.id = :boardId")
    Optional<String> findProjectIdByBoardId(@Param("boardId") Integer boardId);
}

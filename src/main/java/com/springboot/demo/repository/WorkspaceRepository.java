package com.springboot.demo.repository;

import com.springboot.demo.model.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, String> {
    Page<Workspace> findByNameContainingIgnoreCase(String name, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT w FROM Workspace w WHERE w.id IN :workspaceIds")
    Page<Workspace> findByIdIn(@Param("workspaceIds") List<String> workspaceIds, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT w FROM Workspace w WHERE w.id IN :workspaceIds AND LOWER(w.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Workspace> findByIdInAndNameContainingIgnoreCase(@Param("workspaceIds") List<String> workspaceIds,
                                                        @Param("searchTerm") String searchTerm,
                                                        Pageable pageable);

    void deleteByCompanyId(String companyId);

    @Query("select w.id from Workspace w where w.companyId = :companyId")
    List<String> findIdsByCompanyId(@Param("companyId") String companyId);

    boolean existsByCompanyIdAndNameIgnoreCase(String companyId, String name);
}



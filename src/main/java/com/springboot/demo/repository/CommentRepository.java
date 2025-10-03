package com.springboot.demo.repository;

import com.springboot.demo.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTaskIdInAndContentStartingWithOrderByIdAsc(Collection<Integer> taskIds, String prefix);
}

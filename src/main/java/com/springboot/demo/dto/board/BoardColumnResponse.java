package com.springboot.demo.dto.board;

import com.springboot.demo.dto.task.TaskResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO cho BoardColumn - Bao gồm cả tasks
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumnResponse {
    private Integer id;
    private Integer boardId;
    private String name;
    private Integer position;
    private String color;
    private List<TaskResponse> tasks;
}

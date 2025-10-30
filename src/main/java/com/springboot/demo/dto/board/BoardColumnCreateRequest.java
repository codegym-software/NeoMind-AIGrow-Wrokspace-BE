package com.springboot.demo.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để tạo BoardColumn mới
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumnCreateRequest {
    private Integer boardId;
    private String name;
    private Integer position;
    private String color;
}

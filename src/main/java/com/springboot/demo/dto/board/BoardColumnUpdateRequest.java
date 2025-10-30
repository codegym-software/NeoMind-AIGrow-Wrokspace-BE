package com.springboot.demo.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để cập nhật BoardColumn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumnUpdateRequest {
    private String name;
    private Integer position;
    private String color;
}

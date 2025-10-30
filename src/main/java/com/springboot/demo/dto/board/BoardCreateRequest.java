package com.springboot.demo.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để tạo Board mới
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardCreateRequest {
    private String projectId;
    private String name;
    private String description;
    private Boolean isDefault = true;
}

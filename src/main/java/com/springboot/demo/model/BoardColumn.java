// Tạo file BoardColumn.java
package com.springboot.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "board_columns")
public class BoardColumn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private Integer boardId;
    private String name;
    private Integer position;
}
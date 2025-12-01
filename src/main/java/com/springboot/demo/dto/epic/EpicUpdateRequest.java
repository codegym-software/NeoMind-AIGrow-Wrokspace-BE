package com.springboot.demo.dto.epic;

import java.time.LocalDate;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class EpicUpdateRequest {
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}

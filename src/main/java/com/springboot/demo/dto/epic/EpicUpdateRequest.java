package com.springboot.demo.dto.epic;

import com.springboot.demo.validation.ValidDateRange;
import java.time.LocalDate;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@ValidDateRange(startField = "startDate", endField = "endDate", message = "End date must be after or equal to start date")
public class EpicUpdateRequest {
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}

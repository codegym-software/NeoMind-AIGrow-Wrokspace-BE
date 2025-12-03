package com.springboot.demo.dto.epic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EpicListResponse {
    private long totalEpics;
    private long totalTasks;
    private Page<EpicDto> epics;
}


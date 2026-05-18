package com.rikkei.salsp.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DispatchQueueItemDto {

    private Long recordId;
    private String studentName;
    private LocalDate sessionDate;
    private List<String> equipmentSummary;
    private String status;
}


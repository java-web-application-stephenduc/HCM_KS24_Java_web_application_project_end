package com.rikkei.salsp.dto.admin;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `DispatchQueueItemDto` thuộc hệ thống Smart Academic Lab Support Platform
 * (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class DispatchQueueItemDto {

    private Long recordId;
    private String studentName;
    private LocalDate sessionDate;
    private List<String> equipmentSummary;
    private String status;
    private String lecturerNote;
    private String adminNote;
}

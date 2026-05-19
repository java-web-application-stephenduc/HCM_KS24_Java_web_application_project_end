package com.rikkei.salsp.dto.student;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `BorrowedEquipmentHistoryDto` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class BorrowedEquipmentHistoryDto {

    private String equipmentCode;
    private String equipmentName;
    private LocalDateTime borrowedAt;
    private LocalDateTime returnedAt;
    private String status;
    private String note;
}


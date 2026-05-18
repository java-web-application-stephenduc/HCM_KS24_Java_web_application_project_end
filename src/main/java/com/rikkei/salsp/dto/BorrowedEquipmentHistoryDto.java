package com.rikkei.salsp.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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


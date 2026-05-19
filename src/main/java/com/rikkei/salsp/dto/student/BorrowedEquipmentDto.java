package com.rikkei.salsp.dto.student;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `BorrowedEquipmentDto` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class BorrowedEquipmentDto {

    private String equipmentName;
    private Integer quantity;
}


package com.rikkei.salsp.dto.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `EquipmentRequestDto` thuộc hệ thống Smart Academic Lab Support Platform
 * (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class EquipmentRequestDto {

    private Long equipmentId;
    private String equipmentName;
    private String category;
    private Integer quantity;
    private Integer quantityAvailable;
}

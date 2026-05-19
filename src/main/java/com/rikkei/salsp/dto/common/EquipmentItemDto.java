package com.rikkei.salsp.dto.common;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `EquipmentItemDto` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class EquipmentItemDto {

    private Long equipmentId;

    @Min(1)
    private Integer quantity;
}


package com.rikkei.salsp.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EquipmentItemDto {

    private Long equipmentId;

    @Min(1)
    private Integer quantity;
}


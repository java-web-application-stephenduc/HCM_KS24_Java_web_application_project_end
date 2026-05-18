package com.rikkei.salsp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BorrowedEquipmentDto {

    private String equipmentName;
    private Integer quantity;
}


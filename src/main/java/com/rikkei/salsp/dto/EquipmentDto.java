package com.rikkei.salsp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EquipmentDto {

    private Long id;

    @NotBlank
    private String name;

    private String description;

    private String category;

    @Min(0)
    private int quantityTotal;

    @Min(0)
    private int quantityAvailable;
}


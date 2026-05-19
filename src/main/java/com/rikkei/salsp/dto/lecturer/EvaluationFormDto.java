package com.rikkei.salsp.dto.lecturer;
import com.rikkei.salsp.dto.common.EquipmentItemDto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `EvaluationFormDto` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class EvaluationFormDto {

    @NotNull
    private Long sessionId;

    @NotNull
    @Min(0)
    @Max(10)
    private Integer score;

    private String feedback;

    private List<EquipmentItemDto> equipmentItems = new ArrayList<>();
}


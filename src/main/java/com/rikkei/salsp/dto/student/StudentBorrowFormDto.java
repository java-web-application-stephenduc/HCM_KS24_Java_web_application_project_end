package com.rikkei.salsp.dto.student;
import com.rikkei.salsp.dto.common.EquipmentItemDto;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `StudentBorrowFormDto` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class StudentBorrowFormDto {

    @NotNull
    private Long sessionId;

    private List<EquipmentItemDto> equipmentItems = new ArrayList<>();
}

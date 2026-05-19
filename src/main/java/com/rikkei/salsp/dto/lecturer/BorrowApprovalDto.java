package com.rikkei.salsp.dto.lecturer;

import com.rikkei.salsp.dto.common.EquipmentItemDto;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `BorrowApprovalDto` thuộc hệ thống Smart Academic Lab Support Platform
 * (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class BorrowApprovalDto {

    @NotNull
    private Long sessionId;

    private String lecturerNote;

    private List<EquipmentItemDto> additionalEquipmentItems = new ArrayList<>();
}

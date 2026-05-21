package com.rikkei.salsp.dto.lecturer;

import com.rikkei.salsp.dto.common.EquipmentItemDto;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO form gửi lên khi giảng viên duyệt/từ chối yêu cầu mượn thiết bị.
 * Có thể kèm danh sách thiết bị bổ sung và ghi chú.
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

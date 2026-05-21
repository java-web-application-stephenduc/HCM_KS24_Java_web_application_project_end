package com.rikkei.salsp.dto.student;
import com.rikkei.salsp.dto.common.EquipmentItemDto;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO nhận dữ liệu form mượn thiết bị từ sinh viên.
 *
 * MỤC ĐÍCH: Wrapper chứa session ID + danh sách thiết bị mượn.
 *
 * FLOW:
 * Student → /student/borrowing?sessionId=5 → GET form
 * → Check session status = CONFIRMED (phải được giảng viên duyệt trước)
 * → Hiển thị form: Chọn thiết bị + qty
 * → POST /student/borrowing → StudentBorrowFormDto
 *   {
 *     sessionId: 5,
 *     equipmentItems: [
 *       {equipmentId: 1, quantity: 2},  // Oscilloscope x2
 *       {equipmentId: 2, quantity: 1}   // Multimeter x1
 *     ]
 *   }
 *
 * VALIDATION:
 * - sessionId: @NotNull (bắt buộc)
 * - equipmentItems: List của EquipmentItemDto
 *   - Mỗi item validate: equipmentId not null, quantity >= 1
 *
 * SERVICE PROCESSING:
 * → Create BorrowingRecord (header)
 * → ForEach equipment → Create BorrowingDetail
 * → Check quantityAvailable >= quantity_request
 * → Save + send to Admin dispatch queue
 */
@Getter
@Setter
@NoArgsConstructor
public class StudentBorrowFormDto {

    @NotNull
    private Long sessionId;

    private List<EquipmentItemDto> equipmentItems = new ArrayList<>();
}

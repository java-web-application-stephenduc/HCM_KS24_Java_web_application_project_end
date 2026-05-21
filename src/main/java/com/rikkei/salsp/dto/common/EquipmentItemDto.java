package com.rikkei.salsp.dto.common;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO đại diện cho một loại thiết bị trong phiếu mượn.
 *
 * MỤC ĐÍCH: Item-level detail của borrowing request (line item).
 *
 * USAGE:
 * Sinh viên form mượn thiết bị → Chọn
 * [x] Oscilloscope (qty: 2)
 * [x] Multimeter (qty: 1)
 * → POST /student/borrowing → List<EquipmentItemDto>
 *
 * FLOW:
 * 1. Client parse form items → EquipmentItemDto[]
 * 2. Create BorrowingRecord (header)
 * 3. ForEach EquipmentItemDto → Create BorrowingDetail
 * 4. Check stock: Equipment.quantityAvailable >= qty
 * 5. If OK → Admin phê duyệt → Reserve kho
 *
 * VALIDATION:
 * - equipmentId: Foreign key tới Equipment
 * - quantity: @Min(1) (phải mượn ít nhất 1 cái)
 *
 * PARENT: BorrowingRecord (1 record chứa N+items)
 */
@Getter
@Setter
@NoArgsConstructor
public class EquipmentItemDto {

    private Long equipmentId;

    @Min(1)
    private Integer quantity;
}


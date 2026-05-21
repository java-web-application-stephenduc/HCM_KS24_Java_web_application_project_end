package com.rikkei.salsp.dto.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO đại diện cho 1 request mượn thiết bị cụ thể.
 *
 * MỤC ĐÍCH: Hiển thị thông tin thiết bị có sẵn khi sinh viên lựa chọn.
 *
 * USAGE:
 * Student form "Chọn thiết bị" → AJAX GET /api/equipments
 * → Return List<EquipmentRequestDto> gồm:
 *   - equipmentId, name, category (info about equipment)
 *   - quantity: User nhập vào form
 *   - quantityAvailable: Server trả về (to check if enough)
 *
 * VALIDATION LOGIC:
 * - User nhập: "Mượn Oscilloscope 5 cái"
 * - Server check: Oscilloscope chỉ còn 3 cái → Show "Không đủ"
 * - quantityAvailable dùng cho client-side validation
 *
 * SIMPLE STRUCTURE: Dễ serialize/deserialize JSON
 */
@Getter
@Setter
@NoArgsConstructor
public class EquipmentRequestDto {

    private Long equipmentId;
    private String equipmentName;
    private String category;
    private Integer quantity;
    private Integer quantityAvailable;
}

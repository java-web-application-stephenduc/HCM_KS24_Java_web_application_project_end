package com.rikkei.salsp.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO dùng cho form tạo/cập nhật thiết bị (Admin).
 *
 * MỤC ĐÍCH: Nhận dữ liệu từ form HTML, validate, pass xuống Service.
 *
 * VALIDATION:
 * - name: @NotBlank (bắt buộc nhập tên thiết bị)
 * - quantityTotal, quantityAvailable: @Min(0) (không được âm)
 * - description, category: Optional
 *
 * FLOW:
 * Admin form → POST /admin/equipment → Controller parse EquipmentDto
 * → Validate @Valid → AdminEquipmentService.create/update(dto)
 * → Persist thành Equipment entity
 *
 * FIELDS:
 * - id: Null khi tạo mới, có giá trị khi update
 * - quantityTotal: Tổng số mua vào (master data)
 * - quantityAvailable: Số hiện có khả dụng (dynamic)
 */
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


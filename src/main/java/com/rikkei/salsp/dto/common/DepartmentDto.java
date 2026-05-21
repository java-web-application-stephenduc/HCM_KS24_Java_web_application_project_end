package com.rikkei.salsp.dto.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO đại diện cho một khoa/bộ phận.
 *
 * MỤC ĐÍCH: Truyền thông tin khoa tới UI (không cần entire entity).
 *
 * USAGE:
 * Bước 1 (chọn khoa):
 * - GET /student/booking → BookingService.getDepartments()
 * - Return: List<DepartmentDto> gồm id, tên khoa, mã khoa
 * - UI render dropdown để user chọn
 *
 * FIELDS:
 * - id: Primary key (dùng cho query giảng viên)
 * - name: Tên khoa (ví dụ: "Công nghệ Thông tin")
 * - code: Mã khoa (ví dụ: "IT", "EE", "ME")
 */
@Getter
@Setter
@NoArgsConstructor
public class DepartmentDto {

    private Long id;
    private String name;
    private String code;
}


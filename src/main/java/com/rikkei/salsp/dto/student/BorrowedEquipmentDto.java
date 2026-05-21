package com.rikkei.salsp.dto.student;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO đơn giản đại diện cho thiết bị mượn.
 *
 * MỤC ĐÍCH: Hiển thị tóm tắt thiết bị trên giao diện (nested trong AcademicRecordDto).
 *
 * USAGE:
 * - Nested trong AcademicRecordDto.equipments[]
 * - Student xem lịch sử → Hiển thị: "Oscilloscope x2, Multimeter x1"
 *
 * FIELDS:
 * - equipmentName: Tên thiết bị để render
 * - quantity: Số lượng mượn
 *
 * SIMPLE DTO: Chỉ cần 2 fields, không cần ID hay status detail
 */
@Getter
@Setter
@NoArgsConstructor
public class BorrowedEquipmentDto {

    private String equipmentName;
    private Integer quantity;
}


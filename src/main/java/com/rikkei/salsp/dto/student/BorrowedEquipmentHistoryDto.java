package com.rikkei.salsp.dto.student;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO đại diện cho lịch sử mượn 1 loại thiết bị.
 *
 * MỤC ĐÍCH: Hiển thị thông tin mượn/trả chi tiết theo thời gian.
 *
 * USAGE:
 * Student → /student/history/borrowing → List<BorrowedEquipmentHistoryDto>
 * → Render timeline: Khi mượn, khi trả, trạng thái hiện tại
 *
 * FIELDS:
 * - equipmentCode: Mã thiết bị (ví dụ: "OSC-001")
 * - equipmentName: Tên thiết bị (ví dụ: "Oscilloscope Digital")
 * - borrowedAt: Ngày giờ mượn (thức tế = ngày buổi cố vấn)
 * - returnedAt: Ngày giờ trả (nếu đã trả, else null)
 * - status: PENDING, APPROVED, BORROWED, RETURNED, OVERDUE
 * - note: Ghi chú từ Admin/Lecturer
 *
 * FLOW:
 * - Mượn: borrowedAt set, returnedAt=null, status=BORROWED
 * - Trả: returnedAt set, status=RETURNED
 * - Quá hạn: BorrowingOverdueScheduler detect → status=OVERDUE
 */
@Getter
@Setter
@NoArgsConstructor
public class BorrowedEquipmentHistoryDto {

    private String equipmentCode;
    private String equipmentName;
    private LocalDateTime borrowedAt;
    private LocalDateTime returnedAt;
    private String status;
    private String note;
}


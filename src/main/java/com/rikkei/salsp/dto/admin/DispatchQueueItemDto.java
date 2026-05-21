package com.rikkei.salsp.dto.admin;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO đại diện cho một phiếu mượn trong hàng chờ phê duyệt (Dispatch Queue).
 *
 * MỤC ĐÍCH: Hiển thị thông tin phiếu mượn ở Admin Dispatch Page.
 *
 * USAGE:
 * Admin → /admin/dispatch → Xem hàng chờ phiếu PENDING
 * → DispatchService.getDispatchQueue() → List<DispatchQueueItemDto>
 * → Template render table với dữ liệu
 *
 * FIELDS:
 * - recordId: ID phiếu mượn (dùng cho /approve/{id}, /reject/{id})
 * - studentName: Tên sinh viên (from UserProfile)
 * - sessionDate: Ngày buổi cố vấn
 * - equipmentSummary: List["Oscilloscope x 2", "Multimeter x 1"]
 * - status: PENDING, APPROVED, BORROWED, RETURNED
 * - lecturerNote: Ghi chú từ giảng viên
 * - adminNote: Ghi chú từ Admin
 *
 * ACTION: Admin nhấn "Phê duyệt" / "Từ chối"
 */
@Getter
@Setter
@NoArgsConstructor
public class DispatchQueueItemDto {

    private Long recordId;
    private String studentName;
    private LocalDate sessionDate;
    private List<String> equipmentSummary;
    private String status;
    private String lecturerNote;
    private String adminNote;
}

package com.rikkei.salsp.dto.student;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO đại diện cho một bản ghi học tập (academic session record).
 *
 * MỤC ĐÍCH: Hiển thị chi tiết 1 buổi cố vấn trên trang lịch sử sinh viên.
 *
 * USAGE:
 * Student → /student/history/sessions → List<AcademicRecordDto>
 * → Render table: ngày, giảng viên, điểm, feedback, thiết bị mượn v.v.
 *
 * FIELDS:
 * - sessionId, sessionDate, startTime, endTime: Thông tin lịch
 * - status: PENDING, CONFIRMED, COMPLETED, REJECTED, CANCELLED
 * - lecturerName, departmentName: Info giảng viên
 * - score, feedback: Kết quả đánh giá (nếu COMPLETED)
 * - note: Ghi chú từ sinh viên
 * - rejectionReason: Lý do từ chối (nếu REJECTED)
 * - equipments: List thiết bị mượn cho buổi này
 * - cancellable: UI flag (nút "Hủy lịch" nếu true)
 *
 * SOURCE: Native query join 6 bảng (session+user+lecturer+department+evaluation+borrowing)
 */
@Getter
@Setter
@NoArgsConstructor
public class AcademicRecordDto {

    private Long sessionId;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String lecturerName;
    private String departmentName;
    private Integer score;
    private String feedback;
    private String note;
    private String rejectionReason;
    private List<BorrowedEquipmentDto> equipments = new ArrayList<>();
    private boolean cancellable;
}

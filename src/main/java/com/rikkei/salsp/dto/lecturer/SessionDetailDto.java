package com.rikkei.salsp.dto.lecturer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import com.rikkei.salsp.dto.common.EquipmentRequestDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO chi tiết buổi cố vấn — dùng cho trang đánh giá của giảng viên (evaluation-form).
 * Chứa thông tin sinh viên, kết quả đánh giá, trạng thái mượn thiết bị v.v.
 */
@Getter
@Setter
@NoArgsConstructor
public class SessionDetailDto {

    private Long sessionId;
    private String studentName;
    private String studentEmail;
    private String studentPhone;
    private String departmentName;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String note;
    private String status;
    private String rejectionReason;
    private String borrowStatus;
    private String lecturerNote;
    private String adminNote;
    private List<EquipmentRequestDto> requestedEquipments = new ArrayList<>();
    /* Điểm đánh giá (null nếu chưa được đánh giá) */
    private Integer score;
    /* Nhận xét của giảng viên (null nếu chưa có) */
    private String feedback;
    private String studentAvatarUrl;
    /* Cho phép giảng viên hủy/từ chối nếu còn > 24h so với giờ hẹn */
    private boolean cancellable;
}


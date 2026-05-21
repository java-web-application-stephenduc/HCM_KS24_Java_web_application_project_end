package com.rikkei.salsp.dto.lecturer;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO tóm tắt buổi cố vấn — dùng để hiển thị danh sách trong các tab
 * "Lịch hẹn chờ xử lý" và "Lịch sử" trên giao diện giảng viên.
 */
@Getter
@Setter
@NoArgsConstructor
public class SessionSummaryDto {

    private Long sessionId;
    private String studentName;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String note;
}


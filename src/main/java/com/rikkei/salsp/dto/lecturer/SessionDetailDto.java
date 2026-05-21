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
 * Lớp `SessionDetailDto` thuộc hệ thống Smart Academic Lab Support Platform
 * (SALSP).
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
    private Integer score;
    private String feedback;
    private String studentAvatarUrl;
    private boolean cancellable;
}


package com.rikkei.salsp.dto.lecturer;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `SessionDetailDto` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class SessionDetailDto {

    private Long sessionId;
    private String studentName;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String note;
}


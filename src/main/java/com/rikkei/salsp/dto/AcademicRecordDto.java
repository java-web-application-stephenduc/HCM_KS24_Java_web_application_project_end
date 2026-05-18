package com.rikkei.salsp.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private List<BorrowedEquipmentDto> equipments = new ArrayList<>();
    private boolean cancellable;
}



package com.rikkei.salsp.dto.lecturer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `LecturerSummaryDto` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class LecturerSummaryDto {

    private Long id;
    private String fullName;
    private String title;
    private String departmentName;
    private Long departmentId;
    private String bio;
}


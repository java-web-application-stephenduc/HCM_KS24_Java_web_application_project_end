package com.rikkei.salsp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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


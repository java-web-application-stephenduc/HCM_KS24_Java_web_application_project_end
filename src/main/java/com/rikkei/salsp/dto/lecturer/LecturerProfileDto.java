package com.rikkei.salsp.dto.lecturer;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `LecturerProfileDto` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class LecturerProfileDto {

    @NotBlank
    private String fullName;

    private String phone;

    private Long departmentId;

    private String departmentName;

    private String title;

    private String bio;
}



package com.rikkei.salsp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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



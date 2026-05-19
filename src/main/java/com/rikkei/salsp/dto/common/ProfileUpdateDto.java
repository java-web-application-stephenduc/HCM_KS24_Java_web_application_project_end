package com.rikkei.salsp.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `ProfileUpdateDto` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class ProfileUpdateDto {

    @NotBlank
    private String fullName;

    private String phone;
}


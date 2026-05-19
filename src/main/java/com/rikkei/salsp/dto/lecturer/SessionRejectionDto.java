package com.rikkei.salsp.dto.lecturer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp `SessionRejectionDto` thuộc hệ thống Smart Academic Lab Support Platform
 * (SALSP).
 */
@Getter
@Setter
@NoArgsConstructor
public class SessionRejectionDto {

    @NotNull
    private Long sessionId;

    @NotBlank
    private String rejectionReason;
}

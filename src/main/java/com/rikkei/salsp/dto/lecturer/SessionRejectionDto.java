package com.rikkei.salsp.dto.lecturer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO form nhập — giảng viên từ chối buổi hẹn kèm lý do bắt buộc.
 * Cả hai trường đều được validate: sessionId không null, rejectionReason không rỗng.
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

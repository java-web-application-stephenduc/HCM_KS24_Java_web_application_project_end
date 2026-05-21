package com.rikkei.salsp.dto.lecturer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO form gửi lên khi giảng viên đánh giá buổi cố vấn.
 * score bắt buộc, thang 0-10 (@Min/@Max); feedback dạng text tùy chọn.
 */
@Getter
@Setter
@NoArgsConstructor
public class EvaluationFormDto {

    @NotNull
    private Long sessionId;

    @NotNull
    @Min(0)
    @Max(10)
    private Integer score;

    private String feedback;
}

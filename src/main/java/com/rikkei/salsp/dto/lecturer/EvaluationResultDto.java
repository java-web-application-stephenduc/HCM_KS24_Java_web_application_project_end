package com.rikkei.salsp.dto.lecturer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO phản hồi sau khi lưu đánh giá thành công — trả về sessionId vừa đánh giá.
 */
@Getter
@Setter
@NoArgsConstructor
public class EvaluationResultDto {
    private Long sessionId;
}


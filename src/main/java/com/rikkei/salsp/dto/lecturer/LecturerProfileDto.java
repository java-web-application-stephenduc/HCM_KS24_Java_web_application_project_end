package com.rikkei.salsp.dto.lecturer;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO dùng để cập nhật thông tin hồ sơ giảng viên (gửi từ form chỉnh sửa).
 * fullName bắt buộc (@NotBlank), các trường còn lại tùy chọn.
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



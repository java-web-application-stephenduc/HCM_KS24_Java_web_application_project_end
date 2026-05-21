package com.rikkei.salsp.dto.lecturer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO tóm tắt thông tin giảng viên — dùng cho dropdown chọn giảng viên
 * hoặc hiển thị danh sách giảng viên trong booking form.
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


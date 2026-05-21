package com.rikkei.salsp.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO nhận dữ liệu từ form cập nhật hồ sơ (Edit Profile).
 *
 * MỤC ĐÍCH: Update thông tin cá nhân người dùng.
 *
 * USAGE:
 * User → /profile/edit → Form có fullName, phone
 * → POST /profile/update → Controller parse ProfileUpdateDto
 * → Validate → ProfileService.updateProfile(userId, dto)
 * → Persist thành UserProfile entity
 *
 * VALIDATION:
 * - fullName: @NotBlank (bắt buộc, không được trống)
 * - phone: Optional (có thể bỏ trống)
 *
 * FIELDS:
 * - fullName: Tên đầy đủ (max 150 ký tự)
 * - phone: Số điện thoại (max 20 ký tự)
 */
@Getter
@Setter
@NoArgsConstructor
public class ProfileUpdateDto {

    @NotBlank
    private String fullName;

    private String phone;
}


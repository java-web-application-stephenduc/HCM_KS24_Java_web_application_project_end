package com.rikkei.salsp.dto.student;

import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO đại diện cho một khung giờ (time slot) cố vấn.
 *
 * MỤC ĐÍCH: Gửi danh sách khung giờ trống/bận về client (AJAX response).
 *
 * USAGE:
 * Bước 2 (chọn khung giờ):
 * - Client gọi AJAX: GET /student/booking/slots?lecturerId=5&date=2025-05-25
 * - Backend: BookingService.getAvailableSlots() → check conflict → return List<SlotDto>
 * - Response JSON: [
 *     {startTime: "08:00", endTime: "09:00", available: true},
 *     {startTime: "09:00", endTime: "10:00", available: false},  ← trùng lịch
 *     ...
 *   ]
 * - UI: Vô hiệu hóa (disable) nút booking cho slot available=false
 *
 * FIELDS:
 * - startTime: Giờ bắt đầu (ví dụ: 08:00)
 * - endTime: Giờ kết thúc (ví dụ: 09:00)
 * - available: true = có thể đặt, false = đã có buổi khác
 *
 * TOTAL SLOTS: 8 cố định (08:00-09:00, 09:00-10:00, ..., 16:00-17:00)
 */
@Getter
@Setter
@NoArgsConstructor
public class SlotDto {

    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
}


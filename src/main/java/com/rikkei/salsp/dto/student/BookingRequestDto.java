package com.rikkei.salsp.dto.student;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * DTO nhận dữ liệu từ form đặt lịch cố vấn (sinh viên).
 *
 * MỤC ĐÍCH: Bind data từ form HTML 3-bước xác nhận booking.
 *
 * VALIDATION:
 * - lecturerId: @NotNull (phải chọn giảng viên)
 * - sessionDate: @NotNull + @DateTimeFormat (ngày hợp lệ)
 * - startTime, endTime: @NotNull + @DateTimeFormat (giờ hợp lệ)
 * - note: Optional (ghi chú tùy ý)
 *
 * FLOW:
 * Bước 1: Chọn khoa + giảng viên
 * Bước 2: Chọn ngày + khung giờ (gợi ý từ BookingService.getAvailableSlots)
 * Bước 3: Nhập ghi chú + Xác nhận → POST /student/booking/create
 * → StudentBookingController.create() → BookingService.createBooking(dto, email)
 *
 * SERVER-SIDE CHECKS (ở BookingService):
 * - startTime < endTime (logic check)
 * - Không ở quá khứ (time check)
 * - Không xung đột giảng viên (conflict check)
 *
 * @DateTimeFormat: Chuyển string HTML input → LocalDate/LocalTime
 */
@Getter
@Setter
@NoArgsConstructor
public class BookingRequestDto {

    @NotNull
    private Long lecturerId;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate sessionDate;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime startTime;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime endTime;

    private String note;
}


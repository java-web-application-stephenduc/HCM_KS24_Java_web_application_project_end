package com.rikkei.salsp.controller;

import com.rikkei.salsp.dto.BookingRequestDto;
import com.rikkei.salsp.dto.LecturerSummaryDto;
import com.rikkei.salsp.dto.SlotDto;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.service.BookingService;
import com.rikkei.salsp.service.CancellationService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/booking")
public class StudentBookingController {

    private final BookingService bookingService;
    private final CancellationService cancellationService;

    /**
     * Hiển thị bước 1: Chọn khoa/phòng ban.
     * 
     * @param model Model chứa danh sách phòng ban
     * @return Template chọn phòng ban
     */
    @GetMapping("/step1")
    public String step1(Model model) {
        model.addAttribute("departments", bookingService.getDepartments());
        return "student/booking/step1-department";
    }

    /**
     * Hiển thị bước 2: Chọn giảng viên dựa trên khoa đã chọn.
     * 
     * @param departmentId ID khoa/phòng ban
     * @param model Model chứa danh sách giảng viên
     * @return Template chọn giảng viên
     */
    @GetMapping("/step2")
    public String step2(@RequestParam(required = false) Long departmentId,
                        Model model,
                        RedirectAttributes flash) {
        if (departmentId == null) {
            flash.addFlashAttribute("error", "Vui lòng chọn khoa trước khi tiếp tục.");
            return "redirect:/student/booking/step1";
        }
        model.addAttribute("lecturers", bookingService.getLecturersByDepartment(departmentId));
        model.addAttribute("departmentId", departmentId);
        return "student/booking/step2-lecturer";
    }

    /**
     * Hiển thị bước 3: Chọn thời gian và điền form đặt lịch.
     * 
     * @param lecturerId ID giảng viên
     * @param model Model khởi tạo form đặt lịch
     * @return Template chọn thời gian
     */
    @GetMapping("/step3")
    public String step3(@RequestParam(required = false) Long lecturerId,
                        Model model,
                        RedirectAttributes flash) {
        if (lecturerId == null) {
            flash.addFlashAttribute("error", "Vui lòng chọn giảng viên trước khi tiếp tục.");
            return "redirect:/student/booking/step1";
        }
        BookingRequestDto dto = new BookingRequestDto();
        dto.setLecturerId(lecturerId);
        model.addAttribute("booking", dto);
        LecturerSummaryDto lecturer = bookingService.getLecturerSummary(lecturerId);
        model.addAttribute("lecturer", lecturer);
        model.addAttribute("departmentId", lecturer.getDepartmentId());
        return "student/booking/step3-slot";
    }

    /**
     * API lấy danh sách các khung giờ trống của giảng viên trong một ngày.
     * 
     * @param lecturerId ID giảng viên
     * @param date Ngày muốn xem lịch
     * @return Danh sách khung giờ trống định dạng JSON
     */
    @GetMapping("/available-slots")
    @ResponseBody
    public List<SlotDto> getAvailableSlots(@RequestParam Long lecturerId, @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        return bookingService.getAvailableSlots(lecturerId, localDate);
    }

    /**
     * Xử lý lưu lịch đặt cố vấn học tập.
     * 
     * @param dto Dữ liệu form đặt lịch
     * @param errors Kết quả validate
     * @param authentication Xác thực sinh viên
     * @param flash Chứa thông báo kết quả
     * @param model Model render lỗi nếu có
     * @return Chuyển hướng về trang thành công hoặc trả lại form nếu lỗi
     */
    @PostMapping("/confirm")
    public String confirm(@Valid @ModelAttribute("booking") BookingRequestDto dto,
                          BindingResult errors,
                          Authentication authentication,
                          RedirectAttributes flash,
                          Model model) {
        if (errors.hasErrors()) {
            LecturerSummaryDto lecturer = bookingService.getLecturerSummary(dto.getLecturerId());
            model.addAttribute("lecturer", lecturer);
            model.addAttribute("departmentId", lecturer.getDepartmentId());
            return "student/booking/step3-slot";
        }
        try {
            bookingService.createBooking(dto, authentication.getName());
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            LecturerSummaryDto lecturer = bookingService.getLecturerSummary(dto.getLecturerId());
            model.addAttribute("lecturer", lecturer);
            model.addAttribute("departmentId", lecturer.getDepartmentId());
            return "student/booking/step3-slot";
        }
        flash.addFlashAttribute("success", "Đặt lịch thành công");
        return "redirect:/student/booking/success";
    }

    /**
     * Hiển thị trang thông báo đặt lịch thành công.
     * 
     * @return Template đặt lịch thành công
     */
    @GetMapping("/success")
    public String success() {
        return "student/booking/success";
    }

    /**
     * Xử lý hủy lịch hẹn đã đặt.
     * 
     * @param id ID của buổi cố vấn
     * @param authentication Xác thực sinh viên
     * @param flash Chứa thông báo kết quả
     * @return Chuyển hướng về trang lịch sử
     */
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, Authentication authentication, RedirectAttributes flash) {
        cancellationService.cancelSession(id, authentication.getName());
        flash.addFlashAttribute("success", "Đã hủy lịch");
        return "redirect:/student/history";
    }
}


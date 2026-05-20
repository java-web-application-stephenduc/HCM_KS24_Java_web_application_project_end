package com.rikkei.salsp.controller.student;

import com.rikkei.salsp.dto.common.EquipmentItemDto;
import com.rikkei.salsp.dto.student.StudentBorrowFormDto;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.service.common.ProfileService;
import com.rikkei.salsp.service.student.StudentHistoryService;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller hiển thị lịch sử cố vấn học tập và quản lý các yêu cầu mượn thiết
 * bị của sinh viên.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/student/history")
public class StudentHistoryController {

    private final StudentHistoryService historyService;
    private final ProfileService profileService;

    /**
     * Hiển thị danh sách.
     * 
     * @param authentication Tham số đầu vào authentication
     * @param model          Tham số đầu vào model
     * 
     * @return Kết quả trả về của phương thức
     */
    @GetMapping
    public String list(Authentication authentication, Model model) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        Long studentId = profileService.findUserByEmail(authentication.getName()).getId();
        model.addAttribute("records", historyService.getAcademicHistory(studentId));
        model.addAttribute("borrowedEquipments", historyService.getBorrowedEquipmentHistory(studentId));
        return "student/history/list";
    }

    /**
     * Phương thức xử lý nghiệp vụ detail.
     * 
     * @param id             Tham số đầu vào id
     * @param authentication Tham số đầu vào authentication
     * @param model          Tham số đầu vào model
     * 
     * @return Kết quả trả về của phương thức
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication authentication, Model model) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        Long studentId = profileService.findUserByEmail(authentication.getName()).getId();
        try {
            model.addAttribute("record", historyService.getAcademicRecordByIdForStudent(id, studentId));
        } catch (ResourceNotFoundException e) {
            model.addAttribute("error", "Không tìm thấy buổi tư vấn.");
            model.addAttribute("records", historyService.getAcademicHistory(studentId));
            model.addAttribute("borrowedEquipments", historyService.getBorrowedEquipmentHistory(studentId));
            return "student/history/list";
        }
        return "student/history/detail";
    }

    /**
     * Hiển thị giao diện.
     * 
     * @param id             Tham số đầu vào id
     * @param authentication Tham số đầu vào authentication
     * @param model          Tham số đầu vào model
     * @param flash          Tham số đầu vào flash
     * 
     * @return Kết quả trả về của phương thức
     */
    @GetMapping("/{id}/borrow")
    public String showBorrowForm(@PathVariable Long id, Authentication authentication, Model model,
            RedirectAttributes flash) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        try {
            Long studentId = profileService.findUserByEmail(authentication.getName()).getId();
            var record = historyService.getAcademicRecordByIdForStudent(id, studentId);
            if ("CANCELLED".equals(record.getStatus())
                    || "REJECTED".equals(record.getStatus())
                    || "CANCELED_BY_STUDENT".equals(record.getStatus())) {
                flash.addFlashAttribute("error", "Không thể mượn thiết bị cho ca tư vấn đã hủy.");
                return "redirect:/student/history/" + id;
            }
            // Check if borrowing record already exists
            if (record.getEquipments() != null && !record.getEquipments().isEmpty()) {
                flash.addFlashAttribute("error", "Buổi tư vấn này đã có phiếu mượn thiết bị.");
                return "redirect:/student/history/" + id;
            }

            StudentBorrowFormDto form = new StudentBorrowFormDto();
            form.setSessionId(id);

            var equipments = historyService.getAvailableEquipment();
            for (var eq : equipments) {
                EquipmentItemDto item = new EquipmentItemDto();
                item.setEquipmentId(eq.getId());
                item.setQuantity(0);
                form.getEquipmentItems().add(item);
            }

            model.addAttribute("borrowForm", form);
            model.addAttribute("record", record);
            model.addAttribute("equipments", equipments);
        } catch (ResourceNotFoundException e) {
            flash.addFlashAttribute("error", "Không tìm thấy buổi tư vấn.");
            return "redirect:/student/history";
        }
        return "student/history/borrow-form";
    }

    /**
     * Xử lý gửi form.
     * 
     * @param id                       Tham số đầu vào id
     * @param ModelAttributeborrowForm Tham số đầu vào ModelAttributeborrowForm
     * 
     * @return Kết quả trả về của phương thức
     */
    @PostMapping("/{id}/borrow")
    public String submitBorrowForm(@PathVariable Long id,
            @ModelAttribute("borrowForm") StudentBorrowFormDto dto,
            Authentication authentication,
            Model model,
            RedirectAttributes flash) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        try {
            historyService.createBorrowRequest(id, dto.getEquipmentItems(), authentication.getName());
            flash.addFlashAttribute("success", "Đã gửi yêu cầu mượn thiết bị thành công, vui lòng chờ Admin xuất kho!");
        } catch (BusinessException | ResourceNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            Long studentId = profileService.findUserByEmail(authentication.getName()).getId();
            var record = historyService.getAcademicRecordByIdForStudent(id, studentId);
            var equipments = historyService.getAvailableEquipment();
            model.addAttribute("borrowForm", dto);
            model.addAttribute("record", record);
            model.addAttribute("equipments", equipments);
            return "student/history/borrow-form";
        }
        return "redirect:/student/history/" + id;
    }
}

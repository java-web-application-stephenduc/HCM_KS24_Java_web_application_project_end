package com.rikkei.salsp.controller.lecturer;

import com.rikkei.salsp.dto.lecturer.BorrowApprovalDto;
import com.rikkei.salsp.dto.lecturer.EvaluationFormDto;
import com.rikkei.salsp.dto.lecturer.SessionRejectionDto;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.service.admin.EquipmentService;
import com.rikkei.salsp.service.lecturer.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý các hoạt động của giảng viên bao gồm xem hàng đợi hướng
 * dẫn và đánh giá kết quả buổi cố vấn.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/lecturer")
public class LecturerController {

    private final EvaluationService evaluationService;
    private final EquipmentService equipmentService;

    /**
     * Hiển thị danh sách các buổi cố vấn đang chờ giảng viên xử lý.
     * 
     * @param authentication Thông tin xác thực giảng viên
     * @param model          Model truyền danh sách buổi hẹn
     * @return Template hàng đợi cố vấn
     */
    @GetMapping("/queue")
    public String queue(Authentication authentication, Model model) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("sessions", evaluationService.getPendingSessions(authentication.getName()));
        return "lecturer/queue";
    }

    /**
     * Hiển thị form đánh giá sau khi hoàn thành buổi cố vấn.
     * 
     * @param id             ID của buổi cố vấn
     * @param authentication Thông tin xác thực giảng viên
     * @param model          Model chứa dữ liệu session và danh sách thiết bị
     * @return Template form đánh giá
     */
    @GetMapping("/session/{id}/evaluate")
    public String evaluateForm(@PathVariable Long id, Authentication authentication, Model model) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("sessionDetail", evaluationService.getSessionDetail(id, authentication.getName()));
        model.addAttribute("equipmentList", equipmentService.findAll());
        EvaluationFormDto form = new EvaluationFormDto();
        form.setSessionId(id);
        model.addAttribute("evaluation", form);
        BorrowApprovalDto borrowForm = new BorrowApprovalDto();
        borrowForm.setSessionId(id);
        model.addAttribute("borrowApproval", borrowForm);
        SessionRejectionDto rejectForm = new SessionRejectionDto();
        rejectForm.setSessionId(id);
        model.addAttribute("sessionReject", rejectForm);
        return "lecturer/evaluation-form";
    }

    /**
     * Xử lý nộp đánh giá và hoàn thành buổi cố vấn.
     * 
     * @param id             ID buổi cố vấn
     * @param evaluation     Dữ liệu đánh giá từ form
     * @param errors         Kết quả validate
     * @param authentication Xác thực giảng viên
     * @param model          Model render lại form nếu có lỗi
     * @param flash          Chứa thông báo kết quả
     * @return Chuyển hướng lại danh sách chờ xử lý
     */
    /**
     * Xử lý gửi form đánh giá kết quả.
     * 
     * @param id     Tham số đầu vào id
     * @param @Valid Tham số đầu vào @Valid
     * 
     * @return Kết quả trả về của phương thức
     */
    @PostMapping("/session/{id}/evaluate")
    public String submitEvaluation(@PathVariable Long id,
            @Valid @ModelAttribute("evaluation") EvaluationFormDto evaluation,
            BindingResult errors,
            Authentication authentication,
            Model model,
            RedirectAttributes flash) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        if (errors.hasErrors()) {
            model.addAttribute("sessionDetail", evaluationService.getSessionDetail(id, authentication.getName()));
            model.addAttribute("equipmentList", equipmentService.findAll());
            BorrowApprovalDto borrowForm = new BorrowApprovalDto();
            borrowForm.setSessionId(id);
            model.addAttribute("borrowApproval", borrowForm);
            SessionRejectionDto rejectForm = new SessionRejectionDto();
            rejectForm.setSessionId(id);
            model.addAttribute("sessionReject", rejectForm);
            return "lecturer/evaluation-form";
        }
        evaluation.setSessionId(id);
        evaluationService.completeSession(evaluation, authentication.getName());
        flash.addFlashAttribute("success", "Đã lưu đánh giá");
        return "redirect:/lecturer/queue";
    }

    /**
     * Giảng viên duyệt yêu cầu mượn thiết bị.
     */
    @PostMapping("/session/{id}/borrow/approve")
    public String approveBorrow(@PathVariable Long id,
            @ModelAttribute("borrowApproval") BorrowApprovalDto dto,
            Authentication authentication,
            RedirectAttributes flash) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        try {
            dto.setSessionId(id);
            evaluationService.approveBorrowRequest(id, dto, authentication.getName());
            flash.addFlashAttribute("success", "Đã duyệt yêu cầu mượn thiết bị");
        } catch (BusinessException | ResourceNotFoundException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lecturer/session/" + id + "/evaluate";
    }

    /**
     * Giảng viên từ chối yêu cầu mượn thiết bị.
     */
    @PostMapping("/session/{id}/borrow/reject")
    public String rejectBorrow(@PathVariable Long id,
            @ModelAttribute("borrowApproval") BorrowApprovalDto dto,
            Authentication authentication,
            RedirectAttributes flash) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        try {
            dto.setSessionId(id);
            evaluationService.rejectBorrowRequest(id, dto, authentication.getName());
            flash.addFlashAttribute("success", "Đã từ chối yêu cầu mượn thiết bị");
        } catch (BusinessException | ResourceNotFoundException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lecturer/session/" + id + "/evaluate";
    }

    /**
     * Giảng viên từ chối buổi hẹn.
     */
    @PostMapping("/session/{id}/reject")
    public String rejectSession(@PathVariable Long id,
            @Valid @ModelAttribute("sessionReject") SessionRejectionDto dto,
            BindingResult errors,
            Authentication authentication,
            Model model,
            RedirectAttributes flash) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        if (errors.hasErrors()) {
            model.addAttribute("sessionDetail", evaluationService.getSessionDetail(id, authentication.getName()));
            model.addAttribute("equipmentList", equipmentService.findAll());
            EvaluationFormDto form = new EvaluationFormDto();
            form.setSessionId(id);
            model.addAttribute("evaluation", form);
            BorrowApprovalDto borrowForm = new BorrowApprovalDto();
            borrowForm.setSessionId(id);
            model.addAttribute("borrowApproval", borrowForm);
            dto.setSessionId(id);
            model.addAttribute("sessionReject", dto);
            return "lecturer/evaluation-form";
        }
        try {
            dto.setSessionId(id);
            evaluationService.rejectSession(id, dto, authentication.getName());
            flash.addFlashAttribute("success", "Đã từ chối buổi hẹn");
        } catch (BusinessException | ResourceNotFoundException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/lecturer/session/" + id + "/evaluate";
        }
        return "redirect:/lecturer/queue";
    }
}

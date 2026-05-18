package com.rikkei.salsp.controller;

import com.rikkei.salsp.dto.EvaluationFormDto;
import com.rikkei.salsp.service.EquipmentService;
import com.rikkei.salsp.service.EvaluationService;
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
     * @param model Model truyền danh sách buổi hẹn
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
     * @param id ID của buổi cố vấn
     * @param authentication Thông tin xác thực giảng viên
     * @param model Model chứa dữ liệu session và danh sách thiết bị
     * @return Template form đánh giá
     */
    @GetMapping("/session/{id}/evaluate")
    public String evaluateForm(@PathVariable Long id, Authentication authentication, Model model) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("session", evaluationService.getSessionDetail(id, authentication.getName()));
        model.addAttribute("equipmentList", equipmentService.findAll());
        EvaluationFormDto form = new EvaluationFormDto();
        form.setSessionId(id);
        model.addAttribute("evaluation", form);
        return "lecturer/evaluation-form";
    }

    /**
     * Xử lý nộp đánh giá và hoàn thành buổi cố vấn.
     * 
     * @param id ID buổi cố vấn
     * @param evaluation Dữ liệu đánh giá từ form
     * @param errors Kết quả validate
     * @param authentication Xác thực giảng viên
     * @param model Model render lại form nếu có lỗi
     * @param flash Chứa thông báo kết quả
     * @return Chuyển hướng lại danh sách chờ xử lý
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
            model.addAttribute("session", evaluationService.getSessionDetail(id, authentication.getName()));
            model.addAttribute("equipmentList", equipmentService.findAll());
            return "lecturer/evaluation-form";
        }
        evaluation.setSessionId(id);
        evaluationService.completeSession(evaluation, authentication.getName());
        flash.addFlashAttribute("success", "Đã lưu đánh giá");
        return "redirect:/lecturer/queue";
    }
}


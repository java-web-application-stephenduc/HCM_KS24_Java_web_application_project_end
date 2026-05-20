package com.rikkei.salsp.controller.admin;

import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.service.admin.DispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý quy trình cấp phát và hoàn trả thiết bị phòng Lab của
 * Admin.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/dispatch")
public class AdminDispatchController {

    private final DispatchService dispatchService;

    /**
     * Hiển thị danh sách các yêu cầu cấp phát thiết bị đang chờ xử lý.
     * 
     * @param model Model để truyền danh sách thiết bị
     * @return Template danh sách chờ cấp phát
     */
    @GetMapping
    public String queue(Model model) {
        model.addAttribute("records", dispatchService.getPendingDispatchQueue());
        return "admin/dispatch/queue";
    }

    /**
     * Xem chi tiết yêu cầu cấp phát dựa trên ID.
     * 
     * @param id    ID của yêu cầu mượn
     * @param model Model để truyền thông tin chi tiết
     * @return Template chi tiết phiếu mượn
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("record", dispatchService.getDispatchDetail(id));
        return "admin/dispatch/detail";
    }

    /**
     * Xác nhận cấp phát thiết bị cho sinh viên.
     * 
     * @param id    ID của phiếu mượn
     * @param flash Chứa thông báo kết quả
     * @return Chuyển hướng lại danh sách hàng đợi
     */
    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id, String adminNote, RedirectAttributes flash) {
        try {
            dispatchService.dispatchEquipment(id, adminNote);
            flash.addFlashAttribute("success", "Đã xác nhận cấp phát");
        } catch (BusinessException | ResourceNotFoundException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dispatch";
    }

    /**
     * Từ chối yêu cầu mượn thiết bị.
     *
     * @param id        ID của phiếu mượn
     * @param adminNote Ghi chú từ admin
     * @param flash     Chứa thông báo kết quả
     * @return Chuyển hướng lại danh sách hàng đợi
     */
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, String adminNote, RedirectAttributes flash) {
        try {
            dispatchService.rejectDispatch(id, adminNote);
            flash.addFlashAttribute("success", "Đã từ chối yêu cầu mượn thiết bị");
        } catch (BusinessException | ResourceNotFoundException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dispatch";
    }

    /**
     * Xác nhận hoàn trả thiết bị về kho.
     * 
     * @param id    ID của phiếu mượn
     * @param flash Chứa thông báo kết quả
     * @return Chuyển hướng lại danh sách hàng đợi
     */
    @PostMapping("/{id}/return")
    public String returnEquipment(@PathVariable Long id, RedirectAttributes flash) {
        try {
            dispatchService.returnEquipment(id);
            flash.addFlashAttribute("success", "Đã hoàn trả thiết bị");
        } catch (BusinessException | ResourceNotFoundException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dispatch";
    }
}

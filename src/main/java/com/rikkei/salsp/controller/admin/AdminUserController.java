package com.rikkei.salsp.controller.admin;

import com.rikkei.salsp.entity.equipment.BorrowingRecord;
import com.rikkei.salsp.entity.session.MentoringSession;
import com.rikkei.salsp.entity.user.User;
import com.rikkei.salsp.entity.user.UserRole;
import com.rikkei.salsp.repository.equipment.BorrowingRecordRepository;
import com.rikkei.salsp.repository.session.MentoringSessionRepository;
import com.rikkei.salsp.repository.user.UserRepository;
import com.rikkei.salsp.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final UserRepository userRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final MentoringSessionRepository mentoringSessionRepository;

    private static final int PAGE_SIZE = 8;

    /**
     * Hiển thị giao diện Quản lý hệ thống 3 Tab (Người dùng, Mượn thiết bị, Lịch tư vấn).
     */
    @GetMapping("/admin/users")
    public String showSystemManagement(
            @RequestParam(value = "userPage", defaultValue = "0") int userPage,
            @RequestParam(value = "borrowPage", defaultValue = "0") int borrowPage,
            @RequestParam(value = "sessionPage", defaultValue = "0") int sessionPage,
            @RequestParam(value = "activeTab", defaultValue = "users") String activeTab,
            Model model) {

        // Tab 1: Quản lý người dùng (phân trang 8, sắp xếp theo ID giảm dần)
        Page<User> users = userRepository.findAll(
                PageRequest.of(userPage, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id")));
        
        // Tab 2: Quản lý mượn thiết bị (phân trang 8, sắp xếp theo ID giảm dần)
        Page<BorrowingRecord> borrows = borrowingRecordRepository.findAll(
                PageRequest.of(borrowPage, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id")));

        // Tab 3: Quản lý lịch tư vấn (phân trang 8, sắp xếp theo ID giảm dần)
        Page<MentoringSession> sessions = mentoringSessionRepository.findAll(
                PageRequest.of(sessionPage, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id")));

        model.addAttribute("users", users);
        model.addAttribute("borrows", borrows);
        model.addAttribute("sessions", sessions);
        
        model.addAttribute("userPage", userPage);
        model.addAttribute("borrowPage", borrowPage);
        model.addAttribute("sessionPage", sessionPage);
        
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("roles", UserRole.values());

        return "admin/users";
    }

    /**
     * Thay đổi vai trò của người dùng.
     */
    @PostMapping("/admin/users/{id}/role")
    public String changeUserRole(
            @PathVariable("id") Long id,
            @RequestParam("role") UserRole role,
            @RequestParam(value = "userPage", defaultValue = "0") int userPage,
            @RequestParam(value = "borrowPage", defaultValue = "0") int borrowPage,
            @RequestParam(value = "sessionPage", defaultValue = "0") int sessionPage,
            RedirectAttributes flash) {
        try {
            adminUserService.changeUserRole(id, role);
            flash.addFlashAttribute("success", "Cập nhật vai trò người dùng thành công");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Lỗi khi cập nhật vai trò: " + e.getMessage());
        }
        return String.format("redirect:/admin/users?userPage=%d&borrowPage=%d&sessionPage=%d&activeTab=users",
                userPage, borrowPage, sessionPage);
    }

    /**
     * Kích hoạt hoặc khóa tài khoản của người dùng.
     */
    @PostMapping("/admin/users/{id}/toggle-status")
    public String toggleUserStatus(
            @PathVariable("id") Long id,
            @RequestParam(value = "userPage", defaultValue = "0") int userPage,
            @RequestParam(value = "borrowPage", defaultValue = "0") int borrowPage,
            @RequestParam(value = "sessionPage", defaultValue = "0") int sessionPage,
            RedirectAttributes flash) {
        try {
            adminUserService.toggleUserStatus(id);
            flash.addFlashAttribute("success", "Thay đổi trạng thái tài khoản thành công");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Lỗi khi thay đổi trạng thái: " + e.getMessage());
        }
        return String.format("redirect:/admin/users?userPage=%d&borrowPage=%d&sessionPage=%d&activeTab=users",
                userPage, borrowPage, sessionPage);
    }
}

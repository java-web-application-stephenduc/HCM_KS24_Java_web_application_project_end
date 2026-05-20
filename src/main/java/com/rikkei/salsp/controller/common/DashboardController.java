package com.rikkei.salsp.controller.common;

import com.rikkei.salsp.service.admin.AdminDashboardService;
import com.rikkei.salsp.service.lecturer.LecturerDashboardService;
import com.rikkei.salsp.service.student.StudentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller điều phối trang chủ Dashboard tương ứng cho từng vai trò người dùng (Admin, Lecturer, Student).
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final StudentDashboardService dashboardService;
    private final LecturerDashboardService lecturerDashboardService;
    private final AdminDashboardService adminDashboardService;

    /**
     * Phương thức xử lý nghiệp vụ dashboardRedirect.
     * @param authentication Tham số đầu vào authentication

     * @return Kết quả trả về của phương thức
     */
    @GetMapping({"/", "/dashboard"})
    public String dashboardRedirect(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        }
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_LECTURER"))) {
            return "redirect:/lecturer/dashboard";
        }
        return "redirect:/student/dashboard";
    }

    /**
     * Phương thức xử lý nghiệp vụ studentDashboard.
     * @param authentication Tham số đầu vào authentication
     * @param model Tham số đầu vào model

     * @return Kết quả trả về của phương thức
     */
    @GetMapping("/student/dashboard")
    public String studentDashboard(Authentication authentication, Model model) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("dashboard", dashboardService.getDashboardData(authentication.getName()));
        return "student/dashboard";
    }

    /**
     * Phương thức xử lý nghiệp vụ lecturerDashboard.
     * @param authentication Tham số đầu vào authentication
     * @param model Tham số đầu vào model

     * @return Kết quả trả về của phương thức
     */
    @GetMapping("/lecturer/dashboard")
    public String lecturerDashboard(Authentication authentication, Model model) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("dashboard", lecturerDashboardService.getDashboardData(authentication.getName()));
        return "lecturer/dashboard";
    }

    /**
     * Phương thức xử lý nghiệp vụ adminDashboard.
     * @param authentication Tham số đầu vào authentication
     * @param model Tham số đầu vào model

     * @return Kết quả trả về của phương thức
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Authentication authentication, Model model) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("dashboard", adminDashboardService.getDashboardData());
        return "admin/dashboard";
    }
}

package com.rikkei.salsp.controller;

import com.rikkei.salsp.service.ProfileService;
import com.rikkei.salsp.service.StudentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ProfileService profileService;
    private final StudentDashboardService dashboardService;

    @GetMapping({"/", "/dashboard"})
    public String dashboardRedirect(Authentication authentication) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        }
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_LECTURER"))) {
            return "redirect:/lecturer/dashboard";
        }
        return "redirect:/student/dashboard";
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard(Authentication authentication, Model model) {
        Long studentId = profileService.findUserByEmail(authentication.getName()).getId();
        model.addAttribute("dashboard", dashboardService.getDashboardData(studentId));
        return "student/dashboard";
    }

    @GetMapping("/lecturer/dashboard")
    public String lecturerDashboard() {
        return "lecturer/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }
}

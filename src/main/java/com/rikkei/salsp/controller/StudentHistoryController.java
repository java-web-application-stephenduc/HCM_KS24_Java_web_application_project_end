package com.rikkei.salsp.controller;

import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.service.ProfileService;
import com.rikkei.salsp.service.StudentHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/history")
public class StudentHistoryController {

    private final StudentHistoryService historyService;
    private final ProfileService profileService;

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

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication authentication, Model model) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        Long studentId = profileService.findUserByEmail(authentication.getName()).getId();
        try {
            model.addAttribute("record", historyService.getAcademicRecordById(id));
        } catch (ResourceNotFoundException e) {
            model.addAttribute("error", "Không tìm thấy buổi tư vấn.");
            model.addAttribute("records", historyService.getAcademicHistory(studentId));
            model.addAttribute("borrowedEquipments", historyService.getBorrowedEquipmentHistory(studentId));
            return "student/history/list";
        }
        return "student/history/detail";
    }
}

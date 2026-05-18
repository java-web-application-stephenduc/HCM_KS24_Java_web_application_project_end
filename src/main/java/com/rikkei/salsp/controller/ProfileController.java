package com.rikkei.salsp.controller;

import com.rikkei.salsp.dto.LecturerProfileDto;
import com.rikkei.salsp.dto.ProfileUpdateDto;
import com.rikkei.salsp.entity.User;
import com.rikkei.salsp.entity.UserRole;
import com.rikkei.salsp.repository.DepartmentRepository;
import com.rikkei.salsp.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final DepartmentRepository departmentRepository;

    /**
     * Hiển thị thông tin hồ sơ cá nhân của người dùng.
     * Nếu là giảng viên sẽ hiển thị thêm thông tin chuyên môn.
     * 
     * @param authentication Thông tin xác thực người dùng
     * @param model Model truyền dữ liệu view
     * @return Template trang profile
     */
    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        User user = profileService.findUserByEmail(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("profile", profileService.getProfileByUserId(user.getId()));
        if (user.getRole() == UserRole.LECTURER) {
            model.addAttribute("lecturer", profileService.getLecturerProfile(user.getId()));
        }
        return "profile/view";
    }

    /**
     * Hiển thị form chỉnh sửa hồ sơ.
     * Tùy role mà sẽ hiển thị form riêng (giảng viên có thêm phòng ban, title).
     * 
     * @param authentication Thông tin xác thực
     * @param model Model truyền dữ liệu khởi tạo
     * @return Template form chỉnh sửa
     */
    @GetMapping("/profile/edit")
    public String editProfile(Authentication authentication, Model model) {
        User user = profileService.findUserByEmail(authentication.getName());
        model.addAttribute("user", user);

        if (user.getRole() == UserRole.LECTURER) {
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("profile", profileService.getLecturerProfile(user.getId()));
            return "profile/lecturer-edit";
        }

        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setFullName(profileService.getProfileByUserId(user.getId()).getFullName());
        dto.setPhone(profileService.getProfileByUserId(user.getId()).getPhone());
        model.addAttribute("profile", dto);
        return "profile/edit";
    }

    /**
     * Xử lý cập nhật thông tin hồ sơ người dùng.
     * 
     * @param authentication Xác thực
     * @param dto Dữ liệu cập nhật từ form
     * @param errors Kết quả validate
     * @param flash Thông báo kết quả
     * @param model Model render form nếu lỗi
     * @return Chuyển hướng về trang xem hồ sơ
     */
    @PostMapping("/profile/edit")
    public String updateProfile(Authentication authentication,
                                @Valid @ModelAttribute("profile") LecturerProfileDto dto,
                                BindingResult errors,
                                RedirectAttributes flash,
                                Model model) {
        User user = profileService.findUserByEmail(authentication.getName());
        if (errors.hasErrors()) {
            model.addAttribute("user", user);
            if (user.getRole() == UserRole.LECTURER) {
                model.addAttribute("departments", departmentRepository.findAll());
                return "profile/lecturer-edit";
            }
            return "profile/edit";
        }

        if (user.getRole() == UserRole.LECTURER) {
            profileService.updateLecturerProfile(user.getId(), dto);
        } else {
            ProfileUpdateDto updateDto = new ProfileUpdateDto();
            updateDto.setFullName(dto.getFullName());
            updateDto.setPhone(dto.getPhone());
            profileService.updateProfile(user.getId(), updateDto);
        }
        flash.addFlashAttribute("success", "Cập nhật thành công");
        return "redirect:/profile";
    }
}


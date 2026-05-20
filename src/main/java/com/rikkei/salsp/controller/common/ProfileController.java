package com.rikkei.salsp.controller.common;

import com.rikkei.salsp.dto.lecturer.LecturerProfileDto;
import com.rikkei.salsp.dto.common.ProfileUpdateDto;
import com.rikkei.salsp.entity.user.User;
import com.rikkei.salsp.entity.user.UserProfile;
import com.rikkei.salsp.entity.user.UserRole;
import com.rikkei.salsp.repository.common.DepartmentRepository;
import com.rikkei.salsp.service.common.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý thông tin cá nhân và cập nhật hồ sơ người dùng.
 */
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
        // Null guard cho authentication (Bug #16)
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
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
        // Null guard cho authentication (Bug #16)
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        User user = profileService.findUserByEmail(authentication.getName());
        model.addAttribute("user", user);

        if (user.getRole() == UserRole.LECTURER) {
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("profile", profileService.getLecturerProfile(user.getId()));
            return "profile/lecturer-edit";
        }

        // Bug #20: Lưu kết quả getProfileByUserId vào biến cục bộ, tránh gọi DB 2 lần.
        UserProfile userProfile = profileService.getProfileByUserId(user.getId());
        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setFullName(userProfile.getFullName());
        dto.setPhone(userProfile.getPhone());
        model.addAttribute("profile", dto);
        return "profile/edit";
    }

    /**
     * Xử lý cập nhật thông tin hồ sơ cho Sinh viên / Admin.
     * 
     * @param authentication Xác thực
     * @param dto Dữ liệu cập nhật sinh viên
     * @param errors Kết quả validate
     * @param flash Thông báo kết quả
     * @param model Model render form nếu lỗi
     * @return Chuyển hướng về trang xem hồ sơ
     */
    @PostMapping("/profile/edit/student")
    public String updateStudentProfile(Authentication authentication,
                                       @Valid @ModelAttribute("profile") ProfileUpdateDto dto,
                                       BindingResult errors,
                                       @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                       RedirectAttributes flash,
                                       Model model) {
        // Null guard cho authentication (Bug #16)
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        User user = profileService.findUserByEmail(authentication.getName());
        if (errors.hasErrors()) {
            model.addAttribute("user", user);
            return "profile/edit";
        }
        try {
            if (avatarFile != null && !avatarFile.isEmpty()) {
                profileService.updateAvatar(user.getId(), avatarFile);
            }
            profileService.updateProfile(user.getId(), dto);
            flash.addFlashAttribute("success", "Cập nhật hồ sơ thành công");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile/edit";
        }
        return "redirect:/profile";
    }

    /**
     * Xử lý cập nhật thông tin hồ sơ cho Giảng viên.
     * 
     * @param authentication Xác thực
     * @param dto Dữ liệu cập nhật giảng viên
     * @param errors Kết quả validate
     * @param flash Thông báo kết quả
     * @param model Model render form nếu lỗi
     * @return Chuyển hướng về trang xem hồ sơ
     */
    @PostMapping("/profile/edit/lecturer")
    public String updateLecturerProfile(Authentication authentication,
                                        @Valid @ModelAttribute("profile") LecturerProfileDto dto,
                                        BindingResult errors,
                                        @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                        RedirectAttributes flash,
                                        Model model) {
        // Null guard cho authentication (Bug #16)
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        User user = profileService.findUserByEmail(authentication.getName());
        if (errors.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("departments", departmentRepository.findAll());
            return "profile/lecturer-edit";
        }
        try {
            if (avatarFile != null && !avatarFile.isEmpty()) {
                profileService.updateAvatar(user.getId(), avatarFile);
            }
            profileService.updateLecturerProfile(user.getId(), dto);
            flash.addFlashAttribute("success", "Cập nhật hồ sơ thành công");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile/edit";
        }
        return "redirect:/profile";
    }

    /**
     * Thay đổi mật khẩu người dùng.
     */
    @PostMapping("/profile/change-password")
    public String changePassword(Authentication authentication,
                                 @RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 RedirectAttributes flash) {
        if (authentication == null || authentication.getName() == null) {
            return "redirect:/auth/login";
        }
        try {
            User user = profileService.findUserByEmail(authentication.getName());
            profileService.changePassword(user.getId(), oldPassword, newPassword, confirmPassword);
            flash.addFlashAttribute("success", "Đổi mật khẩu thành công");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile/edit";
        }
        return "redirect:/profile";
    }
}


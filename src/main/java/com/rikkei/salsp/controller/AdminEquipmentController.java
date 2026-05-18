package com.rikkei.salsp.controller;

import com.rikkei.salsp.dto.EquipmentDto;
import com.rikkei.salsp.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/equipment")
public class AdminEquipmentController {

    private final EquipmentService equipmentService;

    /**
     * Hiển thị danh sách thiết bị trong kho.
     * Có hỗ trợ tìm kiếm theo từ khóa.
     * 
     * @param keyword Từ khóa tìm kiếm (nếu có)
     * @param model Model để truyền danh sách thiết bị
     * @return Template danh sách thiết bị
     */
    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("equipments", equipmentService.search(keyword));
        model.addAttribute("keyword", keyword);
        return "admin/equipment/list";
    }

    /**
     * Hiển thị form thêm mới thiết bị.
     * 
     * @param model Model truyền data mặc định
     * @return Template form thiết bị
     */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("equipment", new EquipmentDto());
        model.addAttribute("mode", "create");
        return "admin/equipment/form";
    }

    /**
     * Xử lý lưu thiết bị mới vào cơ sở dữ liệu.
     * 
     * @param dto Dữ liệu thiết bị từ form
     * @param errors Kết quả validate
     * @param flash Chứa thông báo kết quả
     * @param model Model để render lại form nếu có lỗi
     * @return Chuyển hướng về danh sách nếu thành công
     */
    @PostMapping
    public String create(@Valid @ModelAttribute("equipment") EquipmentDto dto,
                         BindingResult errors,
                         RedirectAttributes flash,
                         Model model) {
        if (errors.hasErrors()) {
            model.addAttribute("mode", "create");
            return "admin/equipment/form";
        }
        equipmentService.create(dto);
        flash.addFlashAttribute("success", "Thêm thiết bị thành công");
        return "redirect:/admin/equipment";
    }

    /**
     * Hiển thị form chỉnh sửa thông tin thiết bị.
     * 
     * @param id ID của thiết bị cần sửa
     * @param model Model truyền dữ liệu thiết bị hiện tại
     * @return Template form thiết bị
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("equipment", equipmentService.findById(id));
        model.addAttribute("mode", "edit");
        return "admin/equipment/form";
    }

    /**
     * Cập nhật thông tin thiết bị.
     * 
     * @param id ID thiết bị
     * @param dto Dữ liệu cập nhật
     * @param errors Kết quả validate
     * @param flash Chứa thông báo kết quả
     * @param model Model để render lại form nếu có lỗi
     * @return Chuyển hướng về danh sách nếu thành công
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("equipment") EquipmentDto dto,
                         BindingResult errors,
                         RedirectAttributes flash,
                         Model model) {
        if (errors.hasErrors()) {
            model.addAttribute("mode", "edit");
            return "admin/equipment/form";
        }
        equipmentService.update(id, dto);
        flash.addFlashAttribute("success", "Cập nhật thiết bị thành công");
        return "redirect:/admin/equipment";
    }

    /**
     * Xem chi tiết một thiết bị.
     * 
     * @param id ID thiết bị
     * @param model Model truyền dữ liệu chi tiết
     * @return Template chi tiết thiết bị
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("equipment", equipmentService.findById(id));
        return "admin/equipment/detail";
    }

    /**
     * Xóa thiết bị khỏi hệ thống.
     * 
     * @param id ID thiết bị cần xóa
     * @param flash Chứa thông báo kết quả
     * @return Chuyển hướng về danh sách thiết bị
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        equipmentService.delete(id);
        flash.addFlashAttribute("success", "Đã xóa thiết bị");
        return "redirect:/admin/equipment";
    }
}


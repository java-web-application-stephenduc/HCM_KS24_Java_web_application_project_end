package com.rikkei.salsp.service.admin;

import com.rikkei.salsp.entity.common.Department;
import com.rikkei.salsp.entity.user.Lecturer;
import com.rikkei.salsp.entity.user.User;
import com.rikkei.salsp.entity.user.UserRole;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.repository.common.DepartmentRepository;
import com.rikkei.salsp.repository.user.LecturerRepository;
import com.rikkei.salsp.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;
    private final LecturerRepository lecturerRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Kích hoạt hoặc khóa tài khoản của người dùng.
     */
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    /**
     * Thay đổi vai trò người dùng (STUDENT, LECTURER, ADMIN).
     * Xử lý tạo/xóa thực thể Lecturer tương ứng.
     */
    public void changeUserRole(Long id, UserRole newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        
        UserRole oldRole = user.getRole();
        if (oldRole == newRole) {
            return;
        }

        user.setRole(newRole);
        userRepository.save(user);

        // Nếu vai trò mới là LECTURER, ta phải đảm bảo có thực thể Lecturer và khoa liên kết hợp lệ
        if (newRole == UserRole.LECTURER) {
            if (!lecturerRepository.findByUserId(id).isPresent()) {
                Department defaultDept = departmentRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new BusinessException("Không có khoa nào tồn tại trong hệ thống"));
                Lecturer lecturer = new Lecturer();
                lecturer.setUser(user);
                lecturer.setDepartment(defaultDept);
                lecturer.setTitle("Giảng viên");
                lecturer.setBio("Chưa cập nhật giới thiệu");
                lecturerRepository.save(lecturer);
            }
        } else {
            // Nếu chuyển đổi từ LECTURER sang vai trò khác, cần xóa thông tin Lecturer
            lecturerRepository.findByUserId(id).ifPresent(lecturerRepository::delete);
        }
    }
}

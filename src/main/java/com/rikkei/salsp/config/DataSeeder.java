package com.rikkei.salsp.config;

import com.rikkei.salsp.entity.Department;
import com.rikkei.salsp.entity.Equipment;
import com.rikkei.salsp.entity.Lecturer;
import com.rikkei.salsp.entity.User;
import com.rikkei.salsp.entity.UserProfile;
import com.rikkei.salsp.entity.UserRole;
import com.rikkei.salsp.repository.DepartmentRepository;
import com.rikkei.salsp.repository.EquipmentRepository;
import com.rikkei.salsp.repository.LecturerRepository;
import com.rikkei.salsp.repository.UserProfileRepository;
import com.rikkei.salsp.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final LecturerRepository lecturerRepository;
    private final DepartmentRepository departmentRepository;
    private final EquipmentRepository equipmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedDepartments();
        seedEquipments();
        seedUsers();
    }

    private void seedDepartments() {
        if (departmentRepository.count() == 0) {
            departmentRepository.save(createDepartment("Khoa Công nghệ thông tin", "CNTT"));
            departmentRepository.save(createDepartment("Khoa Kinh tế", "KT"));
            departmentRepository.save(createDepartment("Khoa Điện tử viễn thông", "DTVT"));
        }
    }

    private Department createDepartment(String name, String code) {
        Department d = new Department();
        d.setName(name);
        d.setCode(code);
        return d;
    }

    private void seedEquipments() {
        if (equipmentRepository.count() == 0) {
            equipmentRepository.save(createEquipment("Laptop Dell XPS 15", "Laptop cấu hình cao cho lập trình", "Máy tính", 5));
            equipmentRepository.save(createEquipment("Máy chiếu Sony", "Máy chiếu phục vụ thuyết trình đồ án", "Trình chiếu", 2));
            equipmentRepository.save(createEquipment("Bảng vẽ Wacom", "Bảng vẽ điện tử cho thiết kế", "Phụ kiện", 10));
            equipmentRepository.save(createEquipment("Arduino Uno R3", "Kit phát triển IoT", "Thiết bị nhúng", 20));
        }
    }

    private Equipment createEquipment(String name, String description, String category, int quantity) {
        Equipment eq = new Equipment();
        eq.setName(name);
        eq.setDescription(description);
        eq.setCategory(category);
        eq.setQuantityTotal(quantity);
        eq.setQuantityAvailable(quantity);
        return eq;
    }

    private void seedUsers() {
        // Cập nhật hoặc tạo mới tài khoản demo với mật khẩu đã được băm chuẩn
        upsertUser("admin@salsp.edu.vn", "admin123", UserRole.ADMIN, "Quản trị viên hệ thống");
        
        User lecturerUser = upsertUser("lecturer01@salsp.edu.vn", "123456", UserRole.LECTURER, "Giảng viên Demo");
        if (!lecturerRepository.findByUserId(lecturerUser.getId()).isPresent()) {
            Optional<Department> department = departmentRepository.findByCode("CNTT");
            if (department.isPresent()) {
                Lecturer lecturer = new Lecturer();
                lecturer.setUser(lecturerUser);
                lecturer.setDepartment(department.get());
                lecturer.setTitle("ThS.");
                lecturer.setBio("Giảng viên tư vấn chuyên sâu về Spring Boot và Microservices.");
                lecturerRepository.save(lecturer);
            }
        }

        upsertUser("student01@salsp.edu.vn", "123456", UserRole.STUDENT, "Sinh viên Demo");
    }

    private User upsertUser(String email, String rawPassword, UserRole role, String fullName) {
        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        User user;
        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            // Force update password to properly hashed BCrypt password if it was plain text or wrong
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            userRepository.save(user);

            // Bug #26: Kiểm tra sự tồn tại của profile và tạo mới nếu bị thiếu.
            if (!userProfileRepository.findByUserId(user.getId()).isPresent()) {
                UserProfile profile = new UserProfile();
                profile.setUser(user);
                profile.setFullName(fullName);
                profile.setPhone("0987654321");
                userProfileRepository.save(profile);
            }
        } else {
            user = new User();
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setActive(true);
            userRepository.save(user);

            UserProfile profile = new UserProfile();
            profile.setUser(user);
            profile.setFullName(fullName);
            profile.setPhone("0987654321");
            userProfileRepository.save(profile);
        }
        return user;
    }
}

package com.rikkei.salsp.config;

import com.rikkei.salsp.entity.common.Department;
import com.rikkei.salsp.entity.equipment.Equipment;
import com.rikkei.salsp.entity.user.Lecturer;
import com.rikkei.salsp.entity.user.User;
import com.rikkei.salsp.entity.user.UserProfile;
import com.rikkei.salsp.entity.user.UserRole;
import com.rikkei.salsp.repository.common.DepartmentRepository;
import com.rikkei.salsp.repository.equipment.EquipmentRepository;
import com.rikkei.salsp.repository.user.LecturerRepository;
import com.rikkei.salsp.repository.user.UserProfileRepository;
import com.rikkei.salsp.repository.user.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Lớp `DataSeeder` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final LecturerRepository lecturerRepository;
    private final DepartmentRepository departmentRepository;
    private final EquipmentRepository equipmentRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Phương thức xử lý nghiệp vụ run.
     * @param args Tham số đầu vào args
     */
    @Override
    @Transactional
    public void run(String... args) {
        seedDepartments();
        seedEquipments();
        seedUsers();
    }

    /**
     * Phương thức xử lý nghiệp vụ seedDepartments.
     */
    private void seedDepartments() {
        if (departmentRepository.count() == 0) {
            departmentRepository.save(createDepartment("Khoa Công nghệ thông tin", "CNTT"));
            departmentRepository.save(createDepartment("Khoa Kinh tế", "KT"));
            departmentRepository.save(createDepartment("Khoa Điện tử viễn thông", "DTVT"));
        }
    }

    /**
     * Tạo mới khoa/ban chuyên môn.
     * @param name Tham số đầu vào name
     * @param code Tham số đầu vào code

     * @return Kết quả trả về của phương thức
     */
    private Department createDepartment(String name, String code) {
        Department d = new Department();
        d.setName(name);
        d.setCode(code);
        return d;
    }

    /**
     * Phương thức xử lý nghiệp vụ seedEquipments.
     */
    private void seedEquipments() {
        if (equipmentRepository.count() == 0) {
            equipmentRepository.save(createEquipment("Laptop Dell XPS 15", "Laptop cấu hình cao cho lập trình", "Máy tính", 5));
            equipmentRepository.save(createEquipment("Máy chiếu Sony", "Máy chiếu phục vụ thuyết trình đồ án", "Trình chiếu", 2));
            equipmentRepository.save(createEquipment("Bảng vẽ Wacom", "Bảng vẽ điện tử cho thiết kế", "Phụ kiện", 10));
            equipmentRepository.save(createEquipment("Arduino Uno R3", "Kit phát triển IoT", "Thiết bị nhúng", 20));
        }
    }

    /**
     * Tạo mới thiết bị phòng Lab.
     * @param name Tham số đầu vào name
     * @param description Tham số đầu vào description
     * @param category Tham số đầu vào category
     * @param quantity Tham số đầu vào quantity

     * @return Kết quả trả về của phương thức
     */
    private Equipment createEquipment(String name, String description, String category, int quantity) {
        Equipment eq = new Equipment();
        eq.setName(name);
        eq.setDescription(description);
        eq.setCategory(category);
        eq.setQuantityTotal(quantity);
        eq.setQuantityAvailable(quantity);
        return eq;
    }

    /**
     * Phương thức xử lý nghiệp vụ seedUsers.
     */
    private void seedUsers() {
        // Cập nhật hoặc tạo mới tài khoản demo với mật khẩu đã được băm chuẩn
        upsertUser("admin@salsp.edu.vn", "admin123", UserRole.ADMIN, "Nguyễn Bá Minh Đạo");
        
        User lecturerUser = upsertUser("lecturer01@salsp.edu.vn", "123456", UserRole.LECTURER, "Nguyễn Thanh Bình Phước");
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

        upsertUser("student01@salsp.edu.vn", "123456", UserRole.STUDENT, "Trần Đức Anh");
    }

    /**
     * Phương thức xử lý nghiệp vụ upsertUser.
     * @param email Tham số đầu vào email
     * @param rawPassword Tham số đầu vào rawPassword
     * @param role Tham số đầu vào role
     * @param fullName Tham số đầu vào fullName

     * @return Kết quả trả về của phương thức
     */
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

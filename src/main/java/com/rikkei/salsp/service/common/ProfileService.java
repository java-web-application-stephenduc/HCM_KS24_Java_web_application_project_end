package com.rikkei.salsp.service.common;

import com.rikkei.salsp.dto.lecturer.LecturerProfileDto;
import com.rikkei.salsp.dto.common.ProfileUpdateDto;
import com.rikkei.salsp.entity.common.Department;
import com.rikkei.salsp.entity.user.Lecturer;
import com.rikkei.salsp.entity.user.User;
import com.rikkei.salsp.entity.user.UserProfile;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.repository.common.DepartmentRepository;
import com.rikkei.salsp.repository.user.LecturerRepository;
import com.rikkei.salsp.repository.user.UserProfileRepository;
import com.rikkei.salsp.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý nghiệp vụ tìm kiếm, hiển thị và cập nhật thông tin hồ sơ cá nhân người dùng.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final LecturerRepository lecturerRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Lấy thông tin hồ sơ cá nhân.
     * @param userId Tham số đầu vào userId

     * @return Kết quả trả về của phương thức
     */
    public UserProfile getProfileByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    /**
     * Lấy thông tin hồ sơ cá nhân.

     * <p><strong>Lưu ý sửa lỗi (Bug #19: Dùng findByUserIdWithDepartment tránh N+1 khi gọi getDepartment().getId())</strong></p>
     * @param userId Tham số đầu vào userId

     * @return Kết quả trả về của phương thức
     */
    public LecturerProfileDto getLecturerProfile(Long userId) {
        UserProfile profile = getProfileByUserId(userId);
        // Bug #19: Dùng findByUserIdWithDepartment tránh N+1 khi gọi getDepartment().getId()
        Lecturer lecturer = lecturerRepository.findByUserIdWithDepartment(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Lecturer profile not found"));

        LecturerProfileDto dto = new LecturerProfileDto();
        dto.setFullName(profile.getFullName());
        dto.setPhone(profile.getPhone());
        dto.setDepartmentId(lecturer.getDepartment().getId());
        dto.setDepartmentName(lecturer.getDepartment().getName());
        dto.setTitle(lecturer.getTitle());
        dto.setBio(lecturer.getBio());
        return dto;
    }

    /**
     * Cập nhật hồ sơ cá nhân.
     * @param userId Tham số đầu vào userId
     * @param dto Tham số đầu vào dto
     */
    @Transactional
    public void updateProfile(Long userId, ProfileUpdateDto dto) {
        UserProfile profile = getProfileByUserId(userId);
        profile.setFullName(dto.getFullName().trim());
        profile.setPhone(dto.getPhone());
        userProfileRepository.save(profile);
    }

    /**
     * Cập nhật hồ sơ cá nhân.
     * @param userId Tham số đầu vào userId
     * @param dto Tham số đầu vào dto
     */
    @Transactional
    public void updateLecturerProfile(Long userId, LecturerProfileDto dto) {
        if (dto.getDepartmentId() == null) {
            throw new BusinessException("Vui lòng chọn khoa");
        }
        UserProfile profile = getProfileByUserId(userId);
        profile.setFullName(dto.getFullName().trim());
        profile.setPhone(dto.getPhone());
        userProfileRepository.save(profile);

        Lecturer lecturer = lecturerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Lecturer profile not found"));
        Department department = departmentRepository.findById(dto.getDepartmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        lecturer.setDepartment(department);
        lecturer.setTitle(dto.getTitle());
        lecturer.setBio(dto.getBio());
        lecturerRepository.save(lecturer);
    }

    /**
     * Tìm kiếm người dùng.
     * @param email Tham số đầu vào email

     * @return Kết quả trả về của phương thức
     */
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}



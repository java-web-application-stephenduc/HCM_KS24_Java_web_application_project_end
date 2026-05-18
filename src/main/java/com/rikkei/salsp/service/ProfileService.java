package com.rikkei.salsp.service;

import com.rikkei.salsp.dto.LecturerProfileDto;
import com.rikkei.salsp.dto.ProfileUpdateDto;
import com.rikkei.salsp.entity.Department;
import com.rikkei.salsp.entity.Lecturer;
import com.rikkei.salsp.entity.User;
import com.rikkei.salsp.entity.UserProfile;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.repository.DepartmentRepository;
import com.rikkei.salsp.repository.LecturerRepository;
import com.rikkei.salsp.repository.UserProfileRepository;
import com.rikkei.salsp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final LecturerRepository lecturerRepository;
    private final DepartmentRepository departmentRepository;

    public UserProfile getProfileByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    public LecturerProfileDto getLecturerProfile(Long userId) {
        UserProfile profile = getProfileByUserId(userId);
        Lecturer lecturer = lecturerRepository.findByUserId(userId)
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

    @Transactional
    public void updateProfile(Long userId, ProfileUpdateDto dto) {
        UserProfile profile = getProfileByUserId(userId);
        profile.setFullName(dto.getFullName().trim());
        profile.setPhone(dto.getPhone());
        userProfileRepository.save(profile);
    }

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

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}



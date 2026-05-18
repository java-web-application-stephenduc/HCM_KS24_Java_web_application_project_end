package com.rikkei.salsp.service;

import com.rikkei.salsp.entity.MentoringSession;
import com.rikkei.salsp.entity.SessionStatus;
import com.rikkei.salsp.entity.User;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.repository.MentoringSessionRepository;
import com.rikkei.salsp.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CancellationService {

    private final MentoringSessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void cancelSession(Long sessionId, String studentEmail) {
        // Kiểm tra quyền, trạng thái và điều kiện 24 giờ trước khi hủy.
        MentoringSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi tư vấn"));

        User student = userRepository.findByEmail(studentEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên"));
        if (!session.getStudent().getId().equals(student.getId())) {
            throw new BusinessException("Không có quyền hủy lịch này");
        }
        if (session.getStatus() != SessionStatus.PENDING) {
            throw new BusinessException("Chỉ hủy được lịch PENDING");
        }

        LocalDateTime sessionDateTime = LocalDateTime.of(session.getSessionDate(), session.getStartTime());
        if (sessionDateTime.isBefore(LocalDateTime.now().plusHours(24))) {
            throw new BusinessException("Chỉ hủy được trước 24 giờ");
        }

        session.setStatus(SessionStatus.CANCELLED);
        sessionRepository.save(session);
    }
}


package com.rikkei.salsp.service.student;

import com.rikkei.salsp.entity.session.MentoringSession;
import com.rikkei.salsp.entity.session.SessionStatus;
import com.rikkei.salsp.entity.user.User;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.repository.session.MentoringSessionRepository;
import com.rikkei.salsp.repository.user.UserRepository;
import com.rikkei.salsp.repository.session.AcademicEvaluationRepository;
import com.rikkei.salsp.repository.equipment.BorrowingDetailRepository;
import com.rikkei.salsp.repository.equipment.BorrowingRecordRepository;
import com.rikkei.salsp.entity.equipment.BorrowingStatus;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý quy trình hủy lịch hẹn cố vấn học tập và thực hiện cập nhật
 * trạng thái xóa mềm.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CancellationService {

    private final MentoringSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final AcademicEvaluationRepository evaluationRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;

    /**
     * Thực hiện hủy buổi cố vấn học thuật (xóa mềm). Cập nhật trạng thái thành
     * CANCELLED và bảo toàn dữ liệu lịch sử.
     * 
     * @param sessionId    Tham số đầu vào sessionId
     * @param studentEmail Tham số đầu vào studentEmail
     */
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

        // Bug #18: Đồng bộ logic kiểm tra 24h với
        // StudentHistoryService.isCancellable().
        // isCancellable() trả true khi: now.plusHours(24).isBefore(sessionTime)
        // tương đương: sessionTime.isAfter(now + 24h).
        // Phiên bản cũ dùng sessionDateTime.isBefore(now+24h) là sai logic đối ngược.
        // Nếu sessionTime == now+24h: isCancellable() = false, server cũ cho phép ->
        // mâu thuẫn.
        LocalDateTime sessionDateTime = LocalDateTime.of(session.getSessionDate(), session.getStartTime());
        if (!sessionDateTime.isAfter(LocalDateTime.now().plusHours(24))) {
            throw new BusinessException("Chỉ hủy được trước 24 giờ");
        }

        // Hủy mềm: cập nhật trạng thái theo sinh viên hủy.
        session.setStatus(SessionStatus.CANCELED_BY_STUDENT);
        sessionRepository.save(session);

        // Đồng bộ hủy phiếu mượn thiết bị nếu có liên kết và đang ở trạng thái chờ duyệt/chờ cấp phát
        borrowingRecordRepository.findBySessionId(sessionId).ifPresent(record -> {
            if (record.getStatus() == BorrowingStatus.PENDING_LECTURER_APPROVAL
                    || record.getStatus() == BorrowingStatus.PENDING_ADMIN_APPROVAL
                    || record.getStatus() == BorrowingStatus.PENDING_DISPATCH) {
                record.setStatus(BorrowingStatus.REJECTED_BY_LECTURER);
                record.setLecturerNote("Buổi tư vấn học tập liên kết đã bị hủy bởi Sinh viên.");
                borrowingRecordRepository.save(record);
            }
        });
    }
}

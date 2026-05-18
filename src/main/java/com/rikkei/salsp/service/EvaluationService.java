package com.rikkei.salsp.service;

import com.rikkei.salsp.dto.EquipmentItemDto;
import com.rikkei.salsp.dto.EvaluationFormDto;
import com.rikkei.salsp.dto.SessionDetailDto;
import com.rikkei.salsp.dto.SessionSummaryDto;
import com.rikkei.salsp.entity.AcademicEvaluation;
import com.rikkei.salsp.entity.BorrowingDetail;
import com.rikkei.salsp.entity.BorrowingRecord;
import com.rikkei.salsp.entity.BorrowingStatus;
import com.rikkei.salsp.entity.Equipment;
import com.rikkei.salsp.entity.MentoringSession;
import com.rikkei.salsp.entity.SessionStatus;
import com.rikkei.salsp.entity.User;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.repository.AcademicEvaluationRepository;
import com.rikkei.salsp.repository.BorrowingDetailRepository;
import com.rikkei.salsp.repository.BorrowingRecordRepository;
import com.rikkei.salsp.repository.EquipmentRepository;
import com.rikkei.salsp.repository.MentoringSessionRepository;
import com.rikkei.salsp.repository.UserRepository;
import jakarta.persistence.LockModeType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvaluationService {

    private final MentoringSessionRepository sessionRepository;
    private final AcademicEvaluationRepository evaluationRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    public List<SessionSummaryDto> getPendingSessions(String lecturerEmail) {
        // Tải danh sách buổi chờ tư vấn của giảng viên.
        User lecturer = userRepository.findByEmail(lecturerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));

        // Tải queue kèm profile sinh viên trong một truy vấn.
        return sessionRepository.findQueueWithStudent(lecturer.getId(), SessionStatus.PENDING).stream()
            .map(session -> {
                SessionSummaryDto dto = new SessionSummaryDto();
                dto.setSessionId(session.getId());
                dto.setStudentName(session.getStudent().getProfile().getFullName());
                dto.setSessionDate(session.getSessionDate());
                dto.setStartTime(session.getStartTime());
                dto.setEndTime(session.getEndTime());
                dto.setStatus(session.getStatus().name());
                dto.setNote(session.getNote());
                return dto;
            })
            .collect(Collectors.toList());
    }

    public SessionDetailDto getSessionDetail(Long sessionId, String lecturerEmail) {
        // Bug #19: Dùng findByIdWithStudentAndLecturer tránh N+1 khi gọi getStudent().getProfile()
        MentoringSession session = sessionRepository.findByIdWithStudentAndLecturer(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi tư vấn"));

        User lecturer = userRepository.findByEmail(lecturerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));
        if (!session.getLecturer().getId().equals(lecturer.getId())) {
            throw new BusinessException("Không có quyền truy cập buổi tư vấn này");
        }

        SessionDetailDto dto = new SessionDetailDto();
        dto.setSessionId(session.getId());
        dto.setStudentName(session.getStudent().getProfile().getFullName());
        dto.setSessionDate(session.getSessionDate());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setNote(session.getNote());
        return dto;
    }

    @Transactional
    public void completeSession(EvaluationFormDto dto, String lecturerEmail) {
        // Tải session và kiểm tra quyền/trạng thái.
        // Bug #13: Dùng findById() bình thường - để phòng TOCTOU,
        // cơ chế DB unique constraint trên session_id trong bảng academic_evaluations
        // sẽ bắt duplicate nếu hai request cùng tạo evaluation đồng thời.
        // Tại service level, ta khoa session entity trước khi read-check để giảm thiểu TOCTOU window.
        // Bug #19: Dùng findByIdWithStudentAndLecturer
        MentoringSession session = sessionRepository.findByIdWithStudentAndLecturer(dto.getSessionId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi tư vấn"));

        User lecturer = userRepository.findByEmail(lecturerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));
        if (!session.getLecturer().getId().equals(lecturer.getId())) {
            throw new BusinessException("Không có quyền đánh giá buổi tư vấn này");
        }
        if (session.getStatus() != SessionStatus.PENDING) {
            throw new BusinessException("Buổi tư vấn không ở trạng thái PENDING");
        }
        // Bug #13: Kiểm tra sau khi session được nạp vào Persistence Context.
        // Nếu hai request đồng thời vượt qua check này, DB unique constraint sẽ bắt lỗi
        // và transaction thứ hai sẽ rollback.
        if (evaluationRepository.existsBySessionId(dto.getSessionId())) {
            throw new BusinessException("Buổi tư vấn đã được đánh giá");
        }

        session.setStatus(SessionStatus.COMPLETED);
        sessionRepository.save(session);

        AcademicEvaluation evaluation = new AcademicEvaluation();
        evaluation.setSession(session);
        evaluation.setScore(dto.getScore());
        evaluation.setFeedback(dto.getFeedback());
        evaluationRepository.save(evaluation);

        // Xây dựng danh sách detail trước khi tạo BorrowingRecord.
        List<BorrowingDetail> details = new ArrayList<>();
        for (EquipmentItemDto item : dto.getEquipmentItems()) {
            if (item.getEquipmentId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                continue;
            }
            // Bug #33: Dùng findById().orElseThrow() thay vì getReferenceById()
            // để fail-fast ngay lập tức nếu equipment không tồn tại,
            // thay vì nhận EntityNotFoundException lúc flush (sau khi đã lưu session + evaluation).
            Equipment equipment = equipmentRepository.findById(item.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy thiết bị ID: " + item.getEquipmentId()));
            BorrowingDetail detail = new BorrowingDetail();
            detail.setEquipment(equipment);
            detail.setQuantity(item.getQuantity());
            details.add(detail);
        }

        // Bug #15: Chỉ tạo BorrowingRecord khi có ít nhất một thiết bị được chọn.
        // Nếu tạo BorrowingRecord mà không có details, sẽ tạo ra một phiếu orphan
        // trong dispatch queue mà admin không thể xử lý (không có item nào).
        if (!details.isEmpty()) {
            BorrowingRecord record = new BorrowingRecord();
            record.setSession(session);
            record.setStatus(BorrowingStatus.PENDING_DISPATCH);
            // Gán record cho từng detail trước khi lưu (vi cascade ALL ở BorrowingRecord.details)
            details.forEach(d -> d.setRecord(record));
            record.getDetails().addAll(details);
            borrowingRecordRepository.save(record);
        }
    }
}




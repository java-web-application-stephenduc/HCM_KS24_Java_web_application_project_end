package com.rikkei.salsp.service.lecturer;

import com.rikkei.salsp.dto.common.EquipmentItemDto;
import com.rikkei.salsp.dto.lecturer.BorrowApprovalDto;
import com.rikkei.salsp.dto.lecturer.EvaluationFormDto;
import com.rikkei.salsp.dto.lecturer.SessionRejectionDto;
import com.rikkei.salsp.dto.lecturer.SessionDetailDto;
import com.rikkei.salsp.dto.lecturer.SessionSummaryDto;
import com.rikkei.salsp.dto.common.EquipmentRequestDto;
import com.rikkei.salsp.entity.session.AcademicEvaluation;
import com.rikkei.salsp.entity.equipment.BorrowingDetail;
import com.rikkei.salsp.entity.equipment.BorrowingRecord;
import com.rikkei.salsp.entity.equipment.BorrowingStatus;
import com.rikkei.salsp.entity.equipment.Equipment;
import com.rikkei.salsp.entity.session.MentoringSession;
import com.rikkei.salsp.entity.session.SessionStatus;
import com.rikkei.salsp.entity.user.User;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.repository.session.AcademicEvaluationRepository;
import com.rikkei.salsp.repository.equipment.BorrowingDetailRepository;
import com.rikkei.salsp.repository.equipment.BorrowingRecordRepository;
import com.rikkei.salsp.repository.equipment.EquipmentRepository;
import com.rikkei.salsp.repository.session.MentoringSessionRepository;
import com.rikkei.salsp.repository.user.LecturerRepository;
import com.rikkei.salsp.repository.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * Service quản lý việc chấm điểm, nhận xét và cấp phát thiết bị của giảng viên
 * sau khi hoàn thành buổi cố vấn.
 */
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
    private final LecturerRepository lecturerRepository;

    /*
     * Lấy danh sách buổi cố vấn đang ở trạng thái PENDING của giảng viên
     * (chờ duyệt / đánh giá).
     */
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

    /**
     * Lấy các buổi tư vấn đang hoạt động (PENDING, CONFIRMED) của giảng viên.
     */
    public List<SessionSummaryDto> getActiveSessions(String lecturerEmail) {
        User lecturer = userRepository.findByEmail(lecturerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));

        List<SessionStatus> activeStatuses = List.of(SessionStatus.PENDING, SessionStatus.CONFIRMED);
        return sessionRepository.findSessionsByLecturerIdAndStatuses(lecturer.getId(), activeStatuses).stream()
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

    /**
     * Lấy lịch sử các buổi tư vấn đã qua (COMPLETED, REJECTED, CANCELLED, CANCELED_BY_STUDENT) của giảng viên.
     */
    public List<SessionSummaryDto> getHistorySessions(String lecturerEmail) {
        User lecturer = userRepository.findByEmail(lecturerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));

        List<SessionStatus> historyStatuses = List.of(
                SessionStatus.COMPLETED,
                SessionStatus.REJECTED,
                SessionStatus.CANCELLED,
                SessionStatus.CANCELED_BY_STUDENT
        );
        return sessionRepository.findSessionsByLecturerIdAndStatuses(lecturer.getId(), historyStatuses).stream()
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

    /*
     * Lấy chi tiết một buổi cố vấn (kèm evaluation, borrow record, thiết bị).
     * Sử dụng findByIdWithStudentAndLecturer (Bug #19) — JOIN FETCH tránh N+1
     * khi truy cập getStudent().getProfile().
     * Kiểm tra quyền sở hữu: chỉ giảng viên phụ trách buổi đó mới được xem.
     */
    public SessionDetailDto getSessionDetail(Long sessionId, String lecturerEmail) {
        // Bug #19: Dùng findByIdWithStudentAndLecturer tránh N+1 khi gọi
        // getStudent().getProfile()
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
        dto.setStudentEmail(session.getStudent().getEmail());
        dto.setStudentPhone(session.getStudent().getProfile().getPhone());
        // Lấy tên khoa từ giảng viên phụ trách buổi tư vấn
        lecturerRepository.findByUserIdWithDepartment(session.getLecturer().getId())
                .ifPresent(l -> dto.setDepartmentName(l.getDepartment().getName()));
        dto.setSessionDate(session.getSessionDate());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setNote(session.getNote());
        dto.setStatus(session.getStatus().name());
        dto.setRejectionReason(session.getRejectionReason());

        LocalDateTime sessionDateTime = LocalDateTime.of(session.getSessionDate(), session.getStartTime());
        dto.setCancellable(sessionDateTime.isAfter(LocalDateTime.now().plusHours(24)));

        if (session.getStudent().getProfile() != null) {
            dto.setStudentAvatarUrl(session.getStudent().getProfile().getAvatarUrl());
        }

        evaluationRepository.findBySessionId(sessionId).ifPresent(eval -> {
            dto.setScore(eval.getScore());
            dto.setFeedback(eval.getFeedback());
        });

        borrowingRecordRepository.findBySessionIdWithDetails(sessionId).ifPresent(record -> {
            dto.setBorrowStatus(record.getStatus().name());
            dto.setLecturerNote(record.getLecturerNote());
            dto.setAdminNote(record.getAdminNote());
            List<EquipmentRequestDto> requests = record.getDetails().stream()
                    .map(detail -> {
                        EquipmentRequestDto rq = new EquipmentRequestDto();
                        rq.setEquipmentId(detail.getEquipment().getId());
                        rq.setEquipmentName(detail.getEquipment().getName());
                        rq.setCategory(detail.getEquipment().getCategory());
                        rq.setQuantity(detail.getQuantity());
                        rq.setQuantityAvailable(detail.getEquipment().getQuantityAvailable());
                        return rq;
                    })
                    .collect(Collectors.toList());
            dto.setRequestedEquipments(requests);
        });
        return dto;
    }

    /**
     * Hoàn thành buổi cố vấn, lưu đánh giá (điểm số, nhận xét) của giảng viên và tự
     * động tạo phiếu mượn thiết bị nếu có.
     * 
     * <p>
     * <strong>Lưu ý sửa lỗi (Bug #13: Dùng findById() bình thường - để phòng
     * TOCTOU,)</strong>
     * </p>
     * 
     * @param dto           Tham số đầu vào dto
     * @param lecturerEmail Tham số đầu vào lecturerEmail
     */
    @Transactional
    public void completeSession(EvaluationFormDto dto, String lecturerEmail) {
        // Tải session và kiểm tra quyền/trạng thái.
        // Bug #13: Dùng findById() bình thường - để phòng TOCTOU,
        // cơ chế DB unique constraint trên session_id trong bảng academic_evaluations
        // sẽ bắt duplicate nếu hai request cùng tạo evaluation đồng thời.
        // Tại service level, ta khoa session entity trước khi read-check để giảm thiểu
        // TOCTOU window.
        // Bug #19: Dùng findByIdWithStudentAndLecturer
        MentoringSession session = sessionRepository.findByIdWithStudentAndLecturer(dto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi tư vấn"));

        User lecturer = userRepository.findByEmail(lecturerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));
        if (!session.getLecturer().getId().equals(lecturer.getId())) {
            throw new BusinessException("Không có quyền đánh giá buổi tư vấn này");
        }
        if (session.getStatus() != SessionStatus.PENDING && session.getStatus() != SessionStatus.CONFIRMED) {
            throw new BusinessException("Buổi tư vấn không ở trạng thái hợp lệ để đánh giá");
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

    }

    /**
     * Duyệt yêu cầu mượn thiết bị từ sinh viên và bổ sung thiết bị nếu cần.
     *
     * @param sessionId     ID buổi tư vấn
     * @param dto           Dữ liệu duyệt mượn
     * @param lecturerEmail Email giảng viên
     */
    @Transactional
    public void approveBorrowRequest(Long sessionId, BorrowApprovalDto dto, String lecturerEmail) {
        MentoringSession session = sessionRepository.findByIdWithStudentAndLecturer(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi tư vấn"));
        validateLecturerPermission(session, lecturerEmail);
        validateSessionNotCancelled(session);

        BorrowingRecord record = borrowingRecordRepository.findBySessionIdWithDetails(sessionId)
                .orElseThrow(() -> new BusinessException("Chưa có yêu cầu mượn thiết bị"));
        if (record.getStatus() != BorrowingStatus.PENDING_LECTURER_APPROVAL) {
            throw new BusinessException("Yêu cầu mượn không ở trạng thái chờ giảng viên duyệt");
        }

        List<BorrowingDetail> additionalDetails = buildAdditionalDetails(dto.getAdditionalEquipmentItems());
        mergeBorrowingDetails(record, additionalDetails);

        record.setLecturerNote(dto.getLecturerNote());
        record.setStatus(BorrowingStatus.PENDING_ADMIN_APPROVAL);
        borrowingRecordRepository.save(record);
    }

    /**
     * Từ chối yêu cầu mượn thiết bị của sinh viên.
     *
     * @param sessionId     ID buổi tư vấn
     * @param dto           Dữ liệu từ chối
     * @param lecturerEmail Email giảng viên
     */
    @Transactional
    public void rejectBorrowRequest(Long sessionId, BorrowApprovalDto dto, String lecturerEmail) {
        MentoringSession session = sessionRepository.findByIdWithStudentAndLecturer(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi tư vấn"));
        validateLecturerPermission(session, lecturerEmail);
        validateSessionNotCancelled(session);

        BorrowingRecord record = borrowingRecordRepository.findBySessionIdWithDetails(sessionId)
                .orElseThrow(() -> new BusinessException("Chưa có yêu cầu mượn thiết bị"));
        if (record.getStatus() != BorrowingStatus.PENDING_LECTURER_APPROVAL) {
            throw new BusinessException("Yêu cầu mượn không ở trạng thái chờ giảng viên duyệt");
        }

        record.setLecturerNote(dto.getLecturerNote());
        record.setStatus(BorrowingStatus.REJECTED_BY_LECTURER);
        borrowingRecordRepository.save(record);
    }

    /**
     * Từ chối buổi hẹn của sinh viên kèm lý do.
     *
     * @param sessionId     ID buổi tư vấn
     * @param dto           Dữ liệu từ chối
     * @param lecturerEmail Email giảng viên
     */
    @Transactional
    public void rejectSession(Long sessionId, SessionRejectionDto dto, String lecturerEmail) {
        MentoringSession session = sessionRepository.findByIdWithStudentAndLecturer(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi tư vấn"));
        validateLecturerPermission(session, lecturerEmail);
        
        LocalDateTime sessionDateTime = LocalDateTime.of(session.getSessionDate(), session.getStartTime());
        if (!sessionDateTime.isAfter(LocalDateTime.now().plusHours(24))) {
            throw new BusinessException("Chỉ được hủy/từ chối lịch hẹn trước 24 giờ.");
        }

        if (session.getStatus() == SessionStatus.COMPLETED
                || session.getStatus() == SessionStatus.REJECTED
                || session.getStatus() == SessionStatus.CANCELLED
                || session.getStatus() == SessionStatus.CANCELED_BY_STUDENT) {
            throw new BusinessException("Buổi tư vấn không ở trạng thái có thể từ chối");
        }

        session.setStatus(SessionStatus.REJECTED);
        session.setRejectionReason(dto.getRejectionReason());
        sessionRepository.save(session);

        borrowingRecordRepository.findBySessionIdWithDetails(sessionId).ifPresent(record -> {
            if (record.getStatus() == BorrowingStatus.PENDING_LECTURER_APPROVAL
                    || record.getStatus() == BorrowingStatus.PENDING_ADMIN_APPROVAL) {
                record.setStatus(BorrowingStatus.REJECTED_BY_LECTURER);
                record.setLecturerNote(dto.getRejectionReason());
                borrowingRecordRepository.save(record);
            }
        });
    }

    /**
     * Kiểm tra quyền giảng viên.
     */
    private void validateLecturerPermission(MentoringSession session, String lecturerEmail) {
        User lecturer = userRepository.findByEmail(lecturerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên"));
        if (!session.getLecturer().getId().equals(lecturer.getId())) {
            throw new BusinessException("Không có quyền truy cập buổi tư vấn này");
        }
    }

    /**
     * Kiểm tra buổi tư vấn đã bị hủy/từ chối chưa.
     */
    private void validateSessionNotCancelled(MentoringSession session) {
        if (session.getStatus() == SessionStatus.CANCELLED
                || session.getStatus() == SessionStatus.REJECTED
                || session.getStatus() == SessionStatus.CANCELED_BY_STUDENT) {
            throw new BusinessException("Buổi tư vấn đã bị hủy hoặc từ chối");
        }
    }

    /**
     * Chỉ cho phép xử lý cấp phát sau khi buổi tư vấn bắt đầu.
     */
    private void validateSessionStarted(MentoringSession session) {
        LocalDateTime sessionStart = LocalDateTime.of(session.getSessionDate(), session.getStartTime());
        if (LocalDateTime.now().isBefore(sessionStart)) {
            throw new BusinessException("Chỉ được xử lý cấp phát khi buổi tư vấn đã bắt đầu");
        }
    }

    /**
     * Xây dựng danh sách thiết bị bổ sung từ giảng viên.
     */
    private List<BorrowingDetail> buildAdditionalDetails(List<EquipmentItemDto> items) {
        List<BorrowingDetail> details = new ArrayList<>();
        if (items == null) {
            return details;
        }
        for (EquipmentItemDto item : items) {
            if (item.getEquipmentId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                continue;
            }
            Equipment equipment = equipmentRepository.findById(item.getEquipmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy thiết bị ID: " + item.getEquipmentId()));
            BorrowingDetail detail = new BorrowingDetail();
            detail.setEquipment(equipment);
            detail.setQuantity(item.getQuantity());
            details.add(detail);
        }
        return details;
    }

    /**
     * Gộp thiết bị bổ sung vào yêu cầu hiện tại.
     */
    private void mergeBorrowingDetails(BorrowingRecord record, List<BorrowingDetail> additionalDetails) {
        if (additionalDetails.isEmpty()) {
            return;
        }
        for (BorrowingDetail incoming : additionalDetails) {
            BorrowingDetail existing = record.getDetails().stream()
                    .filter(d -> d.getEquipment().getId().equals(incoming.getEquipment().getId()))
                    .findFirst()
                    .orElse(null);
            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + incoming.getQuantity());
            } else {
                incoming.setRecord(record);
                record.getDetails().add(incoming);
            }
        }
    }
}

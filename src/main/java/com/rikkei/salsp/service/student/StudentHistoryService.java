package com.rikkei.salsp.service.student;

import com.rikkei.salsp.dto.student.AcademicRecordDto;
import com.rikkei.salsp.dto.student.BorrowedEquipmentDto;
import com.rikkei.salsp.dto.student.BorrowedEquipmentHistoryDto;
import com.rikkei.salsp.dto.common.EquipmentItemDto;
import com.rikkei.salsp.entity.equipment.BorrowingDetail;
import com.rikkei.salsp.entity.equipment.BorrowingRecord;
import com.rikkei.salsp.entity.equipment.BorrowingStatus;
import com.rikkei.salsp.entity.equipment.Equipment;
import com.rikkei.salsp.entity.session.MentoringSession;
import com.rikkei.salsp.entity.session.SessionStatus;
import com.rikkei.salsp.entity.user.User;
import com.rikkei.salsp.repository.equipment.BorrowingDetailRepository;
import com.rikkei.salsp.repository.equipment.BorrowingRecordRepository;
import com.rikkei.salsp.repository.equipment.EquipmentRepository;
import com.rikkei.salsp.repository.session.MentoringSessionRepository;
import com.rikkei.salsp.repository.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service cung cấp lịch sử học thuật, lịch sử mượn thiết bị và xử lý đăng ký
 * mượn thiết bị của sinh viên.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentHistoryService {

    private final MentoringSessionRepository sessionRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    /**
     * Lấy toàn bộ lịch sử cố vấn học tập của sinh viên,
     * gom nhóm thiết bị mượn theo từng buổi.
     *
     * @param studentId ID sinh viên
     * @return Danh sách lịch sử học tập đã gom nhóm
     */
    public List<AcademicRecordDto> getAcademicHistory(Long studentId) {
        // Gom dữ liệu phẳng thành danh sách theo từng buổi.
        List<MentoringSessionRepository.AcademicHistoryProjection> rows = sessionRepository
                .findAcademicHistory(studentId);

        Map<Long, AcademicRecordDto> grouped = new LinkedHashMap<>();
        for (MentoringSessionRepository.AcademicHistoryProjection row : rows) {
            AcademicRecordDto dto = grouped.computeIfAbsent(row.getSessionId(), id -> {
                AcademicRecordDto record = new AcademicRecordDto();
                record.setSessionId(row.getSessionId());
                // Hibernate 6 trả về trực tiếp java.time.LocalDate/LocalTime, không cần
                // convert.
                record.setSessionDate(row.getSessionDate());
                record.setStartTime(row.getStartTime());
                record.setEndTime(row.getEndTime());
                record.setStatus(row.getStatus());
                record.setLecturerName(row.getLecturerName());
                record.setDepartmentName(row.getDepartmentName());
                record.setScore(row.getScore());
                record.setFeedback(row.getFeedback());
                record.setNote(row.getNote());
                record.setRejectionReason(row.getRejectionReason());
                record.setEquipments(new ArrayList<>());
                record.setCancellable(isCancellable(record.getStatus(),
                        record.getSessionDate(), record.getStartTime()));
                return record;
            });

            if (row.getEquipmentName() != null) {
                BorrowedEquipmentDto equipment = new BorrowedEquipmentDto();
                equipment.setEquipmentName(row.getEquipmentName());
                equipment.setQuantity(row.getQuantity());
                dto.getEquipments().add(equipment);
            }
        }
        return new ArrayList<>(grouped.values());
    }

    /**
     * Lấy thông tin bản ghi.
     * 
     * @param sessionId Tham số đầu vào sessionId
     * 
     * @return Kết quả trả về của phương thức
     */
    public AcademicRecordDto getAcademicRecordById(Long sessionId) {
        List<MentoringSessionRepository.AcademicHistoryProjection> rows = sessionRepository
                .findAcademicHistoryBySessionId(sessionId);
        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy buổi tư vấn");
        }
        AcademicRecordDto record = null;
        for (MentoringSessionRepository.AcademicHistoryProjection row : rows) {
            if (record == null) {
                record = new AcademicRecordDto();
                record.setSessionId(row.getSessionId());
                record.setSessionDate(row.getSessionDate());
                record.setStartTime(row.getStartTime());
                record.setEndTime(row.getEndTime());
                record.setStatus(row.getStatus());
                record.setLecturerName(row.getLecturerName());
                record.setDepartmentName(row.getDepartmentName());
                record.setScore(row.getScore());
                record.setFeedback(row.getFeedback());
                record.setNote(row.getNote());
                record.setNote(row.getNote());
                record.setRejectionReason(row.getRejectionReason());
                record.setEquipments(new ArrayList<>());
                record.setCancellable(isCancellable(record.getStatus(),
                        record.getSessionDate(), record.getStartTime()));
            }
            if (row.getEquipmentName() != null) {
                BorrowedEquipmentDto equipment = new BorrowedEquipmentDto();
                equipment.setEquipmentName(row.getEquipmentName());
                equipment.setQuantity(row.getQuantity());
                record.getEquipments().add(equipment);
            }
        }
        return record;
    }

    /**
     * Lấy danh sách thiết bị đã mượn của sinh viên.
     *
     * @param studentId ID sinh viên
     * @return Danh sách thiết bị mượn kèm trạng thái
     */
    public List<BorrowedEquipmentHistoryDto> getBorrowedEquipmentHistory(Long studentId) {
        List<BorrowingDetail> details = borrowingDetailRepository.findByStudentIdWithEquipment(studentId);
        List<BorrowedEquipmentHistoryDto> results = new ArrayList<>();
        for (BorrowingDetail detail : details) {
            BorrowedEquipmentHistoryDto dto = new BorrowedEquipmentHistoryDto();
            dto.setEquipmentCode("TB-" + detail.getEquipment().getId());
            dto.setEquipmentName(detail.getEquipment().getName());
            dto.setBorrowedAt(detail.getRecord().getCreatedAt());
            dto.setReturnedAt(resolveReturnedAt(detail));
            dto.setStatus(detail.getRecord().getStatus().name());
            dto.setNote(detail.getRecord().getSession() != null
                    ? detail.getRecord().getSession().getNote()
                    : null);
            results.add(dto);
        }
        return results;
    }

    /**
     * Phương thức xử lý nghiệp vụ resolveReturnedAt.
     * 
     * @param detail Tham số đầu vào detail
     * 
     * @return Kết quả trả về của phương thức
     */
    private LocalDateTime resolveReturnedAt(BorrowingDetail detail) {
        if (detail.getRecord().getStatus() == BorrowingStatus.RETURNED) {
            return detail.getRecord().getUpdatedAt();
        }
        if (detail.getRecord().getSession() != null && detail.getRecord().getSession().getStatus() == SessionStatus.COMPLETED) {
            MentoringSession session = detail.getRecord().getSession();
            if (session.getSessionDate() != null && session.getEndTime() != null) {
                return LocalDateTime.of(session.getSessionDate(), session.getEndTime());
            }
        }
        return null;
    }

    /**
     * Kiểm tra buổi hẹn có thể hủy được không.
     * Điều kiện: trạng thái PENDING và còn cách ít nhất 24 giờ.
     *
     * @param status    Trạng thái hiện tại
     * @param date      Ngày buổi hẹn
     * @param startTime Giờ bắt đầu
     * @return true nếu có thể hủy
     */
    private boolean isCancellable(String status, LocalDate date, LocalTime startTime) {
        if (!"PENDING".equals(status) || date == null || startTime == null) {
            return false;
        }
        LocalDateTime sessionTime = LocalDateTime.of(date, startTime);
        return LocalDateTime.now().plusHours(24).isBefore(sessionTime);
    }

    /**
     * Lấy thông tin thiết bị phòng Lab.
     * 
     * @return Kết quả trả về của phương thức
     */
    public List<Equipment> getAvailableEquipment() {
        return equipmentRepository.findByDeletedFalse();
    }

    /**
     * Xử lý yêu cầu đăng ký mượn thiết bị tự phục vụ của sinh viên. Kiểm tra tính
     * hợp lệ của ca hẹn và số lượng khả dụng trong kho.
     * 
     * @param sessionId    Tham số đầu vào sessionId
     * @param items        Tham số đầu vào items
     * @param studentEmail Tham số đầu vào studentEmail
     */
    @Transactional
    public void createBorrowRequest(Long sessionId, List<EquipmentItemDto> items, String studentEmail) {
        MentoringSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy buổi tư vấn"));

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên"));

        if (!session.getStudent().getId().equals(student.getId())) {
            throw new BusinessException("Không có quyền đăng ký mượn cho buổi tư vấn này");
        }

        if (session.getStatus() == SessionStatus.CANCELLED
                || session.getStatus() == SessionStatus.REJECTED
                || session.getStatus() == SessionStatus.CANCELED_BY_STUDENT) {
            throw new BusinessException("Không thể mượn thiết bị cho buổi tư vấn đã hủy");
        }

        if (borrowingRecordRepository.findBySessionId(sessionId).isPresent()) {
            throw new BusinessException("Buổi tư vấn này đã có phiếu mượn thiết bị");
        }

        List<BorrowingDetail> details = new ArrayList<>();
        for (EquipmentItemDto item : items) {
            if (item.getEquipmentId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                continue;
            }
            Equipment equipment = equipmentRepository.findById(item.getEquipmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy thiết bị ID: " + item.getEquipmentId()));

            if (item.getQuantity() > equipment.getQuantityAvailable()) {
                throw new BusinessException("Số lượng yêu cầu cho thiết bị '" + equipment.getName() +
                        "' vượt quá số lượng khả dụng hiện có (" + equipment.getQuantityAvailable() + ")");
            }

            BorrowingDetail detail = new BorrowingDetail();
            detail.setEquipment(equipment);
            detail.setQuantity(item.getQuantity());
            details.add(detail);
        }

        if (details.isEmpty()) {
            throw new BusinessException("Vui lòng chọn ít nhất một thiết bị hợp lệ");
        }

        BorrowingRecord record = new BorrowingRecord();
        record.setSession(session);
        record.setStatus(BorrowingStatus.PENDING_LECTURER_APPROVAL);
        details.forEach(d -> d.setRecord(record));
        record.getDetails().addAll(details);
        borrowingRecordRepository.save(record);
    }
}

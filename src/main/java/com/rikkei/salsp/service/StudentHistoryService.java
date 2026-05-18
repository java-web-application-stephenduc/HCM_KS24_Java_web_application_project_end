package com.rikkei.salsp.service;

import com.rikkei.salsp.dto.AcademicRecordDto;
import com.rikkei.salsp.dto.BorrowedEquipmentDto;
import com.rikkei.salsp.dto.BorrowedEquipmentHistoryDto;
import com.rikkei.salsp.entity.BorrowingDetail;
import com.rikkei.salsp.entity.BorrowingStatus;
import com.rikkei.salsp.repository.BorrowingDetailRepository;
import com.rikkei.salsp.repository.MentoringSessionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentHistoryService {

    private final MentoringSessionRepository sessionRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;

    /**
     * Lấy toàn bộ lịch sử cố vấn học tập của sinh viên,
     * gom nhóm thiết bị mượn theo từng buổi.
     *
     * @param studentId ID sinh viên
     * @return Danh sách lịch sử học tập đã gom nhóm
     */
    public List<AcademicRecordDto> getAcademicHistory(Long studentId) {
        // Gom dữ liệu phẳng thành danh sách theo từng buổi.
        List<MentoringSessionRepository.AcademicHistoryProjection> rows =
            sessionRepository.findAcademicHistory(studentId);

        Map<Long, AcademicRecordDto> grouped = new LinkedHashMap<>();
        for (MentoringSessionRepository.AcademicHistoryProjection row : rows) {
            AcademicRecordDto dto = grouped.computeIfAbsent(row.getSessionId(), id -> {
                AcademicRecordDto record = new AcademicRecordDto();
                record.setSessionId(row.getSessionId());
                // Hibernate 6 trả về trực tiếp java.time.LocalDate/LocalTime, không cần convert.
                record.setSessionDate(row.getSessionDate());
                record.setStartTime(row.getStartTime());
                record.setEndTime(row.getEndTime());
                record.setStatus(row.getStatus());
                record.setLecturerName(row.getLecturerName());
                record.setDepartmentName(row.getDepartmentName());
                record.setScore(row.getScore());
                record.setFeedback(row.getFeedback());
                record.setNote(row.getNote());
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

    public AcademicRecordDto getAcademicRecordById(Long sessionId) {
        List<MentoringSessionRepository.AcademicHistoryProjection> rows =
            sessionRepository.findAcademicHistoryBySessionId(sessionId);
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
                ? detail.getRecord().getSession().getNote() : null);
            results.add(dto);
        }
        return results;
    }

    private LocalDateTime resolveReturnedAt(BorrowingDetail detail) {
        if (detail.getRecord().getStatus() == BorrowingStatus.RETURNED) {
            return detail.getRecord().getUpdatedAt();
        }
        return null;
    }

    /**
     * Kiểm tra buổi hẹn có thể hủy được không.
     * Điều kiện: trạng thái PENDING và còn cách ít nhất 24 giờ.
     *
     * @param status Trạng thái hiện tại
     * @param date Ngày buổi hẹn
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
}

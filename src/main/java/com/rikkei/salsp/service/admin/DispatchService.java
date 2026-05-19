package com.rikkei.salsp.service.admin;

import com.rikkei.salsp.dto.admin.DispatchQueueItemDto;
import com.rikkei.salsp.entity.equipment.BorrowingDetail;
import com.rikkei.salsp.entity.equipment.BorrowingRecord;
import com.rikkei.salsp.entity.equipment.BorrowingStatus;
import com.rikkei.salsp.entity.equipment.Equipment;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.InsufficientStockException;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.repository.equipment.BorrowingDetailRepository;
import com.rikkei.salsp.repository.equipment.BorrowingRecordRepository;
import com.rikkei.salsp.repository.equipment.EquipmentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý quy trình nghiệp vụ xuất kho cấp phát và nhập kho hoàn trả
 * thiết bị phòng Lab.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DispatchService {

    private final BorrowingRecordRepository borrowingRecordRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;
    private final EquipmentRepository equipmentRepository;

    /**
     * Lấy thông tin cấp phát thiết bị.
     * 
     * @return Kết quả trả về của phương thức
     */
    public List<DispatchQueueItemDto> getPendingDispatchQueue() {
        List<BorrowingStatus> statuses = List.of(
                BorrowingStatus.PENDING_ADMIN_APPROVAL,
                BorrowingStatus.PENDING_DISPATCH);
        return borrowingRecordRepository.findByStatusInWithDetails(statuses).stream()
                .map(record -> {
                    DispatchQueueItemDto dto = new DispatchQueueItemDto();
                    dto.setRecordId(record.getId());
                    dto.setStudentName(record.getSession().getStudent().getProfile().getFullName());
                    dto.setSessionDate(record.getSession().getSessionDate());
                    dto.setStatus(record.getStatus().name());
                    dto.setLecturerNote(record.getLecturerNote());
                    List<String> summary = record.getDetails().stream()
                            .map(detail -> detail.getEquipment().getName() + " x" + detail.getQuantity())
                            .collect(Collectors.toList());
                    dto.setEquipmentSummary(summary);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin cấp phát thiết bị.
     * 
     * <p>
     * <strong>Lưu ý sửa lỗi (Bug #19: Dùng findByIdWithAssociations để tránh N+1
     * khi truy cập session -> student -> profile)</strong>
     * </p>
     * 
     * @param recordId Tham số đầu vào recordId
     * 
     * @return Kết quả trả về của phương thức
     */
    public DispatchQueueItemDto getDispatchDetail(Long recordId) {
        // Bug #19: Dùng findByIdWithAssociations để tránh N+1 khi truy cập session ->
        // student -> profile
        BorrowingRecord record = borrowingRecordRepository.findByIdWithAssociations(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu mượn"));
        DispatchQueueItemDto dto = new DispatchQueueItemDto();
        dto.setRecordId(record.getId());
        dto.setStudentName(record.getSession().getStudent().getProfile().getFullName());
        dto.setSessionDate(record.getSession().getSessionDate());
        dto.setStatus(record.getStatus().name());
        dto.setLecturerNote(record.getLecturerNote());
        dto.setAdminNote(record.getAdminNote());
        // Bug #19: Dùng findByRecordIdWithEquipment để tránh N+1 khi gọi
        // detail.getEquipment().getName()
        List<String> summary = borrowingDetailRepository.findByRecordIdWithEquipment(recordId).stream()
                .map(detail -> detail.getEquipment().getName() + " x" + detail.getQuantity())
                .collect(Collectors.toList());
        dto.setEquipmentSummary(summary);
        return dto;
    }

    /**
     * Thực hiện xuất kho cấp phát thiết bị cho sinh viên. Giảm số lượng khả dụng
     * trong kho tương ứng.
     * 
     * <p>
     * <strong>Lưu ý sửa lỗi (Bug #19: Sử dụng findByIdWithAssociations)</strong>
     * </p>
     * 
     * @param recordId Tham số đầu vào recordId
     */
    @Transactional
    public void dispatchEquipment(Long recordId, String adminNote) {
        // Khóa dòng thiết bị và xuất kho nếu đủ tồn.
        // Bug #19: Sử dụng findByIdWithAssociations
        BorrowingRecord record = borrowingRecordRepository.findByIdWithAssociations(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu mượn"));
        if (record.getStatus() != BorrowingStatus.PENDING_ADMIN_APPROVAL
                && record.getStatus() != BorrowingStatus.PENDING_DISPATCH) {
            throw new BusinessException("Phiếu không ở trạng thái chờ duyệt cấp phát");
        }

        // Bug #19: Sử dụng findByRecordIdWithEquipment
        List<BorrowingDetail> details = borrowingDetailRepository.findByRecordIdWithEquipment(recordId);
        List<String> insufficientItems = new ArrayList<>();
        for (BorrowingDetail detail : details) {
            Equipment equipment = equipmentRepository.findByIdWithLock(detail.getEquipment().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị"));
            if (equipment.getQuantityAvailable() < detail.getQuantity()) {
                insufficientItems.add(String.format("%s (cần %d, còn %d)",
                        equipment.getName(), detail.getQuantity(), equipment.getQuantityAvailable()));
            }
        }
        if (!insufficientItems.isEmpty()) {
            throw new InsufficientStockException("Không đủ tồn kho: " + String.join(", ", insufficientItems));
        }

        for (BorrowingDetail detail : details) {
            Equipment equipment = equipmentRepository.findByIdWithLock(detail.getEquipment().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị"));
            equipment.setQuantityAvailable(equipment.getQuantityAvailable() - detail.getQuantity());
            equipmentRepository.save(equipment);
        }

        record.setAdminNote(adminNote);
        record.setStatus(BorrowingStatus.DISPATCHED);
        borrowingRecordRepository.save(record);
    }

    /**
     * Từ chối yêu cầu mượn thiết bị.
     *
     * @param recordId  Tham số đầu vào recordId
     * @param adminNote Ghi chú từ admin
     */
    @Transactional
    public void rejectDispatch(Long recordId, String adminNote) {
        BorrowingRecord record = borrowingRecordRepository.findByIdWithAssociations(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu mượn"));
        if (record.getStatus() != BorrowingStatus.PENDING_ADMIN_APPROVAL
                && record.getStatus() != BorrowingStatus.PENDING_DISPATCH) {
            throw new BusinessException("Phiếu không ở trạng thái chờ duyệt cấp phát");
        }
        record.setAdminNote(adminNote);
        record.setStatus(BorrowingStatus.REJECTED_BY_ADMIN);
        borrowingRecordRepository.save(record);
    }

    /**
     * Thực hiện hoàn trả thiết bị vào kho. Tăng số lượng khả dụng trong kho tương
     * ứng và kiểm kho an toàn.
     * 
     * <p>
     * <strong>Lưu ý sửa lỗi (Bug #19: Sử dụng findByIdWithAssociations)</strong>
     * </p>
     * 
     * @param recordId Tham số đầu vào recordId
     */
    @Transactional
    public void returnEquipment(Long recordId) {
        // Hoàn trả tồn kho và cập nhật trạng thái phiếu.
        // Bug #19: Sử dụng findByIdWithAssociations
        BorrowingRecord record = borrowingRecordRepository.findByIdWithAssociations(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu mượn"));
        if (record.getStatus() != BorrowingStatus.DISPATCHED) {
            throw new BusinessException("Chỉ hoàn trả phiếu đã cấp phát");
        }

        // Bug #19: Sử dụng findByRecordIdWithEquipment
        List<BorrowingDetail> details = borrowingDetailRepository.findByRecordIdWithEquipment(recordId);
        for (BorrowingDetail detail : details) {
            // Bug #11: Dùng pessimistic lock tương tự dispatchEquipment để tránh lost
            // update
            // khi hai request hoàn trả cùng lúc đọc cùng một giá trị quantityAvailable.
            Equipment equipment = equipmentRepository.findByIdWithLock(detail.getEquipment().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị"));
            int updated = equipment.getQuantityAvailable() + detail.getQuantity();
            // Bug #14: Không được silent clamp. Nếu trả vượt tổng số lượng, đây là dấu hiệu
            // corruption.
            // Throw exception để operator biết và xử lý thủ công.
            if (updated > equipment.getQuantityTotal()) {
                throw new BusinessException(String.format(
                        "Dữ liệu kho bất nhất quán: %s (%d trả + %d hiện tại > %d tổng)",
                        equipment.getName(), detail.getQuantity(),
                        equipment.getQuantityAvailable(), equipment.getQuantityTotal()));
            }
            equipment.setQuantityAvailable(updated);
            equipmentRepository.save(equipment);
        }
        record.setStatus(BorrowingStatus.RETURNED);
        borrowingRecordRepository.save(record);
    }
}

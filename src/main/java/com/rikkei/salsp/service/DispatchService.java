package com.rikkei.salsp.service;

import com.rikkei.salsp.dto.DispatchQueueItemDto;
import com.rikkei.salsp.entity.BorrowingDetail;
import com.rikkei.salsp.entity.BorrowingRecord;
import com.rikkei.salsp.entity.BorrowingStatus;
import com.rikkei.salsp.entity.Equipment;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.InsufficientStockException;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.repository.BorrowingDetailRepository;
import com.rikkei.salsp.repository.BorrowingRecordRepository;
import com.rikkei.salsp.repository.EquipmentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DispatchService {

    private final BorrowingRecordRepository borrowingRecordRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;
    private final EquipmentRepository equipmentRepository;

    public List<DispatchQueueItemDto> getPendingDispatchQueue() {
        return borrowingRecordRepository.findByStatusWithDetails(BorrowingStatus.PENDING_DISPATCH).stream()
            .map(record -> {
                DispatchQueueItemDto dto = new DispatchQueueItemDto();
                dto.setRecordId(record.getId());
                dto.setStudentName(record.getSession().getStudent().getProfile().getFullName());
                dto.setSessionDate(record.getSession().getSessionDate());
                dto.setStatus(record.getStatus().name());
                List<String> summary = record.getDetails().stream()
                    .map(detail -> detail.getEquipment().getName() + " x" + detail.getQuantity())
                    .collect(Collectors.toList());
                dto.setEquipmentSummary(summary);
                return dto;
            })
            .collect(Collectors.toList());
    }

    public DispatchQueueItemDto getDispatchDetail(Long recordId) {
        // Bug #19: Dùng findByIdWithAssociations để tránh N+1 khi truy cập session -> student -> profile
        BorrowingRecord record = borrowingRecordRepository.findByIdWithAssociations(recordId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu mượn"));
        DispatchQueueItemDto dto = new DispatchQueueItemDto();
        dto.setRecordId(record.getId());
        dto.setStudentName(record.getSession().getStudent().getProfile().getFullName());
        dto.setSessionDate(record.getSession().getSessionDate());
        dto.setStatus(record.getStatus().name());
        // Bug #19: Dùng findByRecordIdWithEquipment để tránh N+1 khi gọi detail.getEquipment().getName()
        List<String> summary = borrowingDetailRepository.findByRecordIdWithEquipment(recordId).stream()
            .map(detail -> detail.getEquipment().getName() + " x" + detail.getQuantity())
            .collect(Collectors.toList());
        dto.setEquipmentSummary(summary);
        return dto;
    }

    @Transactional
    public void dispatchEquipment(Long recordId) {
        // Khóa dòng thiết bị và xuất kho nếu đủ tồn.
        // Bug #19: Sử dụng findByIdWithAssociations
        BorrowingRecord record = borrowingRecordRepository.findByIdWithAssociations(recordId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu mượn"));
        if (record.getStatus() != BorrowingStatus.PENDING_DISPATCH) {
            throw new BusinessException("Phiếu không ở trạng thái chờ cấp phát");
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

        record.setStatus(BorrowingStatus.DISPATCHED);
        borrowingRecordRepository.save(record);
    }

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
            // Bug #11: Dùng pessimistic lock tương tự dispatchEquipment để tránh lost update
            // khi hai request hoàn trả cùng lúc đọc cùng một giá trị quantityAvailable.
            Equipment equipment = equipmentRepository.findByIdWithLock(detail.getEquipment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị"));
            int updated = equipment.getQuantityAvailable() + detail.getQuantity();
            // Bug #14: Không được silent clamp. Nếu trả vượt tổng số lượng, đây là dấu hiệu corruption.
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




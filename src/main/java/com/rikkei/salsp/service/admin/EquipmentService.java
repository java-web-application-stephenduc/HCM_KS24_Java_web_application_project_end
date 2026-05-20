package com.rikkei.salsp.service.admin;

import com.rikkei.salsp.dto.admin.EquipmentDto;
import com.rikkei.salsp.entity.equipment.Equipment;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.ResourceNotFoundException;
import com.rikkei.salsp.repository.equipment.EquipmentRepository;
import com.rikkei.salsp.repository.equipment.BorrowingDetailRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý nghiệp vụ quản lý danh mục thiết bị, bao gồm thêm mới, cập nhật thông tin và kiểm tra cảnh báo tồn kho thấp.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;

    /**
     * Tìm kiếm.

     * @return Kết quả trả về của phương thức
     */
    public List<EquipmentDto> findAll() {
        return equipmentRepository.findByDeletedFalse().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm.
     * @param id Tham số đầu vào id

     * @return Kết quả trả về của phương thức
     */
    public EquipmentDto findById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
            .filter(eq -> !eq.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));
        return toDto(equipment);
    }

    /**
     * Phương thức xử lý nghiệp vụ search.
     * @param keyword Tham số đầu vào keyword

     * @return Kết quả trả về của phương thức
     */
    public List<EquipmentDto> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return equipmentRepository.findByNameContainingIgnoreCaseAndDeletedFalse(keyword.trim()).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Tạo mới.
     * @param dto Tham số đầu vào dto
     */
    @Transactional
    public void create(EquipmentDto dto) {
        if (dto.getQuantityAvailable() > dto.getQuantityTotal()) {
            throw new BusinessException("Số lượng khả dụng không được lớn hơn tổng số lượng");
        }
        // Tạo thiết bị mới với tồn kho ban đầu.
        Equipment equipment = new Equipment();
        equipment.setName(dto.getName().trim());
        equipment.setDescription(dto.getDescription());
        equipment.setCategory(dto.getCategory());
        equipment.setQuantityTotal(dto.getQuantityTotal());
        equipment.setQuantityAvailable(dto.getQuantityAvailable());
        equipmentRepository.save(equipment);
    }

    /**
     * Cập nhật.

     * <p><strong>Lưu ý sửa lỗi (Bug #12: Dùng findByIdWithLock để tránh lost update khi hai admin)</strong></p>
     * @param id Tham số đầu vào id
     * @param dto Tham số đầu vào dto
     */
    @Transactional
    public void update(Long id, EquipmentDto dto) {
        // Bug #12: Dùng findByIdWithLock để tránh lost update khi hai admin
        // cùng lúc sửa cùng một thiết bị (race condition).
        Equipment equipment = equipmentRepository.findByIdWithLock(id)
            .filter(eq -> !eq.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        int oldTotal = equipment.getQuantityTotal();
        int newTotal = dto.getQuantityTotal();
        int diff = newTotal - oldTotal;

        equipment.setName(dto.getName().trim());
        equipment.setDescription(dto.getDescription());
        equipment.setCategory(dto.getCategory());
        equipment.setQuantityTotal(newTotal);

        int newAvailable = equipment.getQuantityAvailable() + diff;
        // Bug #31: Không được silent clamp về 0. Nếu quantityAvailable âm,
        // đây là dấu hiệu dữ liệu kho bất nhất quán cần operator xử lý.
        if (newAvailable < 0) {
            throw new BusinessException(String.format(
                "Không thể giảm tổng số lượng: số có sẵn sẽ âm (%d). Kiểm tra tồn kho trước khi cập nhật.",
                newAvailable));
        }
        if (newAvailable > newTotal) {
            newAvailable = newTotal;
        }
        equipment.setQuantityAvailable(newAvailable);

        equipmentRepository.save(equipment);
    }

    /**
     * Xóa.
     * @param id Tham số đầu vào id
     */
    @Transactional
    public void delete(Long id) {
        // Xóa mềm để đảm bảo lịch sử mượn.
        Equipment equipment = equipmentRepository.findById(id)
            .filter(eq -> !eq.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));
        if (isInActiveBorrowing(id)) {
            throw new BusinessException("Thiết bị đang được mượn, không thể xóa");
        }
        equipment.setDeleted(true);
        equipmentRepository.save(equipment);
    }

    /**
     * Phương thức xử lý nghiệp vụ isInActiveBorrowing.
     * @param equipmentId Tham số đầu vào equipmentId

     * @return Kết quả trả về của phương thức
     */
    private boolean isInActiveBorrowing(Long equipmentId) {
        return borrowingDetailRepository.existsByEquipmentIdAndActiveStatus(equipmentId);
    }

    /**
     * Phương thức xử lý nghiệp vụ toDto.
     * @param equipment Tham số đầu vào equipment

     * @return Kết quả trả về của phương thức
     */
    private EquipmentDto toDto(Equipment equipment) {
        EquipmentDto dto = new EquipmentDto();
        dto.setId(equipment.getId());
        dto.setName(equipment.getName());
        dto.setDescription(equipment.getDescription());
        dto.setCategory(equipment.getCategory());
        dto.setQuantityTotal(equipment.getQuantityTotal());
        dto.setQuantityAvailable(equipment.getQuantityAvailable());
        return dto;
    }
}



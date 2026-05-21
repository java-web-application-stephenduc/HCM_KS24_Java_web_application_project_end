package com.rikkei.salsp.entity.equipment;

import com.rikkei.salsp.entity.session.MentoringSession;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Thực thể BorrowingRecord đại diện cho một phiếu mượn thiết bị gắn liền với buổi cố vấn.
 *
 * PHÂN LOẠI: Header-Detail Pattern
 * - BorrowingRecord: Header (phiếu mượn cấp cao)
 * - BorrowingDetail: Line items (chi tiết từng loại thiết bị)
 *
 * VÒ ĐỜI CỦA MỘT PHIẾU MƯỢN:
 * 1. PENDING: Sinh viên vừa yêu cầu, chờ Admin phê duyệt & xuất kho
 * 2. APPROVED: Admin phê duyệt, thiết bị đã bị lock (quantityAvailable giảm)
 * 3. BORROWED: Sinh viên đã nhận thiết bị
 * 4. RETURNED: Sinh viên trả hết, quantityAvailable cộng lại
 * 5. OVERDUE: Quá hạn trả (tự động detect bởi BorrowingOverdueScheduler)
 * 6. REJECTED / CANCELLED: Admin hoặc sinh viên hủy
 *
 * LIÊN KẾT:
 * - @ManyToOne session -> MentoringSession: Phiếu mượn gắn liền với buổi cố vấn
 * - @OneToMany details -> BorrowingDetail[]: Danh sách thiết bị mượn (1..N)
 *
 * GIAO DỊCH (Transaction):
 * - Khi APPROVED: Lock Equipment, giảm quantityAvailable
 * - Khi RETURNED: Update BorrowingDetail.returnedDate, tăng quantityAvailable
 * - Nếu lỗi: Rollback toàn bộ để dữ liệu nhất quán
 *
 * CASCADE & ORPHAN REMOVAL:
 * - cascade = ALL: Lưu/xóa Record -> tự động lưu/xóa Details
 * - orphanRemoval = true: Xóa Detail khỏi list -> xóa khỏi DB (tránh FK orphan)
 */
@Entity
@Table(name = "borrowing_records")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BorrowingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Mối quan hệ tới buổi cố vấn (MentoringSession).
     *
     * MỤC ĐÍCH:
     * - Một buổi cố vấn có thể có 0 hoặc 1 phiếu mượn (nếu không cần thiết bị)
     * - Phiếu mượn phải gắn với 1 buổi hợp lệ (CONFIRMED trở lên)
     *
     * LAZY FETCH: Chỉ load MentoringSession khi cần
     * @JoinColumn: Foreign Key tên "session_id" ở bảng borrowing_records
     *
     * BUSINESS RULE: Không được NULL (nullable = false)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private MentoringSession session;

    /**
     * Trạng thái hiện tại của phiếu mượn (@Enumerated STRING).
     *
     * GIÁ TRỊ:
     * - PENDING: Sinh viên vừa yêu cầu, chờ Admin duyệt
     * - APPROVED: Admin duyệt, thiết bị đã bị reserve (quantityAvailable -= qty_requested)
     * - BORROWED: Sinh viên đã nhận thiết bị
     * - RETURNED: Sinh viên trả hết thiết bị (quantityAvailable += qty_returned)
     * - OVERDUE: Qua hạn trả (tự động detect daily bởi BorrowingOverdueScheduler)
     * - REJECTED / CANCELLED: Admin hoặc sinh viên hủy phiếu
     *
     * LUỒNG CHÍNH: PENDING -> APPROVED -> BORROWED -> RETURNED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BorrowingStatus status;

    /**
     * Ghi chú từ giảng viên (ví dụ: "Cần oscilloscope để demo mạch").
     * Stored: Nguyên bản, nhập từ form booking hoặc phiếu mượn.
     * Dùng: Giúp Admin hiểu mục đích mượn -> phê duyệt nhanh hơn.
     */
//    @Column(name = "lecturer_note", columnDefinition = "TEXT")
    private String lecturerNote;

    /**
     * Ghi chú từ Admin (ví dụ: "Thiếu multimeter, thay thế bằng universal meter").
     * Stored: Khi Admin phê duyệt hoặc từ chối.
     * Dùng: Ghi lại lý do thay đổi, từ chối, hay bất kỳ thay đổi nào.
     */
//    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    /**
     * Dấu thời gian tạo phiếu mượn.
     * @CreationTimestamp tự động set khi INSERT.
     * Dùng: Tính tổng thời gian mượn, audit trail.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Dấu thời gian cập nhật gần nhất.
     * @UpdateTimestamp tự động update khi thực hiện UPDATE.
     * Dùng: Kiểm soát khi phiếu được cập nhật lần cuối.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Danh sách chi tiết thiết bị trong phiếu mượn (1--N relation).
     *
     * THIẾT KẾ:
     * - mappedBy = "record": BorrowingDetail giữ foreign key (record_id)
     * - cascade = ALL: Lưu/xóa Record -> tự động lưu/xóa Details
     * - orphanRemoval = true: Xóa Detail khỏi list -> DELETE ở database
     *   (tránh orphan row gây lỗi FK constraint)
     *
     * LOGIC:
     * - Một phiếu mượn có 1..N loại thiết bị
     * - Ví dụ: Record 1 có Details: [Oscilloscope x2, Multimeter x1]
     *
     * INITIALIZATION:
     * - Mặc định = new ArrayList<>() (trống, để insert Details sau)
     *
     * USAGE:
     * - Thêm detail: record.getDetails().add(new BorrowingDetail(...))
     * - Xóa detail: record.getDetails().remove(detail) -> auto DELETE từ DB
     * - Save: recordRepository.save(record) -> persist Details qua cascade
     */
    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BorrowingDetail> details = new ArrayList<>();
}

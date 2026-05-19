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
 * Thực thể đại diện cho một phiếu mượn thiết bị gắn liền với ca cố vấn học thuật.
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private MentoringSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BorrowingStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Danh sách chi tiết thiết bị trong phiếu mượn.
     * cascade = ALL: lưu/xóa BorrowingRecord sẽ tự động lưu/xóa các BorrowingDetail con.
     * orphanRemoval = true: xóa detail khỏi list sẽ xóa khỏi DB, tránh FK constraint khi delete record.
     */
    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BorrowingDetail> details = new ArrayList<>();
}


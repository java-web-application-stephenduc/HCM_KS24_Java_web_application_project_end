package com.rikkei.salsp.entity.session;

import com.rikkei.salsp.entity.user.User;

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
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Thực thể MentoringSession đại diện cho một buổi cố vấn học thuật giữa giảng viên và sinh viên.
 *
 * MỤC ĐÍCH: Quản lý lịch hẹn, trạng thái approval, và liên kết đến các buổi có yêu cầu mượn thiết bị.
 *
 * VÒ ĐỜI CỦA MỘT BUỔI CỐ VẤN:
 * 1. PENDING: Sinh viên vừa đặt lịch, chờ giảng viên phê duyệt
 * 2. CONFIRMED: Giảng viên duyệt, buổi học được xác nhận
 * 3. COMPLETED: Buổi thực hiện xong, giảng viên ghi nhận kết quả/đánh giá
 * 4. REJECTED / CANCELED / CANCELED_BY_STUDENT: Buổi học bị hủy (lý do ở rejectionReason)
 *
 * LIÊN KẾT DỮ LIỆU:
 * - @ManyToOne(fetch=LAZY) student -> User: Sinh viên đặt lịch
 * - @ManyToOne(fetch=LAZY) lecturer -> User: Giảng viên hướng dẫn
 * - @OneToOne AcademicEvaluation: Kết quả đánh giá sau buổi (có thể null nếu chưa đánh giá)
 * - @OneToMany BorrowingRecord: Có thể có 0 hoặc nhiều phiếu mượn thiết bị
 *
 * FETCH TYPE:
 * - LAZY: Student/Lecturer không auto-load khi query Session (tiết kiệm query)
 * - Repository dùng @Query + JOIN FETCH khi cần load chi tiết
 *
 * SLOT CONFLICT DETECTION:
 * - Giảng viên không thể đặt 2 buổi trùng giờ cùng ngày (ở BookingService.checkConflict)
 * - Kiểm tra: start_time < other.end_time AND end_time > other.start_time
 */
@Entity
@Table(name = "mentoring_sessions")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MentoringSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Sinh viên tham gia buổi cố vấn (LAZY fetch).
     * LAZY: Chỉ load khi gọi getStudent() (không auto-load trong query)
     * Giúp tiết kiệm query khi chỉ cần liệt kê sessions mà không cần tên student.
     *
     * Khi cần thông tin student, Repository dùng JOIN FETCH để nạp trong 1 query.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /**
     * Giảng viên phụ trách buổi cố vấn (LAZY fetch).
     * Kiểm soát xung đột lịch: không được đặt 2 buổi trùng giờ cùng ngày.
     *
     * Khi cần thông tin giảng viên, dùng JOIN FETCH để tối ưu.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private User lecturer;

    /**
     * Ngày diễn ra buổi cố vấn (LocalDate - không có giờ).
     * Dùng kiểm soát xung đột: so sánh sessionDate và (startTime, endTime).
     */
    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    /**
     * Giờ bắt đầu buổi cố vấn (LocalTime).
     * Ví dụ: 09:00 (9 giờ sáng).
     */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /**
     * Giờ kết thúc buổi cố vấn (LocalTime).
     * Ví dụ: 10:30 (10 giờ 30 phút).
     * Thư viện: endTime > startTime (validate ở Service)
     */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Trạng thái hiện tại của buổi cố vấn (@Enumerated STRING).
     *
     * GIÁ TRỊ:
     * - PENDING: Sinh viên vừa đặt, chờ giảng viên duyệt
     * - CONFIRMED: Giảng viên chấp nhận, buổi được xác nhận
     * - COMPLETED: Buổi kết thúc, giảng viên ghi nhận điểm/feedback
     * - REJECTED: Giảng viên từ chối (lý do ở rejectionReason)
     * - CANCELLED / CANCELED_BY_STUDENT: Buổi bị hủy bởi admin/hệ thống
     *
     * QUERY: Các repository method thường loại trừ REJECTED/CANCELLED khi thống kê.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    /**
     * Nội dung / lý do sinh viên đặt lịch (ví dụ: "Hỏi về mạch điện", "Review bài tập").
     * Lưu dạng TEXT (unlimited length).
     * Có thể NULL nếu sinh viên không nhập ghi chú.
     */
    @Column(columnDefinition = "TEXT")
    private String note;

    /**
     * Lý do giảng viên từ chối hoặc Admin hủy buổi.
     * Ví dụ: "Giảng viên bị ốm", "Lịch trùng", "Request không hợp lệ".
     *
     * LỬU ÝỨ:
     * - Chỉ set khi status = REJECTED / CANCELLED / CANCELED_BY_STUDENT
     * - Mặc định NULL khi PENDING hoặc CONFIRMED
     *
     * LƯU ÝỨ: Sinh viên sẽ nhìn thấy lý do này trên giao diện.
     */
//    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    /**
     * Dấu thời gian tạo bản ghi.
     * @CreationTimestamp tự động set khi INSERT, không thể UPDATE.
     * Dùng cho audit trail và debug (sinh viên đặt lịch lúc nào?).
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

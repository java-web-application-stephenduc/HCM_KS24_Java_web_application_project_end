package com.rikkei.salsp.repository.session;

import com.rikkei.salsp.entity.session.MentoringSession;
import com.rikkei.salsp.entity.session.SessionStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository quản lý truy vấn dữ liệu thực thể MentoringSession (buổi cố vấn).
 * Các truy vấn phức tạp đều dùng JOIN FETCH để giải quyết N+1,
 * và native query để phục vụ báo cáo tổng hợp đa bảng.
 */
public interface MentoringSessionRepository extends JpaRepository<MentoringSession, Long> {
    @Override
    @EntityGraph(attributePaths = {"student", "student.profile", "lecturer", "lecturer.profile"})
    Page<MentoringSession> findAll(Pageable pageable);

    /*
     * Kiểm tra xung đột lịch giảng viên: so sánh khoảng thời gian (start-end)
     * với các buổi đã tồn tại trong cùng ngày, loại trừ các trạng thái đã hủy.
     * Dùng JPQL subquery COUNT > 0 thay vì truy vấn toàn bộ record.
     */
    @Query("""
                SELECT COUNT(ms) > 0 FROM MentoringSession ms
                WHERE ms.lecturer.id = :lecturerId
                  AND ms.sessionDate = :date
                  AND ms.startTime < :endTime
                  AND ms.endTime > :startTime
                                    AND ms.status NOT IN (
                                            com.rikkei.salsp.entity.session.SessionStatus.CANCELLED,
                                            com.rikkei.salsp.entity.session.SessionStatus.REJECTED,
                                            com.rikkei.salsp.entity.session.SessionStatus.CANCELED_BY_STUDENT
                                    )
            """)
    boolean existsConflict(@Param("lecturerId") Long lecturerId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /*
     * Truy vấn buổi cố vấn kèm student + profile + lecturer trong một câu JPQL.
     * JOIN FETCH giải quyết triệt để N+1 khi controller/service gọi
     * getStudent().getProfile().getFullName().
     */
    @Query("""
                SELECT ms FROM MentoringSession ms
                JOIN FETCH ms.student st
                LEFT JOIN FETCH st.profile p
                JOIN FETCH ms.lecturer l
                WHERE ms.id = :id
            """)
    Optional<MentoringSession> findByIdWithStudentAndLecturer(@Param("id") Long id);

    /* Sinh viên xem danh sách buổi cố vấn đã đặt, sắp xếp mới nhất xuống dưới */
    List<MentoringSession> findByStudentIdOrderBySessionDateDesc(Long studentId);

    /* Kiểm tra sinh viên có quyền truy cập buổi cố vấn không (dùng trong CancellationService) */
    boolean existsByIdAndStudentId(Long id, Long studentId);

    @Query("SELECT COUNT(ms) FROM MentoringSession ms WHERE ms.student.id = :studentId AND ms.status IN :statuses")
    long countByStudentIdAndStatusIn(@Param("studentId") Long studentId,
            @Param("statuses") List<SessionStatus> statuses);

    /* Sinh viên xem danh sách buổi cố vấn kèm thông tin giảng viên (JOIN FETCH chống N+1) */
    @Query("""
                SELECT ms FROM MentoringSession ms
                JOIN FETCH ms.lecturer
                WHERE ms.student.id = :studentId
                ORDER BY ms.sessionDate DESC, ms.startTime DESC
            """)
    List<MentoringSession> findByStudentIdWithLecturerProfile(@Param("studentId") Long studentId);

    /* Giảng viên lọc danh sách buổi cố vấn theo trạng thái (VD: PENDING — chờ xử lý) */
    List<MentoringSession> findByLecturerIdAndStatus(Long lecturerId, SessionStatus status);

    /*
     * Giảng viên xem hàng đợi (queue) sinh viên chờ xử lý theo một trạng thái cụ thể.
     * JOIN FETCH student + profile để render tên và avatar sinh viên mà không gây N+1.
     */
    @Query("""
                SELECT ms FROM MentoringSession ms
                JOIN FETCH ms.student st
                LEFT JOIN FETCH st.profile p
                WHERE ms.lecturer.id = :lecturerId
                  AND ms.status = :status
                ORDER BY ms.sessionDate DESC, ms.startTime DESC
            """)
    List<MentoringSession> findQueueWithStudent(@Param("lecturerId") Long lecturerId,
            @Param("status") SessionStatus status);

    /*
     * Giảng viên xem danh sách buổi cố vấn theo nhiều trạng thái cùng lúc.
     * Dùng IN :statuses + JOIN FETCH giúp nạp student + profile trong một query.
     * Được dùng để phân tách tab "đang hoạt động" và "lịch sử" trên giao diện.
     */
    @Query("""
                SELECT ms FROM MentoringSession ms
                JOIN FETCH ms.student st
                LEFT JOIN FETCH st.profile p
                WHERE ms.lecturer.id = :lecturerId
                  AND ms.status IN :statuses
                ORDER BY ms.sessionDate DESC, ms.startTime DESC
            """)
    List<MentoringSession> findSessionsByLecturerIdAndStatuses(@Param("lecturerId") Long lecturerId,
            @Param("statuses") List<SessionStatus> statuses);

    /*
     * Giảng viên xem lịch hẹn trong ngày, loại trừ các trạng thái đã hủy.
     * JOIN FETCH student + profile, sắp xếp theo giờ bắt đầu để hiển thị timeline.
     * Dùng cho Dashboard "Lịch hẹn hôm nay".
     */
    @Query("""
                SELECT ms FROM MentoringSession ms
                JOIN FETCH ms.student st
                LEFT JOIN FETCH st.profile p
                WHERE ms.lecturer.id = :lecturerId
                  AND ms.sessionDate = :date
                                    AND ms.status NOT IN (
                                            com.rikkei.salsp.entity.session.SessionStatus.CANCELLED,
                                            com.rikkei.salsp.entity.session.SessionStatus.REJECTED,
                                            com.rikkei.salsp.entity.session.SessionStatus.CANCELED_BY_STUDENT
                                    )
                ORDER BY ms.startTime
            """)
    List<MentoringSession> findByLecturerIdAndDateWithStudent(@Param("lecturerId") Long lecturerId,
            @Param("date") LocalDate date);

    /*
     * Đếm số buổi cố vấn hợp lệ (không bị hủy) của giảng viên trong khoảng thời gian.
     * Dùng COUNT với BETWEEN để lấy thống kê Dashboard (tháng này).
     */
    @Query("""
                SELECT COUNT(ms) FROM MentoringSession ms
                WHERE ms.lecturer.id = :lecturerId
                  AND ms.sessionDate BETWEEN :startDate AND :endDate
                  AND ms.status NOT IN (
                      com.rikkei.salsp.entity.session.SessionStatus.CANCELLED,
                      com.rikkei.salsp.entity.session.SessionStatus.REJECTED,
                      com.rikkei.salsp.entity.session.SessionStatus.CANCELED_BY_STUDENT
                  )
            """)
    long countByLecturerIdAndDateBetween(@Param("lecturerId") Long lecturerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /* Đếm tổng số buổi cố vấn theo trạng thái (dùng cho Admin Dashboard) */
    @Query("SELECT COUNT(ms) FROM MentoringSession ms WHERE ms.status = :status")
    long countByStatus(@Param("status") SessionStatus status);

    @Query("""
                SELECT ms.lecturer.id AS lecturerId, COUNT(ms) AS cnt
                FROM MentoringSession ms
                WHERE ms.status = 'COMPLETED'
                GROUP BY ms.lecturer.id
                ORDER BY cnt DESC
            """)
    /*
     * Top giảng viên có nhiều buổi COMPLETED nhất — dùng cho Admin Dashboard.
     * Group by + Order by + Pageable phân trang kết quả hiệu quả.
     */
    List<Object[]> findTopLecturers(Pageable pageable);

    /*
     * Native query tổng hợp lịch sử học tập của sinh viên: join 6 bảng
     * (mentoring_sessions + users + user_profiles + lecturers + departments
     * + academic_evaluations + borrowing_records + borrowing_details + equipments)
     * trong một lần truy vấn duy nhất, trả về projection.
     * Dùng LEFT JOIN cho evaluation và borrow (có thể null).
     */
    @Query(value = """
                SELECT
                    ms.id AS sessionId,
                    ms.session_date AS sessionDate,
                    ms.start_time AS startTime,
                    ms.end_time AS endTime,
                    ms.status AS status,
                    up.full_name AS lecturerName,
                    d.name AS departmentName,
                    ae.score AS score,
                    ae.feedback AS feedback,
                    ms.note AS note,
                    ms.rejection_reason AS rejectionReason,
                    br.id AS recordId,
                    br.status AS borrowStatus,
                    e.name AS equipmentName,
                    bd.quantity AS quantity
                FROM mentoring_sessions ms
                    JOIN users u ON ms.lecturer_id = u.id
                    JOIN user_profiles up ON u.id = up.user_id
                    JOIN lecturers l ON u.id = l.user_id
                    JOIN departments d ON l.department_id = d.id
                    LEFT JOIN academic_evaluations ae ON ms.id = ae.session_id
                    LEFT JOIN borrowing_records br ON ms.id = br.session_id
                    LEFT JOIN borrowing_details bd ON br.id = bd.record_id
                    LEFT JOIN equipments e ON bd.equipment_id = e.id
                WHERE ms.student_id = :studentId
                ORDER BY ms.session_date DESC, ms.start_time DESC
            """, nativeQuery = true)
    List<AcademicHistoryProjection> findAcademicHistory(@Param("studentId") Long studentId);

    /*
     * Native query chi tiết một buổi cố vấn, join đầy đủ các bảng liên quan.
     * Dùng cho trang xem chi tiết lịch sử sinh viên.
     */
    @Query(value = """
                SELECT
                    ms.id AS sessionId,
                    ms.session_date AS sessionDate,
                    ms.start_time AS startTime,
                    ms.end_time AS endTime,
                    ms.status AS status,
                    up.full_name AS lecturerName,
                    d.name AS departmentName,
                    ae.score AS score,
                    ae.feedback AS feedback,
                    ms.note AS note,
                    ms.rejection_reason AS rejectionReason,
                    br.id AS recordId,
                    br.status AS borrowStatus,
                    e.name AS equipmentName,
                    bd.quantity AS quantity
                FROM mentoring_sessions ms
                    JOIN users u ON ms.lecturer_id = u.id
                    JOIN user_profiles up ON u.id = up.user_id
                    JOIN lecturers l ON u.id = l.user_id
                    JOIN departments d ON l.department_id = d.id
                    LEFT JOIN academic_evaluations ae ON ms.id = ae.session_id
                    LEFT JOIN borrowing_records br ON ms.id = br.session_id
                    LEFT JOIN borrowing_details bd ON br.id = bd.record_id
                    LEFT JOIN equipments e ON bd.equipment_id = e.id
                WHERE ms.id = :sessionId
                ORDER BY ms.session_date DESC, ms.start_time DESC
            """, nativeQuery = true)
    List<AcademicHistoryProjection> findAcademicHistoryBySessionId(@Param("sessionId") Long sessionId);

    /*
     * Đếm số buổi hoàn thành trong tháng — dùng cho thống kê Admin Dashboard
     * (biểu đồ xu hướng theo tháng).
     */
    @Query("""
        SELECT COUNT(ms) FROM MentoringSession ms
        WHERE ms.status = com.rikkei.salsp.entity.session.SessionStatus.COMPLETED
          AND MONTH(ms.sessionDate) = :month
          AND YEAR(ms.sessionDate) = :year
    """)
    long countCompletedSessionsByMonthAndYear(@Param("month") int month, @Param("year") int year);


    /*
     * Projection interface cho native query lịch sử học tập (findAcademicHistory).
     * Dùng java.time (LocalDate, LocalTime) thay vì java.sql để tương thích Hibernate 6.
     * Native query join 6 bảng trả về dữ liệu gộp trong một lần truy vấn duy nhất.
     */
    interface AcademicHistoryProjection {
        Long getSessionId();
        java.time.LocalDate getSessionDate();
        java.time.LocalTime getStartTime();
        java.time.LocalTime getEndTime();
        String getStatus();
        String getLecturerName();
        String getDepartmentName();
        Integer getScore();
        String getFeedback();
        String getNote();
        String getRejectionReason();
        String getEquipmentName();
        Integer getQuantity();
    }
}

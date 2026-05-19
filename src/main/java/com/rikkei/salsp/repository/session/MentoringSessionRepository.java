package com.rikkei.salsp.repository.session;

import com.rikkei.salsp.entity.session.MentoringSession;
import com.rikkei.salsp.entity.session.SessionStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

/**
 * Repository quản lý truy vấn dữ liệu thực thể MentoringSession.
 */
public interface MentoringSessionRepository extends JpaRepository<MentoringSession, Long> {

    @Query("""
        SELECT COUNT(ms) > 0 FROM MentoringSession ms
        WHERE ms.lecturer.id = :lecturerId
          AND ms.sessionDate = :date
          AND ms.startTime < :endTime
          AND ms.endTime > :startTime
          AND ms.status <> com.rikkei.salsp.entity.SessionStatus.CANCELLED
    """)
    boolean existsConflict(@Param("lecturerId") Long lecturerId,
                           @Param("date") LocalDate date,
                           @Param("startTime") LocalTime startTime,
                           @Param("endTime") LocalTime endTime);

    @Query("""
        SELECT ms FROM MentoringSession ms
        JOIN FETCH ms.student st
        JOIN FETCH st.profile p
        JOIN FETCH ms.lecturer l
        WHERE ms.id = :id
    """)
    Optional<MentoringSession> findByIdWithStudentAndLecturer(@Param("id") Long id);

    /**
     * Tìm kiếm buổi cố vấn học thuật.
     * @param studentId Tham số đầu vào studentId

     * @return Kết quả trả về của phương thức
     */
    List<MentoringSession> findByStudentIdOrderBySessionDateDesc(Long studentId);

    @Query("SELECT COUNT(ms) FROM MentoringSession ms WHERE ms.student.id = :studentId AND ms.status IN :statuses")
    long countByStudentIdAndStatusIn(@Param("studentId") Long studentId, @Param("statuses") List<SessionStatus> statuses);

    @Query("""
        SELECT ms FROM MentoringSession ms
        JOIN FETCH ms.lecturer
        WHERE ms.student.id = :studentId
        ORDER BY ms.sessionDate DESC, ms.startTime DESC
    """)
    List<MentoringSession> findByStudentIdWithLecturerProfile(@Param("studentId") Long studentId);

    /**
     * Tìm kiếm giảng viên.
     * @param lecturerId Tham số đầu vào lecturerId
     * @param status Tham số đầu vào status

     * @return Kết quả trả về của phương thức
     */
    List<MentoringSession> findByLecturerIdAndStatus(Long lecturerId, SessionStatus status);

    @Query("""
        SELECT ms FROM MentoringSession ms
        JOIN FETCH ms.student st
        JOIN FETCH st.profile p
        WHERE ms.lecturer.id = :lecturerId
          AND ms.status = :status
        ORDER BY ms.sessionDate DESC, ms.startTime DESC
    """)
    List<MentoringSession> findQueueWithStudent(@Param("lecturerId") Long lecturerId,
                                                @Param("status") SessionStatus status);

    @Query("""
        SELECT ms FROM MentoringSession ms
        JOIN FETCH ms.student st
        JOIN FETCH st.profile p
        WHERE ms.lecturer.id = :lecturerId
          AND ms.sessionDate = :date
          AND ms.status <> 'CANCELLED'
        ORDER BY ms.startTime
    """)
    List<MentoringSession> findByLecturerIdAndDateWithStudent(@Param("lecturerId") Long lecturerId,
                                                              @Param("date") LocalDate date);

    @Query("SELECT COUNT(ms) FROM MentoringSession ms WHERE ms.lecturer.id = :lecturerId AND ms.sessionDate BETWEEN :startDate AND :endDate AND ms.status <> 'CANCELLED'")
    long countByLecturerIdAndDateBetween(@Param("lecturerId") Long lecturerId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(ms) FROM MentoringSession ms WHERE ms.status = :status")
    long countByStatus(@Param("status") SessionStatus status);

    @Query("""
        SELECT ms.lecturer.id AS lecturerId, COUNT(ms) AS cnt
        FROM MentoringSession ms
        WHERE ms.status = 'COMPLETED'
        GROUP BY ms.lecturer.id
        ORDER BY cnt DESC
    """)
    /**
     * Tìm kiếm giảng viên.
     * @param pageable Tham số đầu vào pageable

     * @return Kết quả trả về của phương thức
     */
    List<Object[]> findTopLecturers(Pageable pageable);

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

    /**
     * Giao diện projection cho kết quả lịch sử học tập.
     * Dùng java.time thay vì java.sql để tương thích Hibernate 6.
     */
    interface AcademicHistoryProjection {
        /**
         * Lấy thông tin buổi cố vấn học thuật.

         * @return Kết quả trả về của phương thức
         */
        Long getSessionId();
        /**
         * Lấy thông tin buổi cố vấn học thuật.

         * @return Kết quả trả về của phương thức
         */
        java.time.LocalDate getSessionDate();
        /**
         * Lấy thông tin.

         * @return Kết quả trả về của phương thức
         */
        java.time.LocalTime getStartTime();
        /**
         * Lấy thông tin.

         * @return Kết quả trả về của phương thức
         */
        java.time.LocalTime getEndTime();
        /**
         * Lấy thông tin.

         * @return Kết quả trả về của phương thức
         */
        String getStatus();
        /**
         * Lấy thông tin giảng viên.

         * @return Kết quả trả về của phương thức
         */
        String getLecturerName();
        /**
         * Lấy thông tin khoa/ban chuyên môn.

         * @return Kết quả trả về của phương thức
         */
        String getDepartmentName();
        /**
         * Lấy thông tin.

         * @return Kết quả trả về của phương thức
         */
        Integer getScore();
        /**
         * Lấy thông tin.

         * @return Kết quả trả về của phương thức
         */
        String getFeedback();
        /**
         * Lấy thông tin.

         * @return Kết quả trả về của phương thức
         */
        String getNote();
        /**
         * Lấy thông tin thiết bị phòng Lab.

         * @return Kết quả trả về của phương thức
         */
        String getEquipmentName();
        /**
         * Lấy thông tin.

         * @return Kết quả trả về của phương thức
         */
        Integer getQuantity();
    }
}


package com.rikkei.salsp.repository;

import com.rikkei.salsp.entity.MentoringSession;
import com.rikkei.salsp.entity.SessionStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
        String getEquipmentName();
        Integer getQuantity();
    }
}


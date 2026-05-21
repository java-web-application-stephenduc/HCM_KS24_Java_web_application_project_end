package com.rikkei.salsp.exception;

/**
 * Exception khi không tìm thấy resource (404 Not Found).
 *
 * MỤC ĐÍCH: Phân biệt rõ ràng giữa resource không tồn tại vs lỗi khác.
 *
 * KỊCH BẢN SỬ DỤNG:
 * - findById(id) trả về Optional.empty()
 * - User cố truy cập session / equipment / lecturer không tồn tại
 * - CrudRepository.getById() throw EntityNotFoundException
 *
 * HANDLER:
 * - GlobalExceptionHandler.handleNotFound()
 * - Render error/404.html với error message
 *
 * EXAMPLE:
 * User student = userRepository.findById(studentId)
 *     .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên"));
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}


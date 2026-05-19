package com.rikkei.salsp.entity.equipment;

/**
 * Enum định nghĩa trạng thái của phiếu mượn thiết bị.
 */
public enum BorrowingStatus {
    PENDING_LECTURER_APPROVAL,
    PENDING_ADMIN_APPROVAL,
    REJECTED_BY_LECTURER,
    REJECTED_BY_ADMIN,
    PENDING_DISPATCH,
    DISPATCHED,
    RETURNED,
    OVERDUE
}

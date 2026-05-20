package com.rikkei.salsp.service.admin;

import com.rikkei.salsp.repository.equipment.BorrowingRecordRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BorrowingOverdueScheduler {

    private final BorrowingRecordRepository borrowingRecordRepository;

    @Scheduled(cron = "0 5 * * * *")
    @Transactional
    public void markOverdueRecords() {
        int updated = borrowingRecordRepository.markDispatchedAsOverdue(LocalDate.now());
        if (updated > 0) {
            log.info("Marked {} borrowing record(s) as OVERDUE", updated);
        }
    }
}

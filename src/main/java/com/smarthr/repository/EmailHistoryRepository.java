
package com.smarthr.repository;

import com.smarthr.entity.EmailHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface EmailHistoryRepository
        extends JpaRepository<EmailHistory, Long> {

    boolean existsByEmployeeIdAndEmailTypeAndStatusAndSentAtBetween(
            Long employeeId,
            String emailType,
            String status,
            LocalDateTime start,
            LocalDateTime end
    );
}

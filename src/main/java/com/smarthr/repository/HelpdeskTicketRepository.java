
package com.smarthr.repository;

import com.smarthr.entity.HelpdeskTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpdeskTicketRepository extends JpaRepository<HelpdeskTicket, Long> {

    List<HelpdeskTicket> findByStatusIgnoreCase(String status);

    List<HelpdeskTicket> findByPriorityIgnoreCase(String priority);

    List<HelpdeskTicket> findByCategoryIgnoreCase(String category);

    List<HelpdeskTicket> findByAssignedToIgnoreCase(String assignedTo);

    List<HelpdeskTicket> findByEmployeeIdIgnoreCase(String employeeId);

    List<HelpdeskTicket> findByTicketNumberContainingIgnoreCaseOrTitleContainingIgnoreCaseOrEmployeeNameContainingIgnoreCase(
            String ticketNumber,
            String title,
            String employeeName
    );
}

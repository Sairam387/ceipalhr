package com.smarthr.controller;

import com.smarthr.entity.Employee;
import com.smarthr.repository.EmailHistoryRepository;
import com.smarthr.repository.EmployeeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final EmployeeRepository employeeRepository;
    private final EmailHistoryRepository emailHistoryRepository;

    public DashboardController(
            EmployeeRepository employeeRepository,
            EmailHistoryRepository emailHistoryRepository) {

        this.employeeRepository = employeeRepository;
        this.emailHistoryRepository = emailHistoryRepository;
    }

    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {

        LocalDate today = LocalDate.now();

        List<Employee> employees =
                employeeRepository.findAll();

        long totalEmployees = employees.size();

        long birthdaysToday =
                employees.stream()
                        .filter(employee ->
                                employee.getDob() != null
                                && employee.getDob().getMonthValue()
                                == today.getMonthValue()
                                && employee.getDob().getDayOfMonth()
                                == today.getDayOfMonth())
                        .count();

        long anniversariesToday =
                employees.stream()
                        .filter(employee ->
                                employee.getJoiningDate() != null
                                && employee.getJoiningDate().getMonthValue()
                                == today.getMonthValue()
                                && employee.getJoiningDate().getDayOfMonth()
                                == today.getDayOfMonth()
                                && employee.getJoiningDate().isBefore(today))
                        .count();

        LocalDateTime startOfDay =
                today.atStartOfDay();

        LocalDateTime endOfDay =
                today.plusDays(1)
                        .atStartOfDay()
                        .minusNanos(1);

        long emailsSentToday =
                emailHistoryRepository
                        .findAll()
                        .stream()
                        .filter(history ->
                                history.getSentAt() != null
                                && !history.getSentAt().isBefore(startOfDay)
                                && !history.getSentAt().isAfter(endOfDay)
                                && "SENT".equalsIgnoreCase(
                                        history.getStatus()))
                        .count();

        long emailsFailedToday =
                emailHistoryRepository
                        .findAll()
                        .stream()
                        .filter(history ->
                                history.getSentAt() != null
                                && !history.getSentAt().isBefore(startOfDay)
                                && !history.getSentAt().isAfter(endOfDay)
                                && "FAILED".equalsIgnoreCase(
                                        history.getStatus()))
                        .count();

        Map<String, Object> stats =
                new HashMap<>();

        stats.put("totalEmployees", totalEmployees);
        stats.put("birthdaysToday", birthdaysToday);
        stats.put("anniversariesToday", anniversariesToday);
        stats.put("emailsSentToday", emailsSentToday);
        stats.put("emailsFailedToday", emailsFailedToday);
        stats.put("automationStatus", "ACTIVE");
        stats.put("date", today);

        return stats;
    }
}
package com.smarthr.controller;

import com.smarthr.entity.EmailHistory;
import com.smarthr.entity.Employee;
import com.smarthr.repository.EmailHistoryRepository;
import com.smarthr.repository.EmployeeRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hr-statistics")
public class HRStatisticsController {

    private static final ZoneId INDIA_ZONE =
            ZoneId.of("Asia/Kolkata");

    private final EmployeeRepository employeeRepository;

    private final EmailHistoryRepository emailHistoryRepository;

    public HRStatisticsController(
            EmployeeRepository employeeRepository,
            EmailHistoryRepository emailHistoryRepository) {

        this.employeeRepository =
                employeeRepository;

        this.emailHistoryRepository =
                emailHistoryRepository;
    }

    // =========================================================
    // DEPARTMENT-WISE EMPLOYEE COUNT
    // =========================================================

    @GetMapping("/departments")
    public Map<String, Long> getDepartmentStatistics() {

        List<Employee> employees =
                employeeRepository.findAll();

        return employees.stream()
                .filter(employee ->
                        employee.getDepartment() != null
                                && !employee.getDepartment()
                                .isBlank())
                .collect(
                        Collectors.groupingBy(
                                Employee::getDepartment,
                                LinkedHashMap::new,
                                Collectors.counting()
                        )
                );
    }

    // =========================================================
    // DESIGNATION-WISE EMPLOYEE COUNT
    // =========================================================

    @GetMapping("/designations")
    public Map<String, Long> getDesignationStatistics() {

        List<Employee> employees =
                employeeRepository.findAll();

        return employees.stream()
                .filter(employee ->
                        employee.getDesignation() != null
                                && !employee.getDesignation()
                                .isBlank())
                .collect(
                        Collectors.groupingBy(
                                Employee::getDesignation,
                                LinkedHashMap::new,
                                Collectors.counting()
                        )
                );
    }

    // =========================================================
    // LOCATION-WISE EMPLOYEE COUNT
    // =========================================================

    @GetMapping("/locations")
    public Map<String, Long> getLocationStatistics() {

        List<Employee> employees =
                employeeRepository.findAll();

        return employees.stream()
                .filter(employee ->
                        employee.getLocation() != null
                                && !employee.getLocation()
                                .isBlank())
                .collect(
                        Collectors.groupingBy(
                                Employee::getLocation,
                                LinkedHashMap::new,
                                Collectors.counting()
                        )
                );
    }

    // =========================================================
    // TODAY'S BIRTHDAYS
    // =========================================================

    @GetMapping("/today/birthdays")
    public List<Employee> getTodayBirthdays() {

        LocalDate today =
                LocalDate.now(INDIA_ZONE);

        return employeeRepository.findAll()
                .stream()
                .filter(employee ->
                        employee.getDob() != null
                )
                .filter(employee ->
                        employee.getDob().getMonthValue()
                                == today.getMonthValue()
                                &&
                        employee.getDob().getDayOfMonth()
                                == today.getDayOfMonth()
                )
                .collect(Collectors.toList());
    }

    // =========================================================
    // TODAY'S WORK ANNIVERSARIES
    // =========================================================

    @GetMapping("/today/anniversaries")
    public List<Employee> getTodayAnniversaries() {

        LocalDate today =
                LocalDate.now(INDIA_ZONE);

        return employeeRepository.findAll()
                .stream()
                .filter(employee ->
                        employee.getJoiningDate() != null
                )
                .filter(employee ->
                        employee.getJoiningDate()
                                .getMonthValue()
                                == today.getMonthValue()
                                &&
                        employee.getJoiningDate()
                                .getDayOfMonth()
                                == today.getDayOfMonth()
                )
                .filter(employee ->
                        employee.getJoiningDate()
                                .isBefore(today)
                )
                .collect(Collectors.toList());
    }

    // =========================================================
    // TODAY'S BIRTHDAY COUNT
    // =========================================================

    @GetMapping("/today/birthday-count")
    public long getTodayBirthdayCount() {

        return getTodayBirthdays().size();
    }

    // =========================================================
    // TODAY'S ANNIVERSARY COUNT
    // =========================================================

    @GetMapping("/today/anniversary-count")
    public long getTodayAnniversaryCount() {

        return getTodayAnniversaries().size();
    }

    // =========================================================
    // EMAIL STATISTICS
    // =========================================================

    @GetMapping("/email-statistics")
    public Map<String, Long> getEmailStatistics() {

        List<EmailHistory> history =
                emailHistoryRepository.findAll();

        Map<String, Long> result =
                new LinkedHashMap<>();

        long birthdaySent =
                history.stream()
                        .filter(email ->
                                "BIRTHDAY".equalsIgnoreCase(
                                        email.getEmailType()
                                )
                        )
                        .filter(email ->
                                "SENT".equalsIgnoreCase(
                                        email.getStatus()
                                )
                        )
                        .count();

        long anniversarySent =
                history.stream()
                        .filter(email ->
                                "ANNIVERSARY".equalsIgnoreCase(
                                        email.getEmailType()
                                )
                        )
                        .filter(email ->
                                "SENT".equalsIgnoreCase(
                                        email.getStatus()
                                )
                        )
                        .count();

        long failed =
                history.stream()
                        .filter(email ->
                                "FAILED".equalsIgnoreCase(
                                        email.getStatus()
                                )
                        )
                        .count();

        long totalSent =
                history.stream()
                        .filter(email ->
                                "SENT".equalsIgnoreCase(
                                        email.getStatus()
                                )
                        )
                        .count();

        result.put(
                "birthdayEmailsSent",
                birthdaySent
        );

        result.put(
                "anniversaryEmailsSent",
                anniversarySent
        );

        result.put(
                "totalEmailsSent",
                totalSent
        );

        result.put(
                "failedEmails",
                failed
        );

        return result;
    }

    // =========================================================
    // COMPLETE HR STATISTICS
    // =========================================================

    @GetMapping("/summary")
    public Map<String, Object> getHRStatistics() {

        List<Employee> employees =
                employeeRepository.findAll();

        List<Employee> birthdays =
                getTodayBirthdays();

        List<Employee> anniversaries =
                getTodayAnniversaries();

        Map<String, Long> emailStatistics =
                getEmailStatistics();

        Map<String, Object> result =
                new LinkedHashMap<>();

        // -----------------------------------------------------
        // EMPLOYEE TOTAL
        // -----------------------------------------------------

        result.put(
                "totalEmployees",
                employees.size()
        );

        // -----------------------------------------------------
        // TODAY'S MILESTONES
        // -----------------------------------------------------

        result.put(
                "todayBirthdays",
                birthdays.size()
        );

        result.put(
                "todayAnniversaries",
                anniversaries.size()
        );

        // -----------------------------------------------------
        // EMAIL STATISTICS
        // -----------------------------------------------------

        result.put(
                "birthdayEmailsSent",
                emailStatistics.get(
                        "birthdayEmailsSent"
                )
        );

        result.put(
                "anniversaryEmailsSent",
                emailStatistics.get(
                        "anniversaryEmailsSent"
                )
        );

        result.put(
                "totalEmailsSent",
                emailStatistics.get(
                        "totalEmailsSent"
                )
        );

        result.put(
                "failedEmails",
                emailStatistics.get(
                        "failedEmails"
                )
        );

        // -----------------------------------------------------
        // AUTOMATION STATUS
        // -----------------------------------------------------

        result.put(
                "automationStatus",
                "ACTIVE"
        );

        result.put(
                "automationTime",
                "09:00 AM IST"
        );

        // -----------------------------------------------------
        // EXISTING STATISTICS
        // -----------------------------------------------------

        result.put(
                "departments",
                getDepartmentStatistics()
        );

        result.put(
                "designations",
                getDesignationStatistics()
        );

        result.put(
                "locations",
                getLocationStatistics()
        );

        return result;
    }
}
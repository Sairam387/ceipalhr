package com.smarthr.controller;

import com.smarthr.entity.Employee;
import com.smarthr.repository.EmployeeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/upcoming")
public class UpcomingMilestoneController {

    private final EmployeeRepository employeeRepository;

    public UpcomingMilestoneController(
            EmployeeRepository employeeRepository) {

        this.employeeRepository = employeeRepository;
    }

    // =========================================================
    // UPCOMING BIRTHDAYS - NEXT 30 DAYS
    // =========================================================

    @GetMapping("/birthdays")
    public List<Employee> getUpcomingBirthdays() {

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(30);

        return employeeRepository.findAll()
                .stream()
                .filter(employee -> employee.getDob() != null)
                .filter(employee ->
                        isDateInNext30Days(
                                employee.getDob(),
                                today,
                                endDate
                        )
                )
                .sorted(
                        Comparator.comparing(
                                employee ->
                                        getNextOccurrence(
                                                employee.getDob(),
                                                today
                                        )
                        )
                )
                .toList();
    }

    // =========================================================
    // UPCOMING WORK ANNIVERSARIES - NEXT 30 DAYS
    // =========================================================

    @GetMapping("/anniversaries")
    public List<Employee> getUpcomingAnniversaries() {

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(30);

        return employeeRepository.findAll()
                .stream()
                .filter(employee ->
                        employee.getJoiningDate() != null
                )
                .filter(employee ->
                        employee.getJoiningDate()
                                .isBefore(today)
                )
                .filter(employee ->
                        isDateInNext30Days(
                                employee.getJoiningDate(),
                                today,
                                endDate
                        )
                )
                .sorted(
                        Comparator.comparing(
                                employee ->
                                        getNextOccurrence(
                                                employee.getJoiningDate(),
                                                today
                                        )
                        )
                )
                .toList();
    }

    // =========================================================
    // CHECK DATE WITHIN NEXT 30 DAYS
    // =========================================================

    private boolean isDateInNext30Days(
            LocalDate originalDate,
            LocalDate today,
            LocalDate endDate) {

        LocalDate occurrence =
                getNextOccurrence(
                        originalDate,
                        today
                );

        return !occurrence.isBefore(today)
                && !occurrence.isAfter(endDate);
    }

    // =========================================================
    // GET NEXT YEARLY OCCURRENCE
    // =========================================================

    private LocalDate getNextOccurrence(
            LocalDate originalDate,
            LocalDate today) {

        LocalDate occurrence;

        try {

            occurrence = LocalDate.of(
                    today.getYear(),
                    originalDate.getMonth(),
                    originalDate.getDayOfMonth()
            );

        } catch (Exception e) {

            /*
             * Handles February 29 in non-leap years.
             * Treats it as February 28 for yearly calculation.
             */
            occurrence = LocalDate.of(
                    today.getYear(),
                    originalDate.getMonth(),
                    Math.min(
                            originalDate.getDayOfMonth(),
                            originalDate.getMonth()
                                    .length(
                                            java.time.Year.isLeap(
                                                    today.getYear()
                                            )
                                    )
                    )
            );
        }

        if (occurrence.isBefore(today)) {

            try {

                occurrence = LocalDate.of(
                        today.getYear() + 1,
                        originalDate.getMonth(),
                        originalDate.getDayOfMonth()
                );

            } catch (Exception e) {

                occurrence = LocalDate.of(
                        today.getYear() + 1,
                        originalDate.getMonth(),
                        Math.min(
                                originalDate.getDayOfMonth(),
                                originalDate.getMonth()
                                        .length(
                                                java.time.Year.isLeap(
                                                        today.getYear() + 1
                                                )
                                        )
                        )
                );
            }
        }

        return occurrence;
    }
}
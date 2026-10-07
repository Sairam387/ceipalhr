package com.smarthr.controller;

import com.smarthr.entity.Employee;
import com.smarthr.repository.EmployeeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/milestones")
public class MilestoneController {

    private final EmployeeRepository employeeRepository;

    public MilestoneController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/birthdays")
    public List<Employee> getTodaysBirthdays() {

        LocalDate today = LocalDate.now();

        return employeeRepository.findAll()
                .stream()
                .filter(employee ->
                        employee.getDob() != null
                                && employee.getDob().getMonthValue() == today.getMonthValue()
                                && employee.getDob().getDayOfMonth() == today.getDayOfMonth()
                )
                .toList();
    }

    @GetMapping("/anniversaries")
    public List<Employee> getTodaysAnniversaries() {

        LocalDate today = LocalDate.now();

        return employeeRepository.findAll()
                .stream()
                .filter(employee ->
                        employee.getJoiningDate() != null
                                && employee.getJoiningDate().getMonthValue() == today.getMonthValue()
                                && employee.getJoiningDate().getDayOfMonth() == today.getDayOfMonth()
                )
                .toList();
    }
}
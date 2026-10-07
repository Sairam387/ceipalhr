package com.smarthr.controller;

import com.smarthr.entity.Employee;
import com.smarthr.repository.EmployeeRepository;
import com.smarthr.service.AnniversaryCardService;
import com.smarthr.service.EmailService;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/automation")
public class AutomationController {

    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;
    private final AnniversaryCardService anniversaryCardService;

    public AutomationController(
            EmployeeRepository employeeRepository,
            EmailService emailService,
            AnniversaryCardService anniversaryCardService) {

        this.employeeRepository = employeeRepository;
        this.emailService = emailService;
        this.anniversaryCardService = anniversaryCardService;
    }

    @PostMapping("/birthday/{id}")
    public String testBirthdayEmail(@PathVariable Long id) {

        Employee employee =
                employeeRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found"
                                ));

        emailService.sendBirthdayEmail(employee);

        return "Birthday email test completed for "
                + employee.getName();
    }

    @PostMapping("/anniversary/{id}")
    public String testAnniversaryEmail(@PathVariable Long id) {

        Employee employee =
                employeeRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found"
                                ));

        emailService.sendAnniversaryEmail(employee);

        return "Anniversary email test completed for "
                + employee.getName();
    }

    @PostMapping("/anniversary-card/{id}")
    public String generateAnniversaryCard(@PathVariable Long id) {

        Employee employee =
                employeeRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found"
                                ));

        var file =
                anniversaryCardService.generateCard(employee);

        return "Anniversary card generated successfully: "
                + file.toAbsolutePath();
    }

    @GetMapping("/today")
    public String today() {

        return "CeipalHR automation date: "
                + LocalDate.now();
    }
}
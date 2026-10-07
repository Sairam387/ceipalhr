package com.smarthr.controller;

import com.smarthr.entity.Employee;
import com.smarthr.repository.EmployeeRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    public EmployeeController(
            EmployeeRepository employeeRepository) {

        this.employeeRepository = employeeRepository;
    }

    // =========================================================
    // GET ALL EMPLOYEES
    // =========================================================

    @GetMapping
    public List<Employee> getAllEmployees() {

        return employeeRepository.findAll();
    }

    // =========================================================
    // SEARCH EMPLOYEES
    // =========================================================

    @GetMapping("/search")
    public List<Employee> searchEmployees(
            @RequestParam String keyword) {

        String search =
                keyword == null
                        ? ""
                        : keyword.trim().toLowerCase();

        if (search.isEmpty()) {
            return employeeRepository.findAll();
        }

        return employeeRepository.findAll()
                .stream()
                .filter(employee ->
                        contains(
                                employee.getEmployeeId(),
                                search
                        )
                        ||
                        contains(
                                employee.getName(),
                                search
                        )
                        ||
                        contains(
                                employee.getDesignation(),
                                search
                        )
                        ||
                        contains(
                                employee.getDepartment(),
                                search
                        )
                        ||
                        contains(
                                employee.getLocation(),
                                search
                        )
                        ||
                        contains(
                                employee.getEmail(),
                                search
                        )
                )
                .toList();
    }

    // =========================================================
    // GET EMPLOYEE BY ID
    // =========================================================

    @GetMapping("/{id}")
    public Employee getEmployee(
            @PathVariable Long id) {

        return employeeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Employee not found"
                        ));
    }

    // =========================================================
    // CREATE EMPLOYEE
    // =========================================================

    @PostMapping
    public Employee createEmployee(
            @RequestBody Employee employee) {

        return employeeRepository.save(employee);
    }

    // =========================================================
    // UPDATE EMPLOYEE
    // =========================================================

    @PutMapping("/{id}")
    public Employee updateEmployee(
            @PathVariable Long id,
            @RequestBody Employee updatedEmployee) {

        Employee employee =
                employeeRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found"
                                ));


        employee.setEmployeeId(
                updatedEmployee.getEmployeeId()
        );

        employee.setName(
                updatedEmployee.getName()
        );

        employee.setDesignation(
                updatedEmployee.getDesignation()
        );

        employee.setDepartment(
                updatedEmployee.getDepartment()
        );

        employee.setLocation(
                updatedEmployee.getLocation()
        );

        employee.setEmail(
                updatedEmployee.getEmail()
        );

        employee.setDob(
                updatedEmployee.getDob()
        );

        employee.setJoiningDate(
                updatedEmployee.getJoiningDate()
        );


        return employeeRepository.save(employee);
    }

    // =========================================================
    // DELETE EMPLOYEE
    // =========================================================

    @DeleteMapping("/{id}")
    public String deleteEmployee(
            @PathVariable Long id) {

        if (!employeeRepository.existsById(id)) {

            throw new RuntimeException(
                    "Employee not found"
            );
        }


        employeeRepository.deleteById(id);

        return "Employee deleted successfully";
    }

    // =========================================================
    // STRING SEARCH HELPER
    // =========================================================

    private boolean contains(
            String value,
            String keyword) {

        return value != null
                && value
                .toLowerCase()
                .contains(keyword);
    }
}
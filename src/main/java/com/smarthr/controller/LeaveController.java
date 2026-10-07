package com.smarthr.controller;

import com.smarthr.entity.Employee;
import com.smarthr.entity.LeaveRequest;
import com.smarthr.entity.LeaveStatus;
import com.smarthr.repository.EmployeeRepository;
import com.smarthr.repository.LeaveRequestRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin
public class LeaveController {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveController(
            LeaveRequestRepository leaveRequestRepository,
            EmployeeRepository employeeRepository) {

        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    // =========================================================
    // GET ALL LEAVE REQUESTS
    // =========================================================

    @GetMapping
    public List<LeaveRequest> getAllLeaves() {

        return leaveRequestRepository.findAll();
    }

    // =========================================================
    // GET LEAVE BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getLeaveById(
            @PathVariable Long id) {

        LeaveRequest request =
                leaveRequestRepository.findById(id).orElse(null);

        if (request == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Leave request not found");
        }

        return ResponseEntity.ok(request);
    }

    // =========================================================
    // GET EMPLOYEE LEAVES
    // =========================================================

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getEmployeeLeaves(
            @PathVariable Long employeeId) {

        if (!employeeRepository.existsById(employeeId)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Employee not found");
        }

        return ResponseEntity.ok(
                leaveRequestRepository.findByEmployeeId(employeeId)
        );
    }

    // =========================================================
    // GET BY STATUS
    // =========================================================

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(
            @PathVariable String status) {

        try {

            LeaveStatus leaveStatus =
                    LeaveStatus.valueOf(status.toUpperCase());

            return ResponseEntity.ok(
                    leaveRequestRepository.findByStatus(leaveStatus)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body("Invalid leave status: " + status);
        }
    }

    // =========================================================
    // APPLY LEAVE
    // =========================================================

    @PostMapping
    public ResponseEntity<?> applyLeave(
            @RequestBody LeaveRequest request) {

        try {

            // -------------------------------------------------
            // VALIDATE EMPLOYEE
            // -------------------------------------------------

            if (request.getEmployeeId() == null) {
                return ResponseEntity
                        .badRequest()
                        .body("Employee ID is required");
            }

            Employee employee =
                    employeeRepository
                            .findById(request.getEmployeeId())
                            .orElse(null);

            if (employee == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Employee not found");
            }

            // -------------------------------------------------
            // VALIDATE DATES
            // -------------------------------------------------

            if (request.getFromDate() == null) {
                return ResponseEntity
                        .badRequest()
                        .body("From date is required");
            }

            if (request.getToDate() == null) {
                return ResponseEntity
                        .badRequest()
                        .body("To date is required");
            }

            LocalDate fromDate = request.getFromDate();
            LocalDate toDate = request.getToDate();

            if (toDate.isBefore(fromDate)) {
                return ResponseEntity
                        .badRequest()
                        .body("To date cannot be before from date");
            }

            // -------------------------------------------------
            // VALIDATE LEAVE TYPE
            // -------------------------------------------------

            if (request.getLeaveTypeId() == null) {
                return ResponseEntity
                        .badRequest()
                        .body("Leave type ID is required");
            }

            // -------------------------------------------------
            // CALCULATE NUMBER OF DAYS
            // -------------------------------------------------

            long days =
                    ChronoUnit.DAYS.between(
                            fromDate,
                            toDate
                    ) + 1;

            request.setNumberOfDays((int) days);

            // -------------------------------------------------
            // INITIAL STATUS
            // -------------------------------------------------

            request.setStatus(LeaveStatus.PENDING);

            // -------------------------------------------------
            // SAVE
            // -------------------------------------------------

            LeaveRequest saved =
                    leaveRequestRepository.save(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(saved);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Failed to apply leave: "
                                    + e.getMessage()
                    );
        }
    }

    // =========================================================
    // APPROVE LEAVE
    // =========================================================

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveLeave(
            @PathVariable Long id) {

        LeaveRequest request =
                leaveRequestRepository
                        .findById(id)
                        .orElse(null);

        if (request == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Leave request not found");
        }

        if (request.getStatus() == LeaveStatus.APPROVED) {
            return ResponseEntity
                    .badRequest()
                    .body("Leave is already approved");
        }

        if (request.getStatus() == LeaveStatus.CANCELLED) {
            return ResponseEntity
                    .badRequest()
                    .body("Cancelled leave cannot be approved");
        }

        request.setStatus(LeaveStatus.APPROVED);

        return ResponseEntity.ok(
                leaveRequestRepository.save(request)
        );
    }

    // =========================================================
    // REJECT LEAVE
    // =========================================================

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectLeave(
            @PathVariable Long id) {

        LeaveRequest request =
                leaveRequestRepository
                        .findById(id)
                        .orElse(null);

        if (request == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Leave request not found");
        }

        if (request.getStatus() == LeaveStatus.APPROVED) {
            return ResponseEntity
                    .badRequest()
                    .body("Approved leave cannot be rejected");
        }

        if (request.getStatus() == LeaveStatus.CANCELLED) {
            return ResponseEntity
                    .badRequest()
                    .body("Cancelled leave cannot be rejected");
        }

        request.setStatus(LeaveStatus.REJECTED);

        return ResponseEntity.ok(
                leaveRequestRepository.save(request)
        );
    }

    // =========================================================
    // CANCEL LEAVE
    // =========================================================

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelLeave(
            @PathVariable Long id) {

        LeaveRequest request =
                leaveRequestRepository
                        .findById(id)
                        .orElse(null);

        if (request == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Leave request not found");
        }

        if (request.getStatus() == LeaveStatus.REJECTED) {
            return ResponseEntity
                    .badRequest()
                    .body("Rejected leave cannot be cancelled");
        }

        if (request.getStatus() == LeaveStatus.CANCELLED) {
            return ResponseEntity
                    .badRequest()
                    .body("Leave is already cancelled");
        }

        request.setStatus(LeaveStatus.CANCELLED);

        return ResponseEntity.ok(
                leaveRequestRepository.save(request)
        );
    }

    // =========================================================
    // DELETE LEAVE
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLeave(
            @PathVariable Long id) {

        if (!leaveRequestRepository.existsById(id)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Leave request not found");
        }

        leaveRequestRepository.deleteById(id);

        return ResponseEntity.ok(
                "Leave request deleted successfully"
        );
    }
}

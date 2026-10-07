package com.smarthr.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smarthr.entity.LeaveStatus;
import com.smarthr.repository.LeaveRequestRepository;

@Service
@Transactional
public class LeaveRequest {

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveRequest(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    // =========================================================
    // GET ALL LEAVES
    // =========================================================

    public List<com.smarthr.entity.LeaveRequest> getAllLeaves() {
        return leaveRequestRepository.findAll();
    }

    // =========================================================
    // GET EMPLOYEE LEAVES
    // =========================================================

    public List<com.smarthr.entity.LeaveRequest> getEmployeeLeaves(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    // =========================================================
    // GET LEAVES BY STATUS
    // =========================================================

    public List<com.smarthr.entity.LeaveRequest> getLeavesByStatus(LeaveStatus status) {
        return leaveRequestRepository.findByStatus(status);
    }

    // =========================================================
    // APPLY LEAVE
    // =========================================================

    public com.smarthr.entity.LeaveRequest applyLeave(
            Long employeeId,
            Long leaveTypeId,
            LocalDate fromDate,
            LocalDate toDate,
            String reason) {

        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID is required");
        }

        if (leaveTypeId == null) {
            throw new IllegalArgumentException("Leave Type ID is required");
        }

        if (fromDate == null) {
            throw new IllegalArgumentException("From date is required");
        }

        if (toDate == null) {
            throw new IllegalArgumentException("To date is required");
        }

        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException(
                    "To date cannot be before From date");
        }

        com.smarthr.entity.LeaveRequest leave =
                new com.smarthr.entity.LeaveRequest();

        leave.setEmployeeId(employeeId);
        leave.setLeaveTypeId(leaveTypeId);
        leave.setFromDate(fromDate);
        leave.setToDate(toDate);
        leave.setReason(reason);

        int numberOfDays = calculateLeaveDays(fromDate, toDate);

        leave.setNumberOfDays(numberOfDays);
        leave.setStatus(LeaveStatus.PENDING);

        return leaveRequestRepository.save(leave);
    }

    // =========================================================
    // APPROVE LEAVE
    // =========================================================

    public com.smarthr.entity.LeaveRequest approveLeave(Long leaveId) {

        com.smarthr.entity.LeaveRequest leave =
                leaveRequestRepository.findById(leaveId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Leave request not found: " + leaveId));

        leave.setStatus(LeaveStatus.APPROVED);

        return leaveRequestRepository.save(leave);
    }

    // =========================================================
    // REJECT LEAVE
    // =========================================================

    public com.smarthr.entity.LeaveRequest rejectLeave(Long leaveId) {

        com.smarthr.entity.LeaveRequest leave =
                leaveRequestRepository.findById(leaveId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Leave request not found: " + leaveId));

        leave.setStatus(LeaveStatus.REJECTED);

        return leaveRequestRepository.save(leave);
    }

    // =========================================================
    // CANCEL LEAVE
    // =========================================================

    public com.smarthr.entity.LeaveRequest cancelLeave(Long leaveId) {

        com.smarthr.entity.LeaveRequest leave =
                leaveRequestRepository.findById(leaveId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Leave request not found: " + leaveId));

        leave.setStatus(LeaveStatus.CANCELLED);

        return leaveRequestRepository.save(leave);
    }

    // =========================================================
    // DELETE LEAVE
    // =========================================================

    public void deleteLeave(Long leaveId) {

        if (!leaveRequestRepository.existsById(leaveId)) {
            throw new RuntimeException(
                    "Leave request not found: " + leaveId);
        }

        leaveRequestRepository.deleteById(leaveId);
    }

    // =========================================================
    // CALCULATE LEAVE DAYS
    // =========================================================

    public int calculateLeaveDays(
            LocalDate fromDate,
            LocalDate toDate) {

        if (fromDate == null || toDate == null) {
            return 0;
        }

        if (toDate.isBefore(fromDate)) {
            return 0;
        }

        return (int) (
                ChronoUnit.DAYS.between(
                        fromDate,
                        toDate) + 1
        );
    }
}
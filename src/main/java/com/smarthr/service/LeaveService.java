package com.smarthr.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smarthr.entity.LeaveRequest;
import com.smarthr.entity.LeaveStatus;
import com.smarthr.repository.LeaveRequestRepository;

@Service
@Transactional
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public List<LeaveRequest> getAllLeaves() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> getEmployeeLeaves(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    public List<LeaveRequest> getLeavesByStatus(LeaveStatus status) {
        return leaveRequestRepository.findByStatus(status);
    }

    public LeaveRequest applyLeave(
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

        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("Leave dates are required");
        }

        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException(
                    "To date cannot be before From date");
        }

        LeaveRequest leave = new LeaveRequest();

        leave.setEmployeeId(employeeId);
        leave.setLeaveTypeId(leaveTypeId);
        leave.setFromDate(fromDate);
        leave.setToDate(toDate);
        leave.setReason(reason);

        int numberOfDays =
                (int) ChronoUnit.DAYS.between(fromDate, toDate) + 1;

        leave.setNumberOfDays(numberOfDays);
        leave.setStatus(LeaveStatus.PENDING);

        return leaveRequestRepository.save(leave);
    }

    public LeaveRequest approveLeave(Long leaveId) {

        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Leave request not found: " + leaveId));

        leave.setStatus(LeaveStatus.APPROVED);

        return leaveRequestRepository.save(leave);
    }

    public LeaveRequest rejectLeave(Long leaveId) {

        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Leave request not found: " + leaveId));

        leave.setStatus(LeaveStatus.REJECTED);

        return leaveRequestRepository.save(leave);
    }

    public LeaveRequest cancelLeave(Long leaveId) {

        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Leave request not found: " + leaveId));

        leave.setStatus(LeaveStatus.CANCELLED);

        return leaveRequestRepository.save(leave);
    }

    public void deleteLeave(Long leaveId) {

        if (!leaveRequestRepository.existsById(leaveId)) {
            throw new RuntimeException(
                    "Leave request not found: " + leaveId);
        }

        leaveRequestRepository.deleteById(leaveId);
    }

    public int calculateLeaveDays(
            LocalDate fromDate,
            LocalDate toDate) {

        if (fromDate == null || toDate == null) {
            return 0;
        }

        if (toDate.isBefore(fromDate)) {
            return 0;
        }

        return (int) ChronoUnit.DAYS.between(
                fromDate,
                toDate) + 1;
    }
}
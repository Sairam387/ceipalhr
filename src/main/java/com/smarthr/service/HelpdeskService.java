package com.smarthr.service;

import com.smarthr.entity.HelpdeskTicket;
import com.smarthr.repository.HelpdeskTicketRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HelpdeskService {

    private final HelpdeskTicketRepository repository;

    public HelpdeskService(HelpdeskTicketRepository repository) {
        this.repository = repository;
    }

    // =========================================================
    // CREATE TICKET
    // =========================================================

    public HelpdeskTicket createTicket(HelpdeskTicket ticket) {

        if (ticket.getTitle() == null || ticket.getTitle().isBlank()) {
            throw new IllegalArgumentException("Ticket title is required");
        }

        if (ticket.getCategory() == null || ticket.getCategory().isBlank()) {
            ticket.setCategory("GENERAL");
        }

        if (ticket.getStatus() == null || ticket.getStatus().isBlank()) {
            ticket.setStatus("OPEN");
        }

        if (ticket.getPriority() == null || ticket.getPriority().isBlank()) {
            ticket.setPriority("MEDIUM");
        }

        return repository.save(ticket);
    }

    // =========================================================
    // GET ALL TICKETS
    // =========================================================

    public List<HelpdeskTicket> getAllTickets() {
        return repository.findAll();
    }

    // =========================================================
    // GET TICKET BY ID
    // =========================================================

    public HelpdeskTicket getTicketById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Ticket not found with ID: " + id));
    }

    // =========================================================
    // UPDATE TICKET
    // =========================================================

    public HelpdeskTicket updateTicket(Long id, HelpdeskTicket updatedTicket) {

        HelpdeskTicket existingTicket = getTicketById(id);

        // Employee details
        if (updatedTicket.getEmployeeId() != null) {
            existingTicket.setEmployeeId(updatedTicket.getEmployeeId());
        }

        if (updatedTicket.getEmployeeName() != null) {
            existingTicket.setEmployeeName(updatedTicket.getEmployeeName());
        }

        // Ticket information
        if (updatedTicket.getTitle() != null
                && !updatedTicket.getTitle().isBlank()) {

            existingTicket.setTitle(updatedTicket.getTitle());
        }

        if (updatedTicket.getDescription() != null) {
            existingTicket.setDescription(updatedTicket.getDescription());
        }

        // Category
        if (updatedTicket.getCategory() != null
                && !updatedTicket.getCategory().isBlank()) {

            existingTicket.setCategory(updatedTicket.getCategory());
        }

        // Priority
        if (updatedTicket.getPriority() != null
                && !updatedTicket.getPriority().isBlank()) {

            existingTicket.setPriority(updatedTicket.getPriority());
        }

        // Status
        if (updatedTicket.getStatus() != null
                && !updatedTicket.getStatus().isBlank()) {

            existingTicket.setStatus(
                    updatedTicket.getStatus().toUpperCase()
            );
        }

        // Assigned engineer
        if (updatedTicket.getAssignedTo() != null) {
            existingTicket.setAssignedTo(updatedTicket.getAssignedTo());
        }

        return repository.save(existingTicket);
    }

    // =========================================================
    // DELETE TICKET
    // =========================================================

    public void deleteTicket(Long id) {

        if (!repository.existsById(id)) {

            throw new RuntimeException(
                    "Cannot delete. Ticket not found with ID: " + id
            );
        }

        repository.deleteById(id);
    }

    // =========================================================
    // SEARCH TICKETS
    // =========================================================

    public List<HelpdeskTicket> searchTickets(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return repository.findAll();
        }

        return repository
                .findByTicketNumberContainingIgnoreCaseOrTitleContainingIgnoreCaseOrEmployeeNameContainingIgnoreCase(
                        keyword,
                        keyword,
                        keyword
                );
    }

    // =========================================================
    // FILTER BY STATUS
    // =========================================================

    public List<HelpdeskTicket> getByStatus(String status) {
        return repository.findByStatusIgnoreCase(status);
    }

    // =========================================================
    // FILTER BY PRIORITY
    // =========================================================

    public List<HelpdeskTicket> getByPriority(String priority) {
        return repository.findByPriorityIgnoreCase(priority);
    }

    // =========================================================
    // FILTER BY CATEGORY
    // =========================================================

    public List<HelpdeskTicket> getByCategory(String category) {
        return repository.findByCategoryIgnoreCase(category);
    }

    // =========================================================
    // FILTER BY ASSIGNED ENGINEER
    // =========================================================

    public List<HelpdeskTicket> getByAssignedTo(String assignedTo) {
        return repository.findByAssignedToIgnoreCase(assignedTo);
    }

    // =========================================================
    // EMPLOYEE TICKET HISTORY
    // =========================================================

    public List<HelpdeskTicket> getByEmployeeId(String employeeId) {
        return repository.findByEmployeeIdIgnoreCase(employeeId);
    }

    // =========================================================
    // DASHBOARD STATISTICS
    // =========================================================

    public long getTotalTickets() {
        return repository.count();
    }

    public long getOpenTickets() {
        return repository.findByStatusIgnoreCase("OPEN").size();
    }

    public long getInProgressTickets() {
        return repository.findByStatusIgnoreCase("IN_PROGRESS").size();
    }

    public long getResolvedTickets() {
        return repository.findByStatusIgnoreCase("RESOLVED").size();
    }

    public long getClosedTickets() {
        return repository.findByStatusIgnoreCase("CLOSED").size();
    }

    public long getCriticalTickets() {
        return repository.findByPriorityIgnoreCase("CRITICAL").size();
    }
}

package com.smarthr.controller;

import com.smarthr.entity.HelpdeskTicket;
import com.smarthr.service.HelpdeskService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/helpdesk")
@CrossOrigin(origins = "*")
public class HelpdeskController {

    private final HelpdeskService helpdeskService;

    public HelpdeskController(HelpdeskService helpdeskService) {
        this.helpdeskService = helpdeskService;
    }

    // Create ticket
    @PostMapping("/tickets")
    public ResponseEntity<HelpdeskTicket> createTicket(
            @RequestBody HelpdeskTicket ticket) {

        HelpdeskTicket createdTicket =
                helpdeskService.createTicket(ticket);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdTicket);
    }

    // Get all tickets
    @GetMapping("/tickets")
    public ResponseEntity<List<HelpdeskTicket>> getAllTickets() {

        return ResponseEntity.ok(
                helpdeskService.getAllTickets()
        );
    }

    // Get ticket by ID
    @GetMapping("/tickets/{id}")
    public ResponseEntity<HelpdeskTicket> getTicketById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                helpdeskService.getTicketById(id)
        );
    }

    // Update ticket
    @PutMapping("/tickets/{id}")
    public ResponseEntity<HelpdeskTicket> updateTicket(
            @PathVariable Long id,
            @RequestBody HelpdeskTicket ticket) {

        return ResponseEntity.ok(
                helpdeskService.updateTicket(id, ticket)
        );
    }

    // Delete ticket
    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<Map<String, String>> deleteTicket(
            @PathVariable Long id) {

        helpdeskService.deleteTicket(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Ticket deleted successfully");

        return ResponseEntity.ok(response);
    }

    // Search tickets
    @GetMapping("/search")
    public ResponseEntity<List<HelpdeskTicket>> searchTickets(
            @RequestParam String keyword) {

        return ResponseEntity.ok(
                helpdeskService.searchTickets(keyword)
        );
    }

    // Filter by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<HelpdeskTicket>> getByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                helpdeskService.getByStatus(status)
        );
    }

    // Filter by priority
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<HelpdeskTicket>> getByPriority(
            @PathVariable String priority) {

        return ResponseEntity.ok(
                helpdeskService.getByPriority(priority)
        );
    }

    // Filter by category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<HelpdeskTicket>> getByCategory(
            @PathVariable String category) {

        return ResponseEntity.ok(
                helpdeskService.getByCategory(category)
        );
    }

    // Filter by assigned engineer
    @GetMapping("/assigned/{assignedTo}")
    public ResponseEntity<List<HelpdeskTicket>> getByAssignedTo(
            @PathVariable String assignedTo) {

        return ResponseEntity.ok(
                helpdeskService.getByAssignedTo(assignedTo)
        );
    }

    // Employee ticket history
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<HelpdeskTicket>> getByEmployeeId(
            @PathVariable String employeeId) {

        return ResponseEntity.ok(
                helpdeskService.getByEmployeeId(employeeId)
        );
    }

    // Helpdesk dashboard statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStatistics() {

        Map<String, Long> stats = new HashMap<>();

        stats.put("totalTickets",
                helpdeskService.getTotalTickets());

        stats.put("openTickets",
                helpdeskService.getOpenTickets());

        stats.put("inProgressTickets",
                helpdeskService.getInProgressTickets());

        stats.put("resolvedTickets",
                helpdeskService.getResolvedTickets());

        stats.put("closedTickets",
                helpdeskService.getClosedTickets());

        stats.put("criticalTickets",
                helpdeskService.getCriticalTickets());

        return ResponseEntity.ok(stats);
    }
}
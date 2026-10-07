package com.smarthr.controller;

import com.smarthr.entity.EmailHistory;
import com.smarthr.repository.EmailHistoryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email-history")
@CrossOrigin(origins = "*")
public class EmailHistoryController {

    private final EmailHistoryRepository emailHistoryRepository;

    public EmailHistoryController(
            EmailHistoryRepository emailHistoryRepository) {

        this.emailHistoryRepository = emailHistoryRepository;
    }

    @GetMapping
    public List<EmailHistory> getAllEmailHistory() {

        return emailHistoryRepository.findAll();
    }

    @GetMapping("/{id}")
    public EmailHistory getEmailHistory(
            @PathVariable Long id) {

        return emailHistoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Email history not found"
                        ));
    }

    @DeleteMapping("/{id}")
    public String deleteEmailHistory(
            @PathVariable Long id) {

        emailHistoryRepository.deleteById(id);

        return "Email history deleted successfully";
    }
}
package com.hostelmanagement.controller;

import com.hostelmanagement.model.Complaint;
import com.hostelmanagement.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping
    public ResponseEntity<List<Complaint>> getAllComplaints(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        List<Complaint> complaints = complaintService.getAllComplaints(status, category);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Complaint> getComplaintById(@PathVariable Integer id) {
        Complaint complaint = complaintService.getComplaintById(id);
        return ResponseEntity.ok(complaint);
    }

    @PostMapping
    public ResponseEntity<Complaint> createComplaint(@Valid @RequestBody Complaint complaint) {
        Complaint created = complaintService.createComplaint(complaint);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Complaint> updateComplaint(
            @PathVariable Integer id,
            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        String resolution = payload.get("resolution");
        Complaint updated = complaintService.updateComplaint(id, status, resolution);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteComplaint(@PathVariable Integer id) {
        complaintService.deleteComplaint(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Complaint deleted successfully.");
        return ResponseEntity.ok(response);
    }
}

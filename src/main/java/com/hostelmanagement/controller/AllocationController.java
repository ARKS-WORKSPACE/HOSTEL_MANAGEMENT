package com.hostelmanagement.controller;

import com.hostelmanagement.model.Allocation;
import com.hostelmanagement.service.AllocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/allocations")
public class AllocationController {

    private final AllocationService allocationService;

    public AllocationController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @GetMapping
    public ResponseEntity<List<Allocation>> getAllAllocations() {
        List<Allocation> allocations = allocationService.getAllAllocations();
        return ResponseEntity.ok(allocations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Allocation> getAllocationById(@PathVariable Integer id) {
        Allocation allocation = allocationService.getAllocationById(id);
        return ResponseEntity.ok(allocation);
    }

    @PostMapping
    public ResponseEntity<Allocation> allocateRoom(@Valid @RequestBody Allocation allocation) {
        Allocation created = allocationService.allocateRoom(allocation);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> cancelAllocation(@PathVariable Integer id) {
        allocationService.cancelAllocation(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Allocation cancelled successfully. Room occupancy updated.");
        return ResponseEntity.ok(response);
    }
}

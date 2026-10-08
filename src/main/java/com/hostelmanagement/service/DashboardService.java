package com.hostelmanagement.service;

import com.hostelmanagement.model.Allocation;
import com.hostelmanagement.model.Complaint;
import com.hostelmanagement.repository.AllocationRepository;
import com.hostelmanagement.repository.ComplaintRepository;
import com.hostelmanagement.repository.RoomRepository;
import com.hostelmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final AllocationRepository allocationRepository;
    private final ComplaintRepository complaintRepository;

    public DashboardService(StudentRepository studentRepository,
                            RoomRepository roomRepository,
                            AllocationRepository allocationRepository,
                            ComplaintRepository complaintRepository) {
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
        this.allocationRepository = allocationRepository;
        this.complaintRepository = complaintRepository;
    }

    public Map<String, Object> getDashboardStats() {
        int totalStudents = studentRepository.count();
        int totalRooms = roomRepository.count();
        int totalCapacity = roomRepository.getTotalCapacity();
        int totalOccupied = roomRepository.getTotalOccupied();
        int availableBeds = Math.max(0, totalCapacity - totalOccupied);
        double occupancyRate = totalCapacity > 0 ? Math.round(((double) totalOccupied / totalCapacity) * 1000.0) / 10.0 : 0.0;

        int pendingComplaints = complaintRepository.countByStatus("PENDING");
        int inProgressComplaints = complaintRepository.countByStatus("IN_PROGRESS");
        int resolvedComplaints = complaintRepository.countByStatus("RESOLVED");
        int totalComplaints = pendingComplaints + inProgressComplaints + resolvedComplaints;

        List<Allocation> recentAllocations = allocationRepository.findRecentAllocations(5);
        List<Complaint> recentComplaints = complaintRepository.findRecentComplaints(5);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", totalStudents);
        stats.put("totalRooms", totalRooms);
        stats.put("totalCapacity", totalCapacity);
        stats.put("totalOccupied", totalOccupied);
        stats.put("availableBeds", availableBeds);
        stats.put("occupancyRate", occupancyRate);
        stats.put("pendingComplaints", pendingComplaints);
        stats.put("inProgressComplaints", inProgressComplaints);
        stats.put("resolvedComplaints", resolvedComplaints);
        stats.put("totalComplaints", totalComplaints);
        stats.put("recentAllocations", recentAllocations);
        stats.put("recentComplaints", recentComplaints);

        return stats;
    }
}

package com.hostelmanagement.service;

import com.hostelmanagement.exception.ResourceNotFoundException;
import com.hostelmanagement.exception.StudentNotFoundException;
import com.hostelmanagement.model.Complaint;
import com.hostelmanagement.model.Student;
import com.hostelmanagement.repository.ComplaintRepository;
import com.hostelmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final StudentRepository studentRepository;

    public ComplaintService(ComplaintRepository complaintRepository, StudentRepository studentRepository) {
        this.complaintRepository = complaintRepository;
        this.studentRepository = studentRepository;
    }

    public List<Complaint> getAllComplaints(String status, String category) {
        return complaintRepository.findAll(status, category);
    }

    public Complaint getComplaintById(Integer id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));
    }

    public Complaint createComplaint(Complaint complaint) {
        Student student = studentRepository.findById(complaint.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException(complaint.getStudentId()));

        if (complaint.getComplaintDate() == null) {
            complaint.setComplaintDate(LocalDate.now());
        }
        if (complaint.getStatus() == null || complaint.getStatus().trim().isEmpty()) {
            complaint.setStatus("PENDING");
        }

        Complaint saved = complaintRepository.save(complaint);
        saved.setStudentName(student.getName());
        saved.setStudentRollNo(student.getRollNo());
        saved.setRoomNo(student.getRoomNo());
        return saved;
    }

    public Complaint updateComplaint(Integer id, String status, String resolution) {
        Complaint complaint = getComplaintById(id);

        if (status != null && !status.trim().isEmpty()) {
            String s = status.trim().toUpperCase();
            if (!s.equals("PENDING") && !s.equals("IN_PROGRESS") && !s.equals("RESOLVED")) {
                throw new IllegalArgumentException("Invalid status: " + status + ". Allowed values: PENDING, IN_PROGRESS, RESOLVED.");
            }
            complaint.setStatus(s);
        }

        if (resolution != null) {
            complaint.setResolution(resolution.trim());
        }

        complaintRepository.update(complaint);
        return getComplaintById(id);
    }

    public void deleteComplaint(Integer id) {
        getComplaintById(id); // verify existence
        complaintRepository.deleteById(id);
    }
}

package com.hostelmanagement.service;

import com.hostelmanagement.exception.AllocationException;
import com.hostelmanagement.exception.StudentNotFoundException;
import com.hostelmanagement.model.Allocation;
import com.hostelmanagement.model.Student;
import com.hostelmanagement.repository.AllocationRepository;
import com.hostelmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final AllocationRepository allocationRepository;

    public StudentService(StudentRepository studentRepository, AllocationRepository allocationRepository) {
        this.studentRepository = studentRepository;
        this.allocationRepository = allocationRepository;
    }

    public List<Student> getAllStudents(String search) {
        return studentRepository.findAll(search);
    }

    public Student getStudentById(Integer id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    public Student createStudent(Student student) {
        // Check uniqueness of roll number
        Optional<Student> existing = studentRepository.findByRollNo(student.getRollNo());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("A student with Roll Number '" + student.getRollNo() + "' already exists.");
        }
        return studentRepository.save(student);
    }

    public Student updateStudent(Integer id, Student student) {
        Student current = getStudentById(id);

        // Check if updating to a roll number already used by another student
        Optional<Student> existing = studentRepository.findByRollNo(student.getRollNo());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new IllegalArgumentException("Roll Number '" + student.getRollNo() + "' is already assigned to another student.");
        }

        student.setId(id);
        student.setRoomId(current.getRoomId()); // preserve existing room_id link
        studentRepository.update(student);
        return getStudentById(id);
    }

    public void deleteStudent(Integer id) {
        Student current = getStudentById(id);

        // Check business rule: Deleting a student with active allocations should be blocked
        Optional<Allocation> activeAlloc = allocationRepository.findActiveByStudentId(id);
        if (activeAlloc.isPresent()) {
            throw new AllocationException("Cannot delete student '" + current.getName() + 
                    "' because they have an active room allocation in room " + 
                    (current.getRoomNo() != null ? current.getRoomNo() : activeAlloc.get().getRoomId()) + 
                    ". Please cancel the allocation first.");
        }

        studentRepository.deleteById(id);
    }
}

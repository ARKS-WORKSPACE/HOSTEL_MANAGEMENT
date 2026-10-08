package com.hostelmanagement.exception;

public class StudentNotFoundException extends ResourceNotFoundException {
    public StudentNotFoundException(Integer id) {
        super("Student not found with ID: " + id);
    }
    
    public StudentNotFoundException(String message) {
        super(message);
    }
}

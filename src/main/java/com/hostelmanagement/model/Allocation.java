package com.hostelmanagement.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Allocation {
    private Integer id;

    @NotNull(message = "Student ID is required")
    private Integer studentId;

    @NotNull(message = "Room ID is required")
    private Integer roomId;

    @NotBlank(message = "Bed number is required")
    private String bedNo;

    private LocalDate allocationDate = LocalDate.now();
    private String status = "ACTIVE"; // ACTIVE, CANCELLED
    private LocalDateTime createdAt;

    // Joined descriptive fields for UI
    private String studentName;
    private String studentRollNo;
    private String roomNo;
    private String roomBlock;

    public Allocation() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getBedNo() {
        return bedNo;
    }

    public void setBedNo(String bedNo) {
        this.bedNo = bedNo;
    }

    public LocalDate getAllocationDate() {
        return allocationDate;
    }

    public void setAllocationDate(LocalDate allocationDate) {
        this.allocationDate = allocationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public String getRoomBlock() {
        return roomBlock;
    }

    public void setRoomBlock(String roomBlock) {
        this.roomBlock = roomBlock;
    }
}

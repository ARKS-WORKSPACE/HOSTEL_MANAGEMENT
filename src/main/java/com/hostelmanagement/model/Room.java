package com.hostelmanagement.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class Room {
    private Integer id;

    @NotBlank(message = "Room number is required")
    private String roomNo;

    @NotBlank(message = "Block is required")
    private String block;

    @NotNull(message = "Floor is required")
    private Integer floor;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Room capacity must be positive (at least 1)")
    private Integer capacity;

    private Integer occupiedBeds = 0;
    private String status = "AVAILABLE"; // AVAILABLE, PARTIALLY OCCUPIED, FULL
    private LocalDateTime createdAt;

    public Room() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getOccupiedBeds() {
        return occupiedBeds != null ? occupiedBeds : 0;
    }

    public void setOccupiedBeds(Integer occupiedBeds) {
        this.occupiedBeds = occupiedBeds;
    }

    public int getAvailableBeds() {
        int cap = capacity != null ? capacity : 0;
        int occ = occupiedBeds != null ? occupiedBeds : 0;
        return Math.max(0, cap - occ);
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
}

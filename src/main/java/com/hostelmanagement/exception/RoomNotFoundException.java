package com.hostelmanagement.exception;

public class RoomNotFoundException extends ResourceNotFoundException {
    public RoomNotFoundException(Integer id) {
        super("Room not found with ID: " + id);
    }

    public RoomNotFoundException(String message) {
        super(message);
    }
}

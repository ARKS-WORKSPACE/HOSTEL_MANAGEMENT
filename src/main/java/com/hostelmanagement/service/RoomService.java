package com.hostelmanagement.service;

import com.hostelmanagement.exception.AllocationException;
import com.hostelmanagement.exception.RoomNotFoundException;
import com.hostelmanagement.model.Room;
import com.hostelmanagement.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> getAllRooms(String block, String status) {
        return roomRepository.findAll(block, status);
    }

    public Room getRoomById(Integer id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
    }

    public Room createRoom(Room room) {
        if (room.getCapacity() == null || room.getCapacity() <= 0) {
            throw new IllegalArgumentException("Room capacity must be greater than zero.");
        }

        Optional<Room> existing = roomRepository.findByRoomNo(room.getRoomNo());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Room number '" + room.getRoomNo() + "' already exists.");
        }

        if (room.getOccupiedBeds() == null) {
            room.setOccupiedBeds(0);
        }

        return roomRepository.save(room);
    }

    public Room updateRoom(Integer id, Room room) {
        Room existingRoom = getRoomById(id);

        Optional<Room> duplicate = roomRepository.findByRoomNo(room.getRoomNo());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new IllegalArgumentException("Room number '" + room.getRoomNo() + "' already exists.");
        }

        if (room.getCapacity() == null || room.getCapacity() <= 0) {
            throw new IllegalArgumentException("Room capacity must be greater than zero.");
        }

        if (room.getCapacity() < existingRoom.getOccupiedBeds()) {
            throw new IllegalArgumentException("Capacity cannot be reduced below current occupied beds (" + 
                    existingRoom.getOccupiedBeds() + ").");
        }

        room.setId(id);
        room.setOccupiedBeds(existingRoom.getOccupiedBeds()); // maintain current occupancy count
        roomRepository.update(room);
        return getRoomById(id);
    }

    public void deleteRoom(Integer id) {
        Room room = getRoomById(id);
        if (room.getOccupiedBeds() > 0) {
            throw new AllocationException("Cannot delete room '" + room.getRoomNo() + 
                    "' because it currently has " + room.getOccupiedBeds() + 
                    " active occupant(s). Please reassign or cancel allocations first.");
        }
        roomRepository.deleteById(id);
    }
}

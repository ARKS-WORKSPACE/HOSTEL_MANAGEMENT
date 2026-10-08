package com.hostelmanagement.controller;

import com.hostelmanagement.model.Room;
import com.hostelmanagement.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms(
            @RequestParam(required = false) String block,
            @RequestParam(required = false) String status) {
        List<Room> rooms = roomService.getAllRooms(block, status);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Integer id) {
        Room room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@Valid @RequestBody Room room) {
        Room created = roomService.createRoom(room);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Integer id, @Valid @RequestBody Room room) {
        Room updated = roomService.updateRoom(id, room);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRoom(@PathVariable Integer id) {
        roomService.deleteRoom(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Room deleted successfully.");
        return ResponseEntity.ok(response);
    }
}

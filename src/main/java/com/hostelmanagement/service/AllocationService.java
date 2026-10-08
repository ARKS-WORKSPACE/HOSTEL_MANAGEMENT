package com.hostelmanagement.service;

import com.hostelmanagement.exception.AllocationException;
import com.hostelmanagement.exception.DuplicateAllocationException;
import com.hostelmanagement.exception.ResourceNotFoundException;
import com.hostelmanagement.exception.RoomNotFoundException;
import com.hostelmanagement.exception.StudentNotFoundException;
import com.hostelmanagement.model.Allocation;
import com.hostelmanagement.model.Room;
import com.hostelmanagement.model.Student;
import com.hostelmanagement.repository.AllocationRepository;
import com.hostelmanagement.repository.RoomRepository;
import com.hostelmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AllocationService {

    private final AllocationRepository allocationRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;

    public AllocationService(AllocationRepository allocationRepository,
                             StudentRepository studentRepository,
                             RoomRepository roomRepository) {
        this.allocationRepository = allocationRepository;
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
    }

    public List<Allocation> getAllAllocations() {
        return allocationRepository.findAll();
    }

    public Allocation getAllocationById(Integer id) {
        return allocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found with ID: " + id));
    }

    @Transactional
    public Allocation allocateRoom(Allocation allocation) {
        // 1. Validate student exists
        Student student = studentRepository.findById(allocation.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException(allocation.getStudentId()));

        // 2. Validate room exists
        Room room = roomRepository.findById(allocation.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException(allocation.getRoomId()));

        // 3. Business Rule: Check if student already has an active room allocation
        Optional<Allocation> activeStudentAlloc = allocationRepository.findActiveByStudentId(student.getId());
        if (activeStudentAlloc.isPresent()) {
            throw new DuplicateAllocationException("Student '" + student.getName() + " (" + student.getRollNo() + 
                    ")' already has an active room allocation in room " + 
                    (student.getRoomNo() != null ? student.getRoomNo() : activeStudentAlloc.get().getRoomId()) + ".");
        }

        // 4. Business Rule: Check room availability (prevent over-capacity allocation)
        if (room.getOccupiedBeds() >= room.getCapacity()) {
            throw new AllocationException("Room " + room.getRoomNo() + " is already FULL. " +
                    "Capacity: " + room.getCapacity() + ", Occupied: " + room.getOccupiedBeds() + ".");
        }

        // 5. Business Rule: Check if bed number is already occupied within the same room
        Optional<Allocation> activeBedAlloc = allocationRepository.findActiveByRoomAndBed(room.getId(), allocation.getBedNo());
        if (activeBedAlloc.isPresent()) {
            throw new DuplicateAllocationException("Bed '" + allocation.getBedNo() + 
                    "' is already occupied in room " + room.getRoomNo() + ".");
        }

        // 6. Set allocation attributes
        if (allocation.getAllocationDate() == null) {
            allocation.setAllocationDate(LocalDate.now());
        }
        allocation.setStatus("ACTIVE");

        // 7. Save allocation
        Allocation saved = allocationRepository.save(allocation);

        // 8. Update room occupancy (+1) and status
        roomRepository.updateOccupancy(room.getId(), 1);

        // 9. Update student record with room_id
        studentRepository.updateRoom(student.getId(), room.getId());

        // 10. Enrich saved object for response
        saved.setStudentName(student.getName());
        saved.setStudentRollNo(student.getRollNo());
        saved.setRoomNo(room.getRoomNo());
        saved.setRoomBlock(room.getBlock());

        return saved;
    }

    @Transactional
    public void cancelAllocation(Integer id) {
        Allocation allocation = getAllocationById(id);

        if ("CANCELLED".equalsIgnoreCase(allocation.getStatus())) {
            throw new AllocationException("Allocation with ID " + id + " is already cancelled.");
        }

        // 1. Mark allocation status as CANCELLED
        allocationRepository.cancelAllocation(id);

        // 2. Decrement room occupancy (-1)
        roomRepository.updateOccupancy(allocation.getRoomId(), -1);

        // 3. Clear room_id on student
        studentRepository.updateRoom(allocation.getStudentId(), null);
    }
}

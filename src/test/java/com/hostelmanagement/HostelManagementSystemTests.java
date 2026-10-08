package com.hostelmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostelmanagement.model.Allocation;
import com.hostelmanagement.model.Complaint;
import com.hostelmanagement.model.Room;
import com.hostelmanagement.model.Student;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HostelManagementSystemTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // T01: Admin login with valid credentials
    @Test
    @Order(1)
    void testT01_AdminLoginValid() throws Exception {
        Map<String, String> credentials = Map.of("username", "admin", "password", "admin123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.admin.username", is("admin")));
    }

    // T02: Admin login with invalid credentials
    @Test
    @Order(2)
    void testT02_AdminLoginInvalid() throws Exception {
        Map<String, String> credentials = Map.of("username", "admin", "password", "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid username or password")));
    }

    // T03: Add student
    @Test
    @Order(3)
    void testT03_AddStudent() throws Exception {
        Student s = new Student();
        s.setName("Test Student");
        s.setRollNo("TST2025001");
        s.setEmail("test.student@hostel.edu");
        s.setPhone("9988776655");
        s.setCourse("B.Tech");
        s.setBranch("CSE");
        s.setYear(1);
        s.setGuardianName("Guardian Name");
        s.setGuardianPhone("9988776600");
        s.setAddress("Test City");

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(s)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Test Student")))
                .andExpect(jsonPath("$.rollNo", is("TST2025001")));
    }

    // T04: Edit student
    @Test
    @Order(4)
    void testT04_EditStudent() throws Exception {
        // Edit student with id 1
        Student s = new Student();
        s.setName("Aarav Sharma Updated");
        s.setRollNo("CS2023001");
        s.setEmail("aarav.updated@hostel.edu");
        s.setPhone("9876543210");
        s.setCourse("B.Tech");
        s.setBranch("Computer Science");
        s.setYear(2);
        s.setGuardianName("Rajesh Sharma");
        s.setGuardianPhone("9876500001");
        s.setAddress("Updated Address");

        mockMvc.perform(put("/api/students/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(s)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Aarav Sharma Updated")));
    }

    // T05: Delete student (student 5 has no room allocation)
    @Test
    @Order(5)
    void testT05_DeleteStudent() throws Exception {
        mockMvc.perform(delete("/api/students/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    // T06: Add room
    @Test
    @Order(6)
    void testT06_AddRoom() throws Exception {
        Room r = new Room();
        r.setRoomNo("D-101");
        r.setBlock("Block D");
        r.setFloor(1);
        r.setCapacity(2);

        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(r)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomNo", is("D-101")))
                .andExpect(jsonPath("$.capacity", is(2)))
                .andExpect(jsonPath("$.status", is("AVAILABLE")));
    }

    // T07: Edit room
    @Test
    @Order(7)
    void testT07_EditRoom() throws Exception {
        Room r = new Room();
        r.setRoomNo("C-101");
        r.setBlock("Block C");
        r.setFloor(2); // modified floor
        r.setCapacity(1);

        mockMvc.perform(put("/api/rooms/6")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(r)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.floor", is(2)));
    }

    // T08: Allocate student to available room (Room 3 A-201 is available with 3 beds)
    @Test
    @Order(8)
    void testT08_AllocateStudent() throws Exception {
        // Let's create an unallocated student first
        Student s = new Student();
        s.setName("Nehal Gupta");
        s.setRollNo("NG2025099");
        s.setEmail("nehal@hostel.edu");
        s.setPhone("9123456780");
        s.setCourse("B.Tech");
        s.setBranch("Civil");
        s.setYear(1);
        s.setGuardianName("R. Gupta");
        s.setGuardianPhone("9123456789");
        s.setAddress("Jaipur");

        String res = mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(s)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Student saved = objectMapper.readValue(res, Student.class);

        Allocation a = new Allocation();
        a.setStudentId(saved.getId());
        a.setRoomId(3); // Room A-201
        a.setBedNo("Bed-1");
        a.setAllocationDate(LocalDate.now());

        mockMvc.perform(post("/api/allocations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(a)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId", is(saved.getId())))
                .andExpect(jsonPath("$.roomId", is(3)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    // T09: Allocate student twice -> Second active allocation rejected
    @Test
    @Order(9)
    void testT09_AllocateStudentTwiceRejected() throws Exception {
        // Student 1 (Aarav Sharma) already has an active allocation
        Allocation a = new Allocation();
        a.setStudentId(1);
        a.setRoomId(3);
        a.setBedNo("Bed-2");
        a.setAllocationDate(LocalDate.now());

        mockMvc.perform(post("/api/allocations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(a)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already has an active room allocation")));
    }

    // T10: Allocate into full room -> Allocation rejected
    @Test
    @Order(10)
    void testT10_AllocateIntoFullRoomRejected() throws Exception {
        // Create an unallocated student
        Student s = new Student();
        s.setName("Full Room Test Student");
        s.setRollNo("FRT2025001");
        s.setEmail("frt@hostel.edu");
        s.setPhone("9111111111");
        s.setCourse("B.Tech");
        s.setBranch("IT");
        s.setYear(2);
        s.setGuardianName("Guardian");
        s.setGuardianPhone("9222222222");

        String res = mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(s)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Student saved = objectMapper.readValue(res, Student.class);

        // Room 1 (A-101) capacity 2, occupied 2 (FULL)
        Allocation a = new Allocation();
        a.setStudentId(saved.getId());
        a.setRoomId(1);
        a.setBedNo("Bed-3");
        a.setAllocationDate(LocalDate.now());

        mockMvc.perform(post("/api/allocations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(a)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already FULL")));
    }

    // T11: Cancel allocation -> Occupancy -1 and student becomes unallocated
    @Test
    @Order(11)
    void testT11_CancelAllocation() throws Exception {
        // Allocation 4 is for student 4 in Room 4 (B-101)
        mockMvc.perform(delete("/api/allocations/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Verify Student 4 room_id is now cleared
        mockMvc.perform(get("/api/students/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId", nullValue()));
    }

    // T12: Add complaint
    @Test
    @Order(12)
    void testT12_AddComplaint() throws Exception {
        Complaint c = new Complaint();
        c.setStudentId(1);
        c.setCategory("Electrical");
        c.setDescription("Study table lamp socket sparking.");
        c.setComplaintDate(LocalDate.now());

        mockMvc.perform(post("/api/complaints")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(c)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.category", is("Electrical")));
    }

    // T13: Resolve complaint
    @Test
    @Order(13)
    void testT13_ResolveComplaint() throws Exception {
        Map<String, String> payload = Map.of(
                "status", "RESOLVED",
                "resolution", "Socket replaced by maintenance electrician."
        );

        mockMvc.perform(put("/api/complaints/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")))
                .andExpect(jsonPath("$.resolution", containsString("Socket replaced")));
    }

    // T14: Dashboard refresh -> Counts reflect current data
    @Test
    @Order(14)
    void testT14_DashboardRefresh() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalRooms", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.availableBeds", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.occupancyRate", notNullValue()));
    }
}

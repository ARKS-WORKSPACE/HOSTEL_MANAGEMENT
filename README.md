# HOSTEL MANAGEMENT SYSTEM
### Complete Java Full Stack Mini Project Report & Implementation

A modern, robust, and production-grade **Hostel Management System** built with **Java 21**, **Spring Boot 3**, **Spring JdbcTemplate**, **MySQL** (with automated **H2** development fallback), and a responsive **HTML5/CSS3/JavaScript** administrative web portal.

---

## 1. Project Overview & Architecture

### Objectives
- Maintain a centralized student resident directory with full academic and guardian records.
- Track real-time hostel room inventory, block allocation, bed capacities, and current occupancy.
- Allocate students to available rooms while preventing over-capacity allocations, duplicate active allocations, and duplicate bed assignments.
- Track maintenance requests and student complaints with lifecycle status transitions (`PENDING` &rarr; `IN_PROGRESS` &rarr; `RESOLVED`).
- Provide an administrative dashboard with live occupancy gauges, capacity metrics, and recent activities.

### Layered Architecture
```
+-------------------------------------------------------------------+
|               Presentation Layer (Browser Web UI)                 |
|   HTML5, Modern CSS3 (Dark Sidebar + Clean Panels), Vanilla JS    |
|   (index.html, dashboard.html, students.html, rooms.html, etc.)   |
+---------------------------------+---------------------------------+
                                  | HTTP fetch() / JSON
                                  v
+-------------------------------------------------------------------+
|               REST API & Controller Layer                         |
|   Spring Boot Controllers (@RestController, @RequestMapping)      |
|   AuthController, StudentController, RoomController, etc.         |
+---------------------------------+---------------------------------+
                                  | Java Method Invocation
                                  v
+-------------------------------------------------------------------+
|               Business Logic & Service Layer                      |
|   Java Services + OOP (@Service, @Transactional)                  |
|   Business validation, capacity enforcement, exception handling   |
+---------------------------------+---------------------------------+
                                  | Parameterized SQL
                                  v
+-------------------------------------------------------------------+
|               Data Persistence Layer                              |
|   Spring JdbcTemplate / Repositories (@Repository)                |
|   StudentRepository, RoomRepository, AllocationRepository, etc.   |
+---------------------------------+---------------------------------+
                                  | JDBC Driver Connection
                                  v
+-------------------------------------------------------------------+
|               Database Tier (MySQL / Embedded Fallback)           |
|   MySQL 8.0+ (Port 3306) / Automatic H2 MySQL-Mode Fallback       |
+-------------------------------------------------------------------+
```

---

## 2. Technology Stack & Prerequisites

| Tool / Technology | Purpose | Implementation Detail |
| :--- | :--- | :--- |
| **JDK 21** | Java Runtime & Compiler | OpenJDK 21 / Eclipse Temurin |
| **Spring Boot 3.2.5** | Backend Framework | Spring Web, Spring JDBC, Bean Validation |
| **Maven 3.9+** | Build & Dependency Tool | `mvn clean compile`, `mvn test`, `mvn spring-boot:run` |
| **Database** | Relational Database | MySQL 8.0+ (with seamless H2 dev fallback) |
| **DB Connectivity** | Parameterized Execution | `JdbcTemplate` with `PreparedStatement` |
| **Frontend** | Presentation Layer | HTML5, CSS3 (responsive grid/flexbox), JavaScript ES6+ |
| **Development IDE** | Code Editor | VS Code / IntelliJ IDEA / Eclipse |

---

## 3. Project Directory Structure

```
Hostel-Management-System/
├── pom.xml
├── README.md
├── database/
│   └── hostel_management.sql
├── src/
│   ├── main/
│   │   ├── java/com/hostelmanagement/
│   │   │   ├── HostelManagementApplication.java
│   │   │   ├── config/
│   │   │   │   ├── DataSourceConfig.java
│   │   │   │   └── DatabaseInitializer.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── StudentController.java
│   │   │   │   ├── RoomController.java
│   │   │   │   ├── AllocationController.java
│   │   │   │   ├── ComplaintController.java
│   │   │   │   └── DashboardController.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── StudentService.java
│   │   │   │   ├── RoomService.java
│   │   │   │   ├── AllocationService.java
│   │   │   │   ├── ComplaintService.java
│   │   │   │   └── DashboardService.java
│   │   │   ├── repository/
│   │   │   │   ├── AdminRepository.java
│   │   │   │   ├── StudentRepository.java
│   │   │   │   ├── RoomRepository.java
│   │   │   │   ├── AllocationRepository.java
│   │   │   │   └── ComplaintRepository.java
│   │   │   ├── model/
│   │   │   │   ├── Admin.java
│   │   │   │   ├── Student.java
│   │   │   │   ├── Room.java
│   │   │   │   ├── Allocation.java
│   │   │   │   └── Complaint.java
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       ├── ResourceNotFoundException.java
│   │   │       ├── AllocationException.java
│   │   │       ├── DuplicateAllocationException.java
│   │   │       ├── StudentNotFoundException.java
│   │   │       └── RoomNotFoundException.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── index.html          (Admin Login Page)
│   │           ├── dashboard.html      (Hostel Admin Dashboard)
│   │           ├── students.html       (Student Directory & Registration)
│   │           ├── rooms.html          (Room Inventory & Block Capacities)
│   │           ├── allocations.html    (Bed Allocations & Rollbacks)
│   │           ├── complaints.html     (Maintenance Queue & Ticket Resolution)
│   │           ├── css/
│   │           │   └── style.css       (Modern Responsive Styling)
│   │           └── js/
│   │               ├── app.js          (Shared API, Auth, Toast & Modal Helpers)
│   │               ├── students.js     (Student Controller JS)
│   │               ├── rooms.js        (Room Controller JS)
│   │               ├── allocations.js  (Allocation Controller JS)
│   │               └── complaints.js   (Complaints Controller JS)
│   └── test/
│       └── java/com/hostelmanagement/
│           └── HostelManagementSystemTests.java (Comprehensive T01-T14 Test Suite)
```

---

## 4. Database Schema & ER Model

The relational database uses **5 interconnected tables**:

```
+-------------+         +----------------+         +-------------+
|   admins    |         |    students    |         |    rooms    |
+-------------+         +----------------+         +-------------+
| id (PK)     |         | id (PK)        |         | id (PK)     |
| username    |         | name           |         | room_no     |
| password    |         | roll_no (UQ)   |         | block       |
| full_name   |         | email          |         | floor       |
| created_at  |         | phone          |         | capacity    |
+-------------+         | course         |         | occupied_bed|
                        | branch         |         | status      |
                        | `year`         |         | created_at  |
                        | guardian_name  |         +------+------+
                        | guardian_phone |                |
                        | address        |                |
                        | room_id (FK) --+----------------+
                        | created_at     |
                        +---+--------+---+
                            |        |
         +------------------+        +------------------+
         |                                              |
         v                                              v
+-------------------+                          +-------------------+
|    allocations    |                          |    complaints     |
+-------------------+                          +-------------------+
| id (PK)           |                          | id (PK)           |
| student_id (FK)   |                          | student_id (FK)   |
| room_id (FK)      |                          | category          |
| bed_no            |                          | description       |
| allocation_date   |                          | complaint_date    |
| status            |                          | status            |
| created_at        |                          | resolution        |
+-------------------+                          | created_at        |
                                               +-------------------+
```

The database SQL script is located at:
[`database/hostel_management.sql`](file:///c:/Projects/HOSTEL_MANAGEMENT/database/hostel_management.sql)

---

## 5. REST API Catalog

| HTTP Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Authenticate admin (`username`, `password`) |
| `POST` | `/api/auth/logout` | Invalidate administrator session |
| `GET` | `/api/auth/current` | Retrieve current authenticated admin info |
| `GET` | `/api/students` | List all students (optional `?search=...`) |
| `GET` | `/api/students/{id}` | Get single student details |
| `POST` | `/api/students` | Register a new student |
| `PUT` | `/api/students/{id}` | Update student information |
| `DELETE` | `/api/students/{id}` | Delete student record (blocked if actively allocated) |
| `GET` | `/api/rooms` | List rooms (optional `?block=...&status=...`) |
| `GET` | `/api/rooms/{id}` | Get single room details |
| `POST` | `/api/rooms` | Create a new room with capacity |
| `PUT` | `/api/rooms/{id}` | Update room details |
| `DELETE` | `/api/rooms/{id}` | Delete room (blocked if occupied beds > 0) |
| `GET` | `/api/allocations` | List all room allocations |
| `GET` | `/api/allocations/{id}` | Get single allocation record |
| `POST` | `/api/allocations` | Allocate student to room/bed (enforces business rules) |
| `DELETE` | `/api/allocations/{id}` | Cancel allocation (decrements room occupancy) |
| `GET` | `/api/complaints` | List complaints (optional `?status=...&category=...`) |
| `GET` | `/api/complaints/{id}`| Get single complaint ticket |
| `POST` | `/api/complaints` | File a new complaint ticket |
| `PUT` | `/api/complaints/{id}` | Update status (`IN_PROGRESS`, `RESOLVED`) & remarks |
| `DELETE` | `/api/complaints/{id}`| Delete a complaint ticket |
| `GET` | `/api/dashboard/stats` | Retrieve live dashboard metric counters |

---

## 6. Business Rules & Validation Enforced

1. **Required Fields & Formats:** Non-empty validation on mandatory fields. Email format validation.
2. **Identifier Uniqueness:** Student roll numbers and room numbers must be unique across the system.
3. **Room Capacity:** Room capacity must be a positive integer ($\ge 1$).
4. **Bed Availability Rule:** Available beds are computed as $\text{Available Beds} = \text{Capacity} - \text{Occupied Beds}$.
5. **No Double Allocation:** A student cannot have more than one active room allocation simultaneously (`DuplicateAllocationException`).
6. **Full Room Protection:** Allocating into a room with $0$ available beds is rejected with a clear business error (`AllocationException`).
7. **Unique Bed Assignment:** A bed number (e.g., `Bed-1`) cannot be assigned twice within the same room (`DuplicateAllocationException`).
8. **Occupancy Rollback:** Cancelling an allocation marks status as `CANCELLED`, decrements room occupancy by 1, and clears the student's room assignment atomically (`@Transactional`).
9. **Referential Deletion Protection:** Deleting a student who holds an active allocation is blocked until the allocation is cancelled. Deleting an occupied room is blocked until occupants are reassigned.
10. **Global Exception Handling:** Custom exceptions return standardized, safe JSON responses without exposing raw stack traces:
```json
{
  "timestamp": "2026-10-08T20:10:48",
  "status": 400,
  "error": "Allocation Error",
  "message": "Room A-101 is already FULL. Capacity: 2, Occupied: 2."
}
```

---

## 7. Installation & Run Procedure

### Prerequisites
1. **JDK 21** installed and verified (`java -version`).
2. **Maven 3.8+** installed (`mvn -version`).
3. Modern Web Browser (Chrome, Edge, Firefox).

### Step 1: Clone or Open Project
Open the project directory in VS Code or your terminal:
```bash
cd c:\Projects\HOSTEL_MANAGEMENT
```

### Step 2: Configure MySQL (Optional for Demo)
If running with MySQL:
1. Open MySQL Workbench or MySQL CLI.
2. Run the script in `database/hostel_management.sql`:
   ```bash
   mysql -u root -p < database/hostel_management.sql
   ```
3. Update `src/main/resources/application.properties` with your MySQL password:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/hostel_management?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=YOUR_PASSWORD
   ```

> **Automated Fallback Note:** If MySQL credentials are not configured or MySQL is not running, the application will automatically activate the **H2 file-persistent fallback database** with MySQL syntax compatibility. You can test immediately without any setup!

### Step 3: Build & Run
Run the Spring Boot application using Maven:
```bash
mvn spring-boot:run
```

### Step 4: Access Application
Open your web browser and navigate to:
```
http://localhost:8080
```

- **Default Administrator Credentials:**
  - **Username:** `admin`
  - **Password:** `admin123`

---

## 8. Verification & Test Plan (T01 - T14)

All 14 test cases specified in the project report are automated in [`HostelManagementSystemTests.java`](file:///c:/Projects/HOSTEL_MANAGEMENT/src/test/java/com/hostelmanagement/HostelManagementSystemTests.java):

| Test ID | Test Scenario | Expected Result | Status |
| :---: | :--- | :--- | :---: |
| **T01** | Admin login with valid credentials | Dashboard opens with success message | **PASSED** |
| **T02** | Admin login with invalid credentials | Clear error message: "Invalid username or password" | **PASSED** |
| **T03** | Add student | Student record saved and appears in directory | **PASSED** |
| **T04** | Edit student | Updated student attributes persist | **PASSED** |
| **T05** | Delete student | Student removed from directory | **PASSED** |
| **T06** | Add room | Room created with positive capacity and status `AVAILABLE` | **PASSED** |
| **T07** | Edit room | Room floor/block details update | **PASSED** |
| **T08** | Allocate student to available room | Allocation record created, room occupancy increments +1 | **PASSED** |
| **T09** | Allocate student twice | Second active allocation rejected with duplicate error | **PASSED** |
| **T10** | Allocate into full room | Allocation rejected with room full business error | **PASSED** |
| **T11** | Cancel allocation | Occupancy decrements -1 and student room becomes unallocated | **PASSED** |
| **T12** | Add complaint | Ticket created with initial `PENDING` status | **PASSED** |
| **T13** | Resolve complaint | Status updates to `RESOLVED` with technician resolution notes | **PASSED** |
| **T14** | Dashboard refresh | Summary counters accurately reflect database state | **PASSED** |

To execute the entire test suite anytime:
```bash
mvn test
```

---

## 9. Recommended College Viva Demo Sequence

1. **Login:** Log in with `admin` / `admin123`.
2. **Dashboard Overview:** Review metric cards (Total Students, Total Rooms, Occupied Beds, Available Beds, Pending Issues, Occupancy Rate).
3. **Student Management:** Navigate to **Students**, search an existing resident, add a new student, and edit student details.
4. **Room Inventory:** Navigate to **Rooms**, show rooms with status badges (`AVAILABLE`, `PARTIALLY OCCUPIED`, `FULL`), add a new room.
5. **Room Allocation (Core Business Rule Demo):**
   - Navigate to **Room Allocation**, click **Allocate Room / Bed**.
   - Show available rooms and bed selector.
   - Allocate a student &rarr; notice occupancy increase on the dashboard.
   - Attempt to allocate the same student again &rarr; show duplicate rejection.
   - Attempt to allocate into a full room &rarr; show capacity full rejection.
6. **Cancellation & Rollback:** Cancel an allocation &rarr; verify occupancy rollback and bed restoration.
7. **Complaints Workflow:** Navigate to **Complaints**, record an electrical issue with `PENDING` status, update status to `IN_PROGRESS`, and mark `RESOLVED` with technician resolution notes.

---

## 10. Viva / Presentation Points

| Question | Answer |
| :--- | :--- |
| **Why Spring Boot?** | Simplifies Java web development, provides embedded Tomcat, handles dependency management via Starters, and configures REST APIs with minimal boilerplate. |
| **What is a REST API?** | Representational State Transfer; an architectural style where resources are accessed using standard HTTP verbs (`GET`, `POST`, `PUT`, `DELETE`) exchanging JSON payloads. |
| **Why MySQL?** | A reliable relational database suitable for normalized data models with primary and foreign key constraints between students, rooms, and allocations. |
| **What is JDBC / Spring JdbcTemplate?** | Java Database Connectivity. Spring's `JdbcTemplate` simplifies JDBC development by handling connection opening/closing, transaction boundaries, and result-set mapping while preventing SQL injection through parameterized queries. |
| **Why Layered Architecture?** | Separates presentation (HTML/CSS/JS), API/Controller layer, Service/business rules layer, and Data/Repository layer, making the codebase modular, testable, and maintainable. |
| **How are SQL Injections Prevented?** | By using parameterized queries with `PreparedStatement` and `JdbcTemplate` rather than concatenating user input strings. |
| **What happens when a room is full?** | `AllocationService` verifies $\text{occupied\_beds} \ge \text{capacity}$ before saving and throws `AllocationException`, returning a clean HTTP 400 error. |

---

## 11. Security, Limitations & Future Scope

- **Basic Security:** Server-side validation, parameterized SQL via `JdbcTemplate`, exception masking, and admin session management.
- **Current Limitations:** No live SMS/WhatsApp gateway, no payment gateway for fee collection, simplified authentication suited for an academic mini project.
- **Future Scope:** Student self-service login, online hostel fee payment integration (Razorpay/Stripe), biometric gate attendance integration, and automated maintenance work orders.

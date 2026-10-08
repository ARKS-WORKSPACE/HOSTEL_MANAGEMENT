package com.hostelmanagement.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        logger.info("Initializing Hostel Management System Database Schema...");

        // 1. Create Admins table
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS admins (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                password VARCHAR(100) NOT NULL,
                full_name VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // 2. Create Rooms table
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS rooms (
                id INT AUTO_INCREMENT PRIMARY KEY,
                room_no VARCHAR(20) NOT NULL UNIQUE,
                block VARCHAR(20) NOT NULL,
                floor INT NOT NULL,
                capacity INT NOT NULL,
                occupied_beds INT NOT NULL DEFAULT 0,
                status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // 3. Create Students table
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS students (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                roll_no VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) NOT NULL,
                phone VARCHAR(20) NOT NULL,
                course VARCHAR(50) NOT NULL,
                branch VARCHAR(50) NOT NULL,
                `year` INT NOT NULL,
                guardian_name VARCHAR(100) NOT NULL,
                guardian_phone VARCHAR(20) NOT NULL,
                address TEXT,
                room_id INT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
            )
        """);

        // 4. Create Allocations table
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS allocations (
                id INT AUTO_INCREMENT PRIMARY KEY,
                student_id INT NOT NULL,
                room_id INT NOT NULL,
                bed_no VARCHAR(10) NOT NULL,
                allocation_date DATE NOT NULL,
                status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
                FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
            )
        """);

        // 5. Create Complaints table
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS complaints (
                id INT AUTO_INCREMENT PRIMARY KEY,
                student_id INT NOT NULL,
                category VARCHAR(50) NOT NULL,
                description TEXT NOT NULL,
                complaint_date DATE NOT NULL,
                status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                resolution TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
            )
        """);

        // Seed default Admin if not exists
        Integer adminCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admins", Integer.class);
        if (adminCount == null || adminCount == 0) {
            logger.info("Seeding initial administrator account (admin / admin123)...");
            jdbcTemplate.update(
                "INSERT INTO admins (username, password, full_name) VALUES (?, ?, ?)",
                "admin", "admin123", "Hostel Chief Warden"
            );
        }

        // Seed initial Rooms if empty
        Integer roomCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM rooms", Integer.class);
        if (roomCount == null || roomCount == 0) {
            logger.info("Seeding initial hostel rooms...");
            jdbcTemplate.update("INSERT INTO rooms (id, room_no, block, floor, capacity, occupied_beds, status) VALUES (1, 'A-101', 'Block A', 1, 2, 2, 'FULL')");
            jdbcTemplate.update("INSERT INTO rooms (id, room_no, block, floor, capacity, occupied_beds, status) VALUES (2, 'A-102', 'Block A', 1, 2, 1, 'PARTIALLY OCCUPIED')");
            jdbcTemplate.update("INSERT INTO rooms (id, room_no, block, floor, capacity, occupied_beds, status) VALUES (3, 'A-201', 'Block A', 2, 3, 0, 'AVAILABLE')");
            jdbcTemplate.update("INSERT INTO rooms (id, room_no, block, floor, capacity, occupied_beds, status) VALUES (4, 'B-101', 'Block B', 1, 2, 1, 'PARTIALLY OCCUPIED')");
            jdbcTemplate.update("INSERT INTO rooms (id, room_no, block, floor, capacity, occupied_beds, status) VALUES (5, 'B-202', 'Block B', 2, 3, 0, 'AVAILABLE')");
            jdbcTemplate.update("INSERT INTO rooms (id, room_no, block, floor, capacity, occupied_beds, status) VALUES (6, 'C-101', 'Block C', 1, 1, 0, 'AVAILABLE')");
        }

        // Seed initial Students if empty
        Integer studentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Integer.class);
        if (studentCount == null || studentCount == 0) {
            logger.info("Seeding initial student directory...");
            jdbcTemplate.update("INSERT INTO students (id, name, roll_no, email, phone, course, branch, `year`, guardian_name, guardian_phone, address, room_id) VALUES " +
                "(1, 'Aarav Sharma', 'CS2023001', 'aarav.sharma@hostel.edu', '9876543210', 'B.Tech', 'Computer Science', 2, 'Rajesh Sharma', '9876500001', '124 MG Road, Jaipur', 1)");
            jdbcTemplate.update("INSERT INTO students (id, name, roll_no, email, phone, course, branch, `year`, guardian_name, guardian_phone, address, room_id) VALUES " +
                "(2, 'Rohan Mehta', 'CS2023002', 'rohan.mehta@hostel.edu', '9876543211', 'B.Tech', 'Computer Science', 2, 'Suresh Mehta', '9876500002', '45 Park Street, Delhi', 1)");
            jdbcTemplate.update("INSERT INTO students (id, name, roll_no, email, phone, course, branch, `year`, guardian_name, guardian_phone, address, room_id) VALUES " +
                "(3, 'Priya Patel', 'EC2023015', 'priya.patel@hostel.edu', '9876543212', 'B.Tech', 'Electronics', 3, 'Mahesh Patel', '9876500003', '78 Ring Road, Ahmedabad', 2)");
            jdbcTemplate.update("INSERT INTO students (id, name, roll_no, email, phone, course, branch, `year`, guardian_name, guardian_phone, address, room_id) VALUES " +
                "(4, 'Vikram Singh', 'ME2023040', 'vikram.singh@hostel.edu', '9876543213', 'B.Tech', 'Mechanical', 2, 'Ajay Singh', '9876500004', '89 Civil Lines, Lucknow', 4)");
            jdbcTemplate.update("INSERT INTO students (id, name, roll_no, email, phone, course, branch, `year`, guardian_name, guardian_phone, address, room_id) VALUES " +
                "(5, 'Ananya Verma', 'IT2023022', 'ananya.verma@hostel.edu', '9876543214', 'B.Tech', 'Information Tech', 1, 'Vikas Verma', '9876500005', '12 Lake View, Pune', NULL)");
        }

        // Seed Allocations if empty
        Integer allocCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM allocations", Integer.class);
        if (allocCount == null || allocCount == 0) {
            logger.info("Seeding initial active room allocations...");
            jdbcTemplate.update("INSERT INTO allocations (id, student_id, room_id, bed_no, allocation_date, status) VALUES " +
                "(1, 1, 1, 'Bed-1', '2025-08-01', 'ACTIVE')");
            jdbcTemplate.update("INSERT INTO allocations (id, student_id, room_id, bed_no, allocation_date, status) VALUES " +
                "(2, 2, 1, 'Bed-2', '2025-08-01', 'ACTIVE')");
            jdbcTemplate.update("INSERT INTO allocations (id, student_id, room_id, bed_no, allocation_date, status) VALUES " +
                "(3, 3, 2, 'Bed-1', '2025-08-02', 'ACTIVE')");
            jdbcTemplate.update("INSERT INTO allocations (id, student_id, room_id, bed_no, allocation_date, status) VALUES " +
                "(4, 4, 4, 'Bed-1', '2025-08-05', 'ACTIVE')");
        }

        // Seed Complaints if empty
        Integer compCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM complaints", Integer.class);
        if (compCount == null || compCount == 0) {
            logger.info("Seeding initial hostel complaints queue...");
            jdbcTemplate.update("INSERT INTO complaints (id, student_id, category, description, complaint_date, status, resolution) VALUES " +
                "(1, 1, 'Electrical', 'Ceiling fan in room A-101 making excessive noise and vibrating.', '2025-08-10', 'PENDING', NULL)");
            jdbcTemplate.update("INSERT INTO complaints (id, student_id, category, description, complaint_date, status, resolution) VALUES " +
                "(2, 3, 'Plumbing', 'Bathroom tap leaking continuously in room A-102.', '2025-08-11', 'IN_PROGRESS', 'Plumber dispatched to inspect pipe washer.')");
            jdbcTemplate.update("INSERT INTO complaints (id, student_id, category, description, complaint_date, status, resolution) VALUES " +
                "(3, 2, 'Internet', 'Wi-Fi connectivity dropping frequently during evening hours.', '2025-08-05', 'RESOLVED', 'Router reset and antenna readjusted on Floor 1.')");
        }

        logger.info("Database initialization completed successfully.");
    }
}

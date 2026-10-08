package com.hostelmanagement.repository;

import com.hostelmanagement.model.Student;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class StudentRepository {

    private final JdbcTemplate jdbcTemplate;

    public StudentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Student> studentRowMapper = (rs, rowNum) -> {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setRollNo(rs.getString("roll_no"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setCourse(rs.getString("course"));
        s.setBranch(rs.getString("branch"));
        s.setYear(rs.getInt("year"));
        s.setGuardianName(rs.getString("guardian_name"));
        s.setGuardianPhone(rs.getString("guardian_phone"));
        s.setAddress(rs.getString("address"));
        int roomId = rs.getInt("room_id");
        if (!rs.wasNull()) {
            s.setRoomId(roomId);
        }
        try {
            s.setRoomNo(rs.getString("room_no"));
        } catch (Exception ignored) {}
        try {
            s.setBedNo(rs.getString("bed_no"));
        } catch (Exception ignored) {}
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            s.setCreatedAt(ts.toLocalDateTime());
        }
        return s;
    };

    public List<Student> findAll(String search) {
        StringBuilder sql = new StringBuilder(
            "SELECT s.*, r.room_no, a.bed_no FROM students s " +
            "LEFT JOIN rooms r ON s.room_id = r.id " +
            "LEFT JOIN allocations a ON s.id = a.student_id AND a.status = 'ACTIVE' "
        );
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            String pattern = "%" + search.trim() + "%";
            sql.append("WHERE LOWER(s.name) LIKE LOWER(?) OR LOWER(s.roll_no) LIKE LOWER(?) OR LOWER(s.branch) LIKE LOWER(?) OR LOWER(s.email) LIKE LOWER(?) ");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append("ORDER BY s.id DESC");
        return jdbcTemplate.query(sql.toString(), studentRowMapper, params.toArray());
    }

    public Optional<Student> findById(Integer id) {
        String sql = "SELECT s.*, r.room_no, a.bed_no FROM students s " +
                     "LEFT JOIN rooms r ON s.room_id = r.id " +
                     "LEFT JOIN allocations a ON s.id = a.student_id AND a.status = 'ACTIVE' " +
                     "WHERE s.id = ?";
        try {
            Student student = jdbcTemplate.queryForObject(sql, studentRowMapper, id);
            return Optional.ofNullable(student);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Student> findByRollNo(String rollNo) {
        String sql = "SELECT s.*, r.room_no, a.bed_no FROM students s " +
                     "LEFT JOIN rooms r ON s.room_id = r.id " +
                     "LEFT JOIN allocations a ON s.id = a.student_id AND a.status = 'ACTIVE' " +
                     "WHERE s.roll_no = ?";
        try {
            Student student = jdbcTemplate.queryForObject(sql, studentRowMapper, rollNo);
            return Optional.ofNullable(student);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Student save(Student student) {
        String sql = "INSERT INTO students (name, roll_no, email, phone, course, branch, `year`, guardian_name, guardian_phone, address, room_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, student.getName());
            ps.setString(2, student.getRollNo());
            ps.setString(3, student.getEmail());
            ps.setString(4, student.getPhone());
            ps.setString(5, student.getCourse());
            ps.setString(6, student.getBranch());
            ps.setInt(7, student.getYear());
            ps.setString(8, student.getGuardianName());
            ps.setString(9, student.getGuardianPhone());
            ps.setString(10, student.getAddress());
            if (student.getRoomId() != null) {
                ps.setInt(11, student.getRoomId());
            } else {
                ps.setNull(11, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        if (keyHolder.getKeys() != null) {
            Object idVal = keyHolder.getKeys().get("id");
            if (idVal == null) idVal = keyHolder.getKeys().get("ID");
            if (idVal instanceof Number n) {
                student.setId(n.intValue());
            }
        } else if (keyHolder.getKey() != null) {
            student.setId(keyHolder.getKey().intValue());
        }
        return student;
    }

    public int update(Student student) {
        String sql = "UPDATE students SET name = ?, roll_no = ?, email = ?, phone = ?, course = ?, branch = ?, " +
                     "`year` = ?, guardian_name = ?, guardian_phone = ?, address = ?, room_id = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                student.getName(),
                student.getRollNo(),
                student.getEmail(),
                student.getPhone(),
                student.getCourse(),
                student.getBranch(),
                student.getYear(),
                student.getGuardianName(),
                student.getGuardianPhone(),
                student.getAddress(),
                student.getRoomId(),
                student.getId()
        );
    }

    public int updateRoom(Integer studentId, Integer roomId) {
        String sql = "UPDATE students SET room_id = ? WHERE id = ?";
        return jdbcTemplate.update(sql, roomId, studentId);
    }

    public int deleteById(Integer id) {
        String sql = "DELETE FROM students WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int count() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM students", Integer.class);
        return count != null ? count : 0;
    }
}

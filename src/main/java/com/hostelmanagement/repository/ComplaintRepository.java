package com.hostelmanagement.repository;

import com.hostelmanagement.model.Complaint;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ComplaintRepository {

    private final JdbcTemplate jdbcTemplate;

    public ComplaintRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Complaint> complaintRowMapper = (rs, rowNum) -> {
        Complaint c = new Complaint();
        c.setId(rs.getInt("id"));
        c.setStudentId(rs.getInt("student_id"));
        c.setCategory(rs.getString("category"));
        c.setDescription(rs.getString("description"));
        Date d = rs.getDate("complaint_date");
        if (d != null) {
            c.setComplaintDate(d.toLocalDate());
        }
        c.setStatus(rs.getString("status"));
        c.setResolution(rs.getString("resolution"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            c.setCreatedAt(ts.toLocalDateTime());
        }

        try {
            c.setStudentName(rs.getString("student_name"));
        } catch (Exception ignored) {}
        try {
            c.setStudentRollNo(rs.getString("student_roll_no"));
        } catch (Exception ignored) {}
        try {
            c.setRoomNo(rs.getString("room_no"));
        } catch (Exception ignored) {}

        return c;
    };

    public List<Complaint> findAll(String status, String category) {
        StringBuilder sql = new StringBuilder(
            "SELECT c.*, s.name AS student_name, s.roll_no AS student_roll_no, r.room_no " +
            "FROM complaints c " +
            "JOIN students s ON c.student_id = s.id " +
            "LEFT JOIN rooms r ON s.room_id = r.id " +
            "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND c.status = ? ");
            params.add(status.trim().toUpperCase());
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            sql.append("AND LOWER(c.category) = LOWER(?) ");
            params.add(category.trim());
        }

        sql.append("ORDER BY c.id DESC");
        return jdbcTemplate.query(sql.toString(), complaintRowMapper, params.toArray());
    }

    public Optional<Complaint> findById(Integer id) {
        String sql = "SELECT c.*, s.name AS student_name, s.roll_no AS student_roll_no, r.room_no " +
                     "FROM complaints c " +
                     "JOIN students s ON c.student_id = s.id " +
                     "LEFT JOIN rooms r ON s.room_id = r.id " +
                     "WHERE c.id = ?";
        try {
            Complaint c = jdbcTemplate.queryForObject(sql, complaintRowMapper, id);
            return Optional.ofNullable(c);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Complaint save(Complaint complaint) {
        String sql = "INSERT INTO complaints (student_id, category, description, complaint_date, status, resolution) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, complaint.getStudentId());
            ps.setString(2, complaint.getCategory());
            ps.setString(3, complaint.getDescription());
            ps.setDate(4, Date.valueOf(complaint.getComplaintDate()));
            ps.setString(5, complaint.getStatus() != null ? complaint.getStatus() : "PENDING");
            ps.setString(6, complaint.getResolution());
            return ps;
        }, keyHolder);

        if (keyHolder.getKeys() != null) {
            Object idVal = keyHolder.getKeys().get("id");
            if (idVal == null) idVal = keyHolder.getKeys().get("ID");
            if (idVal instanceof Number n) {
                complaint.setId(n.intValue());
            }
        } else if (keyHolder.getKey() != null) {
            complaint.setId(keyHolder.getKey().intValue());
        }
        return complaint;
    }

    public int update(Complaint complaint) {
        String sql = "UPDATE complaints SET status = ?, resolution = ? WHERE id = ?";
        return jdbcTemplate.update(sql, complaint.getStatus(), complaint.getResolution(), complaint.getId());
    }

    public int deleteById(Integer id) {
        String sql = "DELETE FROM complaints WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int countByStatus(String status) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM complaints WHERE status = ?", Integer.class, status);
        return count != null ? count : 0;
    }

    public List<Complaint> findRecentComplaints(int limit) {
        String sql = "SELECT c.*, s.name AS student_name, s.roll_no AS student_roll_no, r.room_no " +
                     "FROM complaints c " +
                     "JOIN students s ON c.student_id = s.id " +
                     "LEFT JOIN rooms r ON s.room_id = r.id " +
                     "ORDER BY c.id DESC LIMIT ?";
        return jdbcTemplate.query(sql, complaintRowMapper, limit);
    }
}

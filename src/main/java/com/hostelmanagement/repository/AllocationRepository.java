package com.hostelmanagement.repository;

import com.hostelmanagement.model.Allocation;
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
import java.util.List;
import java.util.Optional;

@Repository
public class AllocationRepository {

    private final JdbcTemplate jdbcTemplate;

    public AllocationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Allocation> allocationRowMapper = (rs, rowNum) -> {
        Allocation a = new Allocation();
        a.setId(rs.getInt("id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setRoomId(rs.getInt("room_id"));
        a.setBedNo(rs.getString("bed_no"));
        Date d = rs.getDate("allocation_date");
        if (d != null) {
            a.setAllocationDate(d.toLocalDate());
        }
        a.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            a.setCreatedAt(ts.toLocalDateTime());
        }

        try {
            a.setStudentName(rs.getString("student_name"));
        } catch (Exception ignored) {}
        try {
            a.setStudentRollNo(rs.getString("student_roll_no"));
        } catch (Exception ignored) {}
        try {
            a.setRoomNo(rs.getString("room_no"));
        } catch (Exception ignored) {}
        try {
            a.setRoomBlock(rs.getString("room_block"));
        } catch (Exception ignored) {}

        return a;
    };

    public List<Allocation> findAll() {
        String sql = "SELECT a.*, s.name AS student_name, s.roll_no AS student_roll_no, " +
                     "r.room_no, r.block AS room_block " +
                     "FROM allocations a " +
                     "JOIN students s ON a.student_id = s.id " +
                     "JOIN rooms r ON a.room_id = r.id " +
                     "ORDER BY a.id DESC";
        return jdbcTemplate.query(sql, allocationRowMapper);
    }

    public Optional<Allocation> findById(Integer id) {
        String sql = "SELECT a.*, s.name AS student_name, s.roll_no AS student_roll_no, " +
                     "r.room_no, r.block AS room_block " +
                     "FROM allocations a " +
                     "JOIN students s ON a.student_id = s.id " +
                     "JOIN rooms r ON a.room_id = r.id " +
                     "WHERE a.id = ?";
        try {
            Allocation a = jdbcTemplate.queryForObject(sql, allocationRowMapper, id);
            return Optional.ofNullable(a);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Allocation> findActiveByStudentId(Integer studentId) {
        String sql = "SELECT a.*, s.name AS student_name, s.roll_no AS student_roll_no, " +
                     "r.room_no, r.block AS room_block " +
                     "FROM allocations a " +
                     "JOIN students s ON a.student_id = s.id " +
                     "JOIN rooms r ON a.room_id = r.id " +
                     "WHERE a.student_id = ? AND a.status = 'ACTIVE'";
        try {
            Allocation a = jdbcTemplate.queryForObject(sql, allocationRowMapper, studentId);
            return Optional.ofNullable(a);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Allocation> findActiveByRoomAndBed(Integer roomId, String bedNo) {
        String sql = "SELECT a.*, s.name AS student_name, s.roll_no AS student_roll_no, " +
                     "r.room_no, r.block AS room_block " +
                     "FROM allocations a " +
                     "JOIN students s ON a.student_id = s.id " +
                     "JOIN rooms r ON a.room_id = r.id " +
                     "WHERE a.room_id = ? AND LOWER(a.bed_no) = LOWER(?) AND a.status = 'ACTIVE'";
        try {
            Allocation a = jdbcTemplate.queryForObject(sql, allocationRowMapper, roomId, bedNo.trim());
            return Optional.ofNullable(a);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Allocation save(Allocation allocation) {
        String sql = "INSERT INTO allocations (student_id, room_id, bed_no, allocation_date, status) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, allocation.getStudentId());
            ps.setInt(2, allocation.getRoomId());
            ps.setString(3, allocation.getBedNo());
            ps.setDate(4, Date.valueOf(allocation.getAllocationDate()));
            ps.setString(5, allocation.getStatus() != null ? allocation.getStatus() : "ACTIVE");
            return ps;
        }, keyHolder);

        if (keyHolder.getKeys() != null) {
            Object idVal = keyHolder.getKeys().get("id");
            if (idVal == null) idVal = keyHolder.getKeys().get("ID");
            if (idVal instanceof Number n) {
                allocation.setId(n.intValue());
            }
        } else if (keyHolder.getKey() != null) {
            allocation.setId(keyHolder.getKey().intValue());
        }
        return allocation;
    }

    public int cancelAllocation(Integer id) {
        String sql = "UPDATE allocations SET status = 'CANCELLED' WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public List<Allocation> findRecentAllocations(int limit) {
        String sql = "SELECT a.*, s.name AS student_name, s.roll_no AS student_roll_no, " +
                     "r.room_no, r.block AS room_block " +
                     "FROM allocations a " +
                     "JOIN students s ON a.student_id = s.id " +
                     "JOIN rooms r ON a.room_id = r.id " +
                     "ORDER BY a.id DESC LIMIT ?";
        return jdbcTemplate.query(sql, allocationRowMapper, limit);
    }
}

package com.hostelmanagement.repository;

import com.hostelmanagement.model.Admin;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class AdminRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Admin> adminRowMapper = (rs, rowNum) -> {
        Admin admin = new Admin();
        admin.setId(rs.getInt("id"));
        admin.setUsername(rs.getString("username"));
        admin.setPassword(rs.getString("password"));
        admin.setFullName(rs.getString("full_name"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            admin.setCreatedAt(ts.toLocalDateTime());
        }
        return admin;
    };

    public Optional<Admin> findByUsername(String username) {
        String sql = "SELECT * FROM admins WHERE username = ?";
        try {
            Admin admin = jdbcTemplate.queryForObject(sql, adminRowMapper, username);
            return Optional.ofNullable(admin);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Admin> findById(Integer id) {
        String sql = "SELECT * FROM admins WHERE id = ?";
        try {
            Admin admin = jdbcTemplate.queryForObject(sql, adminRowMapper, id);
            return Optional.ofNullable(admin);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}

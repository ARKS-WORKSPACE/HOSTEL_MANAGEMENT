package com.hostelmanagement.repository;

import com.hostelmanagement.model.Room;
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
public class RoomRepository {

    private final JdbcTemplate jdbcTemplate;

    public RoomRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Room> roomRowMapper = (rs, rowNum) -> {
        Room r = new Room();
        r.setId(rs.getInt("id"));
        r.setRoomNo(rs.getString("room_no"));
        r.setBlock(rs.getString("block"));
        r.setFloor(rs.getInt("floor"));
        r.setCapacity(rs.getInt("capacity"));
        r.setOccupiedBeds(rs.getInt("occupied_beds"));
        r.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            r.setCreatedAt(ts.toLocalDateTime());
        }
        return r;
    };

    public List<Room> findAll(String block, String status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM rooms WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (block != null && !block.trim().isEmpty() && !block.equalsIgnoreCase("ALL")) {
            sql.append("AND LOWER(block) = LOWER(?) ");
            params.add(block.trim());
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND status = ? ");
            params.add(status.trim().toUpperCase());
        }

        sql.append("ORDER BY room_no ASC");
        return jdbcTemplate.query(sql.toString(), roomRowMapper, params.toArray());
    }

    public Optional<Room> findById(Integer id) {
        String sql = "SELECT * FROM rooms WHERE id = ?";
        try {
            Room room = jdbcTemplate.queryForObject(sql, roomRowMapper, id);
            return Optional.ofNullable(room);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Room> findByRoomNo(String roomNo) {
        String sql = "SELECT * FROM rooms WHERE LOWER(room_no) = LOWER(?)";
        try {
            Room room = jdbcTemplate.queryForObject(sql, roomRowMapper, roomNo);
            return Optional.ofNullable(room);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Room save(Room room) {
        // Calculate status on creation
        int cap = room.getCapacity() != null ? room.getCapacity() : 1;
        int occ = room.getOccupiedBeds() != null ? room.getOccupiedBeds() : 0;
        String status = "AVAILABLE";
        if (occ >= cap) {
            status = "FULL";
        } else if (occ > 0) {
            status = "PARTIALLY OCCUPIED";
        }
        room.setStatus(status);

        String sql = "INSERT INTO rooms (room_no, block, floor, capacity, occupied_beds, status) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, room.getRoomNo());
            ps.setString(2, room.getBlock());
            ps.setInt(3, room.getFloor());
            ps.setInt(4, room.getCapacity());
            ps.setInt(5, room.getOccupiedBeds());
            ps.setString(6, room.getStatus());
            return ps;
        }, keyHolder);

        if (keyHolder.getKeys() != null) {
            Object idVal = keyHolder.getKeys().get("id");
            if (idVal == null) idVal = keyHolder.getKeys().get("ID");
            if (idVal instanceof Number n) {
                room.setId(n.intValue());
            }
        } else if (keyHolder.getKey() != null) {
            room.setId(keyHolder.getKey().intValue());
        }
        return room;
    }

    public int update(Room room) {
        // Refresh status based on current capacity and occupancy
        int cap = room.getCapacity() != null ? room.getCapacity() : 1;
        int occ = room.getOccupiedBeds() != null ? room.getOccupiedBeds() : 0;
        String status = "AVAILABLE";
        if (occ >= cap) {
            status = "FULL";
        } else if (occ > 0) {
            status = "PARTIALLY OCCUPIED";
        }
        room.setStatus(status);

        String sql = "UPDATE rooms SET room_no = ?, block = ?, floor = ?, capacity = ?, occupied_beds = ?, status = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                room.getRoomNo(),
                room.getBlock(),
                room.getFloor(),
                room.getCapacity(),
                room.getOccupiedBeds(),
                room.getStatus(),
                room.getId()
        );
    }

    public void updateOccupancy(Integer roomId, int change) {
        Optional<Room> optionalRoom = findById(roomId);
        if (optionalRoom.isEmpty()) {
            return;
        }
        Room room = optionalRoom.get();
        int newOcc = Math.max(0, room.getOccupiedBeds() + change);
        room.setOccupiedBeds(newOcc);

        String status = "AVAILABLE";
        if (newOcc >= room.getCapacity()) {
            status = "FULL";
        } else if (newOcc > 0) {
            status = "PARTIALLY OCCUPIED";
        }
        room.setStatus(status);

        String sql = "UPDATE rooms SET occupied_beds = ?, status = ? WHERE id = ?";
        jdbcTemplate.update(sql, newOcc, status, roomId);
    }

    public int deleteById(Integer id) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int count() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM rooms", Integer.class);
        return count != null ? count : 0;
    }

    public int getTotalCapacity() {
        Integer cap = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(capacity), 0) FROM rooms", Integer.class);
        return cap != null ? cap : 0;
    }

    public int getTotalOccupied() {
        Integer occ = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(occupied_beds), 0) FROM rooms", Integer.class);
        return occ != null ? occ : 0;
    }
}

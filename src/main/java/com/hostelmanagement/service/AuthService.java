package com.hostelmanagement.service;

import com.hostelmanagement.model.Admin;
import com.hostelmanagement.repository.AdminRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final AdminRepository adminRepository;

    public AuthService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Admin authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        Optional<Admin> adminOpt = adminRepository.findByUsername(username.trim());
        if (adminOpt.isEmpty() || !adminOpt.get().getPassword().equals(password.trim())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        Admin admin = adminOpt.get();
        // Hide password before returning
        admin.setPassword(null);
        return admin;
    }

    public Optional<Admin> findById(Integer id) {
        return adminRepository.findById(id).map(a -> {
            a.setPassword(null);
            return a;
        });
    }
}

package com.university.ums.service;

import com.university.ums.model.Admin;
import com.university.ums.repository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService extends PersonService<Admin> {

    public AdminService(AdminRepository repo, PasswordEncoder encoder, UserLookup lookup) {
        super(repo, encoder, lookup, "Admin");
    }

    @Override
    protected void copyExtraFields(Admin target, Admin source) {
        // Admins have no extra fields
    }
}
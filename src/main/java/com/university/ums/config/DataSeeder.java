package com.university.ums.config;

import com.university.ums.model.Admin;
import com.university.ums.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AdminRepository admins;
    private final PasswordEncoder encoder;

    public DataSeeder(AdminRepository admins, PasswordEncoder encoder) {
        this.admins = admins;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (admins.count() == 0) {
            Admin admin = new Admin();
            admin.setName("Default Admin");
            admin.setEmail("admin@ums.local");
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("admin123"));
            admins.save(admin);
        }
    }
}
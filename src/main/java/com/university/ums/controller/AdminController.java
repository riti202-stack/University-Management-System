package com.university.ums.controller;

import com.university.ums.model.Admin;
import com.university.ums.service.AdminService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admins")
public class AdminController extends PersonController<Admin> {
    public AdminController(AdminService service) {
        super(service);
    }
}
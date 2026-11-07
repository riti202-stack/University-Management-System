package com.university.ums.controller;

import com.university.ums.model.Teacher;
import com.university.ums.service.TeacherService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController extends PersonController<Teacher> {
    public TeacherController(TeacherService service) {
        super(service);
    }
}
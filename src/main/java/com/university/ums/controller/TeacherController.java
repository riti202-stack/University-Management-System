package com.university.ums.controller;

import com.university.ums.model.Teacher;
import com.university.ums.service.TeacherService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController extends PersonController<Teacher> {

    private final TeacherService teacherService;

    public TeacherController(TeacherService service) {
        super(service);
        this.teacherService = service;
    }

    @PutMapping("/{id}/approve")
    public Teacher approve(@PathVariable Long id) {
        return teacherService.approve(id);
    }
}
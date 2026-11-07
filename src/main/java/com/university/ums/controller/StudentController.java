package com.university.ums.controller;

import com.university.ums.model.Student;
import com.university.ums.service.StudentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentController extends PersonController<Student> {
    public StudentController(StudentService service) {
        super(service);
    }
}
package com.university.ums.controller;

import com.university.ums.model.Student;
import com.university.ums.model.Teacher;
import com.university.ums.service.StudentService;
import com.university.ums.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/register")
public class RegistrationController {

    private final StudentService students;
    private final TeacherService teachers;

    public RegistrationController(StudentService students, TeacherService teachers) {
        this.students = students;
        this.teachers = teachers;
    }

    @PostMapping("/student")
    @ResponseStatus(HttpStatus.CREATED)
    public Student registerStudent(@Valid @RequestBody Student body) {
        body.setGpa(null);   // only teachers/admins can set GPA
        return students.create(body);
    }

    @PostMapping("/teacher")
    @ResponseStatus(HttpStatus.CREATED)
    public Teacher registerTeacher(@Valid @RequestBody Teacher body) {
        return teachers.register(body);   // starts as pending
    }
}
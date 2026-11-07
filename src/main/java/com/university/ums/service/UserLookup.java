package com.university.ums.service;

import com.university.ums.model.Person;
import com.university.ums.repository.AdminRepository;
import com.university.ums.repository.StudentRepository;
import com.university.ums.repository.TeacherRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UserLookup {

    private final AdminRepository admins;
    private final TeacherRepository teachers;
    private final StudentRepository students;

    public UserLookup(AdminRepository admins, TeacherRepository teachers, StudentRepository students) {
        this.admins = admins;
        this.teachers = teachers;
        this.students = students;
    }

    public Optional<Person> find(String username) {
        Optional<Person> result = admins.findByUsername(username).map(p -> (Person) p);
        if (result.isEmpty()) result = teachers.findByUsername(username).map(p -> (Person) p);
        if (result.isEmpty()) result = students.findByUsername(username).map(p -> (Person) p);
        return result;
    }

    public boolean exists(String username) {
        return find(username).isPresent();
    }
}
package com.university.ums.service;

import com.university.ums.model.Student;
import com.university.ums.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StudentService extends PersonService<Student> {

    public StudentService(StudentRepository repo, PasswordEncoder encoder, UserLookup lookup) {
        super(repo, encoder, lookup, "Student");
    }

    @Override
    protected void copyExtraFields(Student target, Student source) {
        target.setDepartment(source.getDepartment());
    }
}
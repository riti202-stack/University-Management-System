package com.university.ums.service;

import com.university.ums.model.Teacher;
import com.university.ums.repository.TeacherRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class TeacherService extends PersonService<Teacher> {

    public TeacherService(TeacherRepository repo, PasswordEncoder encoder, UserLookup lookup) {
        super(repo, encoder, lookup, "Teacher");
    }

    @Override
    protected void copyExtraFields(Teacher target, Teacher source) {
        target.setDepartment(source.getDepartment());
    }
}
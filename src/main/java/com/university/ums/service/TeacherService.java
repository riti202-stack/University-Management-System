package com.university.ums.service;

import com.university.ums.model.Teacher;
import com.university.ums.repository.TeacherRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class TeacherService extends PersonService<Teacher> {

    private final TeacherRepository teachers;

    public TeacherService(TeacherRepository repo, PasswordEncoder encoder, UserLookup lookup) {
        super(repo, encoder, lookup, "Teacher");
        this.teachers = repo;
    }

    /** Created by an admin: approved straight away. */
    @Override
    public Teacher create(Teacher t) {
        t.setApproved(true);
        return super.create(t);
    }

    /** Self-registration: waits for admin approval. */
    public Teacher register(Teacher t) {
        t.setApproved(false);
        return super.create(t);
    }

    public Teacher approve(Long id) {
        Teacher t = findById(id);
        t.setApproved(true);
        return teachers.save(t);
    }

    /** A teacher editing their own profile: username cannot be changed. */
    public Teacher updateOwn(Long id, Teacher in) {
        Teacher existing = findById(id);
        in.setUsername(existing.getUsername());
        return update(id, in);
    }

    @Override
    protected void copyExtraFields(Teacher target, Teacher source) {
        target.setDepartment(source.getDepartment());
        // 'approved' is deliberately not copied: it changes only through approve()
    }
}

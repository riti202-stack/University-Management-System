package com.university.ums.service;

import com.university.ums.exception.ApiException;
import com.university.ums.model.Student;
import com.university.ums.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StudentService extends PersonService<Student> {

    private final StudentRepository students;

    public StudentService(StudentRepository repo, PasswordEncoder encoder, UserLookup lookup) {
        super(repo, encoder, lookup, "Student");
        this.students = repo;
    }

    @Override
    public Student create(Student s) {
        if (students.existsByRoll(s.getRoll())) {
            throw ApiException.conflict("Roll already exists");
        }
        return super.create(s);
    }

    @Override
    public Student update(Long id, Student in) {
        if (students.existsByRollAndIdNot(in.getRoll(), id)) {
            throw ApiException.conflict("Roll already exists");
        }
        return super.update(id, in);
    }

    /** A student editing their own profile: username and GPA cannot be changed. */
    public Student updateOwn(Long id, Student in) {
        Student existing = findById(id);
        in.setUsername(existing.getUsername());
        in.setGpa(existing.getGpa());
        return update(id, in);
    }

    @Override
    protected void copyExtraFields(Student target, Student source) {
        target.setDepartment(source.getDepartment());
        target.setRoll(source.getRoll());
        target.setBatch(source.getBatch());
        target.setClassName(source.getClassName());
        target.setAge(source.getAge());
        target.setGpa(source.getGpa());
    }
}
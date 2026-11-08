package com.university.ums.repository;

import com.university.ums.model.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUsername(String username);
    boolean existsByRoll(String roll);
    boolean existsByRollAndIdNot(String roll, Long id);
}
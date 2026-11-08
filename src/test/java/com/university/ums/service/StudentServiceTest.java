package com.university.ums.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.university.ums.exception.ApiException;
import com.university.ums.model.Student;
import com.university.ums.repository.StudentRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock StudentRepository repo;
    @Mock PasswordEncoder encoder;
    @Mock UserLookup lookup;

    StudentService service;

    @BeforeEach
    void setUp() {
        service = new StudentService(repo, encoder, lookup);
    }

    private Student student(String username, String roll) {
        Student s = new Student();
        s.setName("Abida");
        s.setEmail("abida@ums.edu");
        s.setUsername(username);
        s.setPassword("stud123");
        s.setRoll(roll);
        s.setBatch("2022");
        s.setClassName("CSE-A");
        s.setAge(21);
        s.setGpa(3.5);
        return s;
    }

    // 1
    @Test
    void create_hashesPasswordAndSaves() {
        Student s = student("abida", "R1");
        when(repo.existsByRoll("R1")).thenReturn(false);
        when(lookup.exists("abida")).thenReturn(false);
        when(encoder.encode("stud123")).thenReturn("HASHED");
        when(repo.save(any(Student.class))).thenAnswer(i -> i.getArgument(0));

        Student saved = service.create(s);

        assertEquals("HASHED", saved.getPassword());
        verify(repo).save(s);
    }

    // 2
    @Test
    void create_duplicateRoll_throwsConflict() {
        when(repo.existsByRoll("R1")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> service.create(student("abida", "R1")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(repo, never()).save(any(Student.class));
    }

    // 3
    @Test
    void updateOwn_cannotChangeUsernameOrGpa() {
        Student existing = student("abida", "R1");
        existing.setId(1L);
        existing.setGpa(3.5);

        Student incoming = student("hacker", "R1");
        incoming.setName("New Name");
        incoming.setGpa(4.0);
        incoming.setPassword(null);

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.existsByRollAndIdNot("R1", 1L)).thenReturn(false);
        when(repo.save(any(Student.class))).thenAnswer(i -> i.getArgument(0));

        Student result = service.updateOwn(1L, incoming);

        assertEquals("abida", result.getUsername());
        assertEquals(Double.valueOf(3.5), result.getGpa());
        assertEquals("New Name", result.getName());
    }
}
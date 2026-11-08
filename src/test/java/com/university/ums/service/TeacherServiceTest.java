package com.university.ums.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.university.ums.model.Teacher;
import com.university.ums.repository.TeacherRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock TeacherRepository repo;
    @Mock PasswordEncoder encoder;
    @Mock UserLookup lookup;

    TeacherService service;

    @BeforeEach
    void setUp() {
        service = new TeacherService(repo, encoder, lookup);
    }

    private Teacher teacher(String username) {
        Teacher t = new Teacher();
        t.setName("Dr Rahman");
        t.setEmail("rahman@ums.edu");
        t.setUsername(username);
        t.setPassword("teach123");
        t.setDepartment("CSE");
        return t;
    }

    // 4
    @Test
    void register_startsAsPending() {
        when(lookup.exists("rahman")).thenReturn(false);
        when(encoder.encode("teach123")).thenReturn("HASHED");
        when(repo.save(any(Teacher.class))).thenAnswer(i -> i.getArgument(0));

        Teacher saved = service.register(teacher("rahman"));

        assertFalse(saved.isApproved());
        assertEquals("HASHED", saved.getPassword());
    }

    // 5
    @Test
    void create_byAdmin_isApprovedImmediately() {
        when(lookup.exists("rahman")).thenReturn(false);
        when(encoder.encode("teach123")).thenReturn("HASHED");
        when(repo.save(any(Teacher.class))).thenAnswer(i -> i.getArgument(0));

        Teacher saved = service.create(teacher("rahman"));

        assertTrue(saved.isApproved());
    }

    // 6
    @Test
    void approve_marksTeacherApproved() {
        Teacher pending = teacher("rahman");
        pending.setId(5L);
        pending.setApproved(false);
        when(repo.findById(5L)).thenReturn(Optional.of(pending));
        when(repo.save(any(Teacher.class))).thenAnswer(i -> i.getArgument(0));

        Teacher result = service.approve(5L);

        assertTrue(result.isApproved());
        verify(repo).save(pending);
    }
}
package com.university.ums.controller;

import com.university.ums.exception.ApiException;
import com.university.ums.model.Student;
import com.university.ums.repository.StudentRepository;
import com.university.ums.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students/me")
public class StudentSelfController {

    private final StudentRepository repo;
    private final StudentService service;

    public StudentSelfController(StudentRepository repo, StudentService service) {
        this.repo = repo;
        this.service = service;
    }

    private Student current(Authentication auth) {
        return repo.findByUsername(auth.getName())
                .orElseThrow(() -> ApiException.notFound("Account not found"));
    }

    @GetMapping
    public Student get(Authentication auth) {
        return current(auth);
    }

    @PutMapping
    public Student update(Authentication auth, @Valid @RequestBody Student body) {
        return service.updateOwn(current(auth).getId(), body);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(Authentication auth, HttpServletRequest req) {
        service.delete(current(auth).getId());
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
}
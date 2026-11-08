package com.university.ums.controller;

import com.university.ums.exception.ApiException;
import com.university.ums.model.Teacher;
import com.university.ums.repository.TeacherRepository;
import com.university.ums.service.TeacherService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers/me")
public class TeacherSelfController {

    private final TeacherRepository repo;
    private final TeacherService service;

    public TeacherSelfController(TeacherRepository repo, TeacherService service) {
        this.repo = repo;
        this.service = service;
    }

    private Teacher current(Authentication auth) {
        return repo.findByUsername(auth.getName())
                .orElseThrow(() -> ApiException.notFound("Account not found"));
    }

    @GetMapping
    public Teacher get(Authentication auth) {
        return current(auth);
    }

    @PutMapping
    public Teacher update(Authentication auth, @Valid @RequestBody Teacher body) {
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
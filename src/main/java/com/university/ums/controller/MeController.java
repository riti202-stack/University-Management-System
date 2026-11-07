package com.university.ums.controller;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    @GetMapping("/api/me")
    public Map<String, Object> me(Authentication auth) {
        return Map.of(
                "username", auth.getName(),
                "roles", auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }
}
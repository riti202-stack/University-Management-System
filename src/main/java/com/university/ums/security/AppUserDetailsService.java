package com.university.ums.security;

import com.university.ums.model.Person;
import com.university.ums.model.Teacher;
import com.university.ums.service.UserLookup;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserLookup lookup;

    public AppUserDetailsService(UserLookup lookup) {
        this.lookup = lookup;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Person p = lookup.find(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean pending = p instanceof Teacher t && !t.isApproved();

        return User.withUsername(p.getUsername())
                .password(p.getPassword())
                .roles(p.getRole())
                .disabled(pending)      // pending teachers cannot log in
                .build();
    }
}
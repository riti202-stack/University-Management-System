package com.university.ums.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // public: login page, sign-up page, static files, sign-up API
                        .requestMatchers("/", "/index.html", "/register.html", "/favicon.ico", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/api/auth/register/**").permitAll()

                        // portal pages
                        .requestMatchers("/admin.html").hasRole("ADMIN")
                        .requestMatchers("/teacher.html").hasRole("TEACHER")
                        .requestMatchers("/student.html").hasRole("STUDENT")

                        // own profile (must come before the broader rules below)
                        .requestMatchers("/api/students/me").hasRole("STUDENT")
                        .requestMatchers("/api/teachers/me").hasRole("TEACHER")

                        // managing other people
                        .requestMatchers("/api/admins/**").hasRole("ADMIN")
                        .requestMatchers("/api/teachers/**").hasRole("ADMIN")
                        .requestMatchers("/api/students/**").hasAnyRole("ADMIN", "TEACHER")

                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler((req, res, auth) -> {
                            res.setStatus(HttpStatus.OK.value());
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"Login successful\"}");
                        })
                        .failureHandler((req, res, ex) -> {
                            boolean pending = ex instanceof org.springframework.security.authentication.DisabledException;
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                            res.setContentType("application/json");
                            res.getWriter().write(pending
                                    ? "{\"error\":\"Your account is waiting for admin approval\"}"
                                    : "{\"error\":\"Invalid username or password\"}");
                        })
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((req, res, auth) -> {
                            res.setStatus(HttpStatus.OK.value());
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"Logged out\"}");
                        })
                        .permitAll())
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            if (req.getRequestURI().startsWith("/api")) {
                                res.setStatus(HttpStatus.UNAUTHORIZED.value());
                                res.setContentType("application/json");
                                res.getWriter().write("{\"error\":\"Not logged in\"}");
                            } else {
                                res.sendRedirect("/index.html");
                            }
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            if (req.getRequestURI().startsWith("/api")) {
                                res.setStatus(HttpStatus.FORBIDDEN.value());
                                res.setContentType("application/json");
                                res.getWriter().write("{\"error\":\"Access denied for your role\"}");
                            } else {
                                res.sendRedirect("/index.html?denied=1");
                            }
                        }));
        return http.build();
    }
}
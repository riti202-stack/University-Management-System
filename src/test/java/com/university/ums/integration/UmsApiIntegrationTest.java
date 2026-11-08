package com.university.ums.integration;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.university.ums.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class UmsApiIntegrationTest {

    @Autowired WebApplicationContext context;
    @Autowired TeacherRepository teacherRepository;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private String studentJson(String username, String roll) {
        return """
            {"name":"Test Student","email":"%s@ums.edu","username":"%s","password":"stud123",
             "roll":"%s","batch":"2022","className":"CSE-A","age":21,"department":"CSE"}
            """.formatted(username, username, roll);
    }

    private String teacherJson(String username) {
        return """
            {"name":"Test Teacher","email":"%s@ums.edu","username":"%s","password":"teach123",
             "department":"CSE"}
            """.formatted(username, username);
    }

    // 1: a student registers, sees only their own profile
    @Test
    void studentRegistersAndSeesOnlyOwnProfile() throws Exception {
        mvc.perform(post("/api/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("it_student1", "IT-001")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist());

        mvc.perform(get("/api/students/me").with(httpBasic("it_student1", "stud123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roll").value("IT-001"));

        mvc.perform(get("/api/students").with(httpBasic("it_student1", "stud123")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/students")).andExpect(status().isUnauthorized());
    }

    // 2: a self-registered teacher must be approved by an admin before login
    @Test
    void teacherNeedsAdminApprovalBeforeLogin() throws Exception {
        mvc.perform(post("/api/auth/register/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(teacherJson("it_teacher1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.approved").value(false));

        mvc.perform(get("/api/teachers/me").with(httpBasic("it_teacher1", "teach123")))
                .andExpect(status().isUnauthorized());

        Long id = teacherRepository.findByUsername("it_teacher1").orElseThrow().getId();
        mvc.perform(put("/api/teachers/" + id + "/approve").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());

        mvc.perform(get("/api/teachers/me").with(httpBasic("it_teacher1", "teach123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("it_teacher1"));
    }

    // 3: admin creates a teacher; the teacher manages students but nothing else
    @Test
    void teacherManagesStudentsButNotAdminsOrOtherTeachers() throws Exception {
        mvc.perform(post("/api/teachers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(teacherJson("it_teacher2")))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/students")
                        .with(httpBasic("it_teacher2", "teach123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("it_student2", "IT-002")))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/students").with(httpBasic("it_teacher2", "teach123")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("it_student2")));

        mvc.perform(get("/api/admins").with(httpBasic("it_teacher2", "teach123")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/teachers").with(httpBasic("it_teacher2", "teach123")))
                .andExpect(status().isForbidden());
    }
}
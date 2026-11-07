package com.university.ums.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "teachers")
public class Teacher extends Person {

    private String department;

    @Override
    public String getRole() { return "TEACHER"; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
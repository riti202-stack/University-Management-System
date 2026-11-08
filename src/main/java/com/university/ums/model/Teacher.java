package com.university.ums.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "teachers")
public class Teacher extends Person {

    private String department;

    // null (teachers created before this feature) counts as approved
    private Boolean approved;

    @Override
    public String getRole() { return "TEACHER"; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public boolean isApproved() { return approved == null || approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
}
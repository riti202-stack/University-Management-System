package com.university.ums.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "students")
public class Student extends Person {

    private String department;

    @NotBlank
    @Column(unique = true)
    private String roll;

    @NotBlank
    private String batch;

    private String className;

    @Min(1)
    @Max(120)
    private Integer age;

    @DecimalMin("0.0")
    @DecimalMax("4.0")
    private Double gpa;

    @Override
    public String getRole() { return "STUDENT"; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getRoll() { return roll; }
    public void setRoll(String roll) { this.roll = roll; }
    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public Double getGpa() { return gpa; }
    public void setGpa(Double gpa) { this.gpa = gpa; }
}
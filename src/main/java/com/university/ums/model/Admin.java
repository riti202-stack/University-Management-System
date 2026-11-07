package com.university.ums.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
public class Admin extends Person {
    @Override
    public String getRole() { return "ADMIN"; }
}
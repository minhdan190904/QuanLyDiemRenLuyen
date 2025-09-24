package com.trilogy.quanlydiemrenluyen.model;

public class User {
    public String username;
    public String role;       // "admin" | "student"
    public String student_id; // null nếu admin
}

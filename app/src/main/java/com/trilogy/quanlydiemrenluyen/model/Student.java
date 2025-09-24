package com.trilogy.quanlydiemrenluyen.model;

public class Student {
    public String student_id;
    public String name;
    public String clazz;

    public Student() {}
    public Student(String student_id, String name, String clazz) {
        this.student_id = student_id; this.name = name; this.clazz = clazz;
    }

    @Override public String toString() {
        return student_id + " - " + name + " (" + clazz + ")";
    }
}

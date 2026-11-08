package com.academy.courseregistration.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "course_offerings")
public class CourseOffering {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String title;
    @Column(nullable = false)
    private int capacity;
    @Version
    private long version;

    protected CourseOffering() {}
    public CourseOffering(String title, int capacity) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Course title is required");
        if (capacity < 1) throw new IllegalArgumentException("Capacity must be at least 1");
        this.title = title; this.capacity = capacity;
    }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public int getCapacity() { return capacity; }
    public long getVersion() { return version; }
}

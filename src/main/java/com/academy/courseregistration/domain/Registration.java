package com.academy.courseregistration.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "registrations", uniqueConstraints = @UniqueConstraint(name = "uk_course_learner", columnNames = {"course_id", "learner_id"}), indexes = @Index(name = "idx_course_status_created", columnList = "course_id,status,created_at"))
public class Registration {
    public enum Status { ENROLLED, WAITLISTED, DROPPED, SKIPPED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "course_id", nullable = false)
    private CourseOffering course;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "learner_id", nullable = false)
    private Learner learner;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Status status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Registration() {}
    public Registration(CourseOffering course, Learner learner, Status status) {
        this.course = course; this.learner = learner; this.status = status;
        this.createdAt = Instant.now(); this.updatedAt = this.createdAt;
    }
    public Long getId() { return id; }
    public CourseOffering getCourse() { return course; }
    public Learner getLearner() { return learner; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setStatus(Status status) { this.status = status; this.updatedAt = Instant.now(); }
}

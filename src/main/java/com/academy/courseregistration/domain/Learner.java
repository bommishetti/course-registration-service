package com.academy.courseregistration.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "learners")
public class Learner {
    @Id @Column(length = 80)
    private String id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false)
    private boolean prerequisiteCompleted;

    protected Learner() {}
    public Learner(String id, String name, boolean prerequisiteCompleted) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Learner id is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Learner name is required");
        this.id = id; this.name = name; this.prerequisiteCompleted = prerequisiteCompleted;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public boolean isPrerequisiteCompleted() { return prerequisiteCompleted; }
    public void setPrerequisiteCompleted(boolean value) { this.prerequisiteCompleted = value; }
}

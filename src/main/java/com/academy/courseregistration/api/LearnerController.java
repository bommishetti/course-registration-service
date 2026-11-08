package com.academy.courseregistration.api;

import com.academy.courseregistration.domain.Learner;
import com.academy.courseregistration.repository.LearnerRepository;
import com.academy.courseregistration.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/learners")
public class LearnerController {
    private final LearnerRepository learners;
    public LearnerController(LearnerRepository learners) { this.learners = learners; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Learner create(@Valid @RequestBody CreateLearnerRequest request) {
        if (learners.existsById(request.id())) throw new IllegalArgumentException("Learner id already exists");
        return learners.save(new Learner(request.id(), request.name(), request.prerequisiteCompleted()));
    }
    @PatchMapping("/{learnerId}/prerequisite")
    public Learner updatePrerequisite(@PathVariable String learnerId, @RequestBody UpdateLearnerPrerequisiteRequest request) {
        Learner learner = learners.findById(learnerId).orElseThrow(() -> new IllegalArgumentException("Learner not found"));
        learner.setPrerequisiteCompleted(request.prerequisiteCompleted());
        return learners.save(learner);
    }
}

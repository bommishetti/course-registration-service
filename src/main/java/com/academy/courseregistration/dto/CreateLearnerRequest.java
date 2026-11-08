package com.academy.courseregistration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLearnerRequest(
        @NotBlank @Size(max = 80) String id,
        @NotBlank @Size(max = 120) String name,
        boolean prerequisiteCompleted) {
}

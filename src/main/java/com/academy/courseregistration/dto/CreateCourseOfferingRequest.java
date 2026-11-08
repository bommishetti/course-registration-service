package com.academy.courseregistration.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCourseOfferingRequest(
        @NotBlank @Size(max = 120) String title,
        @Min(1) int capacity) {
}

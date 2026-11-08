package com.academy.courseregistration.dto;

import java.time.Instant;

public record RegistrationResponse(Long courseId, String learnerId, String learnerName,
                                   String status, String message, Instant createdAt) {
}

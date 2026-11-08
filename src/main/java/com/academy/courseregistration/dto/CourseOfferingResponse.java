package com.academy.courseregistration.dto;

public record CourseOfferingResponse(Long id, String title, int capacity,
                                     long enrolledCount, long waitlistCount) {
}

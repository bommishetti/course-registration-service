package com.academy.courseregistration.api;

import com.academy.courseregistration.domain.CourseOffering;
import com.academy.courseregistration.repository.CourseOfferingRepository;
import com.academy.courseregistration.dto.*;
import com.academy.courseregistration.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseOfferingController {
    private final CourseOfferingRepository courses;
    private final RegistrationService registrations;
    public CourseOfferingController(CourseOfferingRepository courses, RegistrationService registrations) { this.courses = courses; this.registrations = registrations; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CourseOfferingResponse create(@Valid @RequestBody CreateCourseOfferingRequest request) {
        CourseOffering c = courses.save(new CourseOffering(request.title(), request.capacity()));
        return registrations.getCourse(c.getId());
    }
    @GetMapping("/{courseId}") public CourseOfferingResponse get(@PathVariable Long courseId) { return registrations.getCourse(courseId); }
    @GetMapping("/{courseId}/registrations") public List<RegistrationResponse> registrations(@PathVariable Long courseId) { return registrations.getRegistrations(courseId); }
}

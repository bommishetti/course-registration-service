package com.academy.courseregistration.api;

import com.academy.courseregistration.dto.RegistrationResponse;
import com.academy.courseregistration.service.RegistrationService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController @Validated
@RequestMapping("/api/courses/{courseId}/registrations")
public class RegistrationController {
    private final RegistrationService service;
    public RegistrationController(RegistrationService service) { this.service = service; }
    @PostMapping public RegistrationResponse enroll(@PathVariable Long courseId, @RequestParam @NotBlank String learnerId) { return service.enroll(courseId, learnerId); }
    @DeleteMapping("/{learnerId}") public RegistrationResponse drop(@PathVariable Long courseId, @PathVariable String learnerId) { return service.drop(courseId, learnerId); }
}

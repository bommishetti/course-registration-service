package com.academy.courseregistration.service;

import com.academy.courseregistration.domain.*;
import com.academy.courseregistration.repository.*;
import com.academy.courseregistration.dto.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class RegistrationService {
    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);
    private final CourseOfferingRepository courses;
    private final LearnerRepository learners;
    private final RegistrationRepository registrations;

    public RegistrationService(CourseOfferingRepository courses, LearnerRepository learners, RegistrationRepository registrations) {
        this.courses = courses; this.learners = learners; this.registrations = registrations;
    }

    @Transactional
    public RegistrationResponse enroll(Long courseId, String learnerId) {
        CourseOffering course = lockCourse(courseId);
        Learner learner = findLearner(learnerId);
        Optional<Registration> existing = registrations.findByCourse_IdAndLearner_Id(courseId, learnerId);
        if (existing.isPresent()) {
            Registration r = existing.get();
            String msg = r.getStatus() == Registration.Status.WAITLISTED ? "Learner is already waitlisted" : "Learner already has a registration record; duplicate enrollment is not allowed";
            return response(r, msg);
        }
        if (!learner.isPrerequisiteCompleted()) throw new BusinessRuleException("PREREQUISITE_NOT_MET", "Learner has not completed the required prerequisite");
        long enrolled = registrations.countByCourse_IdAndStatus(courseId, Registration.Status.ENROLLED);
        Registration.Status status = enrolled < course.getCapacity() ? Registration.Status.ENROLLED : Registration.Status.WAITLISTED;
        Registration saved = registrations.save(new Registration(course, learner, status));
        log.info("Registration created: courseId={}, status={}", courseId, status);
        return response(saved, status == Registration.Status.ENROLLED ? "Enrollment successful" : "Course is full; learner added to waitlist");
    }

    @Transactional
    public RegistrationResponse drop(Long courseId, String learnerId) {
        lockCourse(courseId);
        Registration registration = registrations.findByCourse_IdAndLearner_Id(courseId, learnerId)
                .orElseThrow(() -> new BusinessRuleException("REGISTRATION_NOT_FOUND", "No registration exists for this learner and course"));
        if (registration.getStatus() != Registration.Status.ENROLLED)
            throw new BusinessRuleException("NOT_CURRENTLY_ENROLLED", "Learner is not currently enrolled; no seat was released");
        registration.setStatus(Registration.Status.DROPPED);
        log.info("Registration dropped: courseId={}", courseId);
        promoteNextEligible(courseId);
        return response(registration, "Enrollment dropped; waitlist promotion processed");
    }

    private void promoteNextEligible(Long courseId) {
        List<Registration> queue = registrations.findByCourse_IdAndStatusOrderByCreatedAtAscIdAsc(courseId, Registration.Status.WAITLISTED);
        for (Registration candidate : queue) {
            if (candidate.getLearner().isPrerequisiteCompleted()) {
                candidate.setStatus(Registration.Status.ENROLLED);
                return;
            }
            // Policy: skip an entry that has become ineligible; keep its audit record.
            candidate.setStatus(Registration.Status.SKIPPED);
            log.warn("Ineligible waitlist entry skipped: courseId={}", courseId);
        }
    }

    @Transactional(readOnly = true)
    public CourseOfferingResponse getCourse(Long courseId) {
        CourseOffering course = courses.findById(courseId).orElseThrow(() -> new BusinessRuleException("COURSE_NOT_FOUND", "Course offering not found"));
        return new CourseOfferingResponse(course.getId(), course.getTitle(), course.getCapacity(),
                registrations.countByCourse_IdAndStatus(courseId, Registration.Status.ENROLLED),
                registrations.countByCourse_IdAndStatus(courseId, Registration.Status.WAITLISTED));
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getRegistrations(Long courseId) {
        if (!courses.existsById(courseId)) throw new BusinessRuleException("COURSE_NOT_FOUND", "Course offering not found");
        return registrations.findByCourse_IdOrderByCreatedAtAscIdAsc(courseId).stream().map(r -> response(r, "")).toList();
    }

    private CourseOffering lockCourse(Long courseId) {
        return courses.findByIdForUpdate(courseId).orElseThrow(() -> new BusinessRuleException("COURSE_NOT_FOUND", "Course offering not found"));
    }
    private Learner findLearner(String learnerId) {
        return learners.findById(learnerId).orElseThrow(() -> new BusinessRuleException("LEARNER_NOT_FOUND", "Learner not found"));
    }
    private RegistrationResponse response(Registration r, String message) {
        return new RegistrationResponse(r.getCourse().getId(), r.getLearner().getId(), r.getLearner().getName(), r.getStatus().name(), message, r.getCreatedAt());
    }
}

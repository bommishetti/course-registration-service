package com.academy.courseregistration.service;

import com.academy.courseregistration.domain.*;
import com.academy.courseregistration.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationServiceUnitTest {
    @Test void prerequisiteFailureDoesNotPersistRegistration() {
        CourseOfferingRepository courses = mock(CourseOfferingRepository.class);
        LearnerRepository learners = mock(LearnerRepository.class);
        RegistrationRepository registrations = mock(RegistrationRepository.class);
        when(courses.findByIdForUpdate(1L)).thenReturn(Optional.of(new CourseOffering("Java", 1)));
        when(learners.findById("L1")).thenReturn(Optional.of(new Learner("L1", "Learner", false)));
        when(registrations.findByCourse_IdAndLearner_Id(1L, "L1")).thenReturn(Optional.empty());
        RegistrationService service = new RegistrationService(courses, learners, registrations);
        assertThatThrownBy(() -> service.enroll(1L, "L1")).isInstanceOf(BusinessRuleException.class);
        verify(registrations, never()).save(any());
    }
}

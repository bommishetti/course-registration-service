package com.academy.courseregistration.service;

import com.academy.courseregistration.domain.*;
import com.academy.courseregistration.dto.RegistrationResponse;
import com.academy.courseregistration.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

@SpringBootTest
class RegistrationServiceIntegrationTest {
    @Autowired RegistrationService service;
    @Autowired CourseOfferingRepository courses;
    @Autowired LearnerRepository learners;
    @Autowired RegistrationRepository registrations;

    private Long courseId;
    @BeforeEach void setUp() {
        registrations.deleteAll(); learners.deleteAll(); courses.deleteAll();
        courseId = courses.save(new CourseOffering("Java Fundamentals", 2)).getId();
        learners.save(new Learner("A", "Learner A", true));
        learners.save(new Learner("B", "Learner B", true));
        learners.save(new Learner("C", "Learner C", true));
        learners.save(new Learner("D", "Learner D", true));
        learners.save(new Learner("X", "Learner X", false));
    }

    @Test void enrollsWhileCapacityRemainsThenQueuesWhenFull() {
        assertThat(service.enroll(courseId, "A").status()).isEqualTo("ENROLLED");
        assertThat(service.enroll(courseId, "B").status()).isEqualTo("ENROLLED");
        assertThat(service.enroll(courseId, "C").status()).isEqualTo("WAITLISTED");
        assertThat(service.getCourse(courseId).enrolledCount()).isEqualTo(2);
        assertThat(service.getCourse(courseId).waitlistCount()).isEqualTo(1);
    }

    @Test void duplicateRequestDoesNotCreateAnotherRecord() {
        service.enroll(courseId, "A");
        RegistrationResponse duplicate = service.enroll(courseId, "A");
        assertThat(duplicate.status()).isEqualTo("ENROLLED");
        assertThat(registrations.count()).isEqualTo(1);
    }

    @Test void duplicateWaitlistRequestDoesNotCreateAnotherRecord() {
        service.enroll(courseId, "A"); service.enroll(courseId, "B");
        assertThat(service.enroll(courseId, "C").status()).isEqualTo("WAITLISTED");
        assertThat(service.enroll(courseId, "C").status()).isEqualTo("WAITLISTED");
        assertThat(registrations.count()).isEqualTo(3);
    }

    @Test void rejectsLearnerWhoHasNotCompletedPrerequisite() {
        assertThatThrownBy(() -> service.enroll(courseId, "X"))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("prerequisite");
        assertThat(registrations.count()).isZero();
    }

    @Test void droppingEnrollmentPromotesEarliestEligibleWaitlistedLearner() {
        service.enroll(courseId, "A"); service.enroll(courseId, "B");
        service.enroll(courseId, "C"); service.enroll(courseId, "D");
        service.drop(courseId, "A");
        assertThat(registrations.findByCourse_IdAndLearner_Id(courseId, "C").orElseThrow().getStatus()).isEqualTo(Registration.Status.ENROLLED);
        assertThat(registrations.findByCourse_IdAndLearner_Id(courseId, "D").orElseThrow().getStatus()).isEqualTo(Registration.Status.WAITLISTED);
    }

    @Test void skipsLearnerWhosePrerequisiteBecomesIncompleteAndPromotesNextEligible() {
        service.enroll(courseId, "A"); service.enroll(courseId, "B");
        service.enroll(courseId, "C"); service.enroll(courseId, "D");
        Learner c = learners.findById("C").orElseThrow();
        c.setPrerequisiteCompleted(false);
        learners.save(c);
        service.drop(courseId, "A");
        assertThat(registrations.findByCourse_IdAndLearner_Id(courseId, "C").orElseThrow().getStatus()).isEqualTo(Registration.Status.SKIPPED);
        assertThat(registrations.findByCourse_IdAndLearner_Id(courseId, "D").orElseThrow().getStatus()).isEqualTo(Registration.Status.ENROLLED);
    }

    @Test void repeatedDropDoesNotReleaseAnotherSeat() {
        service.enroll(courseId, "A");
        service.drop(courseId, "A");
        assertThatThrownBy(() -> service.drop(courseId, "A"))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("not currently enrolled");
        assertThat(service.getCourse(courseId).enrolledCount()).isZero();
    }

    @Test void concurrentEnrollmentsNeverExceedCourseCapacity() throws Exception {
        IntStream.range(0, 20).forEach(i -> learners.save(new Learner("P" + i, "Parallel " + i, true)));
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            var tasks = IntStream.range(0, 20).<Callable<RegistrationResponse>>mapToObj(i -> () -> service.enroll(courseId, "P" + i)).toList();
            for (Future<RegistrationResponse> future : pool.invokeAll(tasks)) future.get();
        } finally {
            pool.shutdownNow();
        }
        assertThat(registrations.countByCourse_IdAndStatus(courseId, Registration.Status.ENROLLED)).isEqualTo(2);
        assertThat(registrations.countByCourse_IdAndStatus(courseId, Registration.Status.WAITLISTED)).isEqualTo(18);
    }

    @Test void invalidLearnerAndCourseAreRejected() {
        assertThatThrownBy(() -> service.enroll(courseId, "missing")).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> service.enroll(99999L, "A")).isInstanceOf(BusinessRuleException.class);
    }
}

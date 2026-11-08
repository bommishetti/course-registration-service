package com.academy.courseregistration.repository;

import com.academy.courseregistration.domain.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Optional<Registration> findByCourse_IdAndLearner_Id(Long courseId, String learnerId);
    long countByCourse_IdAndStatus(Long courseId, Registration.Status status);
    List<Registration> findByCourse_IdAndStatusOrderByCreatedAtAscIdAsc(Long courseId, Registration.Status status);
    List<Registration> findByCourse_IdOrderByCreatedAtAscIdAsc(Long courseId);
}

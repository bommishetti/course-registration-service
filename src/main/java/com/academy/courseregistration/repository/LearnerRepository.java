package com.academy.courseregistration.repository;
import com.academy.courseregistration.domain.Learner;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LearnerRepository extends JpaRepository<Learner, String> {}

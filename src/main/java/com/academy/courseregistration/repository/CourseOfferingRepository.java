package com.academy.courseregistration.repository;

import com.academy.courseregistration.domain.CourseOffering;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CourseOfferingRepository extends JpaRepository<CourseOffering, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CourseOffering c where c.id = :id")
    Optional<CourseOffering> findByIdForUpdate(@Param("id") Long id);
}

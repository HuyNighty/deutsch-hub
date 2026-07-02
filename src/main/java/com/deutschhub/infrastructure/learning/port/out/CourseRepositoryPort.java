package com.deutschhub.infrastructure.learning.port.out;

import com.deutschhub.common.util.PageResponse;
import com.deutschhub.domain.learning.model.aggregate.Course;

import java.util.Optional;
import java.util.UUID;

public interface CourseRepositoryPort {

    Course save(Course course);

    Optional<Course> findById(UUID courseId);

    PageResponse<Course> findAll(String keyword, int page, int size);
}

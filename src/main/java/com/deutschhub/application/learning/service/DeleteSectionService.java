package com.deutschhub.application.learning.service;

import com.deutschhub.application.learning.port.in.DeleteSectionUseCase;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeleteSectionService implements DeleteSectionUseCase {

    CourseRepositoryPort courseRepositoryPort;

    @Override
    public void deleteSection(UUID courseId, UUID sectionId, UUID actorId, boolean admin) {
        Course course = courseRepositoryPort.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        course.deleteSection(sectionId, actorId, admin);

        courseRepositoryPort.save(course);
    }
}
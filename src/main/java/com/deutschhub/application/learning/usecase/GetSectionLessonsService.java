package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.dto.response.LessonResponse;
import com.deutschhub.application.learning.port.in.GetSectionLessonsUseCase;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.domain.learning.model.entity.Lesson;
import com.deutschhub.domain.learning.model.entity.Section;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetSectionLessonsService implements GetSectionLessonsUseCase {

    CourseRepositoryPort courseRepositoryPort;

    @Override
    public List<LessonResponse> getLessons(UUID courseId, UUID sectionId) {
        Course course = courseRepositoryPort.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (course.isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        Section section = course.getSections()
                .stream()
                .filter(item -> item.getId().equals(sectionId))
                .filter(item -> !item.isDeleted())
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SECTION_NOT_FOUND));

        return section.getLessons()
                .stream()
                .filter(lesson -> !lesson.isDeleted())
                .sorted(Comparator.comparingInt(Lesson::getOrderIndex))
                .map(this::toLessonResponse)
                .toList();
    }

    private LessonResponse toLessonResponse(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getEstimatedMinutes(),
                lesson.getLevel().toString(),
                lesson.getOrderIndex(),
                lesson.isFreePreview(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }
}

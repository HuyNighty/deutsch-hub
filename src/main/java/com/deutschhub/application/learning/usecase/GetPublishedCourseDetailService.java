package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.dto.response.CourseDetailResponse;
import com.deutschhub.application.learning.dto.response.LessonResponse;
import com.deutschhub.application.learning.dto.response.SectionDetailResponse;
import com.deutschhub.application.learning.dto.response.SectionResponse;
import com.deutschhub.application.learning.port.in.GetPublishedCourseDetailUseCase;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetPublishedCourseDetailService implements GetPublishedCourseDetailUseCase {

    CourseRepositoryPort courseRepositoryPort;

    @Override
    public CourseDetailResponse getPublishedCourseDetail(UUID courseId) {
        Course course = courseRepositoryPort.findPublishedById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        return toResponse(course);
    }

    private CourseDetailResponse toResponse(Course course) {
        List<SectionDetailResponse> sections = course.getSections()
                .stream()
                .filter(section -> !section.isDeleted())
                .sorted(Comparator.comparingInt(Section::getOrderIndex))
                .map(this::toSectionDetailResponse)
                .toList();

        return new CourseDetailResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getLevel().toString(),
                course.getPrice().getAmount(),
                course.getPrice().getCurrency(),
                course.isPublished(),
                course.getInstructorId(),
                course.getEstimatedHours(),
                sections,
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }

    private SectionDetailResponse toSectionDetailResponse(Section section) {

        List<LessonResponse> lessons = section.getLessons()
                .stream()
                .filter(lesson -> !lesson.isDeleted())
                .sorted(Comparator.comparingInt(Lesson::getOrderIndex))
                .map(this::toLessonResponse)
                .toList();


        return new SectionDetailResponse(
                section.getId(),
                section.getTitle(),
                section.getDescription(),
                section.getOrderIndex(),
                lessons,
                section.getCreatedAt(),
                section.getUpdatedAt()
        );
    }

    private LessonResponse toLessonResponse(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getContent(),
                lesson.getEstimatedMinutes(),
                lesson.getLevel().toString(),
                lesson.getOrderIndex(),
                lesson.isFreePreview(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }
}
package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.dto.request.UpdateCourseCommand;
import com.deutschhub.application.learning.dto.response.CourseResponse;
import com.deutschhub.application.learning.port.in.UpdateCourseUseCase;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.domain.learning.model.valueobject.CEFRLevel;
import com.deutschhub.domain.learning.model.valueobject.Money;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UpdateCourseService implements UpdateCourseUseCase {

    CourseRepositoryPort courseRepositoryPort;

    @Override
    public CourseResponse updateCourse(UpdateCourseCommand command) {
        Course course = courseRepositoryPort.findById(command.courseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (course.isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        String title = command.title() != null
                ? command.title()
                : course.getTitle();

        String description = command.description() != null
                ? command.description()
                : course.getDescription();

        CEFRLevel level = command.level() != null
                ? new CEFRLevel(command.level())
                : course.getLevel();

        Money price = command.price() != null || command.currency() != null
                ? new Money(
                command.price() != null ? command.price() : course.getPrice().getAmount(),
                command.currency() != null ? command.currency() : course.getPrice().getCurrency()
        )
                : course.getPrice();
        course.updateMetadata(title, description, level, price, command.actorId(), command.admin());

        Course savedCourse =  courseRepositoryPort.save(course);

        return toResponse(savedCourse );
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getLevel().toString(),
                course.getPrice().getAmount(),
                course.getPrice().getCurrency(),
                course.isPublished(),
                course.getInstructorId(),
                course.getEstimatedHours(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}

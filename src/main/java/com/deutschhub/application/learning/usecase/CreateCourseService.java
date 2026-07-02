package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.dto.request.CreateCourseCommand;
import com.deutschhub.application.learning.dto.response.CourseResponse;
import com.deutschhub.application.learning.port.in.CreateCourseUseCase;
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
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CreateCourseService implements CreateCourseUseCase {

    CourseRepositoryPort courseRepositoryPort;

    @Override
    public CourseResponse createCourse(CreateCourseCommand command) {

        Course course = Course.create(
                command.title(),
                command.description(),
                new CEFRLevel(command.level()),
                new Money(command.price(), command.currency()),
                command.instructorId()
        );

        Course savedCourse = courseRepositoryPort.save(course);

        return toResponse(savedCourse);
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

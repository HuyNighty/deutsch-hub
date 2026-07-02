package com.deutschhub.infrastructure.learning.persistence.adapter;

import com.deutschhub.common.util.PageResponse;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.domain.learning.model.entity.Section;
import com.deutschhub.domain.learning.model.valueobject.CEFRLevel;
import com.deutschhub.domain.learning.model.valueobject.Money;
import com.deutschhub.infrastructure.learning.persistence.entity.CourseJpaEntity;
import com.deutschhub.infrastructure.learning.persistence.entity.SectionJpaEntity;
import com.deutschhub.infrastructure.learning.persistence.repository.SpringDataCourseRepository;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JpaCourseRepositoryAdapter implements CourseRepositoryPort {

    SpringDataCourseRepository repository;

    @Override
    public Course save(Course course) {
        CourseJpaEntity saved = repository.save(toEntity(course));

        return toDomain(saved);
    }

    @Override
    public Optional<Course> findById(UUID courseId) {
        return repository.findById(courseId)
                .map(this::toDomain);
    }

    @Override
    public PageResponse<Course> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<CourseJpaEntity> coursePage = repository.searchCourses(keyword, pageable);

        return PageResponse.<Course>builder()
                .items(coursePage.getContent()
                        .stream()
                        .map(this::toDomain)
                        .toList())
                .page(coursePage.getNumber())
                .size(coursePage.getSize())
                .totalElements(coursePage.getTotalElements())
                .totalPages(coursePage.getTotalPages())
                .build();
    }

    private CourseJpaEntity toEntity(Course course) {
        CourseJpaEntity entity =  CourseJpaEntity.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .level(course.getLevel().toString())
                .priceAmount(course.getPrice().getAmount())
                .priceCurrency(course.getPrice().getCurrency())
                .published(course.isPublished())
                .instructorId(course.getInstructorId())
                .estimatedHours(course.getEstimatedHours())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .deletedAt(course.getDeletedAt())
                .build();

        List<SectionJpaEntity> sections = course.getSections()
                .stream()
                .map(section -> toSectionEntity(section, entity))
                .toList();

        entity.getSections().clear();
        course.getSections()
                .stream()
                .map(section -> toSectionEntity(section, entity))
                .forEach(entity.getSections()::add);
        return entity;
    }

    private Course toDomain(CourseJpaEntity entity) {
        Course course = Course.restore(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                new CEFRLevel(entity.getLevel()),
                new Money(entity.getPriceAmount(), entity.getPriceCurrency()),
                entity.isPublished(),
                entity.getInstructorId(),
                entity.getEstimatedHours(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );

        if (entity.getSections() != null) {
            entity.getSections()
                    .stream()
                    .map(this::toSectionDomain)
                    .forEach(course::restoreSection);
        }

        return course;
    }

    private SectionJpaEntity toSectionEntity(Section section, CourseJpaEntity entity) {
        return SectionJpaEntity.builder()
                .id(section.getId())
                .title(section.getTitle())
                .description(section.getDescription())
                .orderIndex(section.getOrderIndex())
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .deletedAt(section.getDeletedAt())
                .course(entity)
                .build();
    }

    private Section toSectionDomain(SectionJpaEntity entity) {
        return Section.restore(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getOrderIndex(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}

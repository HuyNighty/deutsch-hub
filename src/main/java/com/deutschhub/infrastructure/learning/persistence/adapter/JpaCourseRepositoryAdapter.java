package com.deutschhub.infrastructure.learning.persistence.adapter;

import com.deutschhub.common.util.PageResponse;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.domain.learning.model.entity.Lesson;
import com.deutschhub.domain.learning.model.entity.LessonItem;
import com.deutschhub.domain.learning.model.entity.Section;
import com.deutschhub.domain.learning.model.valueobject.CEFRLevel;
import com.deutschhub.domain.learning.model.enums.LessonItemType;
import com.deutschhub.domain.learning.model.valueobject.Money;
import com.deutschhub.infrastructure.learning.persistence.entity.CourseJpaEntity;
import com.deutschhub.infrastructure.learning.persistence.entity.LessonItemJpaEntity;
import com.deutschhub.infrastructure.learning.persistence.entity.LessonJpaEntity;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class JpaCourseRepositoryAdapter implements CourseRepositoryPort {

    SpringDataCourseRepository repository;

    @Override
    @Transactional
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

    @Override
    public PageResponse<Course> findPublishedCourses(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<CourseJpaEntity> coursePage = repository.searchPublishedCourses(keyword, pageable);

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

    @Override
    public Optional<Course> findPublishedById(UUID courseId) {
        return repository.findPublishedById(courseId).map(this::toDomain);
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
        SectionJpaEntity sectionEntity = SectionJpaEntity.builder()
                .id(section.getId())
                .title(section.getTitle())
                .description(section.getDescription())
                .orderIndex(section.getOrderIndex())
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .deletedAt(section.getDeletedAt())
                .course(entity)
                .build();

        section.getLessons()
                .stream()
                .map(lesson -> toLessonEntity(lesson, sectionEntity))
                .forEach(sectionEntity.getLessons()::add);

        return sectionEntity;
    }
    private LessonJpaEntity toLessonEntity(Lesson lesson, SectionJpaEntity sectionEntity) {
        LessonJpaEntity lessonEntity = LessonJpaEntity.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .estimatedMinutes(lesson.getEstimatedMinutes())
                .level(lesson.getLevel().toString())
                .orderIndex(lesson.getOrderIndex())
                .freePreview(lesson.isFreePreview())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .deletedAt(lesson.getDeletedAt())
                .section(sectionEntity)
                .build();

        lesson.getItems()
                .stream()
                .map(item -> toLessonItemEntity(item, lessonEntity))
                .forEach(lessonEntity.getItems()::add);

        return lessonEntity;
    }

    private Section toSectionDomain(SectionJpaEntity entity) {
        Section section = Section.restore(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getOrderIndex(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );

        if (entity.getLessons() != null) {
            entity.getLessons()
                    .stream()
                    .map(this::toLessonDomain)
                    .forEach(section::restoreLesson);
        }

        return section;
    }

    private Lesson toLessonDomain(LessonJpaEntity entity) {
        Lesson lesson = Lesson.restore(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getEstimatedMinutes(),
                new CEFRLevel(entity.getLevel()),
                entity.getOrderIndex(),
                entity.isFreePreview(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );

        if (entity.getItems() != null) {
            entity.getItems()
                    .stream()
                    .map(this::toLessonItemDomain)
                    .forEach(lesson::restoreItem);
        }

        return lesson;
    }

    private LessonItemJpaEntity toLessonItemEntity(LessonItem item, LessonJpaEntity lessonEntity) {
        return LessonItemJpaEntity.builder()
                .id(item.getId())
                .type(item.getType().name())
                .title(item.getTitle())
                .description(item.getDescription())
                .content(item.getContent())
                .mediaId(item.getMediaId())
                .quizId(item.getQuizId())
                .estimatedMinutes(item.getEstimatedMinutes())
                .orderIndex(item.getOrderIndex())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .deletedAt(item.getDeletedAt())
                .lesson(lessonEntity)
                .build();
    }

    private LessonItem toLessonItemDomain(LessonItemJpaEntity entity) {
        return LessonItem.restore(
                entity.getId(),
                LessonItemType.valueOf(entity.getType()),
                entity.getTitle(),
                entity.getDescription(),
                entity.getContent(),
                entity.getMediaId(),
                entity.getQuizId(),
                entity.getEstimatedMinutes(),
                entity.getOrderIndex(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}

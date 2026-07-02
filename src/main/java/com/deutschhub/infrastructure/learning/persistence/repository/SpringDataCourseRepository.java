package com.deutschhub.infrastructure.learning.persistence.repository;

import com.deutschhub.infrastructure.learning.persistence.entity.CourseJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataCourseRepository extends JpaRepository<CourseJpaEntity, UUID> {

    @Query("""
            select c
            from CourseJpaEntity c
            where c.deletedAt is null
              and (
                    :keyword is null
                 or lower(c.title) like lower(concat('%', :keyword, '%'))
                 or lower(c.description) like lower(concat('%', :keyword, '%'))
                 or lower(c.level) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<CourseJpaEntity> searchCourses(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        select c
        from CourseJpaEntity c
        where c.deletedAt is null
          and c.published = true
          and (
              :keyword is null
              or lower(c.title) like lower(concat('%', :keyword, '%'))
              or lower(c.description) like lower(concat('%', :keyword, '%'))
              or lower(c.level) like lower(concat('%', :keyword, '%'))
          )
        """)
    Page<CourseJpaEntity> searchPublishedCourses(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
        select c
        from CourseJpaEntity c
        where c.id = :courseId
          and c.deletedAt is null
          and c.published = true
        """)
    Optional<CourseJpaEntity> findPublishedById(@Param("courseId") UUID courseId);

}

package com.deutschhub.infrastructure.content.topic.persistence.repository;

import com.deutschhub.infrastructure.content.topic.persistence.entity.TopicJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SpringDataTopicRepository extends JpaRepository<TopicJpaEntity, UUID> {

    boolean existsByCategoryIdAndTopicNameNormalized(UUID categoryId, String topicNameNormalized);

    boolean existsByCategoryIdAndTopicNameNormalizedAndIdNot(UUID categoryId, String topicNameNormalized, UUID id);

    List<TopicJpaEntity> findByCategoryIdAndTopicStatus(UUID categoryId, String topicStatus);

    List<TopicJpaEntity> findByIdIn(Collection<UUID> ids);
}

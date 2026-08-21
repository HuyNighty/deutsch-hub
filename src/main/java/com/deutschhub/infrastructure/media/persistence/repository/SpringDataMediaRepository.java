package com.deutschhub.infrastructure.media.persistence.repository;

import com.deutschhub.infrastructure.media.persistence.entity.MediaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataMediaRepository extends JpaRepository<MediaJpaEntity, UUID> {

    boolean existsByIdAndMediaType(UUID mediaId, String mediaType);
}

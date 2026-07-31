package com.deutschhub.infrastructure.media.storage.adapter;

import com.deutschhub.application.media.port.out.MediaRepositoryPort;
import com.deutschhub.domain.media.model.aggregate.Media;
import com.deutschhub.domain.media.model.valueobject.MediaType;
import com.deutschhub.infrastructure.media.persistence.entity.MediaJpaEntity;
import com.deutschhub.infrastructure.media.persistence.repository.SpringDataMediaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JpaMediaRepositoryAdapter implements MediaRepositoryPort {

    SpringDataMediaRepository repository;

    @Override
    @Transactional
    public Media save(Media media) {
        MediaJpaEntity savedEntity = repository.saveAndFlush(toEntity(media));

        return toDomain(savedEntity);
    }

    @Override
    public Optional<Media> findById(UUID mediaId) {
        return repository.findById(mediaId).map(this::toDomain);
    }

    private MediaJpaEntity toEntity(Media media) {
        return MediaJpaEntity.builder()
                .id(media.getId())
                .originalFileName(media.getOriginalFileName())
                .storageKey(media.getStorageKey())
                .mediaType(media.getMediaType().name())
                .mimeType(media.getMimeType())
                .sizeBytes(media.getSizeBytes())
                .uploadedBy(media.getUploadedBy())
                .createdAt(media.getCreatedAt())
                .build();
    }

    private Media toDomain(MediaJpaEntity entity) {
        return Media.restore(
                entity.getId(),
                entity.getOriginalFileName(),
                entity.getStorageKey(),
                MediaType.valueOf(entity.getMediaType()),
                entity.getMimeType(),
                entity.getSizeBytes(),
                entity.getUploadedBy(),
                entity.getCreatedAt()
        );
    }
}

package com.deutschhub.application.media.port.out;

import com.deutschhub.domain.media.model.aggregate.Media;

import java.util.Optional;
import java.util.UUID;

public interface MediaRepositoryPort {

    Media save(Media media);

    Optional<Media> findById(UUID mediaId);
}

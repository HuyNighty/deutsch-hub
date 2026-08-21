package com.deutschhub.infrastructure.media.persistence.adapter;

import com.deutschhub.application.content.article.port.out.MediaLookupPort;
import com.deutschhub.domain.media.model.valueobject.MediaType;
import com.deutschhub.infrastructure.media.persistence.repository.SpringDataMediaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JpaMediaLookupAdapter implements MediaLookupPort {

    SpringDataMediaRepository springDataMediaRepository;

    @Override
    public boolean exists(UUID mediaId) {
        if (mediaId == null) {
            return false;
        }

        return springDataMediaRepository.existsById(mediaId);
    }

    @Override
    public boolean isUsableAsArticleCover(UUID mediaId) {
        if (mediaId == null) {
            return false;
        }

        return springDataMediaRepository.existsByIdAndMediaType(mediaId, MediaType.IMAGE.name());
    }
}

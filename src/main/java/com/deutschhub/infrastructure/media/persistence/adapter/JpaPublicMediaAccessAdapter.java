package com.deutschhub.infrastructure.media.persistence.adapter;

import com.deutschhub.application.content.article.port.out.ArticleQueryPort;
import com.deutschhub.application.media.port.out.PublicMediaAccessPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JpaPublicMediaAccessAdapter implements PublicMediaAccessPort {

    ArticleQueryPort articleQueryPort;

    @Override
    public boolean isPubliclyAccessible(UUID mediaId) {
        if (mediaId == null) {
            return false;
        }

        return articleQueryPort.isMediaPubliclyReferenced(mediaId);
    }
}

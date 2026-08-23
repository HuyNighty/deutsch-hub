package com.deutschhub.application.media.service;

import com.deutschhub.application.media.dto.response.MediaContentResponse;
import com.deutschhub.application.media.policy.MediaAccessPolicy;
import com.deutschhub.application.media.port.in.GetMediaContentUseCase;
import com.deutschhub.application.media.port.out.MediaRepositoryPort;
import com.deutschhub.application.media.port.out.MediaStoragePort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.media.model.aggregate.Media;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetMediaContentService implements GetMediaContentUseCase {

    MediaRepositoryPort mediaRepositoryPort;
    MediaStoragePort mediaStoragePort;
    MediaAccessPolicy mediaAccessPolicy;

    @Override
    public MediaContentResponse getContent(UUID mediaId) {
        Media media = mediaRepositoryPort.findById(mediaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDIA_NOT_FOUND));

        mediaAccessPolicy.requireCanRead(media);

        InputStream stream = mediaStoragePort.load(media.getStorageKey());

        return new MediaContentResponse(stream, media.getMimeType(),
                media.getOriginalFileName(), media.getSizeBytes());
    }
}

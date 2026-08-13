package com.deutschhub.application.media.service;

import com.deutschhub.application.media.dto.response.MediaResponse;
import com.deutschhub.application.media.port.in.GetMediaUseCase;
import com.deutschhub.application.media.port.out.MediaRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.media.model.aggregate.Media;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetMediaService implements GetMediaUseCase {

    MediaRepositoryPort mediaRepositoryPort;

    @Override
    public MediaResponse getById(UUID mediaId, UUID currentUserId, boolean isAdmin) {
        Media media = mediaRepositoryPort.findById(mediaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDIA_NOT_FOUND));

        if (!isAdmin && !media.belongsTo(currentUserId)) {
            throw new BusinessException(ErrorCode.MEDIA_ACCESS_DENIED);
        }

        return toResponse(media);
    }

    private MediaResponse toResponse(Media media) {

        return new MediaResponse(
                media.getId(),
                media.getOriginalFileName(),
                media.getMediaType(),
                media.getMimeType(),
                media.getSizeBytes(),
                media.getUploadedBy(),
                media.getCreatedAt()
        );
    }

}

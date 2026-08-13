package com.deutschhub.application.media.service;

import com.deutschhub.application.media.dto.request.UploadMediaCommand;
import com.deutschhub.application.media.dto.response.MediaResponse;
import com.deutschhub.application.media.port.in.UploadMediaUseCase;
import com.deutschhub.application.media.port.out.*;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.media.model.aggregate.Media;
import com.deutschhub.domain.media.model.valueobject.MediaType;
import com.deutschhub.domain.media.service.MediaTypeResolver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UploadMediaService implements UploadMediaUseCase {

    MediaRepositoryPort mediaRepositoryPort;
    MediaStoragePort mediaStoragePort;
    MediaUploadPolicy mediaUploadPolicy;
    MediaTypeResolver mediaTypeResolver;

    @Override
    public MediaResponse upload(UploadMediaCommand command) {
        validateUploadSize(command);

        MediaType mediaType =
                mediaTypeResolver.resolve(command.mimeType());

        MediaUploadContent content = new MediaUploadContent(
                command.originalFileName(),
                mediaType,
                command.mimeType(),
                command.sizeBytes(),
                command.inputStream()
        );

        StoredMediaObject storedMedia = mediaStoragePort.store(content);

        try {
            Media media = Media.create(command.originalFileName(), storedMedia.storageKey(),
                    mediaType, command.mimeType(), command.sizeBytes(), command.uploadedBy());

            Media savedMedia = mediaRepositoryPort.save(media);

            return toResponse(savedMedia);
        } catch (RuntimeException e) {
            cleanupStoredFile(storedMedia.storageKey(), e);
            throw e;
        }
    }

    private void validateUploadSize(UploadMediaCommand command) {
        if (command == null || command.inputStream() == null) {
            throw new BusinessException(ErrorCode.INVALID_MEDIA_UPLOAD);
        }

        if (command.sizeBytes() > mediaUploadPolicy.maxUploadSizeBytes()) {
            throw new BusinessException(ErrorCode.MEDIA_FILE_TOO_LARGE);
        }
    }

    private void cleanupStoredFile(String storageKey, RuntimeException e) {
        try {
            mediaStoragePort.delete(storageKey);
        } catch (RuntimeException cleanupE) {
            e.addSuppressed(cleanupE);
        }
    }

    private MediaResponse toResponse(Media media) {
        return new MediaResponse( media.getId(), media.getOriginalFileName(), media.getMediaType(),
                media.getMimeType(), media.getSizeBytes(), media.getUploadedBy(), media.getCreatedAt());
    }
}

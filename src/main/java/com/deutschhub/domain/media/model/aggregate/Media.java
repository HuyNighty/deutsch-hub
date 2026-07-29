package com.deutschhub.domain.media.model.aggregate;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.media.model.valueobject.MediaType;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

public class Media {

    private final UUID id;
    private final String originalFileName;
    private final String storageKey;
    private final MediaType mediaType;
    private final String mimeType;
    private final long sizeBytes;
    private final UUID uploadedBy;
    private final LocalDateTime createdAt;

    private Media(UUID id, String originalFileName, String storageKey, MediaType mediaType, String mimeType,
                  long sizeBytes, UUID uploadedBy, LocalDateTime createdAt) {
        this.id = validateId(id);
        this.originalFileName = validateOriginalFileName(originalFileName);
        this.storageKey = validateStorageKey(storageKey);
        this.mediaType = validateMediaType(mediaType);
        this.mimeType = validateMimeType(mimeType, this.mediaType);
        this.sizeBytes = validateSizeBytes(sizeBytes);
        this.uploadedBy = validateUploadedBy(uploadedBy);
        this.createdAt = validateCreatedAt(createdAt);
    }

    public static Media create(String originalFileName, String storageKey,
                                   MediaType mediaType, String mimeType,
                                   long sizeBytes, UUID uploadedBy) {
        return new Media(UUID.randomUUID(), originalFileName, storageKey, mediaType, mimeType,
                sizeBytes, uploadedBy, LocalDateTime.now());
    }

    public static Media restore(UUID id, String originalFileName, String storageKey, MediaType mediaType,
            String mimeType, long sizeBytes, UUID uploadedBy, LocalDateTime createdAt) {
        return new Media(id, originalFileName, storageKey, mediaType, mimeType, sizeBytes, uploadedBy, createdAt);
    }

    private UUID validateId(UUID id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.MEDIA_ID_CAN_NOT_NULL);
        }
        return id;
    }

    private String validateOriginalFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_MEDIA_FILE_NAME);
        }

        return originalFileName.trim();
    }

    private String validateStorageKey(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_MEDIA_STORAGE_KEY);
        }

        return storageKey.trim();
    }

    private MediaType validateMediaType(MediaType mediaType) {
        if (mediaType == null) {
            throw new BusinessException(ErrorCode.INVALID_MEDIA_TYPE);
        }

        return mediaType;
    }

    private String validateMimeType(String mimeType, MediaType mediaType) {
        if (mimeType == null || mimeType.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_MEDIA_MIME_TYPE);
        }

        String normalizedMimeType = mimeType.trim().toLowerCase(Locale.ROOT);

        if (!mediaType.supportsMimeType(normalizedMimeType)) {
            throw new BusinessException(ErrorCode.INVALID_MEDIA_MIME_TYPE);
        }

        return normalizedMimeType;
    }

    private long validateSizeBytes(long sizeBytes) {
        if (sizeBytes <= 0) {
            throw new BusinessException(ErrorCode.INVALID_MEDIA_SIZE);
        }

        return sizeBytes;
    }

    private UUID validateUploadedBy(UUID uploadedBy) {
        if (uploadedBy == null) {
            throw new BusinessException(ErrorCode.MEDIA_UPLOADER_CAN_NOT_NULL);
        }

        return uploadedBy;
    }

    private LocalDateTime validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new BusinessException(ErrorCode.INVALID_MEDIA_CREATED_AT);
        }

        return createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public UUID getUploadedBy() {
        return uploadedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

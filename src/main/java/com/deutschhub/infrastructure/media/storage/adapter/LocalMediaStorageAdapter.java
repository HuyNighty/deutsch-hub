package com.deutschhub.infrastructure.media.storage.adapter;

import com.deutschhub.application.media.port.out.MediaStoragePort;
import com.deutschhub.application.media.port.out.MediaUploadContent;
import com.deutschhub.application.media.port.out.StoredMediaObject;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.infrastructure.media.config.MediaProperties;
import com.deutschhub.infrastructure.media.storage.generator.StorageKeyGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocalMediaStorageAdapter implements MediaStoragePort {

    MediaProperties mediaProperties;
    StorageKeyGenerator storageKeyGenerator;

    @Override
    public StoredMediaObject store(MediaUploadContent content) {
        String storageKey = storageKeyGenerator.generate(content.mediaType(),  content.originalFileName());

        Path destination = resolveStoragePath(storageKey).normalize();

        createParentDirectories(destination);

        copyFile(content, destination);

        return new StoredMediaObject(storageKey);
    }

    @Override
    public void delete(String storageKey) {
        Path path = resolveStoragePath(storageKey);

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream load(String storageKey) {
        Path path = resolveStoragePath(storageKey);

        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.MEDIA_NOT_FOUND);
        }
    }

    private Path resolveStoragePath(String storageKey) {
        return mediaProperties.storageRoot().resolve(storageKey);
    }

    private void createParentDirectories(Path destination) {
        try {
            Files.createDirectories(destination.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void copyFile(MediaUploadContent content, Path destination) {
        try {
            Files.copy(content.inputStream(), destination);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

package com.deutschhub.infrastructure.media.storage.adapter;

import com.deutschhub.application.media.port.out.MediaStoragePort;
import com.deutschhub.application.media.port.out.MediaUploadContent;
import com.deutschhub.application.media.port.out.StoredMediaObject;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.infrastructure.media.config.MediaProperties;
import com.deutschhub.infrastructure.media.storage.exception.MediaStorageException;
import com.deutschhub.infrastructure.media.storage.generator.StorageKeyGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocalMediaStorageAdapter implements MediaStoragePort {

    MediaProperties mediaProperties;
    StorageKeyGenerator storageKeyGenerator;

    @Override
    public StoredMediaObject store(MediaUploadContent content) {

        String storageKey = storageKeyGenerator.generate(content.mediaType(), content.originalFileName());

        Path destination = resolveStoragePath(storageKey);

        createParentDirectories(destination);

        Path temporaryFile = null;

        try {
            temporaryFile = createTemporaryFile(destination);

            copyToTemporaryFile(content, temporaryFile);

            validateCopiedSize(content, temporaryFile);

            moveAtomically(temporaryFile, destination);

            temporaryFile = null;

            return new StoredMediaObject(storageKey);

        } catch (IOException exception) {

            cleanup(temporaryFile);

            throw new MediaStorageException("Could not store media.", exception);
        }
    }

    @Override
    public void delete(String storageKey) {

        Path path = resolveStoragePath(storageKey);

        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new MediaStorageException("Could not delete media.", exception);
        }
    }

    @Override
    public InputStream load(String storageKey) {

        Path path = resolveStoragePath(storageKey);

        try {
            return Files.newInputStream(path);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.MEDIA_NOT_FOUND);
        }
    }

    private Path resolveStoragePath(String storageKey) {

        Path storageRoot = mediaProperties.storageRoot().toAbsolutePath().normalize();

        Path resolved = storageRoot.resolve(storageKey).normalize();

        if (!resolved.startsWith(storageRoot)) {
            throw new BusinessException(ErrorCode.INVALID_MEDIA_UPLOAD);
        }

        return resolved;
    }

    private void createParentDirectories(Path destination) {

        try {
            Files.createDirectories(destination.getParent());
        } catch (IOException exception) {
            throw new MediaStorageException("Could not create media directory.", exception);
        }
    }

    private Path createTemporaryFile(Path destination)
            throws IOException {

        return Files.createTempFile(destination.getParent(), "upload-", ".tmp");
    }

    private void copyToTemporaryFile(MediaUploadContent content, Path temporaryFile)
            throws IOException {

        try (InputStream inputStream = content.inputStream()) {

            Files.copy(inputStream, temporaryFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void validateCopiedSize(
            MediaUploadContent content,
            Path temporaryFile
    ) throws IOException {

        long actualSize = Files.size(temporaryFile);

        if (actualSize != content.sizeBytes()) {
            throw new BusinessException(ErrorCode.INVALID_MEDIA_UPLOAD);
        }
    }

    private void moveAtomically(Path temporaryFile, Path destination)
            throws IOException {
        try {
            Files.move(temporaryFile, destination, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void cleanup(Path path) {
        if (path == null) {
            return;
        }

        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Ignore cleanup failure.
        }
    }
}
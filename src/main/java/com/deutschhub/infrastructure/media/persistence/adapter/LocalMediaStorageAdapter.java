package com.deutschhub.infrastructure.media.persistence.adapter;

import com.deutschhub.application.media.port.out.MediaStoragePort;
import com.deutschhub.application.media.port.out.MediaUploadContent;
import com.deutschhub.application.media.port.out.StoredMediaObject;
import com.deutschhub.infrastructure.media.config.MediaProperties;
import com.deutschhub.infrastructure.media.persistence.generator.StorageKeyGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;

@Component
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocalMediaStorageAdapter implements MediaStoragePort {

    MediaProperties mediaProperties;
    StorageKeyGenerator storageKeyGenerator;

    @Override
    public StoredMediaObject store(MediaUploadContent content) {
        String storageKey = storageKeyGenerator.generate(content);

        Path destination = resolveStoragePath(storageKey);

        createParentDirectories(destination);

        copyFile(content, destination);

        return new StoredMediaObject(storageKey);
    }

    @Override
    public void delete(String storageKey) {

    }
}

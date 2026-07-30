package com.deutschhub.application.media.port.out;

public interface MediaStoragePort {
    StoredMediaObject store(MediaUploadContent content);

    void delete(String storageKey);
}

package com.deutschhub.application.media.port.out;

import java.io.InputStream;

public interface MediaStoragePort {
    StoredMediaObject store(MediaUploadContent content);

    void delete(String storageKey);

    InputStream load(String storageKey);
}

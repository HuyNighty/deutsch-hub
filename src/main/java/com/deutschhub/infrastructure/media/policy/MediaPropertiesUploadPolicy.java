package com.deutschhub.infrastructure.media.policy;

import com.deutschhub.application.media.port.out.MediaUploadPolicy;
import com.deutschhub.infrastructure.media.config.MediaProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MediaPropertiesUploadPolicy implements MediaUploadPolicy {

    MediaProperties mediaProperties;

    @Override
    public long maxUploadSizeBytes() {
        return mediaProperties.maxUploadSize().toBytes();
    }
}

package com.deutschhub.domain.media.service;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.media.model.valueobject.MediaType;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MediaTypeResolver {

    public MediaType resolve(String mimeType){
        if (mimeType == null || mimeType.isEmpty()){
            throw new BusinessException(ErrorCode.INVALID_MEDIA_MIME_TYPE);
        }

        String normalized = mimeType.trim().toLowerCase(Locale.ROOT);

        for (MediaType mediaType : MediaType.values()) {
            if (mediaType.supportsMimeType(normalized)) {
                return mediaType;
            }
        }

        throw new BusinessException(ErrorCode.INVALID_MEDIA_TYPE);
    }
}

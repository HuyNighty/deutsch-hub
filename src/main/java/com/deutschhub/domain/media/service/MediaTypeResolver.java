package com.deutschhub.domain.media.service;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.media.model.valueobject.MediaType;
import org.springframework.stereotype.Component;

@Component
public class MediaTypeResolver {

    public MediaType resolve(String mimeType){
        if (mimeType == null || mimeType.isEmpty()){
            throw new BusinessException(ErrorCode.INVALID_MEDIA_MIME_TYPE);
        }

        return switch (mimeType) {
            case "application/pdf" -> MediaType.PDF;

            case "image/png", "image/webp", "image/jpeg" -> MediaType.IMAGE;

            case "video/mp4" -> MediaType.VIDEO;

            case "audio/mpeg" -> MediaType.AUDIO;

            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                 "application/msword",
                 "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                 "application/vnd.ms-excel",
                 "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                 "application/vnd.ms-powerpoint",
                 "text/plain" -> MediaType.DOCUMENT;

            default -> throw new BusinessException(ErrorCode.INVALID_MEDIA_TYPE);
        };
    }
}

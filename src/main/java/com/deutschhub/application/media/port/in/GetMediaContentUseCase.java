package com.deutschhub.application.media.port.in;

import com.deutschhub.application.media.dto.response.MediaContentResponse;

import java.util.UUID;

public interface GetMediaContentUseCase {

    MediaContentResponse getContent(UUID mediaId);
}

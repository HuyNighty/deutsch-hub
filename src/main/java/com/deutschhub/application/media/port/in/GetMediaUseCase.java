package com.deutschhub.application.media.port.in;

import com.deutschhub.application.media.dto.response.MediaResponse;

import java.util.UUID;

public interface GetMediaUseCase {

    MediaResponse getById(UUID mediaId, UUID currentUserId, boolean isAdmin);
}

package com.deutschhub.application.media.port.in;

import com.deutschhub.application.media.dto.request.UploadMediaCommand;
import com.deutschhub.application.media.dto.response.MediaResponse;

public interface UploadMediaUseCase {

    MediaResponse upload(UploadMediaCommand command);
}
